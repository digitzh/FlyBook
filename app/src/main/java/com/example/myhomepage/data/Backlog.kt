package com.example.myhomepage.data

import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myhomepage.ui.theme.TodoType

class Backlog(val id: Long, val title: String,
              val text: String, val time: String, val type: TodoType,
              val complete: Boolean = false,
              @DrawableRes val avatar: Int = 0
) {
//    var complete: Boolean by mutableStateOf(false)
}