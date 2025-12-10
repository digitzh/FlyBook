package com.bytedance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytedance.entity.User;
import com.bytedance.service.IConversationMemberService;
import com.bytedance.service.IMessageService;
import com.bytedance.service.IUserService;
import com.bytedance.vo.ConversationVO;
import com.bytedance.entity.Conversation;
import com.bytedance.entity.ConversationMember;
import com.bytedance.mapper.ConversationMapper;
import com.bytedance.mapper.ConversationMemberMapper;
import com.bytedance.service.IConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements IConversationService {

    @Autowired
    private ConversationMemberMapper conversationMemberMapper;

    @Autowired
    private IConversationMemberService conversationMemberService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IMessageService messageService; // 需要调用发消息服务


    @Override
    @Transactional(rollbackFor = Exception.class)
    public long createConversation(String name, Integer type, Long ownerId) {
        // 1. 创建会话
        Conversation conversation = Conversation.builder()
                .name(name)
                .type(type)
                .ownerId(ownerId)
                .currentSeq(0L)
                .createdTime(LocalDateTime.now())
                .build();
        this.save(conversation);

        // 2. 【新增逻辑】把自己（Owner）加入到成员表
        ConversationMember member = ConversationMember.builder()
                .conversationId(conversation.getConversationId())
                .userId(ownerId)
                .role(1) // 1=Admin
                .unreadCount(0) // 自己建的群，未读数是0
                .joinedTime(LocalDateTime.now())
                .build();
        conversationMemberMapper.insert(member);

        return conversation.getConversationId();
    }

    @Override
    public List<ConversationVO> getConversationList(Long userId) {
        // 1. 查出我参与的所有会话关系
        List<ConversationMember> myMemberships = conversationMemberMapper.selectList(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getUserId, userId)
        );

        if (myMemberships.isEmpty()) {
            return new ArrayList<>();
        }

        // 2. 提取 conversationId 列表
        List<Long> conversationIds = myMemberships.stream()
                .map(ConversationMember::getConversationId)
                .collect(Collectors.toList());

        // 3. 查出所有会话的本体信息
        List<Conversation> conversations = this.listByIds(conversationIds);
        Map<Long, Conversation> convMap = conversations.stream()
                .collect(Collectors.toMap(Conversation::getConversationId, c -> c));

        // 4. 组装 VO
        List<ConversationVO> voList = new ArrayList<>();

        for (ConversationMember myMember : myMemberships) {
            Conversation conv = convMap.get(myMember.getConversationId());
            if (conv == null) continue;

            String showName = conv.getName();
            String showAvatar = conv.getAvatarUrl();

            // 如果是单聊，名字和头像不能用会话的，要用“对方”的
            if (conv.getType() == 1) {
                // 这是一个比较耗性能的操作，需要用 Redis 缓存，或者批量查询优化
                // 先简单实现：去找这个会话里的“另一个人”

                // 查该会话的所有成员
                List<ConversationMember> allMembers = conversationMemberMapper.selectList(
                        new LambdaQueryWrapper<ConversationMember>()
                                .eq(ConversationMember::getConversationId, conv.getConversationId())
                );

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
                    User targetUser = userService.getById(targetId);
                    if (targetUser != null) {
                        showName = targetUser.getUsername(); // 或者 nickname
                        showAvatar = targetUser.getAvatarUrl();
                    }
                }
            }

            ConversationVO vo = ConversationVO.builder()
                    .conversationId(conv.getConversationId())
                    .type(conv.getType())
                    .name(showName)       // 最终展示的名字
                    .avatarUrl(showAvatar) // 最终展示的头像
                    .lastMsgContent(conv.getLastMsgContent())
                    .lastMsgTime(conv.getLastMsgTime())
                    .unreadCount(myMember.getUnreadCount())
                    .build();

            voList.add(vo);
        }

        // 5. 排序
        voList.sort((a, b) -> {
            if (b.getLastMsgTime() == null) return -1;
            if (a.getLastMsgTime() == null) return 1;
            return b.getLastMsgTime().compareTo(a.getLastMsgTime());
        });

        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMembers(Long conversationId, List<Long> targetUserIds, Long inviterId) {
        // 1. 校验会话
        Conversation conversation = this.getById(conversationId);
        if (conversation == null) {
            throw new RuntimeException("会话不存在");
        }
        if (conversation.getType() == 1) {
            throw new RuntimeException("单聊不能直接加人，请创建新群");
        }

        // 2. 过滤掉已经在群里的人 (去重)
        List<ConversationMember> existingMembers = conversationMemberMapper.selectList(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conversationId)
        );
        Set<Long> existingUserIds = existingMembers.stream()
                .map(ConversationMember::getUserId)
                .collect(Collectors.toSet());

        List<Long> effectiveUserIds = targetUserIds.stream()
                .distinct() // 列表自身去重
                .filter(uid -> !existingUserIds.contains(uid)) // 排除已在群里的
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
                    .unreadCount(0) // 这里的逻辑可以优化：如果是新入群，未读数是否要设为当前最大seq？先设为0(看不到之前的消息？)
                    .joinedTime(LocalDateTime.now())
                    .build();
            newMembers.add(member);
        }

//        for (ConversationMember member : newMembers) {
//            conversationMemberMapper.insert(member);
//        }
        conversationMemberService.saveBatch(newMembers);

        // 4. 发送一条系统通知消息
        // "InviterName 邀请 UserA, UserB 加入了群聊"

        // 4.1 查名字
        User inviter = userService.getById(inviterId);
        List<User> newUsers = userService.listByIds(effectiveUserIds);
        String joinedNames = newUsers.stream().map(User::getUsername).collect(Collectors.joining("、"));

        String content = String.format("%s 邀请 %s 加入了群聊", inviter.getUsername(), joinedNames);

        // 4.2 发送消息 (复用之前的 sendTextMsg)
        // 这里复用 sendTextMsg 会导致 seq 自增，这是符合预期的，因为“加人”也是一个群事件
        messageService.sendTextMsg(conversationId, inviterId, content);

        // 实际上飞书这里会有一个特殊的 messageType (比如 100=System),
        // 客户端看到 type=100 会居中显示灰色小字，而不是气泡。
        //先当做普通文本发出去。
    }

}
