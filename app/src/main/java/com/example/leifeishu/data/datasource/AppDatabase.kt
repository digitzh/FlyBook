package com.example.leifeishu.data.datasource

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.leifeishu.data.model.ConversationEntity
import com.example.leifeishu.data.model.MessageEntity
import com.example.leifeishu.data.model.Contact

@Database(
    entities = [ConversationEntity::class, MessageEntity::class, Contact::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun contactDao(): ContactDao
}
