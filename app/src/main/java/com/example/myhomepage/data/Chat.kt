package com.example.myhomepage.data

import androidx.annotation.DrawableRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.myhomepage.R
import com.example.myhomepage.ui.theme.TodoType

class Chat(
    var friend: User,
    var msgs: MutableList<Msg>,
    var conversationId: Long? = null,
    var type: Int = 1, // 1=单聊(P2P), 2=群聊(Group)
    var name: String? = null, // 群聊名称
    var avatarUrl: String? = null // 群聊头像
) {
    // 【新增】用于列表展示的摘要数据，使用 State 确保 UI 实时刷新
    var lastContent: String? by mutableStateOf(null)
    var lastTime: String? by mutableStateOf(null)
    var unreadCount: Int by mutableIntStateOf(0)

    val isGroupChat: Boolean get() = type == 2
    val displayName: String get() = if (isGroupChat && name != null) name!! else friend.name
}

class Msg(val from: User, val text: String, val time: String) {
    var read: Boolean by mutableStateOf(true)
}

fun Chat.toBacklog() : Backlog?{
    // 如果有未读消息，则在待办中显示
    if(this.unreadCount > 0) {
        return this.friend.toBacklog()
    }else{
        return null
    }
}