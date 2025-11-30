package com.example.leifeishu.data.model

data class Conversation(
    val id: String,
    val name: String,
    val lastMessage: String,
    val unreadCount: Int
)
