package com.example.myhomepage.todolist.data

import com.example.myhomepage.ui.theme.TodoType


/**
 * -待办任务实体
 *
 * - deadline 用 String? 存 yyyy-MM-dd 文本
 * - 实际可以用 LocalDate / Instant 等时间类型
 */
data class TodoTask(
    val id: Long,              // 任务唯一 id
    val title: String,         // 任务标题（主题）
    val description: String,   // 任务描述
    val deadline: String?,     // 截止日期，格式如 "2025-12-31"，可为空
    val type: TodoType,         // 任务类型
    val isCompleted: Boolean   // 是否完成
)