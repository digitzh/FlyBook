package com.bytedance.controller;

import cn.hutool.log.Log;
import com.bytedance.dto.AddMemberRequest;
import com.bytedance.dto.ConversationDTO;
import com.bytedance.dto.TopRequest;
import com.bytedance.utils.UserContext;
import com.bytedance.vo.ConversationVO;
import com.bytedance.common.Result;
import com.bytedance.service.IConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

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
     * 如果创建会话时提供了成员列表，会先检查是否存在相同会话名和成员的会话
     */
    @PostMapping("/create")
    public Result<Long> create(@RequestBody ConversationDTO request) {
        Long userId = getUserId(request.getUserId());
        // 如果请求体中提供了 ownerId，使用它；否则使用当前用户ID
        Long ownerId = request.getOwnerId() != null ? request.getOwnerId() : userId;
        
        // 如果提供了成员列表，先检查是否存在相同的会话
        if (request.getType() != null
                && request.getTargetUserIds() != null && !request.getTargetUserIds().isEmpty()) {
            // 构建完整的成员列表（包括创建者），并去重
            Set<Long> allMemberSet = new java.util.HashSet<>();
            allMemberSet.add(ownerId);
            allMemberSet.addAll(request.getTargetUserIds());
            List<Long> allMemberIds = new java.util.ArrayList<>(allMemberSet);
            
            // 检查是否存在相同的会话
            Long existingConversationId = conversationService.findExistingConversation(
                    request.getName(), request.getType(), allMemberIds);
            
            if (existingConversationId != null) {
                // 如果存在相同的群聊，返回已有群聊ID
                return Result.success(existingConversationId);
            }
        }
        
        // 创建新群聊
        long id = conversationService.createConversation(request.getName(), request.getType(), ownerId);
        
        // 如果提供了成员列表，添加成员
        if (request.getTargetUserIds() != null && !request.getTargetUserIds().isEmpty()) {
            conversationService.addMembers(id, request.getTargetUserIds(), ownerId);
        }
        
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
     * 清除某个会话的未读消息数
     * URL: POST /api/conversations/unread/clear?conversationId=1
     */
    @PostMapping("/unread/clear")
    public Result<Void> clearUnreadCount(@RequestParam Long conversationId) {
        Long userId = getUserId();
        conversationService.clearUnreadCount(conversationId, userId);
        return Result.success();
    }

    /**
     * 清除用户所有会话的未读消息数
     * URL: POST /api/conversations/unread/clear-all
     */
    @PostMapping("/unread/clear-all")
    public Result<Void> clearAllUnreadCount() {
        Long userId = getUserId();
        conversationService.clearAllUnreadCount(userId);
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

    @PostMapping("/top")
    public Result<String> setTop(@RequestBody TopRequest request) {
        Long userId = UserContext.getUserId(); // 获取当前登录用户ID
        conversationService.setConversationTop(request.getConversationId(), userId, request.getIsTop());
        return Result.success("操作成功");
    }
}

