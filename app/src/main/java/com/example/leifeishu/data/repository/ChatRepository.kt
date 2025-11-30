package com.example.leifeishu.data.repository

import com.example.leifeishu.data.datasource.ChatLocalDataSource
import com.example.leifeishu.data.datasource.ChatRoomDataSource
import com.example.leifeishu.data.model.Conversation
import com.example.leifeishu.data.model.Message
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val dataSource: ChatRoomDataSource) {

    fun getConversations(): Flow<List<Conversation>> = dataSource.getConversations()

    fun getMessages(conversationId: String): Flow<List<Message>> =
        dataSource.getMessages(conversationId)

    suspend fun sendMessage(conversationId: String, content: String) =
        dataSource.sendMessage(conversationId, content)

    // 新增: 会话列表的消息同步和未读计数功能
    suspend fun markConversationRead(conversationId: String) = dataSource.markConversationRead(conversationId)

    suspend fun receiveMessage(conversationId: String, content: String, senderId: String) =
        dataSource.receiveMessage(conversationId, content, senderId)
}
// 替换
//class ChatRepository(private val dataSource: ChatLocalDataSource) {
//
//    fun getConversations(): Flow<List<Conversation>> = dataSource.getConversations()
//
//    fun getMessages(conversationId: String): Flow<List<Message>> =
//        dataSource.getMessages(conversationId)
//
//    fun sendMessage(conversationId: String, content: String) =
//        dataSource.sendMessage(conversationId, content)
//}
