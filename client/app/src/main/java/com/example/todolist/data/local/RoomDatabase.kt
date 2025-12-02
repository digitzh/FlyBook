package com.example.todolist.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Room 数据库入口

@Database(
    entities = [TodoTaskEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TodoTypeConverters::class)
abstract class TodoDatabase : RoomDatabase() {

    abstract fun todoDao(): TodoDao
}
