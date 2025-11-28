package com.bytedance.controller;

import com.bytedance.common.Result;
import com.bytedance.dto.MessageDTO;
import com.bytedance.dto.SendMsgRequest;
import com.bytedance.entity.Message;
import com.bytedance.service.IMessageService;
import com.bytedance.utils.UserContext;
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
    // Controller 方法
    @PostMapping("/send")
    public Result<Message> send(@RequestBody SendMsgRequest request) {
        Long currentUserId = UserContext.getUserId();

        // 简单的校验
        if (request.getMsgType() == null) request.setMsgType(1); // 默认文本

        Message msg = messageService.sendMessage(
                request.getConversationId(),
                currentUserId,
                request.getMsgType(),
                request.getContent()
        );
        return Result.success(msg);
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

