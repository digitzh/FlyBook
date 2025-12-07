package com.bytedance.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bytedance.entity.Message;
import com.bytedance.mapper.MessageMapper;
import com.bytedance.repository.IMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 消息数据访问实现（MySQL）
 */
@Repository
public class MessageRepositoryImpl implements IMessageRepository {

    private final MessageMapper messageMapper;

    @Autowired
    public MessageRepositoryImpl(MessageMapper messageMapper) {
        this.messageMapper = messageMapper;
    }

    @Override
    public void save(Message message) {
        if (message.getMessageId() == null) {
            messageMapper.insert(message);
        } else {
            messageMapper.updateById(message);
        }
    }

    @Override
    public List<Message> findByConversationIdAndSeqAfter(Long conversationId, Long afterSeq, int limit) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getConversationId, conversationId)
                        .gt(Message::getSeq, afterSeq)
                        .orderByAsc(Message::getSeq)
                        .last("LIMIT " + limit)
        );
    }
}

