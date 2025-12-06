package com.example.myhomepage.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,      // 本地自增主键，仅用于Room内部管理
    val msgId: Long,       // 【新增】服务端的消息ID，用于排序
    val conversationId: Long,
    val senderId: Long,
    val content: String,
    val time: String,
    val timestamp: Long
)