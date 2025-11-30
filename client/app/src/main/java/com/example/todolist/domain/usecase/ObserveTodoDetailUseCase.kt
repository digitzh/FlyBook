package com.example.todolist.domain.usecase

import com.example.todolist.domain.model.TodoTask
import com.example.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow

//监听单条任务详情的 UseCase

class ObserveTodoDetailUseCase(private val repository: TodoRepository) {
    operator fun invoke(id: Long): Flow<TodoTask?> {
        return repository.observeTodoById(id)
    }
}
