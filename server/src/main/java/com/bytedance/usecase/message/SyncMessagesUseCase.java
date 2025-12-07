package com.bytedance.usecase.message;

import com.bytedance.entity.Message;
import com.bytedance.repository.IMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 同步消息用例
 * 封装同步历史消息的业务逻辑
 */
@Component
public class SyncMessagesUseCase {

    private final IMessageRepository messageRepository;

    @Autowired
    public SyncMessagesUseCase(IMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * 执行同步消息逻辑
     * @param conversationId 会话ID
     * @param afterSeq 起始序列号（不包含）
     * @return 消息列表
     */
    public List<Message> execute(Long conversationId, Long afterSeq) {
        // 查找该会话中 seq > afterSeq 的所有消息，按 seq 升序排列
        // 限制一次拉取的数量为 100
        return messageRepository.findByConversationIdAndSeqAfter(conversationId, afterSeq, 100);
    }
}

