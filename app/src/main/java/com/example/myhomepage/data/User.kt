package com.example.myhomepage.data

import androidx.annotation.DrawableRes
import com.example.myhomepage.R
import com.example.myhomepage.ui.theme.TodoType

class User(
    val id: String,
    val name: String,
    @DrawableRes val avatar: Int
) {
    companion object {
        val Me: User = User("Me_test", "黄俊霖", R.drawable.avatar_me)
    }
}

fun User.toBacklog() : Backlog{
    return Backlog(
        id = this.id,
        title = this.name,
        text = "未读消息",
        time = "2025-12-01",
        type = TodoType.MSG,
        avatar = this.avatar
    )
}
