package com.example.myhomepage.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val conversationId: Long,
    val senderId: Long,
    val content: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis() // 用于排序
)