package com.example.leifeishu.data.datasource

import androidx.room.*
import com.example.leifeishu.data.model.ConversationEntity
import com.example.leifeishu.data.model.MessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // 会话
    @Query("SELECT * FROM conversations ORDER BY id DESC")
    fun getConversations(): Flow<List<ConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Update
    suspend fun updateConversation(conversation: ConversationEntity)

    // 消息
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessages(conversationId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM conversations")
    suspend fun getConversationsList(): List<ConversationEntity>
}
