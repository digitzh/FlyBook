package com.example.myhomepage.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

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
            // 初始化测试用户数据（参考服务端SQL）
            val users = listOf(
                UserEntity(
                    userId = 1001,
                    username = "ZhangSan",
                    avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Zhang",
                    password = null
                ),
                UserEntity(
                    userId = 1002,
                    username = "LiSi",
                    avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Li",
                    password = null
                ),
                UserEntity(
                    userId = 1003,
                    username = "WangWu",
                    avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=Wang",
                    password = null
                )
            )
            userDao.insertUsers(users)
        }
    }
}


