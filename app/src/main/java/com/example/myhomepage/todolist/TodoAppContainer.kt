package com.example.myhomepage.todolist

import android.content.Context
import androidx.lifecycle.ViewModelProvider
//import androidx.room.Room
import com.example.myhomepage.todolist.data.repository.InMemoryTodoRepository
//import com.example.todolist.data.local.TodoDatabase
//import com.example.todolist.data.repository.RoomTodoRepository
import com.example.myhomepage.todolist.domain.usecase.DeleteTodoUseCase
import com.example.myhomepage.todolist.domain.usecase.ObserveTodoDetailUseCase
import com.example.myhomepage.todolist.domain.usecase.ObserveTodoListUseCase
import com.example.myhomepage.todolist.domain.usecase.SaveTodoUseCase
import com.example.myhomepage.todolist.domain.usecase.ToggleTodoCompletedUseCase

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

    // 暴露给外面用的 Factory 属性
    val todoListViewModelFactory: ViewModelProvider.Factory
        get() = TodoListViewModelFactory(this)

    val todoDetailViewModelFactory: ViewModelProvider.Factory
        get() = TodoDetailViewModelFactory(this)
}

///**
// * 简单的依赖注入容器：现在用 Room 做存储
// */
//class TodoAppContainer(context: Context) {
//
//    // 1. 构建 RoomDatabase
//    private val database: TodoDatabase =
//        Room.databaseBuilder(
//            context.applicationContext,
//            TodoDatabase::class.java,
//            "todo_db"       // 数据库文件名
//        ).build()
//
//    // 2. 用 Dao 创建 Repository
//    private val repository = RoomTodoRepository(database.todoDao())
//
//    // 3. 和之前一样暴露 UseCase
//    val observeTodoListUseCase = ObserveTodoListUseCase(repository)
//    val toggleTodoCompletedUseCase = ToggleTodoCompletedUseCase(repository)
//    val deleteTodoUseCase = DeleteTodoUseCase(repository)
//
//    val observeTodoDetailUseCase = ObserveTodoDetailUseCase(repository)
//    val saveTodoUseCase = SaveTodoUseCase(repository)
//}