package com.example.leifeishu.data.repository

import com.example.leifeishu.data.datasource.ChatLocalDataSource
import com.example.leifeishu.data.model.Conversation
import com.example.leifeishu.data.model.Message
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val dataSource: ChatLocalDataSource) {

    fun getConversations(): Flow<List<Conversation>> = dataSource.getConversations()

    fun getMessages(conversationId: String): Flow<List<Message>> =
        dataSource.getMessages(conversationId)

    fun sendMessage(conversationId: String, content: String) =
        dataSource.sendMessage(conversationId, content)
}
