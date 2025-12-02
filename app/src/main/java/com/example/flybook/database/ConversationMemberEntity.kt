package com.example.flybook.database

import androidx.room.Entity

@Entity(
    tableName = "conversation_members",
    primaryKeys = ["conversationId", "userId"]
)
data class ConversationMemberEntity(
    val conversationId: Long,
    val userId: Long
)

