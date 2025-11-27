package com.bytedance.controller;

import com.bytedance.dto.AddMemberRequest;
import com.bytedance.dto.ConversationDTO;
import com.bytedance.vo.ConversationVO;
import com.bytedance.common.Result;
import com.bytedance.service.IConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    @Autowired
    private IConversationService conversationService;

    /**
     * 获取当前用户的会话列表
     * URL: GET /api/conversations/list?userId=1001
     */
    @GetMapping("/list")
    public Result<List<ConversationVO>> getList(@RequestParam Long userId) {
        // 在真实项目中，userId 应该从 Token 中解析，而不是前端传
        List<ConversationVO> list = conversationService.getConversationList(userId);
        return Result.success(list);
    }

    /**
     * 创建会话 (为了方便 APIFox 测试，也暴露出来)
     */
    @PostMapping("/create")
    public Result<Long> create(@RequestBody ConversationDTO request) {
        long id = conversationService.createConversation(request.getName(), request.getType(), request.getOwnerId());
        return Result.success(id);
    }

    /**
     * 邀请成员入群
     * URL: POST /api/conversations/members/add
     */
    @PostMapping("/members/add")
    public Result<Void> addMembers(@RequestBody AddMemberRequest request) {
        conversationService.addMembers(
                request.getConversationId(),
                request.getTargetUserIds(),
                request.getInviterId()
        );
        return Result.success();
    }
}

