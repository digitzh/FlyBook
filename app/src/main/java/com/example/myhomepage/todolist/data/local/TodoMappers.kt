package com.example.myhomepage.todolist.data.local

import com.example.myhomepage.todolist.domain.TodoTask

fun TodoTaskEntity.toDomain(): TodoTask =
    TodoTask(
        id = id,
        title = title,
        description = description,
        deadline = deadline,
        isCompleted = isCompleted,
        type = type
    )

fun TodoTask.toEntity(): TodoTaskEntity =
    TodoTaskEntity(
        id = id,
        title = title,
        description = description,
        deadline = deadline,
        isCompleted = isCompleted,
        type = type
    )
