package com.bytedance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytedance.entity.Conversation;
import com.bytedance.mapper.ConversationMapper;
import com.bytedance.service.IConversationService;
import com.bytedance.usecase.conversation.AddMembersUseCase;
import com.bytedance.usecase.conversation.CreateConversationUseCase;
import com.bytedance.usecase.conversation.GetConversationListUseCase;
import com.bytedance.vo.ConversationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会话服务实现
 * 作为门面层，协调 UseCase
 */
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements IConversationService {

    private final CreateConversationUseCase createConversationUseCase;
    private final GetConversationListUseCase getConversationListUseCase;
    private final AddMembersUseCase addMembersUseCase;

    @Autowired
    public ConversationServiceImpl(CreateConversationUseCase createConversationUseCase,
                                  GetConversationListUseCase getConversationListUseCase,
                                  AddMembersUseCase addMembersUseCase) {
        this.createConversationUseCase = createConversationUseCase;
        this.getConversationListUseCase = getConversationListUseCase;
        this.addMembersUseCase = addMembersUseCase;
    }

    @Override
    public long createConversation(String name, Integer type, Long ownerId) {
        return createConversationUseCase.execute(name, type, ownerId);
    }

    @Override
    public List<ConversationVO> getConversationList(Long userId) {
        return getConversationListUseCase.execute(userId);
    }

    @Override
    public void addMembers(Long conversationId, List<Long> targetUserIds, Long inviterId) {
        addMembersUseCase.execute(conversationId, targetUserIds, inviterId);
    }
}
