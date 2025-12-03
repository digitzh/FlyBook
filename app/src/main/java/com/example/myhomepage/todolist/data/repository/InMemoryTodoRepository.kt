package com.example.myhomepage.todolist.data.repository

import com.example.myhomepage.todolist.data.TodoTask
import com.example.myhomepage.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * 一个最简单的内存版本实现：
 * - 用 MutableStateFlow 存一份 List<TodoTask>
 * - 进程被杀就全没了，但足够 demo 测试使用
 */
class InMemoryTodoRepository : TodoRepository {

    // 内存中当前的任务列表
    private val todoListFlow = MutableStateFlow<List<TodoTask>>(emptyList())

    override fun observeTodos(): Flow<List<TodoTask>> = todoListFlow

    override fun observeTodoById(id: Long): Flow<TodoTask?> {
        // 用 map 在列表中查找指定 id
        return todoListFlow.map { list ->
            list.find { it.id == id }
        }
    }

    override suspend fun upsertTodo(todo: TodoTask) {
        val current = todoListFlow.value
        val existingIndex = current.indexOfFirst { it.id == todo.id }
        val newList = if (existingIndex >= 0) {
            // 更新
            current.toMutableList().apply {
                this[existingIndex] = todo
            }
        } else {
            // 新增：简单地在末尾追加
            current + todo
        }
        todoListFlow.value = newList
    }

    override suspend fun deleteTodo(id: Long) {
        todoListFlow.value = todoListFlow.value.filterNot { it.id == id }
    }

    override suspend fun toggleCompleted(id: Long) {
        val newList = todoListFlow.value.map { task ->
            if (task.id == id) task.copy(isCompleted = !task.isCompleted) else task
        }
        todoListFlow.value = newList
    }
}
