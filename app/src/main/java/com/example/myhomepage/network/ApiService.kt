package com.example.myhomepage.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class CreateConversationRequest(
    val type: Int, // 1=单聊(P2P), 2=群聊(Group)
    val name: String
)

@Serializable
data class CreateConversationResponse(
    val code: Int,
    val msg: String,
    val data: Long? // conversationId
)

@Serializable
data class AddMembersRequest(
    val conversationId: Long,
    val targetUserIds: List<Long>
)

@Serializable
data class ApiResponse<T>(
    val code: Int,
    val msg: String,
    val data: T?
)

@Serializable
data class ConversationVO(
    val conversationId: Long,
    val type: Int, // 1=单聊(P2P), 2=群聊(Group)
    val name: String? = null,
    val avatarUrl: String? = null,
    val lastMsgContent: String? = null,
    val lastMsgTime: String? = null,
    val unreadCount: Int = 0
)

// 【新增】发送消息请求体
@Serializable
data class SendMessageRequest(
    val conversationId: Long,
    val text: String
)

// 【新增】发送消息响应数据
@Serializable
data class SendMessageData(
    val messageId: Long,
    val conversationId: Long,
    val senderId: Long,
    val seq: Long,
    val msgType: Int,
    val content: String,
    val createdTime: String
)

// 【新增】历史消息数据结构 (对应文档7中的返回结构)
@Serializable
data class MessageVO(
    val messageId: Long,
    val conversationId: Long,
    val senderId: Long,
    val content: String, // "{\"text\": \"...\"}"
    val createdTime: String,
    val seq: Long
)

class ApiService(private val baseUrl: String = "http://10.0.2.2:8081") {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * 创建会话
     */
    suspend fun createConversation(
        userId: String,
        type: Int,
        name: String
    ): Long? = withContext(Dispatchers.IO) {
        try {
            val requestBody = json.encodeToString(CreateConversationRequest(type, name))
                .toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url("$baseUrl/api/conversations/create")
                .post(requestBody)
                .addHeader("X-User-Id", userId)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null

            if (response.isSuccessful) {
                val apiResponse = json.decodeFromString<CreateConversationResponse>(responseBody)
                if (apiResponse.code == 0) {
                    apiResponse.data
                } else {
                    android.util.Log.e("ApiService", "Create conversation failed: ${apiResponse.msg}")
                    null
                }
            } else {
                android.util.Log.e("ApiService", "HTTP error: ${response.code}")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Create conversation error", e)
            null
        }
    }

    /**
     * 添加成员到会话
     */
    suspend fun addMembersToConversation(
        userId: String,
        conversationId: Long,
        targetUserIds: List<Long>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val requestBody = json.encodeToString(AddMembersRequest(conversationId, targetUserIds))
                .toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url("$baseUrl/api/conversations/members/add")
                .post(requestBody)
                .addHeader("X-User-Id", userId)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext false

            if (response.isSuccessful) {
                val apiResponse = json.decodeFromString<ApiResponse<Nothing>>(responseBody)
                apiResponse.code == 0
            } else {
                android.util.Log.e("ApiService", "HTTP error: ${response.code}")
                false
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Add members error", e)
            false
        }
    }

    /**
     * 获取会话列表
     */
    suspend fun getConversationList(userId: String): List<ConversationVO> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/conversations/list")
                .get()
                .addHeader("X-User-Id", userId)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext emptyList()

            if (response.isSuccessful) {
                val apiResponse = json.decodeFromString<ApiResponse<List<ConversationVO>>>(responseBody)
                if (apiResponse.code == 0) {
                    apiResponse.data ?: emptyList()
                } else {
                    android.util.Log.e("ApiService", "Get conversation list failed: ${apiResponse.msg}")
                    emptyList()
                }
            } else {
                android.util.Log.e("ApiService", "HTTP error: ${response.code}")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Get conversation list error", e)
            emptyList()
        }
    }

    /**
     * 【新增】发送消息
     */
    suspend fun sendMessage(
        userId: String,
        conversationId: Long,
        text: String
    ): SendMessageData? = withContext(Dispatchers.IO) {
        try {
            val requestBody = json.encodeToString(SendMessageRequest(conversationId, text))
                .toRequestBody(jsonMediaType)

            val request = Request.Builder()
                .url("$baseUrl/api/messages/send")
                .post(requestBody)
                .addHeader("X-User-Id", userId)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null

            if (response.isSuccessful) {
                val apiResponse = json.decodeFromString<ApiResponse<SendMessageData>>(responseBody)
                if (apiResponse.code == 0) {
                    apiResponse.data
                } else {
                    android.util.Log.e("ApiService", "Send message failed: ${apiResponse.msg}")
                    null
                }
            } else {
                android.util.Log.e("ApiService", "HTTP error: ${response.code}")
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Send message error", e)
            null
        }
    }

    /**
     * 【新增】获取历史消息
     * 接口: /api/messages/sync
     * 参数: conversationId
     */
    suspend fun getMessageHistory(userId: String, conversationId: Long): List<MessageVO> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/messages/sync?conversationId=$conversationId")
                .get()
                .addHeader("X-User-Id", userId)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext emptyList()

            if (response.isSuccessful) {
                val apiResponse = json.decodeFromString<ApiResponse<List<MessageVO>>>(responseBody)
                if (apiResponse.code == 0) {
                    apiResponse.data ?: emptyList()
                } else {
                    android.util.Log.e("ApiService", "Get history failed: ${apiResponse.msg}")
                    emptyList()
                }
            } else {
                android.util.Log.e("ApiService", "HTTP error: ${response.code}")
                emptyList()
            }
        } catch (e: Exception) {
            android.util.Log.e("ApiService", "Get history error", e)
            emptyList()
        }
    }
}