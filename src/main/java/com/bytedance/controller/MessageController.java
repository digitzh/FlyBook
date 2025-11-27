package com.bytedance.controller;

import com.bytedance.common.Result;
import com.bytedance.dto.MessageDTO;
import com.bytedance.entity.Message;
import com.bytedance.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private IMessageService messageService;

    /**
     * 接口1：发送消息
     * URL: POST /api/messages/send
     */
    @PostMapping("/send")
    public Result<Message> sendMessage(@RequestBody MessageDTO request) {
        if (request.getConversationId() == null || request.getText() == null) {
            return Result.fail("参数不完整");
        }

        // 调用 Service (这里暂时模拟当前用户是 request.getSenderId())
        // senderId 应该从 Token 中获取
        Message message = messageService.sendTextMsg(
                request.getConversationId(),
                request.getSenderId(),
                request.getText()
        );

        return Result.success(message);
    }

    /**
     * 接口2：同步/拉取消息
     * URL: GET /api/messages/sync?conversationId=1&afterSeq=0
     */
    @GetMapping("/sync")
    public Result<List<Message>> syncMessages(
            @RequestParam Long conversationId,
            @RequestParam(defaultValue = "0") Long afterSeq
    ) {
        List<Message> messages = messageService.syncMessages(conversationId, afterSeq);
        return Result.success(messages);
    }
}

