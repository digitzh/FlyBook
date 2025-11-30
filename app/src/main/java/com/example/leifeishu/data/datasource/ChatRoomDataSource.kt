package com.example.leifeishu.data.datasource

import com.example.leifeishu.data.model.Conversation
import com.example.leifeishu.data.model.Message
import com.example.leifeishu.data.model.ConversationEntity
import com.example.leifeishu.data.model.MessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.*

class ChatRoomDataSource(private val dao: ChatDao) {

    fun getConversations(): Flow<List<Conversation>> {
        return dao.getConversations().map { list ->
            list.map { entity ->
                Conversation(
                    id = entity.id,
                    name = entity.name,
                    lastMessage = entity.lastMessage,
                    unreadCount = entity.unreadCount
                )
            }
        }
    }

    fun getMessages(conversationId: String): Flow<List<Message>> {
        return dao.getMessages(conversationId).map { list ->
            list.map { entity ->
                Message(
                    id = entity.id,
                    conversationId = entity.conversationId,
                    senderId = entity.senderId,
                    content = entity.content,
                    timestamp = Date(entity.timestamp),
                    isMine = entity.isMine
                )
            }
        }
    }

    suspend fun sendMessage(conversationId: String, content: String) {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = "me",
            content = content,
            timestamp = Date().time,
            isMine = true
        )
        dao.insertMessage(message)

        // 更新会话最后消息
        val convList = dao.getConversations() // 这里可以用 suspend 读取
        // Room Flow 默认是异步，这里可以另外提供一个 suspend 方法读取当前会话
    }

    /**
     * 初始化系统欢迎会话
     */
    suspend fun initWelcomeConversation() {
        withContext(Dispatchers.IO) {
            // 检查数据库中是否存在该会话
            val existingConvs = dao.getConversationsList() // 这里需要一个 suspend 查询所有会话的方法
            val welcomeId = "welcome"

            if (existingConvs.none { it.id == welcomeId }) {
                // 插入欢迎会话
                val welcomeConv = ConversationEntity(
                    id = welcomeId,
                    name = "系统消息",
                    lastMessage = "欢迎使用类飞书聊天应用！",
                    unreadCount = 1
                )
                dao.insertConversation(welcomeConv)

                // 插入欢迎消息
                val welcomeMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = welcomeId,
                    senderId = "system",
                    content = "欢迎使用类飞书聊天应用！",
                    timestamp = Date().time,
                    isMine = false
                )
                dao.insertMessage(welcomeMsg)
            }
        }
    }
}