package com.example.myhomepage

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhomepage.data.Backlog
import com.example.myhomepage.data.Chat
import com.example.myhomepage.data.Msg
import com.example.myhomepage.data.User
import com.example.myhomepage.database.AppDatabase
import com.example.myhomepage.database.MessageEntity
import com.example.myhomepage.database.UserEntity
import com.example.myhomepage.network.ApiService
import com.example.myhomepage.network.ConversationVO
import com.example.myhomepage.network.ImageContent
import com.example.myhomepage.network.VideoContent
import com.example.myhomepage.network.WebSocketManager
import com.example.myhomepage.share.TodoShareCard
import com.example.myhomepage.ui.theme.TodoType
import com.example.myhomepage.ui.theme.WeComposeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Serializable data class WsMessage(val msgType: Int, val messageId: Long, val conversationId: Long, val senderId: Long, val content: String, val createdTime: Long)
@Serializable data class WsContent(val text: String)

class WeViewModel(application: Application) : AndroidViewModel(application) {
  var theme by mutableStateOf(WeComposeTheme.Theme.Light)
  val isLightTheme by derivedStateOf { theme == WeComposeTheme.Theme.Light }
  val isLightThemeFlow = snapshotFlow { isLightTheme }.stateIn(viewModelScope, SharingStarted.Lazily, true)
  var currentUserId by mutableStateOf<String?>(null)
  var currentUser by mutableStateOf<UserEntity?>(null)
    private set
  private val database = AppDatabase.getDatabase(application)
  private val userDao = database.userDao()
  private val messageDao = database.messageDao()
  private val apiService = ApiService()
  val users = userDao.getAllUsers()

  // 【新增】暂存当前要预览的图片数据
  var currentPreviewImageBase64 by mutableStateOf<String?>(null)

  init {
    viewModelScope.launch {
      WebSocketManager.getInstance().receivedMessages.collect { jsonStr ->
        if (!jsonStr.isNullOrBlank()) handleWebSocketMessage(jsonStr)
      }
    }
    viewModelScope.launch {
      syncUsersFromServer()
    }
  }

  suspend fun setCurrentUserIdAndLoadUser(userId: String) {
    currentUserId = userId
    val userIdLong = userId.toLongOrNull()
    if (userIdLong != null) currentUser = userDao.getUserById(userIdLong)
  }

  var chats by mutableStateOf(listOf<Chat>())

  val initbacklogList by mutableStateOf(listOf(
    Backlog(-1L, "周报", "完成周报", "2025-12-03", TodoType.FILE),
    Backlog(-2L, "接口设计文档", "完成接口设计文档", "2025-12-05", TodoType.FILE),
    Backlog(-3L, "数据分析表", "完成数据分析表", "2025-12-08", TodoType.FILE),
    Backlog(-4L, "下午2点开会", "项目讨论", "2025-12-01", TodoType.CONF),
    Backlog(-5L, "下午2点开会", "项目讨论", "2025-12-02", TodoType.CONF),
    Backlog(-6L, "原神启动", "原神启动！！！", "2025-12-01", TodoType.OTHER),
    Backlog(-7L, "原神启动", "原神启动！！！", "2025-12-02", TodoType.OTHER),
  ))

  fun switchTheme() {
    theme = when (theme) {
      WeComposeTheme.Theme.Light -> WeComposeTheme.Theme.Dark;
      WeComposeTheme.Theme.Dark -> WeComposeTheme.Theme.Light
    }
  }

