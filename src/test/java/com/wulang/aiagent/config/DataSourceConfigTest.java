package com.wulang.aiagent.config;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class DataSourceConfigTest {


    @Resource
    private VectorStore vectorStore;
    @Test
    void pgVectorStore() {
        List<Document> documents = List.of(
                new Document("学习编程有什么用，用来找工作吗", Map.of("meta1", "meta1")),
                new Document("编程的作用指南"),
                new Document("编程的历史从什么时候开始.", Map.of("meta2", "meta2")));

// Add the documents to PGVector
        vectorStore.add(documents);

// Retrieve documents similar to a query
        List<Document> results = vectorStore.similaritySearch(SearchRequest.builder().query("为什么学习编程").topK(3).build());
        Assertions.assertNotNull(results);
    }
}