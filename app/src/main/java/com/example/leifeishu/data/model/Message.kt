package com.example.leifeishu.data.model

import java.util.*

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val timestamp: Date,
    val isMine: Boolean
)
