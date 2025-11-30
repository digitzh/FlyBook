package com.example.leifeishu.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey val id: String,
    val name: String,
    val avatarUrl: String? = null, // 后续可改为本地资源或 URL,用于显示头像
    val lastChatId: String? = null // 最近聊天会话ID,用于关联当前联系人已有会话
)