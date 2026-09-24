package com.wulang.aiagent.app;




import cn.hutool.core.lang.Assert;
import com.wulang.aiagent.advisor.MyLogAdvisor;
import com.wulang.aiagent.chatmemory.DatabaseBasedChatMemory;
import com.wulang.aiagent.chatmemory.FileBasedChatMemory;
import com.wulang.aiagent.model.entity.ChatMemoryInfo;
import com.wulang.aiagent.rag.LoveAppRagCloudAdvisorConfig;
import com.wulang.aiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.wulang.aiagent.service.ChatMemoryInfoService;
import com.wulang.aiagent.tools.ToolRegistration;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Flux;


import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;


@Component
@Slf4j
public class LoveApp {

    @Resource
    private DatabaseBasedChatMemory databaseBasedChatMemory;



    private ChatClient chatClient;

    private final ChatModel dashscopeChatModel;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

//    /**
//     * 初始化AI客户端
//     * @param dashscopeChatModel
//     */
//    public LoveApp(ChatModel dashscopeChatModel) {
////        // 初始化基于内存的对话记忆
////        ChatMemory chatMemory = new InMemoryChatMemory();
//        // 基于文件的对话记忆
////        String fileDir = System.getProperty("user.dir") + "/tmp/chat-memory";
////        ChatMemory chatMemory = new FileBasedChatMemory(fileDir);
//        ChatMemory chatMemory = databaseBasedChatMemory;
//        chatClient = ChatClient.builder(dashscopeChatModel)
//                .defaultSystem(SYSTEM_PROMPT)
//                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory),
//                        // 自定义日志 advisor
//                        new MyLogAdvisor())
//                .build();
//    }



    public LoveApp(ChatModel dashscopeChatModel) {
        this.dashscopeChatModel = dashscopeChatModel;
        // 延迟初始化ChatClient（在@PostConstruct中）
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                // 先占位，后续在@PostConstruct中替换为真实的chatMemory
                .defaultAdvisors(new MyLogAdvisor())
                .build();
    }

    @PostConstruct
    public void initChatClient() {
        // 1. 校验数据库版ChatMemory是否注入成功
        Assert.notNull(databaseBasedChatMemory, "DatabaseBasedChatMemory 注入失败！请检查@Component注解和包扫描");

        // 2. 重新构建ChatClient，传入数据库版ChatMemory
        this.chatClient = ChatClient.builder(this.dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(databaseBasedChatMemory), // ✅ 仅用数据库版
                        new MyLogAdvisor()
                )
                .build();

    }






    /**
     * AI 基础对话支持多轮记忆
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;

    }

    /**
     * AI 基础对话支持多轮记忆(SSE 流式传输)
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                // 直接获取内容
                .content();

    }



    record LoveReport(String title, List<String> suggestions) {

    }

    /**
     * AI 恋爱报告（实战结构化输出）
     * @param message
     * @param chatId
     * @return
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话之后都要生成恋爱结果，标题{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("loveReport: {}", loveReport);
        return loveReport;
    }

    /**
     * RAG知识库问答 实现
     */
    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private VectorStore vectorStore;

    @Resource
    private Advisor loveAppRagCloudAdvisor;

    /**
     * 和RAG知识库对话
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 应用RAG知识库
//                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                // 应用Rag 检索增强服务（基于云知识库服务）
//                .advisors(loveAppRagCloudAdvisor)
                // 应用Rag 检索增强服务（基于PgVector）
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                // 应用自定义Rag检索增强顾问（文档查询器+上下文增强）
//                .advisors(LoveAppRagCustomAdvisorFactory.createLoveAppRagCustomAdvisor(loveAppVectorStore, "单身"))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
//        log.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话之后都要生成恋爱结果，标题{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLogAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
//        log.info("content: {}", content);
        return content;

    }



//    // AI调用MCP服务
//    @Resource
//    private ToolCallbackProvider toolCallbackProvider;
//
//
//    public String doChatWithMCP(String message, String chatId) {
//        ChatResponse chatResponse = chatClient
//                .prompt()
//                .user(message)
//                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
//                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
//                .advisors(new MyLogAdvisor())
//                .tools(toolCallbackProvider)
//                .call()
//                .chatResponse();
//        String content = chatResponse.getResult().getOutput().getText();
////        log.info("content: {}", content);
//        return content;
//
//    }



}

