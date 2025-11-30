package com.example.leifeishu.data.datasource

import androidx.room.*
import com.example.leifeishu.data.model.ConversationEntity
import com.example.leifeishu.data.model.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // 已有方法
    @Query("SELECT * FROM conversations ORDER BY id DESC")
    fun getConversations(): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessages(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    // =================== 新增 ===================
    // 获取所有会话列表（suspend 版本）
    @Query("SELECT * FROM conversations")
    suspend fun getConversationsList(): List<ConversationEntity>

    // 将指定会话的未读消息数清零
    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :conversationId")
    suspend fun markConversationAsRead(conversationId: String)

    // 更新最后消息和未读数（发送给别人的消息）
    @Query("UPDATE conversations SET lastMessage = :lastMessage, unreadCount = unreadCount + 1 WHERE id = :conversationId")
    suspend fun updateConversationOnNewMessage(conversationId: String, lastMessage: String)
}

//@Dao
//interface ChatDao {
//
//    // 会话
//    @Query("SELECT * FROM conversations ORDER BY id DESC")
//    fun getConversations(): Flow<List<ConversationEntity>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertConversation(conversation: ConversationEntity)
//
//    @Update
//    suspend fun updateConversation(conversation: ConversationEntity)
//
//    // 消息
//    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
//    fun getMessages(conversationId: String): Flow<List<MessageEntity>>
//
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertMessage(message: MessageEntity)
//
//    @Query("SELECT * FROM conversations")
//    suspend fun getConversationsList(): List<ConversationEntity>
//
//
//}
