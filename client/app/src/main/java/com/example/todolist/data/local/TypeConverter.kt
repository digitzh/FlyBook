package com.example.todolist.data.local

import androidx.room.TypeConverter
import com.example.todolist.ui.theme.TodoType

// 把 TodoType 枚举在数据库里存成 String，再从 String 还原回来

class TodoTypeConverters {

    @TypeConverter
    fun fromType(type: TodoType): String = type.name

    @TypeConverter
    fun toType(value: String): TodoType = TodoType.valueOf(value)
}
