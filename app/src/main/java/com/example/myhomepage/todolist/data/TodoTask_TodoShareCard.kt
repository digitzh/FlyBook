package com.example.myhomepage.todolist.data

import com.example.myhomepage.share.TodoShareCard
import com.example.myhomepage.todolist.domain.TodoTask

fun TodoTask.toShareCard(): TodoShareCard {
    return TodoShareCard(
        todoId = id,
        title = title,
        description = description,
        deadline = deadline ?: "",
        type = type,
        done = isCompleted,
    )
}

// 从分享卡片变成一个“新建”的 TodoTask（id 交给 Room 自增）
fun TodoShareCard.toTodoTask(): TodoTask {
    return TodoTask(
        id = 0L,               // 让 Room autoGenerate
        title = title,
        description = description,
        deadline = deadline,
        type = type,
        isCompleted = done,
    )
}