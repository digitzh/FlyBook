package com.example.myhomepage.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: Long,
    val username: String,
    val avatarUrl: String? = null,
    val password: String? = null,
    val createdTime: Long = System.currentTimeMillis()
)


