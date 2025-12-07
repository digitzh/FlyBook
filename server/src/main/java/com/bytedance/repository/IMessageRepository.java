package com.bytedance.repository;

import com.bytedance.entity.Message;

import java.util.List;

/**
 * 消息数据访问接口
 */
public interface IMessageRepository {
    /**
     * 保存消息
     */
    void save(Message message);

    /**
     * 根据会话ID和序列号范围查询消息
     * @param conversationId 会话ID
     * @param afterSeq 起始序列号（不包含）
     * @param limit 限制数量
     */
    List<Message> findByConversationIdAndSeqAfter(Long conversationId, Long afterSeq, int limit);
}

