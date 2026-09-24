package com.wulang.aiagent.chatmemory;

import cn.hutool.core.lang.Assert;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;


import com.wulang.aiagent.model.entity.ChatMemoryInfo;
import com.wulang.aiagent.service.ChatMemoryInfoService;
import jakarta.annotation.Resource;
import org.objenesis.strategy.StdInstantiatorStrategy;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于数据库持久化的对话记忆
 * 序列化问题
 */
@Component
public class DatabaseBasedChatMemory implements ChatMemory {

    private static final Kryo kryo = new Kryo();

    @Resource
    private ChatMemoryInfoService chatMemoryInfoService;

    static {
        kryo.setRegistrationRequired(false);
        // 设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
        kryo.register(Message.class);
        kryo.register(ArrayList.class);
    }

    public DatabaseBasedChatMemory() {}


    @Override
//    @DS("mysql")
    public void add(String conversationId, List<Message> messages) {

        Assert.notNull(chatMemoryInfoService, "ChatMemoryInfoService 注入失败！");
        Assert.notNull(conversationId, "conversationId 不能为空");
        Assert.notNull(messages, "messages 不能为空");


        List<Message> conversationMessages = getOrCreateConversation(conversationId);
        conversationMessages.addAll(messages);
        saveConversation(conversationId, conversationMessages);
    }

    @Override
//    @DS("mysql")
    public List<Message> get(String conversationId, int lastN) {

        Assert.notNull(chatMemoryInfoService, "ChatMemoryInfoService 注入失败！");
        Assert.notNull(conversationId, "conversationId 不能为空");
        Assert.isTrue(lastN >= 0, "lastN 必须非负");

        List<Message> allMessages = getOrCreateConversation(conversationId);
        return allMessages.stream()
                .skip(Math.max(0, allMessages.size() - lastN))
                .toList();
    }

    @Override
//    @DS("mysql")
    public void clear(String conversationId) {

        Assert.notNull(chatMemoryInfoService, "ChatMemoryInfoService 注入失败！");
        Assert.notNull(conversationId, "conversationId 不能为空");

        QueryWrapper<ChatMemoryInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId);
        ChatMemoryInfo chatMemoryInfo = chatMemoryInfoService.getOne(queryWrapper);
        if (chatMemoryInfo != null) {
            chatMemoryInfoService.remove(queryWrapper);
        }
    }

//    @DS("mysql")
    private List<Message> getOrCreateConversation(String conversationId) {
        List<Message> messages = new ArrayList<>();
        QueryWrapper<ChatMemoryInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("conversation_id", conversationId);
        ChatMemoryInfo chatMemoryinfo = chatMemoryInfoService.getOne(queryWrapper);
        if (chatMemoryinfo != null && chatMemoryinfo.getSerializedMessages() != null) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(chatMemoryinfo.getSerializedMessages())) {
                Input input = new Input(bais);
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                messages = new ArrayList<>();
                e.printStackTrace();
            }
        }
        return messages;
    }

//    @DS("mysql")
    private void saveConversation(String conversationId, List<Message> messages) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             Output output = new Output(baos)) {
            // 序列化时确保传入的是带泛型的List<Message>（已由调用方保证）
            kryo.writeObject(output, messages);
            output.flush();
            byte[] serializedData = baos.toByteArray();

            // MyBatis-Plus 查询时显式指定泛型
            QueryWrapper<ChatMemoryInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("conversation_id", conversationId);
            ChatMemoryInfo chatMemoryInfo = chatMemoryInfoService.getOne(queryWrapper);

            if (chatMemoryInfo != null) {
                chatMemoryInfo.setSerializedMessages(serializedData);
                chatMemoryInfoService.updateById(chatMemoryInfo);
            } else {
                chatMemoryInfo = new ChatMemoryInfo();
                chatMemoryInfo.setConversationId(conversationId);
                chatMemoryInfo.setSerializedMessages(serializedData);
                chatMemoryInfoService.save(chatMemoryInfo);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
