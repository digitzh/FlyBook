package com.example.todolist

import com.example.todolist.data.repository.InMemoryTodoRepository
import com.example.todolist.domain.usecase.DeleteTodoUseCase
import com.example.todolist.domain.usecase.ObserveTodoDetailUseCase
import com.example.todolist.domain.usecase.ObserveTodoListUseCase
import com.example.todolist.domain.usecase.SaveTodoUseCase
import com.example.todolist.domain.usecase.ToggleTodoCompletedUseCase

/**
 * 一个简单的“依赖注入容器”（假装是 Dagger/Hilt）
 * 目的：让列表页和详情页共享同一个 Repository
 */
class TodoAppContainer {

    // 全局共用一个 Repository（内存实现）
    private val repository = InMemoryTodoRepository()

    // 暴露给 ViewModel 使用的 UseCase
    val observeTodoListUseCase = ObserveTodoListUseCase(repository)
    val toggleTodoCompletedUseCase = ToggleTodoCompletedUseCase(repository)
    val deleteTodoUseCase = DeleteTodoUseCase(repository)

    val observeTodoDetailUseCase = ObserveTodoDetailUseCase(repository)
    val saveTodoUseCase = SaveTodoUseCase(repository)
}