  suspend fun syncUsersFromServer() {
    try {
      val remoteUsers = apiService.getUserList()
      if (remoteUsers.isNotEmpty()) {
        val entities = remoteUsers.map { vo -> UserEntity(userId = vo.userId, username = vo.username, avatarUrl = vo.avatarUrl) }
        userDao.insertUsers(entities)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  fun sendImage(conversationId: Long, uri: Uri) {
    val userId = currentUserId ?: return
    viewModelScope.launch(Dispatchers.IO) {
      try {
        val context = getApplication<Application>().applicationContext
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
        val bytes = outputStream.toByteArray()
        val base64Str = "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)

        val contentJson = Json.encodeToString(ImageContent(base64Str))
        sendMessageInternal(conversationId, userId, contentJson, 2)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  fun sendVideo(conversationId: Long, uri: Uri) {
    val userId = currentUserId ?: return
    val contentJson = Json.encodeToString(VideoContent(uri.toString()))
    viewModelScope.launch {
      sendMessageInternal(conversationId, userId, contentJson, 3)
    }
  }

  fun sendTodoCard(conversationId: Long, card: TodoShareCard, onSuccess: () -> Unit) {
    val userId = currentUserId ?: return
    val cardJson = Json.encodeToString(card)
    viewModelScope.launch {
      sendMessageInternal(conversationId, userId, cardJson, 5)
      onSuccess()
    }
  }

  fun boom(chat: Chat, msg: String) {
    val cid = chat.conversationId
    val uid = currentUserId
    if (cid != null && uid != null) {
      viewModelScope.launch {
        sendMessageInternal(cid, uid, msg, 1)
      }
    }
  }

  private suspend fun sendMessageInternal(conversationId: Long, userId: String, content: String, msgType: Int) {
    val result = apiService.sendMessage(userId, conversationId, content, msgType)
    if (result != null) {
      val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
      val entity = MessageEntity(
        msgId = result.messageId,
        conversationId = conversationId,
        senderId = userId.toLong(),
        content = content,
        time = timeStr,
        timestamp = System.currentTimeMillis(),
        msgType = msgType
      )
      messageDao.insertMessage(entity)

      val chat = chats.find { it.conversationId == conversationId }
      chat?.msgs?.add(Msg(User.Me, content, timeStr, type = msgType).apply { read = true })
    }
  }

  fun syncChatHistory(conversationId: Long) {
    val userId = currentUserId ?: return
    viewModelScope.launch {
      val remoteMessages = apiService.getMessageHistory(userId, conversationId)
      if (remoteMessages.isNotEmpty()) {
        val parser = Json { ignoreUnknownKeys = true }
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val serverFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        remoteMessages.forEach { msgVo ->
          val realContent = try {
            val contentObj = parser.decodeFromString<WsContent>(msgVo.content)
            contentObj.text
          } catch (e: Exception) {
            msgVo.content
          }

          var timestamp = System.currentTimeMillis()
          var displayTime = timeFormat.format(Date())
          try {
            val date = serverFormat.parse(msgVo.createdTime)
            if (date != null) {
              timestamp = date.time
              displayTime = timeFormat.format(date)
            }
          } catch (e: Exception) { }

          val existing = messageDao.getMessagesByConversationId(conversationId).find { it.msgId == msgVo.messageId }
          if (existing == null) {
            val entity = MessageEntity(
              msgId = msgVo.messageId,
              conversationId = conversationId,
              senderId = msgVo.senderId,
              content = realContent,
              time = displayTime,
              timestamp = timestamp,
              msgType = msgVo.msgType
            )
            messageDao.insertMessage(entity)
          }
        }
      }
      reloadMessagesFromDb(conversationId)
    }
  }

  fun clearUnreadCount(conversationId: Long) {
    val userId = currentUserId ?: return
    viewModelScope.launch {
      val success = apiService.clearUnreadCount(userId, conversationId)
      if (success) {
        // 更新本地未读数为0
        val chat = chats.find { it.conversationId == conversationId }
        chat?.unreadCount = 0
      }
    }
  }

  private suspend fun reloadMessagesFromDb(conversationId: Long) {
    val chat = chats.find { it.conversationId == conversationId } ?: return
    val dbMessages = messageDao.getMessagesByConversationId(conversationId)
    chat.msgs.clear()
    dbMessages.forEach { entity ->
      val senderUser = if (entity.senderId.toString() == currentUserId) User.Me else {
        User(entity.senderId.toString(), getNameForUser(entity.senderId), getAvatarForUser(entity.senderId))
      }
      chat.msgs.add(Msg(senderUser, entity.content, entity.time, type = entity.msgType).apply { read = true })
    }
  }

  fun refreshConversationList() {
    val userId = currentUserId ?: return
    viewModelScope.launch {
      try {
        val conversationList = apiService.getConversationList(userId)
        val newChats = conversationList.map { vo ->
          val existingChat = chats.find { it.conversationId == vo.conversationId }
          if (existingChat != null) {
            existingChat.name = vo.name
            existingChat.avatarUrl = vo.avatarUrl
            existingChat.lastContent = vo.lastMsgContent
            existingChat.lastTime = vo.lastMsgTime
            existingChat.unreadCount = vo.unreadCount
            if (existingChat.msgs.isEmpty() && !vo.lastMsgContent.isNullOrBlank()) {
              existingChat.msgs.add(Msg(existingChat.friend, vo.lastMsgContent, vo.lastMsgTime?:"", type = 1).apply { read = vo.unreadCount == 0 })
            }
            existingChat
          } else {
            convertConversationVOToChat(vo)
          }
        }
        chats = newChats
      } catch (e: Exception) { e.printStackTrace() }
    }
  }

  private suspend fun convertConversationVOToChat(vo: ConversationVO): Chat {
    val friend = if (vo.type == 2) User("group_${vo.conversationId}", vo.name ?: "群聊", R.drawable.avatar_me) else {
      val friendIdLong = if (vo.name == "ZhangSan") 1001L else if (vo.name == "LiSi") 1002L else 0L
      User("user_${vo.conversationId}", vo.name ?: "用户", getAvatarForUser(friendIdLong))
    }
    val dbMessages = messageDao.getMessagesByConversationId(vo.conversationId)
    val msgs = mutableStateListOf<Msg>()
    dbMessages.forEach { entity ->
      val senderUser = if (entity.senderId.toString() == currentUserId) User.Me else {
        User(entity.senderId.toString(), getNameForUser(entity.senderId), getAvatarForUser(entity.senderId))
      }
      msgs.add(Msg(senderUser, entity.content, entity.time, type = entity.msgType).apply { read = true })
    }
    if (msgs.isEmpty() && !vo.lastMsgContent.isNullOrBlank()) {
      msgs.add(Msg(friend, vo.lastMsgContent, vo.lastMsgTime ?: "", type = 1).apply { read = vo.unreadCount == 0 })
    }
    return Chat(friend, msgs, vo.conversationId, vo.type, vo.name, vo.avatarUrl).apply {
      lastContent = vo.lastMsgContent
      lastTime = vo.lastMsgTime
      unreadCount = vo.unreadCount
    }
  }

  fun createGroupConversation(name: String, selectedUserIds: List<Long>, type: Int, onSuccess: () -> Unit, onError: (String) -> Unit) {
    val userId = currentUserId
    if (userId == null) { onError("用户未登录"); return }
    viewModelScope.launch {
      try {
        // 过滤掉当前用户，服务端期望targetUserIds不包括创建者
        // 服务端会自己添加ownerId来构建完整列表，用于检查是否已存在相同会话
        val targetUserIds = selectedUserIds.filter { it.toString() != userId }
        
        // 创建会话时传递成员列表（不包括创建者），服务端会检查是否存在相同群名和成员的会话
        // 如果存在，服务端会返回已有会话ID；如果不存在，服务端会创建新会话并添加成员
        val cid = apiService.createConversation(userId, type, name, targetUserIds)
        if (cid != null) {
          // 刷新会话列表，确保显示最新的会话信息
          // refreshConversationList()会正确处理已存在的会话（更新而不是重复创建）
          refreshConversationList()
          onSuccess()
        } else onError("创建失败")
      } catch (e: Exception) { onError(e.message ?: "") }
    }
  }

  private fun handleWebSocketMessage(jsonStr: String) {
    try {
      val parser = Json { ignoreUnknownKeys = true }
      val wsMsg = parser.decodeFromString<WsMessage>(jsonStr)
      val chat = chats.find { it.conversationId == wsMsg.conversationId }

      val realContent = try {
        parser.decodeFromString<WsContent>(wsMsg.content).text
      } catch (e: Exception) {
        wsMsg.content
      }

      val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(wsMsg.createdTime))

      val entity = MessageEntity(
        msgId = wsMsg.messageId,
        conversationId = wsMsg.conversationId,
        senderId = wsMsg.senderId,
        content = realContent,
        time = timeStr,
        timestamp = wsMsg.createdTime,
        msgType = wsMsg.msgType
      )
      viewModelScope.launch { messageDao.insertMessage(entity) }

      if (chat != null) {
        chat.lastContent = when(wsMsg.msgType) {
          2 -> "[图片]"
          3 -> "[视频]"
          5 -> "[待办事项]"
          else -> realContent
        }
        chat.lastTime = timeStr
        if (wsMsg.senderId.toString() != currentUserId) chat.unreadCount++

        val isDuplicate = wsMsg.senderId.toString() == currentUserId && chat.msgs.lastOrNull()?.text == realContent
        if (!isDuplicate) {
          val senderUser = if (wsMsg.senderId.toString() == currentUserId) User.Me else {
            User(wsMsg.senderId.toString(), getNameForUser(wsMsg.senderId), getAvatarForUser(wsMsg.senderId))
          }
          chat.msgs.add(Msg(senderUser, realContent, timeStr, type = wsMsg.msgType).apply { read = true })
        }
      } else {
        refreshConversationList()
      }
    } catch (e: Exception) {
      android.util.Log.e("WeViewModel", "WS Error", e)
    }
  }

  private fun getAvatarForUser(userId: Long) = when (userId) { 1001L -> R.drawable.avatar_zhangsan; 1002L -> R.drawable.avatar_lisi; else -> R.drawable.avatar_me }
  private fun getNameForUser(userId: Long) = when (userId) { 1001L -> "ZhangSan"; 1002L -> "LiSi"; else -> "User $userId" }
}
