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
}
