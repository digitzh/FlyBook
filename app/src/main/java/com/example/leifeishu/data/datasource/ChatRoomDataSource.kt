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

//    suspend fun sendMessage(conversationId: String, content: String) {
//        val message = MessageEntity(
//            id = UUID.randomUUID().toString(),
//            conversationId = conversationId,
//            senderId = "me",
//            content = content,
//            timestamp = Date().time,
//            isMine = true
//        )
//        dao.insertMessage(message)
//
//        // 更新会话最后消息
//        val convList = dao.getConversations() // 这里可以用 suspend 读取
//        // Room Flow 默认是异步，这里可以另外提供一个 suspend 方法读取当前会话
//    }
    suspend fun sendMessage(conversationId: String, content: String) {
        // 插入消息
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
        val convs = dao.getConversationsList()
        val existing = convs.find { it.id == conversationId }
        if (existing != null) {
            dao.updateConversation(
                existing.copy(
                    lastMessage = content,
                    unreadCount = existing.unreadCount // 发送方未读数不变
                )
            )
        }
    }

    /**
     * 收到新消息（非自己发送）时调用
     */
    suspend fun receiveMessage(conversationId: String, content: String, senderId: String) {
        val message = MessageEntity(
            id = UUID.randomUUID().toString(),
            conversationId = conversationId,
            senderId = senderId,
            content = content,
            timestamp = Date().time,
            isMine = false
        )
        dao.insertMessage(message)
        dao.updateConversationOnNewMessage(conversationId, content)
    }

    /**
     * 打开聊天页面，标记会话已读
     */
    suspend fun markConversationRead(conversationId: String) {
        dao.markConversationAsRead(conversationId)
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
                    name = "类飞书团队",
                    lastMessage = "欢迎回到类飞书！",
                    unreadCount = 1
                )
                dao.insertConversation(welcomeConv)

                // 插入欢迎消息
                val welcomeMsg = MessageEntity(
                    id = UUID.randomUUID().toString(),
                    conversationId = welcomeId,
                    senderId = "类飞书团队",
                    content = "欢迎回到类飞书！",
                    timestamp = Date().time,
                    isMine = false
                )
                dao.insertMessage(welcomeMsg)
            }
        }
    }


}