package com.example.myhomepage.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Chat(
    var friend: User,
    var msgs: MutableList<Msg>,
    var conversationId: Long? = null,
    var type: Int = 1,
    var name: String? = null,
    var avatarUrl: String? = null
) {
    var lastContent: String? by mutableStateOf(null)
    var lastTime: String? by mutableStateOf(null)
    var unreadCount: Int by mutableIntStateOf(0)

    val isGroupChat: Boolean get() = type == 2
    val displayName: String get() = if (isGroupChat && name != null) name!! else friend.name
}

// 【修改】增加 type 字段，默认 1 (文本)
class Msg(val from: User, val text: String, val time: String, val type: Int = 1) {
    var read: Boolean by mutableStateOf(true)
}

fun Chat.toBacklog() : Backlog?{
    if(this.unreadCount > 0) {
        return this.friend.toBacklog()
    }else{
        return null
    }
}