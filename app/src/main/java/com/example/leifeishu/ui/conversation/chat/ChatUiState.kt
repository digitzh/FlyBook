package com.example.leifeishu.ui.conversation.chat

import com.example.leifeishu.data.model.Message

data class ChatUiState(
    val messages: List<Message> = emptyList()
)
