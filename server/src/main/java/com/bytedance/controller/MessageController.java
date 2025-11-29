package com.bytedance.controller;

import cn.hutool.json.JSONUtil;
import com.bytedance.common.Result;
import com.bytedance.dto.SendMsgRequest;
import com.bytedance.entity.Message;
import com.bytedance.service.IMessageService;
import com.bytedance.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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

        // 处理消息内容：优先使用 text 字段，如果没有则使用 content 字段
        String contentJson;
        if (StringUtils.hasLength(request.getText())) {
            // 如果提供了 text 字段，将其转换为 JSON 格式: {"text": "消息内容"}
            contentJson = JSONUtil.createObj().set("text", request.getText()).toString();
        } else if (StringUtils.hasLength(request.getContent())) {
            // 如果提供了 content 字段（JSON 字符串），直接使用
            contentJson = request.getContent();
        } else {
            // 两者都没有，抛出异常
            throw new RuntimeException("消息内容不能为空，请提供 text 或 content 字段");
        }

        Message msg = messageService.sendMessage(
                request.getConversationId(),
                currentUserId,
                request.getMsgType(),
                contentJson
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

