package com.bytedance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytedance.vo.ConversationVO;
import com.bytedance.entity.Conversation;

import java.util.List;

public interface IConversationService extends IService<Conversation> {
    // 创建单聊或群聊
    long createConversation(String name, Integer type, Long ownerId);

    // 获取用户的会话列表
    List<ConversationVO> getConversationList(Long userId);

    //添加会话成员
    void addMembers(Long conversationId, List<Long> targetUserIds, Long inviterId);

    /**
     * 查找是否存在相同群名和成员的群聊
     * @param name 群名
     * @param type 类型（2=群聊）
     * @param memberIds 成员ID列表（包括创建者）
     * @return 如果存在相同的群聊，返回其ID；否则返回null
     */
    Long findExistingConversation(String name, Integer type, List<Long> memberIds);

    /**
     * 清除某个会话的未读消息数
     * @param conversationId 会话ID
     * @param userId 用户ID
     */
    void clearUnreadCount(Long conversationId, Long userId);

    /**
     * 清除用户所有会话的未读消息数
     * @param userId 用户ID
     */
    void clearAllUnreadCount(Long userId);

    void setConversationTop(Long conversationId, Long userId, boolean isTop);

    /**
     * 设置/取消会话免打扰
     * @param conversationId 会话ID
     * @param userId 用户ID
     * @param isMuted true=开启免打扰, false=取消免打扰
     */
    void setConversationMuted(Long conversationId, Long userId, boolean isMuted);

    /**
     * 设置成员角色（只有群主可以设置）
     * @param conversationId 会话ID
     * @param operatorId 操作者ID（必须是群主）
     * @param targetUserId 目标用户ID
     * @param role 角色：1=成员, 2=管理员
     * @throws RuntimeException 如果操作者不是群主，抛出异常
     */
    void setMemberRole(Long conversationId, Long operatorId, Long targetUserId, Integer role);
}
