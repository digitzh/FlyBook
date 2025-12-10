package com.bytedance.dto;

import lombok.Data;

// DTO
@Data
public class ConversationDTO {
    private String name;
    private Integer type;
    private Long ownerId;
    private Long userId; // 可选的用户ID（当关闭登录系统时使用）
}
