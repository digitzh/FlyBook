package com.bytedance.usecase.message;

import cn.hutool.json.JSONUtil;
import com.bytedance.consumer.WebSocketServer;
import com.bytedance.entity.Conversation;
import com.bytedance.entity.ConversationMember;
import com.bytedance.entity.Message;
import com.bytedance.repository.IConversationMemberRepository;
import com.bytedance.repository.IConversationRepository;
import com.bytedance.repository.IMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 发送消息用例
 * 封装发送消息的业务逻辑
 */
@Component
public class SendMessageUseCase {

    private final IMessageRepository messageRepository;
    private final IConversationRepository conversationRepository;
    private final IConversationMemberRepository conversationMemberRepository;

    @Autowired
    public SendMessageUseCase(IMessageRepository messageRepository,
                             IConversationRepository conversationRepository,
                             IConversationMemberRepository conversationMemberRepository) {
        this.messageRepository = messageRepository;
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
    }

    /**
     * 执行发送消息逻辑
     * @return 保存的消息实体
     */
    @Transactional(rollbackFor = Exception.class)
    public Message execute(Long conversationId, Long senderId, Integer msgType, String contentJson) {
        // 1. 查询 conversation_member 表，看该用户是否在会话中
        if (!conversationMemberRepository.existsByConversationIdAndUserId(conversationId, senderId)) {
            throw new RuntimeException("您不是该会话成员，无法发送消息");
        }

        // 2. 锁会话 & 校验
        Conversation conversation = conversationRepository.findByIdForUpdate(conversationId);
        if (conversation == null) {
            throw new RuntimeException("会话不存在");
        }

        // 3. 生成 Seq
        long newSeq = conversation.getCurrentSeq() + 1;

        // 4. 构建消息
        Message message = Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .seq(newSeq)
                .msgType(msgType)
                .content(contentJson)
                .createdTime(LocalDateTime.now())
                .build();

        messageRepository.save(message);

        // 5. 更新会话摘要
        conversation.setCurrentSeq(newSeq);
        conversation.setLastMsgTime(LocalDateTime.now());

        // 根据类型生成摘要
        String summary = "[未知消息]";
        if (msgType == 1) {
            summary = JSONUtil.parseObj(contentJson).getStr("text");
        } else if (msgType == 2) {
            summary = "[图片]";
        } else if (msgType == 5) {
            summary = "[待办任务]";
        }
        conversation.setLastMsgContent(summary);
        conversationRepository.update(conversation);

        // 6. 更新未读数
        conversationMemberRepository.incrementUnreadCount(conversationId, senderId);

        // 7. 实时推送
        List<ConversationMember> members = conversationMemberRepository
                .findByConversationId(conversationId);

        // 构建推送 JSON
        String pushJson = JSONUtil.toJsonStr(message);

        // 循环推送
        for (ConversationMember member : members) {
            // 排除自己
            if (!member.getUserId().equals(senderId)) {
                WebSocketServer.pushMessage(member.getUserId(), pushJson);
            }
        }

        return message;
    }
}

