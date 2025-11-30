package com.example.leifeishu.ui.conversation.conversationList

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leifeishu.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class ConversationListViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationListUiState())
    val uiState: StateFlow<ConversationListUiState> = _uiState

    init {
        repository.getConversations().onEach { convs ->
            _uiState.value = _uiState.value.copy(conversations = convs)
        }.launchIn(viewModelScope)
    }
}
