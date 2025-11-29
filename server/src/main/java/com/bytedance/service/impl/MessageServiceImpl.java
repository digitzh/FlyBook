package com.bytedance.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bytedance.entity.*;
import com.bytedance.mapper.ConversationMapper;
import com.bytedance.mapper.ConversationMemberMapper;
import com.bytedance.mapper.MessageMapper;
import com.bytedance.service.IMessageService;
import com.bytedance.entity.Conversation;
import com.bytedance.entity.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bytedance.consumer.WebSocketServer;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {

    @Autowired
    private ConversationMapper conversationMapper;

    @Autowired
    private ConversationMemberMapper conversationMemberMapper;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Message sendMessage(Long conversationId, Long senderId, Integer msgType, String contentJson) {
        // 查询 conversation_member 表，看该用户是否在会话中
        Long count = conversationMemberMapper.selectCount(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conversationId)
                        .eq(ConversationMember::getUserId, senderId)
        );

        if (count == null || count == 0) {
            // 抛出异常，GlobalExceptionHandler 会捕获并返回给前端错误提示
            throw new RuntimeException("您不是该会话成员，无法发送消息");
        }

        // 1. 锁会话 & 校验
        Conversation conversation = conversationMapper.selectOne(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getConversationId, conversationId)
                        .last("FOR UPDATE")
        );
        if (conversation == null) throw new RuntimeException("会话不存在");

        // 2. 生成 Seq
        long newSeq = conversation.getCurrentSeq() + 1;

        // 3. 构建消息
        Message message = Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .seq(newSeq)
                .msgType(msgType)
                .content(contentJson)
                .createdTime(LocalDateTime.now())
                .build();

        this.save(message);

        // 4. 更新会话摘要
        conversation.setCurrentSeq(newSeq);
        conversation.setLastMsgTime(LocalDateTime.now());

        // 根据类型生成摘要
        String summary = "[未知消息]";
        if (msgType == 1) {
            summary = JSONUtil.parseObj(contentJson).getStr("text");
        } else if (msgType == 2) {
            summary = "[图片]";
        } else if (msgType == 5) {
            summary = "[待办任务]"; // 为之后做准备
        }
        conversation.setLastMsgContent(summary);
        conversationMapper.updateById(conversation);

        // 5. 更新未读数
        conversationMemberMapper.incrementUnreadCount(conversationId, senderId);

        // 6. 实时推送
        // 6.1 找出群里除了我以外的所有人
        List<ConversationMember> members = conversationMemberMapper.selectList(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conversationId)
        );

        // 6.2 构建推送 JSON
        String pushJson = JSONUtil.toJsonStr(message);

        // 6.3 循环推送
        for (ConversationMember member : members) {
            // 排除自己
            if (!member.getUserId().equals(senderId)) {
                WebSocketServer.pushMessage(member.getUserId(), pushJson);
            }
        }

        return message;
    }


    // 保留旧接口兼容
    @Override
    public Message sendTextMsg(Long conversationId, Long senderId, String text) {
        String json = JSONUtil.createObj().set("text", text).toString();
        return sendMessage(conversationId, senderId, 1, json);
    }


    @Override
    public List<Message> syncMessages(Long conversationId, Long afterSeq) {
        // 查找该会话中 seq > 客户端传来的afterSeq 的所有消息，按 seq 升序排列
        return this.list(new LambdaQueryWrapper<Message>()
                .eq(Message::getConversationId, conversationId)
                .gt(Message::getSeq, afterSeq)
                .orderByAsc(Message::getSeq)
                // 限制一次拉取的数量
                .last("LIMIT 100")
        );
    }

}

