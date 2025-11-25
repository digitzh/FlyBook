package com.example.myhomepage.data

import androidx.annotation.DrawableRes
import com.example.myhomepage.R

class User(
  val id: String,
  val name: String,
  @DrawableRes val avatar: Int
) {
  companion object {
    val Me: User = User("Me_test", "黄俊霖", R.drawable.avatar_me)
  }
}