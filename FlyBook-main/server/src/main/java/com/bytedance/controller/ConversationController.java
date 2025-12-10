package com.bytedance.controller;

import com.bytedance.dto.AddMemberRequest;
import com.bytedance.dto.ConversationDTO;
import com.bytedance.utils.UserContext;
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
    public Result<List<ConversationVO>> getList() {
        Long userId = getUserId();
        List<ConversationVO> list = conversationService.getConversationList(userId);
        return Result.success(list);
    }

    /**
     * 创建会话 (为了方便 APIFox 测试，也暴露出来)
     */
    @PostMapping("/create")
    public Result<Long> create(@RequestBody ConversationDTO request) {
        Long userId = getUserId(request.getUserId());
        // 如果请求体中提供了 ownerId，使用它；否则使用当前用户ID
        Long ownerId = request.getOwnerId() != null ? request.getOwnerId() : userId;
        long id = conversationService.createConversation(request.getName(), request.getType(), ownerId);
        return Result.success(id);
    }

    /**
     * 邀请成员入群
     * URL: POST /api/conversations/members/add
     */
    @PostMapping("/members/add")
    public Result<Void> addMembers(@RequestBody AddMemberRequest request) {
        Long userId = getUserId(request.getInviterId());
        conversationService.addMembers(
                request.getConversationId(),
                request.getTargetUserIds(),
                userId
        );
        return Result.success();
    }

    /**
     * 获取用户ID，优先使用请求体中的 userId，否则使用 UserContext 中的
     */
    private Long getUserId() {
        return getUserId(null);
    }

    /**
     * 获取用户ID，优先级：请求体中的 userId > UserContext 中的 userId
     */
    private Long getUserId(Long requestBodyUserId) {
        // 如果请求体中提供了 userId，优先使用
        if (requestBodyUserId != null) {
            return requestBodyUserId;
        }
        // 否则使用 UserContext 中的 userId（由拦截器设置）
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new RuntimeException("无法获取用户ID，请提供 userId");
        }
        return userId;
    }
}

