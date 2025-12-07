package com.example.myhomepage.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

@Serializable
data class CreateConversationRequest(val type: Int, val name: String)
@Serializable
data class CreateConversationResponse(val code: Int, val msg: String, val data: Long?)
@Serializable
data class AddMembersRequest(val conversationId: Long, val targetUserIds: List<Long>)

// 【修改】给 ApiResponse 增加默认值，防止 MissingFieldException
@Serializable
data class ApiResponse<T>(
    val code: Int = -1,
    val msg: String = "",
    val data: T? = null
)

@Serializable
data class ConversationVO(
    val conversationId: Long, val type: Int, val name: String? = null, val avatarUrl: String? = null,
    val lastMsgContent: String? = null, val lastMsgTime: String? = null, val unreadCount: Int = 0
)
@Serializable
data class SendMessageRequest(val conversationId: Long, val text: String)
@Serializable
data class SendMessageData(
    val messageId: Long, val conversationId: Long, val senderId: Long, val seq: Long,
    val msgType: Int, val content: String, val createdTime: String
)
@Serializable
data class MessageVO(
    val messageId: Long, val conversationId: Long, val senderId: Long, val content: String, val createdTime: String, val seq: Long
)
@Serializable
data class UserVO(val userId: Long, val username: String, val avatarUrl: String? = null)


class ApiService(private val baseUrl: String = "http://10.0.2.2:8081") {
    private val client = OkHttpClient()
    // 配置 JSON 解析器，允许忽略未知键，宽容模式
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun createConversation(userId: String, type: Int, name: String): Long? = withContext(Dispatchers.IO) {
        try {
            val requestBody = json.encodeToString(CreateConversationRequest(type, name)).toRequestBody(jsonMediaType)
            val request = Request.Builder().url("$baseUrl/api/conversations/create").post(requestBody).addHeader("X-User-Id", userId).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null
            if (response.isSuccessful) {
                val apiResponse = json.decodeFromString<CreateConversationResponse>(responseBody)
                if (apiResponse.code == 0) apiResponse.data else null
            } else null
        } catch (e: Exception) { null }
    }

    suspend fun addMembersToConversation(userId: String, conversationId: Long, targetUserIds: List<Long>): Boolean = withContext(Dispatchers.IO) {
        try {
            val requestBody = json.encodeToString(AddMembersRequest(conversationId, targetUserIds)).toRequestBody(jsonMediaType)
            val request = Request.Builder().url("$baseUrl/api/conversations/members/add").post(requestBody).addHeader("X-User-Id", userId).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext false

            if (response.isSuccessful) {
                // 【关键修改】将 <Any?> 改为 <JsonElement?>
                // JsonElement 是 kotlinx.serialization 内置支持的通用类型
                val apiResponse = json.decodeFromString<ApiResponse<JsonElement?>>(responseBody)
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

    suspend fun getConversationList(userId: String): List<ConversationVO> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("$baseUrl/api/conversations/list").get().addHeader("X-User-Id", userId).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext emptyList()
            if (response.isSuccessful) {
                val apiResponse = json.decodeFromString<ApiResponse<List<ConversationVO>>>(responseBody)
                if (apiResponse.code == 0) apiResponse.data ?: emptyList() else emptyList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun sendMessage(userId: String, conversationId: Long, text: String): SendMessageData? = withContext(Dispatchers.IO) {
        try {
            val requestBody = json.encodeToString(SendMessageRequest(conversationId, text)).toRequestBody(jsonMediaType)
            val request = Request.Builder().url("$baseUrl/api/messages/send").post(requestBody).addHeader("X-User-Id", userId).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext null
            if (response.isSuccessful) {
                val apiResponse = json.decodeFromString<ApiResponse<SendMessageData>>(responseBody)
                if (apiResponse.code == 0) apiResponse.data else null
            } else null
        } catch (e: Exception) { null }
    }

    suspend fun getMessageHistory(userId: String, conversationId: Long): List<MessageVO> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("$baseUrl/api/messages/sync?conversationId=$conversationId").get().addHeader("X-User-Id", userId).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext emptyList()
            if (response.isSuccessful) {
                val apiResponse = json.decodeFromString<ApiResponse<List<MessageVO>>>(responseBody)
                if (apiResponse.code == 0) apiResponse.data ?: emptyList() else emptyList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }

    suspend fun getUserList(): List<UserVO> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url("$baseUrl/api/users/list").get().build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext emptyList()
            if (response.isSuccessful) {
                val apiResponse = json.decodeFromString<ApiResponse<List<UserVO>>>(responseBody)
                if (apiResponse.code == 0) apiResponse.data ?: emptyList() else emptyList()
            } else emptyList()
        } catch (e: Exception) { emptyList() }
    }
}
