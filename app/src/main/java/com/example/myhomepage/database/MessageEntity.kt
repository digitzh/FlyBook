package com.example.myhomepage.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val msgId: Long,
    val conversationId: Long,
    val senderId: Long,

    // 【新增】数据所有者ID (当前登录用户的ID)
    val ownerId: Long,

    val content: String,
    val time: String,
    val timestamp: Long,
    val msgType: Int = 1
)
