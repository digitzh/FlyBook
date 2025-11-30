package com.example.todolist.domain.repository

import com.example.todolist.domain.model.TodoTask
import kotlinx.coroutines.flow.Flow

/**
 * Domain 层的仓库接口，只关心“需要什么数据”和“要做什么操作”
 * 不关心数据从哪里来（本地数据库/网络/内存）
 *
 * UI 和 UseCase 只依赖这个接口，满足依赖倒置原则
 */
interface TodoRepository {

    /**
     * 持续监听待办列表
     * Flow 能让 UI 在数据变化时自动刷新（响应式）
     */
    fun observeTodos(): Flow<List<TodoTask>>

    /**
     * 持续监听某一条待办详情（id 不存在时返回 null）。
     */
    fun observeTodoById(id: Long): Flow<TodoTask?>

    /**
     * 新增或更新一条待办。
     * 约定：如果 id 已存在则更新，否则新增
     */
    suspend fun upsertTodo(todo: TodoTask)

    /**
     * 删除一条待办
     */
    suspend fun deleteTodo(id: Long)

    /**
     * 切换完成状态（如果完成则改为未完成，反之亦然）
     */
    suspend fun toggleCompleted(id: Long)
}
