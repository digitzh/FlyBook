package com.example.todolist.domain.usecase

import com.example.todolist.domain.model.TodoTask
import com.example.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow

/**
 * 监听待办列表的 UseCase
 * 如果未来逻辑变复杂（如只显示未完成、排序、过滤等），改在这里即可
 */
class ObserveTodoListUseCase(private val repository: TodoRepository) {
    operator fun invoke(): Flow<List<TodoTask>> {
        return repository.observeTodos()
    }
}
