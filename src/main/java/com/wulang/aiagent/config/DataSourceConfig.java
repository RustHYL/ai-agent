package com.wulang.aiagent.config;

import com.alibaba.cloud.ai.dashscope.embedding.DashScopeEmbeddingModel;
import com.wulang.aiagent.rag.LoveAppDocumentLoader;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;


@Configuration
public class DataSourceConfig {

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    // ==================== 1. MySQL 数据源 (主) ====================
    // MyBatis Plus 会自动寻找 @Primary 或名为 dataSource 的 Bean

    @Primary
    @Bean(name = "mysqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create()
                .type(HikariDataSource.class) // 指定使用 HikariCP
                .build();
    }

    // ==================== 2. PostgreSQL 数据源 (从) ====================

    @Bean(name = "pgVectorDataSource")
    @ConfigurationProperties(prefix = "spring.ai.pgvector.datasource")
    public DataSource pgVectorDataSource() {
        // 使用 Builder 模式，会自动绑定 yml 中的 jdbc-url 等属性
        return DataSourceBuilder.create()
                .type(HikariDataSource.class) // 指定使用 HikariCP
                .build();
    }

    // 为 PG 创建专属的 JdbcTemplate
    @Bean(name = "pgVectorJdbcTemplate")
    public JdbcTemplate pgVectorJdbcTemplate(@Qualifier("pgVectorDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    // ==================== 3. VectorStore 配置 (解决冲突的核心) ====================
    //TODO 避免使用构建VectorStore的时候多次重复添加documents  测试原文查找重复和 利用DigestUtils.md5DigestAsHex(doc.getText().getBytes()); 所用时间
    @Bean
    public VectorStore pgVectorStore(
            @Qualifier("pgVectorJdbcTemplate") JdbcTemplate jdbcTemplate, DashScopeEmbeddingModel dashscopeEmbeddingModel) {

        // 手动构建 PgVectorStore
        VectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1536) // 必须与你的模型维度一致 (OpenAI/DashScope 通常是 1536)
                .initializeSchema(true) // 首次启动自动建表
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .build();

        int batchSize = 10;
        List<Document> documents = loveAppDocumentLoader.loadMarkdowns();
        if (documents == null || documents.isEmpty()) {
            return vectorStore;
        }
        for (int i = 0; i < documents.size(); i+=batchSize) {
            vectorStore.add(documents.subList(i, Math.min(i+batchSize, documents.size())));
        }

        return vectorStore;
    }
}