package com.example.leifeishu.data.datasource

import com.example.leifeishu.data.model.Conversation
import com.example.leifeishu.data.model.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*

class ChatLocalDataSource {

    private val conversations = MutableStateFlow<List<Conversation>>(emptyList())
    private val messages = mutableMapOf<String, MutableStateFlow<List<Message>>>()

    init {
        // 模拟初始化数据
        val initialConversations = listOf(
            Conversation("1", "Alice", "Hi there!", 1),
            Conversation("2", "Bob", "Good morning", 0)
        )
        conversations.value = initialConversations
        initialConversations.forEach {
            messages[it.id] = MutableStateFlow(
                listOf(
                    Message(UUID.randomUUID().toString(), it.id, "Alice", "Hi there!", Date(), false)
                )
            )
        }
    }

    fun getConversations(): Flow<List<Conversation>> = conversations

    fun getMessages(conversationId: String): Flow<List<Message>> {
        return messages[conversationId] ?: MutableStateFlow(emptyList())
    }

    fun sendMessage(conversationId: String, content: String) {
        val msg = Message(
            UUID.randomUUID().toString(),
            conversationId,
            "me",
            content,
            Date(),
            true
        )
        val convMessages = messages[conversationId] ?: MutableStateFlow(emptyList())
        convMessages.value = convMessages.value + msg
        messages[conversationId] = convMessages

        // 更新会话最后消息
        val updated = conversations.value.map {
            if (it.id == conversationId) it.copy(lastMessage = content) else it
        }
        conversations.value = updated
    }
}
