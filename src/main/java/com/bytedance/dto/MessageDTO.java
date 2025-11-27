package com.bytedance.dto;

import lombok.Data;


@Data
public class MessageDTO {
    private Long conversationId;
    private Long senderId; // 暂时由客户端传，以后改为从 Header Token 解析
    private String text;
}
