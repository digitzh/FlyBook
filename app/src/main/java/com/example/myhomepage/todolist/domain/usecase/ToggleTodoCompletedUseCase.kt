package com.example.myhomepage.todolist.domain.usecase

import com.example.myhomepage.todolist.domain.repository.TodoRepository

// 切换一条任务的完成状态

class ToggleTodoCompletedUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(id: Long) {
        repository.toggleCompleted(id)
    }
}
