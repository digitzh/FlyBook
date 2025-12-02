package com.example.flybook.model

data class Conversation(
    val id: Long,
    val name: String,
    val members: List<User>,
    val messages: MutableList<Message> = mutableListOf()
)
