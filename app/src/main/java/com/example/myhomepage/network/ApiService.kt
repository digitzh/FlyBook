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
import okhttp3.Response

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

class ApiService(private val baseUrl: String = "http://10.0.2.2:8081") {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * 创建会话
     * @param userId 当前用户ID
     * @param type 会话类型：1=单聊, 2=群聊
     * @param name 会话名称
     * @return conversationId，失败返回null
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
     * @param userId 当前用户ID
     * @param conversationId 会话ID
     * @param targetUserIds 要添加的用户ID列表
     * @return 是否成功
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
     * @param userId 当前用户ID
     * @return 会话列表，失败返回空列表
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
}


