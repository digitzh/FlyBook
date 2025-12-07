package com.example.myhomepage.data

import androidx.annotation.DrawableRes
import com.example.myhomepage.ui.theme.TodoType

// 【修改】id 改为 Long，移除 mutableStateOf (状态由 ViewModel 管理)
class Backlog(
    val id: Long,
    val title: String,
    val text: String,
    val time: String,
    val type: TodoType,
    val complete: Boolean = false,
    @DrawableRes val avatar: Int = 0
) {
}
