package com.bytedance.usecase.conversation;

import com.bytedance.entity.Conversation;
import com.bytedance.entity.ConversationMember;
import com.bytedance.entity.User;
import com.bytedance.repository.IConversationMemberRepository;
import com.bytedance.repository.IConversationRepository;
import com.bytedance.repository.IUserRepository;
import com.bytedance.usecase.message.SendMessageUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 添加成员用例
 * 封装添加成员到会话的业务逻辑
 */
@Component
public class AddMembersUseCase {

    private final IConversationRepository conversationRepository;
    private final IConversationMemberRepository conversationMemberRepository;
    private final IUserRepository userRepository;
    private final SendMessageUseCase sendMessageUseCase;

    @Autowired
    public AddMembersUseCase(IConversationRepository conversationRepository,
                             IConversationMemberRepository conversationMemberRepository,
                             IUserRepository userRepository,
                             SendMessageUseCase sendMessageUseCase) {
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.userRepository = userRepository;
        this.sendMessageUseCase = sendMessageUseCase;
    }

    /**
     * 执行添加成员逻辑
     */
    @Transactional(rollbackFor = Exception.class)
    public void execute(Long conversationId, List<Long> targetUserIds, Long inviterId) {
        // 1. 校验会话
        Conversation conversation = conversationRepository.findById(conversationId);
        if (conversation == null) {
            throw new RuntimeException("会话不存在");
        }
        if (conversation.getType() == 1) {
            throw new RuntimeException("单聊不能直接加人，请创建新群");
        }

        // 2. 过滤掉已经在群里的人 (去重)
        List<ConversationMember> existingMembers = conversationMemberRepository
                .findByConversationId(conversationId);
        Set<Long> existingUserIds = existingMembers.stream()
                .map(ConversationMember::getUserId)
                .collect(Collectors.toSet());

        List<Long> effectiveUserIds = targetUserIds.stream()
                .distinct()
                .filter(uid -> !existingUserIds.contains(uid))
                .collect(Collectors.toList());

        if (effectiveUserIds.isEmpty()) {
            return; // 都在群里了，无需操作
        }

        // 3. 批量插入成员
        List<ConversationMember> newMembers = new ArrayList<>();
        for (Long userId : effectiveUserIds) {
            ConversationMember member = ConversationMember.builder()
                    .conversationId(conversationId)
                    .userId(userId)
                    .role(0) // 普通成员
                    .unreadCount(0)
                    .joinedTime(LocalDateTime.now())
                    .build();
            newMembers.add(member);
        }
        conversationMemberRepository.saveBatch(newMembers);

        // 4. 发送一条系统通知消息
        User inviter = userRepository.findById(inviterId);
        List<User> newUsers = userRepository.findByIds(effectiveUserIds);
        String joinedNames = newUsers.stream()
                .map(User::getUsername)
                .collect(Collectors.joining("、"));

        String content = String.format("%s 邀请 %s 加入了群聊", inviter.getUsername(), joinedNames);
        sendMessageUseCase.execute(conversationId, inviterId, 1, 
                "{\"text\":\"" + content + "\"}");
    }
}

