package com.example.myhomepage.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myhomepage.ui.theme.TodoType

class Chat(var friend: User, var msgs: MutableList<Msg>) {
}

class Msg(val from: User, val text: String, val time: String) {
  var read: Boolean by mutableStateOf(true)
}

fun Chat.toBacklog() : Backlog?{
    if(this.msgs.lastOrNull() != null && !this.msgs.last().read) {
        return this.friend.toBacklog()
    }else{
        return null
    }
}