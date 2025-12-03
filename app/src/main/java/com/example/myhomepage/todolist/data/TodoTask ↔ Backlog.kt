package com.example.myhomepage.todolist.data

import com.example.myhomepage.data.Backlog
import com.example.myhomepage.todolist.data.TodoTask
import kotlin.toString

fun TodoTask.toBacklog(): Backlog {
    return Backlog(
        id = id,
        title = title,
        text = description,
        time = deadline ?: "",
        type = type,
        complete = isCompleted,
        avatar = 0        // 如有需要再根据 type 或别的信息设置头像
    )
}