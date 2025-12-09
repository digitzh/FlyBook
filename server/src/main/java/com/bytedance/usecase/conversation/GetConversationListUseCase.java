package com.bytedance.usecase.conversation;

import com.bytedance.entity.Conversation;
import com.bytedance.entity.ConversationMember;
import com.bytedance.entity.User;
import com.bytedance.repository.IConversationMemberRepository;
import com.bytedance.repository.IConversationRepository;
import com.bytedance.repository.IUserRepository;
import com.bytedance.vo.ConversationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 获取会话列表用例
 * 封装获取用户会话列表的业务逻辑
 */
@Component
public class GetConversationListUseCase {

    private final IConversationMemberRepository conversationMemberRepository;
    private final IConversationRepository conversationRepository;
    private final IUserRepository userRepository;

    @Autowired
    public GetConversationListUseCase(IConversationMemberRepository conversationMemberRepository,
                                     IConversationRepository conversationRepository,
                                     IUserRepository userRepository) {
        this.conversationMemberRepository = conversationMemberRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
    }

    /**
     * 执行获取会话列表逻辑
     */
    public List<ConversationVO> execute(Long userId) {
        // 1. 查出我参与的所有会话关系
        List<ConversationMember> myMemberships = conversationMemberRepository.findByUserId(userId);

        if (myMemberships.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 提取 conversationId 列表
        List<Long> conversationIds = myMemberships.stream()
                .map(ConversationMember::getConversationId)
                .collect(Collectors.toList());

        // 3. 查出所有会话的本体信息
        List<Conversation> conversations = conversationRepository.findByIds(conversationIds);
        Map<Long, Conversation> convMap = conversations.stream()
                .collect(Collectors.toMap(Conversation::getConversationId, c -> c));

        // 4. 组装 VO
        List<ConversationVO> voList = new ArrayList<>();

        for (ConversationMember myMember : myMemberships) {
            Conversation conv = convMap.get(myMember.getConversationId());
            if (conv == null) continue;

            String showName = conv.getName();
            String showAvatar = conv.getAvatarUrl();

            // 如果是单聊，名字和头像不能用会话的，要用"对方"的
            if (conv.getType() == 1) {
                // 查该会话的所有成员
                List<ConversationMember> allMembers = conversationMemberRepository
                        .findByConversationId(conv.getConversationId());

                // 找到那个 userId 不等于当前 userId 的人
                Long targetId = null;
                for (ConversationMember m : allMembers) {
                    if (!m.getUserId().equals(userId)) {
                        targetId = m.getUserId();
                        break;
                    }
                }

                // 查 User 表拿对方详情
                if (targetId != null) {
                    User targetUser = userRepository.findById(targetId);
                    if (targetUser != null) {
                        showName = targetUser.getUsername();
                        showAvatar = targetUser.getAvatarUrl();
                    }
                }
            }

            ConversationVO vo = ConversationVO.builder()
                    .conversationId(conv.getConversationId())
                    .type(conv.getType())
                    .name(showName)
                    .avatarUrl(showAvatar)
                    .lastMsgContent(conv.getLastMsgContent())
                    .lastMsgTime(conv.getLastMsgTime())
                    .unreadCount(myMember.getUnreadCount())
                    .isTop(myMember.getIsTop() != null && myMember.getIsTop()) // 【新增】设置VO状态
                    .build();

            voList.add(vo);
        }

        // 5. 排序
        voList.sort((a, b) -> {
            boolean topA = Boolean.TRUE.equals(a.getIsTop());
            boolean topB = Boolean.TRUE.equals(b.getIsTop());

            // 1. 先比较置顶状态
            if (topA && !topB) return -1; // A置顶，B不置顶 -> A在前
            if (!topA && topB) return 1;  // B置顶，A不置顶 -> B在前

            // 2. 都在同一层级（都置顶 或 都不置顶），再比较时间
            LocalDateTime timeA = a.getLastMsgTime();
            LocalDateTime timeB = b.getLastMsgTime();

            if (timeB == null) return -1;
            if (timeA == null) return 1;
            return timeB.compareTo(timeA); // 时间倒序
        });

        return voList;
    }
}

