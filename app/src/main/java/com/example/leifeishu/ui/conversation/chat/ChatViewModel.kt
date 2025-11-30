package com.example.leifeishu.ui.conversation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leifeishu.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Date
import java.util.UUID

class ChatViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState

    fun loadMessages(conversationId: String) {
        repository.getMessages(conversationId).onEach { msgs ->
            _uiState.value = _uiState.value.copy(messages = msgs)
        }.launchIn(viewModelScope)
    }

    fun sendMessage(conversationId: String, content: String) {
        val senderId = "me" // 或者从用户信息获取
        repository.sendMessage(conversationId, content)
        val currentMessages = _uiState.value.messages.toMutableList()
        currentMessages.add(
            com.example.leifeishu.data.model.Message(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                senderId = senderId,
                content = content,
                timestamp = Date(),
                isMine = true
            )
        )
        _uiState.value = _uiState.value.copy(messages = currentMessages)
    }
}
