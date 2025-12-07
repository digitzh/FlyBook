package com.bytedance.repository;

import com.bytedance.entity.ConversationMember;

import java.util.List;

/**
 * 会话成员数据访问接口
 */
public interface IConversationMemberRepository {
    /**
     * 根据会话ID查询所有成员
     */
    List<ConversationMember> findByConversationId(Long conversationId);

    /**
     * 根据用户ID查询用户参与的所有会话成员关系
     */
    List<ConversationMember> findByUserId(Long userId);

    /**
     * 检查用户是否为会话成员
     */
    boolean existsByConversationIdAndUserId(Long conversationId, Long userId);

    /**
     * 保存会话成员
     */
    void save(ConversationMember member);

    /**
     * 批量保存会话成员
     */
    void saveBatch(List<ConversationMember> members);

    /**
     * 增加未读数（排除发送者）
     */
    void incrementUnreadCount(Long conversationId, Long senderId);

    /**
     * 根据会话ID和用户ID查询成员关系
     */
    ConversationMember findByConversationIdAndUserId(Long conversationId, Long userId);
}

