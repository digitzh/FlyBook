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

@Serializable data class CreateConversationRequest(val type: Int, val name: String, val targetUserIds: List<Long>? = null)
@Serializable data class CreateConversationResponse(val code: Int = -1, val msg: String = "", val data: Long? = null)
@Serializable data class AddMembersRequest(val conversationId: Long, val targetUserIds: List<Long>)
@Serializable data class ApiResponse<T>(val code: Int = -1, val msg: String = "", val data: T? = null)
@Serializable data class ConversationVO(val conversationId: Long, val type: Int, val name: String? = null, val avatarUrl: String? = null, val lastMsgContent: String? = null, val lastMsgTime: String? = null, val unreadCount: Int = 0)
@Serializable data class SendMessageRequest(val conversationId: Long, val text: String, val msgType: Int = 1)
@Serializable data class SendMessageData(val messageId: Long, val conversationId: Long, val senderId: Long, val seq: Long, val msgType: Int, val content: String, val createdTime: String)
@Serializable data class MessageVO(val messageId: Long, val conversationId: Long, val senderId: Long, val content: String, val createdTime: String, val seq: Long, val msgType: Int = 1)
@Serializable data class UserVO(val userId: Long, val username: String, val avatarUrl: String? = null)

// 【新增】图片和视频的内容结构
@Serializable data class ImageContent(val base64: String)
@Serializable data class VideoContent(val link: String)

class ApiService(private val baseUrl: String = "http://10.0.2.2:8081") {
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; encodeDefaults = true }
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun createConversation(userId: String, type: Int, name: String, targetUserIds: List<Long>? = null): Long? = withContext(Dispatchers.IO) {
        try {
            val requestBody = json.encodeToString(CreateConversationRequest(type, name, targetUserIds)).toRequestBody(jsonMediaType)
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
                val apiResponse = json.decodeFromString<ApiResponse<JsonElement?>>(responseBody)
                apiResponse.code == 0
            } else false
        } catch (e: Exception) { false }
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

    suspend fun sendMessage(userId: String, conversationId: Long, text: String, msgType: Int = 1): SendMessageData? = withContext(Dispatchers.IO) {
        try {
            val requestBody = json.encodeToString(SendMessageRequest(conversationId, text, msgType)).toRequestBody(jsonMediaType)
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

    suspend fun clearUnreadCount(userId: String, conversationId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$baseUrl/api/conversations/unread/clear?conversationId=$conversationId")
                .post(okhttp3.RequestBody.create(jsonMediaType, ""))
                .addHeader("X-User-Id", userId)
                .build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: return@withContext false
            if (response.isSuccessful) {
                val apiResponse = json.decodeFromString<ApiResponse<JsonElement?>>(responseBody)
                apiResponse.code == 0
            } else false
        } catch (e: Exception) { false }
    }
}
