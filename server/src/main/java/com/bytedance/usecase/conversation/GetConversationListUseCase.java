package com.bytedance.usecase.conversation;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.bytedance.entity.Conversation;
import com.bytedance.entity.ConversationMember;
import com.bytedance.entity.User;
import com.bytedance.repository.IConversationMemberRepository;
import com.bytedance.repository.IConversationRepository;
import com.bytedance.repository.IUserRepository;
import com.bytedance.utils.RedisUtils;
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
    private final RedisUtils redisUtils; // 【新增】Redis 工具类

    @Autowired
    public GetConversationListUseCase(IConversationMemberRepository conversationMemberRepository,
                                      IConversationRepository conversationRepository,
                                      IUserRepository userRepository,
                                      RedisUtils redisUtils) {
        this.conversationMemberRepository = conversationMemberRepository;
        this.conversationRepository = conversationRepository;
        this.userRepository = userRepository;
        this.redisUtils = redisUtils;
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
                // 4.1 查该会话的所有成员 (这里其实还有优化空间，避免循环内查库)
                List<ConversationMember> allMembers = conversationMemberRepository
                        .findByConversationId(conv.getConversationId());

                // 4.2 找到那个 userId 不等于当前 userId 的人
                Long targetId = null;
                for (ConversationMember m : allMembers) {
                    if (!m.getUserId().equals(userId)) {
                        targetId = m.getUserId();
                        break;
                    }
                }

                // 4.3 【核心修改】加入 Redis 缓存逻辑
                if (targetId != null) {
                    User targetUser = null;
                    String userCacheKey = "user:info:" + targetId;

                    // A. 先查 Redis
                    String userJson = redisUtils.get(userCacheKey);

                    if (StrUtil.isNotBlank(userJson)) {
                        // B. 缓存命中：反序列化
                        targetUser = JSONUtil.toBean(userJson, User.class);
                    } else {
                        // C. 缓存未命中：查数据库
                        targetUser = userRepository.findById(targetId);
                        if (targetUser != null) {
                            // D. 写入 Redis (过期时间设为 1 天 = 86400 秒)
                            redisUtils.set(userCacheKey, JSONUtil.toJsonStr(targetUser), 86400);
                        }
                    }

                    // E. 赋值
                    if (targetUser != null) {
                        // 优先显示备注或昵称，这里简化为 username
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
                    .isTop(myMember.getIsTop() != null && myMember.getIsTop())
                    .build();

            voList.add(vo);
        }

        // 5. 排序 (置顶优先，其次按时间倒序)
        voList.sort((a, b) -> {
            boolean topA = Boolean.TRUE.equals(a.getIsTop());
            boolean topB = Boolean.TRUE.equals(b.getIsTop());

            // 5.1 先比较置顶状态
            if (topA && !topB) return -1; // A置顶，B不置顶 -> A在前
            if (!topA && topB) return 1;  // B置顶，A不置顶 -> B在前

            // 5.2 都在同一层级，再比较时间
            LocalDateTime timeA = a.getLastMsgTime();
            LocalDateTime timeB = b.getLastMsgTime();

            if (timeB == null) return -1;
            if (timeA == null) return 1;
            return timeB.compareTo(timeA); // 时间倒序
        });

        return voList;
    }
}
