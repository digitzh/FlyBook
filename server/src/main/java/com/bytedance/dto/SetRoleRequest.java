package com.bytedance.dto;

import lombok.Data;

/**
 * 设置管理员请求DTO
 */
@Data
public class SetRoleRequest {
    private Long conversationId;
    private Long targetUserId; // 要设置的用户ID
    private Integer role; // 1=成员, 2=管理员
}

