package com.example.todolist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.todolist.presentation.detail.TodoDetailViewModel
import com.example.todolist.presentation.list.TodoListViewModel

/**
 * 列表页 ViewModel 的 Factory
 * 用来把 UseCase 注入进去
 */
class TodoListViewModelFactory(
    private val container: TodoAppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoListViewModel(
                observeTodoListUseCase = container.observeTodoListUseCase,
                toggleTodoCompletedUseCase = container.toggleTodoCompletedUseCase,
                deleteTodoUseCase = container.deleteTodoUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}

//详情页 ViewModel 的 Factory

class TodoDetailViewModelFactory(
    private val container: TodoAppContainer
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TodoDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TodoDetailViewModel(
                observeTodoDetailUseCase = container.observeTodoDetailUseCase,
                saveTodoUseCase = container.saveTodoUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
