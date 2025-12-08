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
    val content: String,
    val time: String,
    val timestamp: Long,
    val msgType: Int = 1 // 【新增】消息类型：1=文本, 5=待办卡片
)