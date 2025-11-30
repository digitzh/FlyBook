package com.example.leifeishu.data.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.leifeishu.data.model.ConversationEntity
import com.example.leifeishu.data.model.MessageEntity

@Database(
    entities = [ConversationEntity::class, MessageEntity::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}