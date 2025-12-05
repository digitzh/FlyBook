package com.example.myhomepage.data

import androidx.annotation.DrawableRes
import com.example.myhomepage.R
import com.example.myhomepage.ui.theme.TodoType
import kotlin.math.abs

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
    // 基于 userId 生成一个「负数」id，避免和本地自增 id 冲突
    val virtualId = -abs(this.id.hashCode().toLong())  // 必定是 <= 0 的 long
    return Backlog(
        id = virtualId,
        title = this.name, // User的name作为Backlog的title
        text = "未读消息", // 给Backlog的text设默认值（或根据User补充）
        time = "2025-12-01", // 时间默认值（或从User取，若User有time字段）
        type = TodoType.MSG, // 类型默认值（或根据User类型映射）
        avatar = this.avatar // 复用User的avatar
    )
}