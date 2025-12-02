package com.example.flybook.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // ----- 会话 -----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: ConversationEntity)

    @Transaction
    @Query("SELECT * FROM conversations")
    fun getAllConversations(): Flow<List<ConversationWithMembers>>

    @Transaction
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    fun getConversation(conversationId: Long): Flow<ConversationWithMessages?>


    // ----- 消息 -----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    // ----- 成员 -----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<ConversationMemberEntity>)

    @Query("SELECT * FROM conversation_members WHERE conversationId = :conversationId")
    suspend fun getMembersOfConversation(conversationId: Long): List<ConversationMemberEntity>

    @Query("SELECT conversationId FROM conversation_members WHERE userId IN (:userIds) GROUP BY conversationId HAVING COUNT(*) = :size")
    suspend fun findConversationByMembers(userIds: List<Long>, size: Int): List<Long>
}
