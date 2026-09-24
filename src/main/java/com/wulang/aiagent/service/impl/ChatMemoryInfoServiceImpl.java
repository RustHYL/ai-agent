package com.wulang.aiagent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wulang.aiagent.model.entity.ChatMemoryInfo;
import com.wulang.aiagent.service.ChatMemoryInfoService;
import com.wulang.aiagent.mapper.ChatMemoryInfoMapper;
import org.springframework.stereotype.Service;

/**
* @author Admin
* @description 针对表【chat_memory_info(Kryo+MySQL对话记忆表)】的数据库操作Service实现
* @createDate 2026-03-21 15:29:05
*/
@Service
public class ChatMemoryInfoServiceImpl extends ServiceImpl<ChatMemoryInfoMapper, ChatMemoryInfo>
    implements ChatMemoryInfoService{

    @Override
    public String addChatInfo(ChatMemoryInfo chatMemoryInfo) {
        this.baseMapper.insert(chatMemoryInfo);
        return chatMemoryInfo.getConversationId();
    }
}




