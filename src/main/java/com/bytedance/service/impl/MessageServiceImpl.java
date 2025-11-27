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
    @Transactional(rollbackFor = Exception.class) // 开启事务：任何一步报错，全部回滚
    public Message sendTextMsg(Long conversationId, Long senderId, String text) {
        // 1. 悲观锁锁定会话
        // SELECT * FROM conversations WHERE id = ? FOR UPDATE
        // 这行代码会锁住该行记录，直到事务结束。防止多个人同时修改 seq。
        Conversation conversation = conversationMapper.selectOne(
                new LambdaQueryWrapper<Conversation>()
                        .eq(Conversation::getConversationId, conversationId)
                        .last("FOR UPDATE")
        );

        if (conversation == null) {
            throw new RuntimeException("会话不存在");
        }

        // 2. 生成新的 Seq
        long newSeq = conversation.getCurrentSeq() + 1;

        // 3. 组装消息实体
        Message message = Message.builder()
                .conversationId(conversationId)
                .senderId(senderId)
                .seq(newSeq) // 填入连续的序号
                .msgType(1)  // 1代表文本
                .content(JSONUtil.createObj().set("text", text).toString()) // Hutool 生成 JSON
                .createdTime(LocalDateTime.now())
                .build();

        // 4. 保存消息到 DB
        this.save(message);

        // 5. 更新会话的最新状态 (Seq, LastMsg)
        conversation.setCurrentSeq(newSeq);
        conversation.setLastMsgContent(text);
        conversation.setLastMsgTime(LocalDateTime.now());
        conversationMapper.updateById(conversation);

        // 6. 更新群里其他成员的未读数 (调用 Mapper 中写的自定义 SQL)
        conversationMemberMapper.incrementUnreadCount(conversationId, senderId);

        // 7.实时推送
        // 通知这个会话里的“其他成员”
        // 7.1 找出群里除了我以外的所有人
        List<ConversationMember> members = conversationMemberMapper.selectList(
                new LambdaQueryWrapper<ConversationMember>()
                        .eq(ConversationMember::getConversationId, conversationId)
        );

        // 7.2 构建推送的 JSON 数据
        // 推送完整消息，这样客户端收到后可以直接上屏，不用再查一次接口
        String pushJson = JSONUtil.toJsonStr(message);

        // 7.3 循环推送
        for (ConversationMember member : members) {
            // 排除自己 (自己发的没必要推给自己，或者推给自己用于多端同步，这里先排除)
            if (!member.getUserId().equals(senderId)) {
                WebSocketServer.pushMessage(member.getUserId(), pushJson);
            }
        }

        return message;
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

