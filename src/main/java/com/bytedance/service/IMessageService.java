package com.bytedance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bytedance.entity.Message;

import java.util.List;

public interface IMessageService extends IService<Message> {
    /**
     * 发送文本消息 (核心高并发业务)
     * @param conversationId 会话ID
     * @param senderId 发送者ID
     * @param text 文本内容
     * @return 刚刚保存的消息实体（包含生成的 seq 和 messageId）
     */
    Message sendTextMsg(Long conversationId, Long senderId, String text);

    // 通用发送接口
    Message sendMessage(Long conversationId, Long senderId, Integer msgType, String contentJson);

    // 拉取历史消息
    List<Message> syncMessages(Long conversationId, Long afterSeq);
}
