package com.example.myhomepage.todolist.data.local

import androidx.room.TypeConverter
import com.example.myhomepage.ui.theme.TodoType

// 把 TodoType 枚举在数据库里存成 String，再从 String 还原回来

class TodoTypeConverters {

    @TypeConverter
    fun fromType(type: TodoType): String = type.name

    @TypeConverter
    fun toType(value: String): TodoType = TodoType.valueOf(value)
}
