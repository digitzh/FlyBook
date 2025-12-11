package com.bytedance.dto;

import lombok.Data;

/**
 * 免打扰请求DTO
 */
@Data
public class MutedRequest {
    private Long conversationId;
    private Boolean isMuted; // true=开启免打扰, false=取消免打扰
}

