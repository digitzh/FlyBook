package com.example.leifeishu.ui.conversation.conversationList

import com.example.leifeishu.data.model.Conversation

data class ConversationListUiState(
    val conversations: List<Conversation> = emptyList()
)
