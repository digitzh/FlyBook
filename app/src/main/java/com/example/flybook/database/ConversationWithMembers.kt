package com.example.flybook.database

import androidx.room.Embedded
import androidx.room.Relation

data class ConversationWithMembers(
    @Embedded val conversation: ConversationEntity,

    @Relation(
        entity = ConversationMemberEntity::class,
        parentColumn = "id",
        entityColumn = "conversationId"
    )
    val members: List<ConversationMemberEntity>
)