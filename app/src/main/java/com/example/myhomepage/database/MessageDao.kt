package com.example.myhomepage.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {
    // 【修改】只查询属于当前登录用户(ownerId)的消息
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId AND ownerId = :myUserId ORDER BY msgId ASC")
    suspend fun getMessagesByConversationId(conversationId: Long, myUserId: Long): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
}
