package com.example.todolist.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.todolist.ui.theme.TodoType

/**
 * Room 用的实体类，对应表 todo_tasks
 * 这是 data 层模型，用来持久化；和 domain 层的 TodoTask 通过映射互转
 */
@Entity(tableName = "todo_tasks")
data class TodoTaskEntity(
    @PrimaryKey
    val id: Long,                 // 继续用业务层生成的 id，不用 autoGenerate
    val title: String,
    val description: String,
    val deadline: String?,
    val isCompleted: Boolean,
    val type: TodoType           // 用枚举 + TypeConverter 存到数据库
)
