package com.bytedance.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytedance.entity.Conversation;
import com.bytedance.entity.ConversationMember;
import com.bytedance.mapper.ConversationMapper;
import com.bytedance.mapper.ConversationMemberMapper;
import com.bytedance.repository.IConversationMemberRepository;
import com.bytedance.repository.IConversationRepository;
import com.bytedance.service.IConversationService;
import com.bytedance.usecase.conversation.AddMembersUseCase;
import com.bytedance.usecase.conversation.CreateConversationUseCase;
import com.bytedance.usecase.conversation.GetConversationListUseCase;
import com.bytedance.vo.ConversationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bytedance.utils.RedisUtils;
import cn.hutool.json.JSONUtil; // 引入 Hutool JSON 工具
import java.util.concurrent.TimeUnit;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 会话服务实现
 * 作为门面层，协调 UseCase
 */
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements IConversationService {

    private final CreateConversationUseCase createConversationUseCase;
    private final GetConversationListUseCase getConversationListUseCase;
    private final AddMembersUseCase addMembersUseCase;
    private final IConversationRepository conversationRepository;
    private final IConversationMemberRepository conversationMemberRepository;
    private final ConversationMemberMapper conversationMemberMapper;

    @Autowired
    public ConversationServiceImpl(CreateConversationUseCase createConversationUseCase,
                                  GetConversationListUseCase getConversationListUseCase,
                                  AddMembersUseCase addMembersUseCase,
                                  IConversationRepository conversationRepository,
                                  IConversationMemberRepository conversationMemberRepository,
                                  ConversationMemberMapper conversationMemberMapper) {
        this.createConversationUseCase = createConversationUseCase;
        this.getConversationListUseCase = getConversationListUseCase;
        this.addMembersUseCase = addMembersUseCase;
        this.conversationRepository = conversationRepository;
        this.conversationMemberRepository = conversationMemberRepository;
        this.conversationMemberMapper = conversationMemberMapper;
    }

    @Autowired
    private RedisUtils redisUtils; // 【注入 Redis 工具类】

    @Override
    public long createConversation(String name, Integer type, Long ownerId) {
        // 使用分布式锁防止并发创建相同会话
        String lockKey = "lock:conversation:create:" + name + ":" + type + ":" + ownerId;
        String lockValue = java.util.UUID.randomUUID().toString();
        
        try {
            // 尝试获取锁，超时时间5秒
            boolean lockAcquired = redisUtils.tryLock(lockKey, lockValue, 5);
            if (!lockAcquired) {
                throw new RuntimeException("系统繁忙，请稍后重试");
            }
            
            return createConversationUseCase.execute(name, type, ownerId);
        } finally {
            // 释放锁
            redisUtils.releaseLock(lockKey, lockValue);
        }
    }

    @Override
    public List<ConversationVO> getConversationList(Long userId) {
        return getConversationListUseCase.execute(userId);
    }

    @Override
    public void addMembers(Long conversationId, List<Long> targetUserIds, Long inviterId) {
        addMembersUseCase.execute(conversationId, targetUserIds, inviterId);
    }

    @Override
    public Long findExistingConversation(String name, Integer type, List<Long> memberIds) {
        // 如果群名为空或成员列表为空，不进行查找
        if (name == null || name.trim().isEmpty() || memberIds == null || memberIds.isEmpty()) {
            return null;
        }

        // 【新增】使用分布式锁保证查询和创建的原子性
        // 构建锁的key：基于群名、类型和成员列表的排序hash
        List<Long> sortedMemberIds = new java.util.ArrayList<>(memberIds);
        sortedMemberIds.sort(Long::compareTo);
        String memberHash = sortedMemberIds.toString();
        String lockKey = "lock:conversation:find:" + name + ":" + type + ":" + memberHash.hashCode();
        String lockValue = java.util.UUID.randomUUID().toString();

        try {
            // 尝试获取锁，超时时间3秒
            boolean lockAcquired = redisUtils.tryLock(lockKey, lockValue, 3);
            if (!lockAcquired) {
                // 如果获取锁失败，等待一小段时间后重试
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            // 1. 根据群名和类型查询所有群聊
            List<Conversation> conversations = conversationRepository.findByNameAndType(name, type);
            if (conversations.isEmpty()) {
                return null;
            }

            // 2. 将目标成员列表转换为 Set（去重并便于比较）
            Set<Long> targetMemberSet = new HashSet<>(memberIds);

            // 3. 遍历每个群聊，检查成员列表是否完全一致
            for (Conversation conversation : conversations) {
                List<ConversationMember> members = conversationMemberRepository
                        .findByConversationId(conversation.getConversationId());
                
                // 提取成员ID集合
                Set<Long> existingMemberSet = members.stream()
                        .map(ConversationMember::getUserId)
                        .collect(Collectors.toSet());

                // 比较两个集合是否完全一致（大小相同且包含所有元素）
                if (targetMemberSet.size() == existingMemberSet.size() 
                        && targetMemberSet.containsAll(existingMemberSet)) {
                    return conversation.getConversationId();
                }
            }

            return null;
        } finally {
            // 释放锁
            if (lockValue != null) {
                redisUtils.releaseLock(lockKey, lockValue);
            }
        }
    }

    @Override
    public void clearUnreadCount(Long conversationId, Long userId) {
        conversationMemberMapper.clearUnreadCount(conversationId, userId);
    }

    @Override
    public void clearAllUnreadCount(Long userId) {
        conversationMemberMapper.clearAllUnreadCount(userId);
    }

    @Override
    public void setConversationTop(Long conversationId, Long userId, boolean isTop) {
        // 使用 MyBatis-Plus 的 UpdateWrapper 直接更新字段
        // update conversation_member set is_top = ? where conversation_id = ? and user_id = ?
        conversationMemberMapper.update(null,
                new LambdaUpdateWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conversationId)
                        .eq(ConversationMember::getUserId, userId)
                        .set(ConversationMember::getIsTop, isTop) // 更新 isTop 字段
        );
    }

    @Override
    public void setConversationMuted(Long conversationId, Long userId, boolean isMuted) {
        // 使用 MyBatis-Plus 的 UpdateWrapper 直接更新字段
        // update conversation_member set is_muted = ? where conversation_id = ? and user_id = ?
        conversationMemberMapper.update(null,
                new LambdaUpdateWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conversationId)
                        .eq(ConversationMember::getUserId, userId)
                        .set(ConversationMember::getIsMuted, isMuted ? 1 : 0) // 更新 isMuted 字段（Integer类型：1=开启，0=关闭）
        );
    }

    @Override
    public void setMemberRole(Long conversationId, Long operatorId, Long targetUserId, Integer role) {
        // 1. 查询会话信息，验证操作者是否为群主
        Conversation conversation = conversationRepository.findById(conversationId);
        if (conversation == null) {
            throw new RuntimeException("会话不存在");
        }

        // 2. 验证是否为群聊
        if (conversation.getType() == null || conversation.getType() != 2) {
            throw new RuntimeException("只有群聊可以设置管理员");
        }

        // 3. 验证操作者是否为群主
        if (conversation.getOwnerId() == null || !conversation.getOwnerId().equals(operatorId)) {
            throw new RuntimeException("只有群主可以设置管理员");
        }

        // 4. 验证目标用户是否为会话成员
        ConversationMember targetMember = conversationMemberRepository.findByConversationIdAndUserId(conversationId, targetUserId);
        if (targetMember == null) {
            throw new RuntimeException("目标用户不是会话成员");
        }

        // 5. 验证角色值（1=成员, 2=管理员）
        if (role == null || (role != 1 && role != 2)) {
            throw new RuntimeException("角色值无效，必须是1（成员）或2（管理员）");
        }

        // 6. 更新角色
        conversationMemberMapper.update(null,
                new LambdaUpdateWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conversationId)
                        .eq(ConversationMember::getUserId, targetUserId)
                        .set(ConversationMember::getRole, role)
        );
    }
}
