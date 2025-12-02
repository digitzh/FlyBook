package com.example.flybook.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flybook.database.ChatDao
import com.example.flybook.database.MessageEntity
import com.example.flybook.model.Message
import kotlinx.coroutines.launch

class ChatViewModel(private val
                    chatDao: ChatDao
) : ViewModel() {

    private val _messages = mutableStateMapOf<Long
            , SnapshotStateList<Message>>()
    val messages: Map<Long, SnapshotStateList<Message>> get
        () = _messages

    fun getMessages(conversationId: Long
    ): List<Message> {
        return _messages.getOrPut(conversationId) { SnapshotStateList() }
    }

    fun sendMessage(conversationId: Long, message: Message
    ) {
        viewModelScope.launch {
            // 保存到数据库
            chatDao.insertMessage(
                MessageEntity(
                    id = message.id,
                    conversationId = conversationId,
                    senderId = message.senderId,
                    content = message.content,
                    timestamp = message.timestamp
                )
            )
            // 更新 UI
            val
                    list = _messages.getOrPut(conversationId) { SnapshotStateList() }
            list.add(message)
        }
    }

    // 初始化时加载历史消息
    fun loadMessages(conversationId: Long
    ) {
        viewModelScope.launch {
            chatDao.getConversation(conversationId).collect { conv ->
                val
                        list = _messages.getOrPut(conversationId) { SnapshotStateList() }
                list.clear()
                conv?.messages?.sortedBy { it.timestamp }?.forEach {
                    list.add(
                        Message(
                            id = it.id,
                            senderId = it.senderId,
                            content = it.content,
                            timestamp = it.timestamp
                        )
                    )
                }
            }
        }
    }
}
