package com.example.leifeishu.ui.conversation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leifeishu.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    fun loadMessages(conversationId: String) {
        repository.getMessages(conversationId).onEach { msgs ->
            _uiState.value = _uiState.value.copy(messages = msgs)
        }.launchIn(viewModelScope)
    }

    fun sendMessage(conversationId: String, content: String) {
        repository.sendMessage(conversationId, content)
    }
}
