package com.wulang.aiagent.service;

import com.wulang.aiagent.model.entity.ChatMemoryInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author Admin
* @description 针对表【chat_memory_info(Kryo+MySQL对话记忆表)】的数据库操作Service
* @createDate 2026-03-21 15:29:05
*/
public interface ChatMemoryInfoService extends IService<ChatMemoryInfo> {


    /**
     * 获取脱敏的已登录用户信息
     *
     * @return
     */
    String addChatInfo(ChatMemoryInfo chatMemoryInfo);

}
