package com.example.myhomepage.share

import com.example.myhomepage.ui.theme.TodoType
import kotlinx.serialization.Serializable

// 待办卡片数据结构
@Serializable
data class TodoShareCard(
//    val version: Int = 1,           // 协议版本，未来扩展字段时用
    val todoId: Long,               // 待办自身 id，可用于调试、后续扩展（目前仅做标识用）
    val title: String,              // 待办标题（必填）
    val description: String,       // 任务描述
    val type: TodoType,             // 任务类型
    val deadline: String?,          // 截止日期，可为空
    val done: Boolean,              // 是否已完成
)
