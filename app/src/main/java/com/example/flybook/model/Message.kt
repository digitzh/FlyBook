package com.example.flybook.model

data class Message(
    val id: Long,
    val senderId: Long,
    val content: String,
    val timestamp: Long
)