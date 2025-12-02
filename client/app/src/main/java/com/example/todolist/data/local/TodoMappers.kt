package com.example.todolist.data.local

import com.example.todolist.domain.model.TodoTask

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
