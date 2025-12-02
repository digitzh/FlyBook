package com.example.myhomepage.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myhomepage.ui.theme.TodoType

class Chat(
    var friend: User, 
    var msgs: MutableList<Msg>,
    var conversationId: Long? = null,
    var type: Int = 1, // 1=单聊(P2P), 2=群聊(Group)
    var name: String? = null, // 群聊名称
    var avatarUrl: String? = null // 群聊头像
) {
    val isGroupChat: Boolean get() = type == 2
    val displayName: String get() = if (isGroupChat && name != null) name!! else friend.name
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