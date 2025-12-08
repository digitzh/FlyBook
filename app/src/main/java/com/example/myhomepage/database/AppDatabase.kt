package com.example.myhomepage.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 【修改】version = 3
@Database(entities = [UserEntity::class, MessageEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flybook_database"
                )
                    .fallbackToDestructiveMigration() // 允许破坏性迁移（会清空旧数据）
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
        // ... DatabaseCallback 和 populateDatabase 保持不变 ...
        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.userDao())
                    }
                }
            }
        }

        suspend fun populateDatabase(userDao: UserDao) {
            val users = listOf(
                UserEntity(1001, "ZhangSan", "https://api.dicebear.com/7.x/avataaars/svg?seed=Zhang"),
                UserEntity(1002, "LiSi", "https://api.dicebear.com/7.x/avataaars/svg?seed=Li"),
                UserEntity(1003, "WangWu", "https://api.dicebear.com/7.x/avataaars/svg?seed=Wang"),
                UserEntity(1004, "ZhaoLiu", "https://api.dicebear.com/7.x/avataaars/svg?seed=Zhao"),
                UserEntity(1005, "TianQi", "https://api.dicebear.com/7.x/avataaars/svg?seed=Tian")
            )
            userDao.insertUsers(users)
        }
    }
}
