package com.example.myhomepage.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages") // 表名必须是 messages
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val msgId: Long,       // 对应 SQL 中的 msgId
    val conversationId: Long, // 对应 SQL 中的 conversationId
    val senderId: Long,
    val content: String,
    val time: String,
    val timestamp: Long
)