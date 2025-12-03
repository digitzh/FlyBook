package com.example.myhomepage.todolist.domain.usecase

import com.example.myhomepage.todolist.domain.repository.TodoRepository

//删除任务的 UseCase

class DeleteTodoUseCase(private val repository: TodoRepository) {
    suspend operator fun invoke(id: Long) {
        repository.deleteTodo(id)
    }
}
