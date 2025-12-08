package com.example.myhomepage.share

import com.example.myhomepage.ui.theme.TodoType
import kotlinx.serialization.Serializable

// 待办卡片数据结构
@Serializable
data class TodoShareCard(
    val todoId: Long,
    val title: String,
    val description: String,
    val type: TodoType,
    val deadline: String?,
    val done: Boolean
)

// 从待办发起分享的接口
interface ShareTodoToChat {
    fun share(card: TodoShareCard)
}

// 从聊天打开详情的接口
interface OpenSharedTodoDetail {
    fun open(card: TodoShareCard)
}
