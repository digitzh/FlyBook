package com.example.myhomepage.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MessageDao {
    // 【修改】改为根据 msgId 升序排列
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY msgId ASC")
    suspend fun getMessagesByConversationId(conversationId: Long): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)
}
