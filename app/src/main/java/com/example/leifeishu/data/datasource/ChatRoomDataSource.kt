package com.example.leifeishu.data.datasource

import com.example.leifeishu.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.*

class ChatRoomDataSource(private val dao: ChatDao, private val contactDao: ContactDao) {

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

        val convs = dao.getConversationsList()
        val existing = convs.find { it.id == conversationId }
        if (existing != null) {
            dao.updateConversation(
                existing.copy(
                    lastMessage = content
                )
            )
        }
    }

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

    suspend fun markConversationRead(conversationId: String) {
        dao.markConversationAsRead(conversationId)
    }

    /**
     * 初始化系统欢迎会话，同时生成对应联系人
     */
    suspend fun initWelcomeConversation() {
        withContext(Dispatchers.IO) {
            val welcomeId = "welcome"

            // 会话不存在就创建
            val existingConvs = dao.getConversationsList()
            if (existingConvs.none { it.id == welcomeId }) {
                val welcomeConv = ConversationEntity(
                    id = welcomeId,
                    name = "类飞书团队",
                    lastMessage = "欢迎回到类飞书！",
                    unreadCount = 1
                )
                dao.insertConversation(welcomeConv)

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

            // 联系人表里也要有对应的联系人
            val existingContact = contactDao.getContactById(welcomeId)
            if (existingContact == null) {
                val contact = Contact(
                    id = welcomeId,
                    name = "类飞书团队",
                    avatarUrl = null,
                    lastChatId = welcomeId
                )
                contactDao.insert(contact)
            }
        }
    }
}
