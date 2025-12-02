package com.example.todolist

import android.content.Context
import androidx.room.Room
import com.example.todolist.data.repository.InMemoryTodoRepository
import com.example.todolist.data.local.TodoDatabase
import com.example.todolist.data.repository.RoomTodoRepository
import com.example.todolist.domain.usecase.DeleteTodoUseCase
import com.example.todolist.domain.usecase.ObserveTodoDetailUseCase
import com.example.todolist.domain.usecase.ObserveTodoListUseCase
import com.example.todolist.domain.usecase.SaveTodoUseCase
import com.example.todolist.domain.usecase.ToggleTodoCompletedUseCase

///**
// * 一个简单的“依赖注入容器”（假装是 Dagger/Hilt）
// * 目的：让列表页和详情页共享同一个 Repository
// */
//class TodoAppContainer {
//
//    // 全局共用一个 Repository（内存实现）
//    private val repository = InMemoryTodoRepository()
//
//    // 暴露给 ViewModel 使用的 UseCase
//    val observeTodoListUseCase = ObserveTodoListUseCase(repository)
//    val toggleTodoCompletedUseCase = ToggleTodoCompletedUseCase(repository)
//    val deleteTodoUseCase = DeleteTodoUseCase(repository)
//
//    val observeTodoDetailUseCase = ObserveTodoDetailUseCase(repository)
//    val saveTodoUseCase = SaveTodoUseCase(repository)
//}

/**
 * 简单的依赖注入容器：现在用 Room 做存储
 */
class TodoAppContainer(context: Context) {

    // 1. 构建 RoomDatabase
    private val database: TodoDatabase =
        Room.databaseBuilder(
            context.applicationContext,
            TodoDatabase::class.java,
            "todo_db"       // 数据库文件名
        ).build()

    // 2. 用 Dao 创建 Repository
    private val repository = RoomTodoRepository(database.todoDao())

    // 3. 和之前一样暴露 UseCase
    val observeTodoListUseCase = ObserveTodoListUseCase(repository)
    val toggleTodoCompletedUseCase = ToggleTodoCompletedUseCase(repository)
    val deleteTodoUseCase = DeleteTodoUseCase(repository)

    val observeTodoDetailUseCase = ObserveTodoDetailUseCase(repository)
    val saveTodoUseCase = SaveTodoUseCase(repository)
}