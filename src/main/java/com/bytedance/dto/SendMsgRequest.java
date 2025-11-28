package com.bytedance.dto;

import lombok.Data;

@Data
public class SendMsgRequest {
    private Long conversationId;
    private Integer msgType; // 新增：允许前端指定类型 (1=文本, 2=图片, 5=待办事项)
    private String content;  // 这是一个 JSON 字符串
}
