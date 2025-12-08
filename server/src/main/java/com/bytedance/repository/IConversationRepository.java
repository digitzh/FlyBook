package com.bytedance.repository;

import com.bytedance.entity.Conversation;

import java.util.List;

/**
 * 会话数据访问接口
 */
public interface IConversationRepository {
    /**
     * 根据ID查询会话
     */
    Conversation findById(Long conversationId);

    /**
     * 根据ID列表批量查询会话
     */
    List<Conversation> findByIds(List<Long> conversationIds);

    /**
     * 保存会话
     */
    void save(Conversation conversation);

    /**
     * 更新会话（带锁）
     * 用于更新序列号等需要并发控制的场景
     */
    Conversation findByIdForUpdate(Long conversationId);

    /**
     * 更新会话
     */
    void update(Conversation conversation);

    /**
     * 根据群名和类型查询群聊列表
     * @param name 群名
     * @param type 类型（2=群聊）
     * @return 符合条件的群聊列表
     */
    List<Conversation> findByNameAndType(String name, Integer type);
}

