package com.example.myhomepage.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 【修改】entities 数组中增加 MessageEntity::class，version 升级为 2
@Database(entities = [UserEntity::class, MessageEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    // 【新增】暴露 messageDao
    abstract fun messageDao(): MessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // 【注意】由于修改了数据库结构，开发阶段建议直接 fallbackToDestructiveMigration()
                // 这样数据库版本不匹配时会自动清空重建，避免崩溃
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flybook_database"
                )
                    .fallbackToDestructiveMigration() // 【新增】开发阶段允许破坏性迁移
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

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
            // ... 原有的初始化用户代码保持不变 ...
            val users = listOf(
                UserEntity(1001, "ZhangSan", "https://api.dicebear.com/7.x/avataaars/svg?seed=Zhang"),
                UserEntity(1002, "LiSi", "https://api.dicebear.com/7.x/avataaars/svg?seed=Li"),
                UserEntity(1003, "WangWu", "https://api.dicebear.com/7.x/avataaars/svg?seed=Wang"),
                UserEntity(1004, "ZhaoLiu", "https://api.dicebear.com/7.x/avataaars/svg?seed=Zhao")
            )
            userDao.insertUsers(users)
        }
    }
}
