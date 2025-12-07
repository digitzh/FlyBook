package com.bytedance.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytedance.entity.Message;
import com.bytedance.mapper.MessageMapper;
import com.bytedance.service.IMessageService;
import com.bytedance.usecase.message.SendMessageUseCase;
import com.bytedance.usecase.message.SyncMessagesUseCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息服务实现
 * 作为门面层，协调 UseCase
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    private final SendMessageUseCase sendMessageUseCase;
    private final SyncMessagesUseCase syncMessagesUseCase;

    @Autowired
    public MessageServiceImpl(SendMessageUseCase sendMessageUseCase,
                             SyncMessagesUseCase syncMessagesUseCase) {
        this.sendMessageUseCase = sendMessageUseCase;
        this.syncMessagesUseCase = syncMessagesUseCase;
    }

    @Override
    public Message sendMessage(Long conversationId, Long senderId, Integer msgType, String contentJson) {
        return sendMessageUseCase.execute(conversationId, senderId, msgType, contentJson);
    }

    @Override
    public Message sendTextMsg(Long conversationId, Long senderId, String text) {
        String json = JSONUtil.createObj().set("text", text).toString();
        return sendMessage(conversationId, senderId, 1, json);
    }

    @Override
    public List<Message> syncMessages(Long conversationId, Long afterSeq) {
        return syncMessagesUseCase.execute(conversationId, afterSeq);
    }
}

