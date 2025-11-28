package com.bytedance.dto;

import lombok.Data;


@Data
public class MessageDTO {
    private Long conversationId;
    private String text;
}
