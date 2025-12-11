package com.example.myhomepage.data

import androidx.annotation.DrawableRes
import com.example.myhomepage.R
import com.example.myhomepage.ui.theme.TodoType
import kotlin.math.abs // 引入abs

class User(
    val id: String,
    val name: String,
    @DrawableRes val avatar: Int
) {
    companion object {
        val Me: User = User("Me", "我", R.drawable.avatar_me)
    }
}

fun User.toBacklog() : Backlog{
    // 【修改】生成虚拟 Long ID
    // 使用负数 ID 以避免与数据库的自增 ID 冲突
    val virtualId = -abs(this.id.hashCode().toLong())

    return Backlog(
        id = virtualId, // 传入 Long
        title = this.name,
        text = "未读消息",
        time = "2025-12-01",
        type = TodoType.MSG,
        avatar = this.avatar
    )
}
