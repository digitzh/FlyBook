package com.bytedance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytedance.entity.ConversationMember;
import com.bytedance.mapper.ConversationMemberMapper;
import com.bytedance.service.IConversationMemberService;
import org.springframework.stereotype.Service;

@Service
public class ConversationMemberServiceImpl extends ServiceImpl<ConversationMemberMapper, ConversationMember>
        implements IConversationMemberService {
}
