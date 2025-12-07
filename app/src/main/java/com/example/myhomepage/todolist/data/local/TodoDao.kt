package com.example.myhomepage.todolist.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

// Room 的 Dao：写 SQL / 增删改查都在这里

@Dao
interface TodoDao {

    /**
     * 监听全部任务列表，按 id 倒序
     */
    @Query("SELECT * FROM todo_tasks ORDER BY id DESC")
    fun observeTodos(): Flow<List<TodoTaskEntity>>

    /**
     * 监听某一条任务
     */
    @Query("SELECT * FROM todo_tasks WHERE id = :id LIMIT 1")
    fun observeTodoById(id: Long): Flow<TodoTaskEntity?>

    /**
     * 一次性获取某条任务（非 Flow），用于 toggle 之类的逻辑
     */
    @Query("SELECT * FROM todo_tasks WHERE id = :id LIMIT 1")
    suspend fun getTodoByIdOnce(id: Long): TodoTaskEntity?

    /**
     * 插入或更新
     * Room 2.5+ 提供的 @Upsert，会自动按主键是否存在决定 insert/update
     */
    @Upsert
    suspend fun upsert(todo: TodoTaskEntity)

    /**
     * 删除
     */
    @Query("DELETE FROM todo_tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
