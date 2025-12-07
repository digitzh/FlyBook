package com.example.myhomepage.todolist.data.repository

import com.example.myhomepage.todolist.data.local.TodoDao
import com.example.myhomepage.todolist.data.local.toDomain
import com.example.myhomepage.todolist.data.local.toEntity
import com.example.myhomepage.todolist.domain.TodoTask
import com.example.myhomepage.todolist.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 用 Room 做本地存储的 Repository 实现
 */
class RoomTodoRepository(
    private val dao: TodoDao
) : TodoRepository {

    override fun observeTodos(): Flow<List<TodoTask>> =
        dao.observeTodos().map { list ->
            list.map { it.toDomain() }
        }

    override fun observeTodoById(id: Long): Flow<TodoTask?> =
        dao.observeTodoById(id).map { entity ->
            entity?.toDomain()
        }

    override suspend fun upsertTodo(todo: TodoTask) {
        dao.upsert(todo.toEntity())
    }

    override suspend fun deleteTodo(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun toggleCompleted(id: Long) {
        // 这里简单做法：先查到当前值，再反转后 upsert 回去
        val entity = dao.getTodoByIdOnce(id) ?: return
        val updated = entity.copy(isCompleted = !entity.isCompleted)
        dao.upsert(updated)
    }
}
