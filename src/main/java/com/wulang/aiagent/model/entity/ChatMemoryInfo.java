package com.wulang.aiagent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * Kryo+MySQL对话记忆表
 * @TableName chat_memory_info
 */
@TableName(value ="chat_memory_info")
@Data
public class ChatMemoryInfo {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private String conversationId;

    /**
     * 更新时间
     */
    private Date updatedTime;

    /**
     * Kryo序列化后的消息列表
     * */
    private byte[] serializedMessages;
}