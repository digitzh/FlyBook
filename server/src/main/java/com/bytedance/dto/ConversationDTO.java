package com.bytedance.dto;

import lombok.Data;

// DTO
@Data
public class ConversationDTO {
    private String name;
    private Integer type;
    private Long ownerId;
}
