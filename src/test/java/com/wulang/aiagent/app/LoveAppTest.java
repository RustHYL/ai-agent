package com.wulang.aiagent.app;


import com.wulang.aiagent.model.entity.ChatMemoryInfo;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.transformer.SummaryMetadataEnricher;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@SpringBootTest
class LoveAppTest {


    @Resource
    private LoveApp loveApp;


    @Test
    void testChat() {
        String chatId = UUID.randomUUID().toString();
        // 第一轮
        String response = loveApp.doChat("你好，我是猫五郎", chatId);
        Assertions.assertNotNull(response);
        // 第二轮
        response = loveApp.doChat("我想和我女朋友（理查德）表白我该怎么处理", chatId);
        Assertions.assertNotNull(response);
        // 第三轮
        response =loveApp.doChat("你好，你还记得我的名字吗", chatId);
        Assertions.assertNotNull(response);

    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message =  "如何平衡工作和家庭责任";
        String response = loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(response);
    }

    @Test
    void exceptionTest() {
        String chatId = UUID.randomUUID().toString();
        String message =  "如何平衡工作和家庭责任";
        String response = loveApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(response);
    }


}