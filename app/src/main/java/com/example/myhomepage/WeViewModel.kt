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

  // 暂存当前要预览的图片数据
  var currentPreviewImageBase64 by mutableStateOf<String?>(null)

  init {
    viewModelScope.launch {
      WebSocketManager.getInstance().receivedMessages.collect { jsonStr ->
        if (!jsonStr.isNullOrBlank()) handleWebSocketMessage(jsonStr)
      }
    }
    // 启动时尝试同步一次
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

  // 从服务端同步所有用户列表到本地数据库
  suspend fun syncUsersFromServer() {
    try {
      val remoteUsers = apiService.getUserList()
      if (remoteUsers.isNotEmpty()) {
        val entities = remoteUsers.map { vo ->
          UserEntity(userId = vo.userId, username = vo.username, avatarUrl = vo.avatarUrl)
        }
        userDao.insertUsers(entities)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  // 动态解析用户：根据 ID 去本地数据库查找
  private suspend fun resolveUser(userId: Long): User {
    if (userId.toString() == currentUserId) {
      return User.Me
    }
    val entity = userDao.getUserById(userId)
    return if (entity != null) {
      User(entity.userId.toString(), entity.username, R.drawable.avatar_lisi)
    } else {
      User(userId.toString(), "用户 $userId", R.drawable.avatar_me)
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
      } catch (e: Exception) { e.printStackTrace() }
    }
  }

  fun sendVideo(conversationId: Long, uri: Uri) {
    val userId = currentUserId ?: return
    val contentJson = Json.encodeToString(VideoContent(uri.toString()))
    viewModelScope.launch { sendMessageInternal(conversationId, userId, contentJson, 3) }
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
      viewModelScope.launch { sendMessageInternal(cid, uid, msg, 1) }
    }
  }

  private suspend fun sendMessageInternal(conversationId: Long, userId: String, content: String, msgType: Int) {
    val result = apiService.sendMessage(userId, conversationId, content, msgType)
    if (result != null) {
      val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
      val entity = MessageEntity(
        msgId = result.messageId, conversationId = conversationId, senderId = userId.toLong(),
        ownerId = userId.toLong(), // 【修正】写入 ownerId
        content = content, time = timeStr, timestamp = System.currentTimeMillis(), msgType = msgType
      )
      messageDao.insertMessage(entity)
      val chat = chats.find { it.conversationId == conversationId }
      val me = resolveUser(userId.toLong())
      chat?.msgs?.add(Msg(me, content, timeStr, type = msgType).apply { read = true })
    }
  }

  fun syncChatHistory(conversationId: Long) {
    val userId = currentUserId ?: return
    val myUid = userId.toLongOrNull() ?: 0L // 【修正】获取当前用户ID

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
          } catch (e: Exception) { msgVo.content }

          var timestamp = System.currentTimeMillis()
          var displayTime = timeFormat.format(Date())
          try {
            val date = serverFormat.parse(msgVo.createdTime)
            if (date != null) {
              timestamp = date.time
              displayTime = timeFormat.format(date)
            }
          } catch (e: Exception) { }

          // 【修正】去重时加上 ownerId，防止串号
          val existing = messageDao.getMessagesByConversationId(conversationId, myUid).find { it.msgId == msgVo.messageId }
          if (existing == null) {
            val entity = MessageEntity(
              msgId = msgVo.messageId, conversationId = conversationId, senderId = msgVo.senderId,
              ownerId = myUid, // 【修正】写入 ownerId
              content = realContent, time = displayTime, timestamp = timestamp, msgType = msgVo.msgType
            )
            messageDao.insertMessage(entity)
          }
        }
      }
      reloadMessagesFromDb(conversationId)
    }
  }

  private suspend fun reloadMessagesFromDb(conversationId: Long) {
    val chat = chats.find { it.conversationId == conversationId } ?: return
    val myUid = currentUserId?.toLongOrNull() ?: 0L // 【修正】获取当前用户ID

    // 【修正】查询时传入 myUid
    val dbMessages = messageDao.getMessagesByConversationId(conversationId, myUid)
    chat.msgs.clear()

    dbMessages.forEach { entity ->
      val user = resolveUser(entity.senderId)
      chat.msgs.add(Msg(user, entity.content, entity.time, type = entity.msgType).apply { read = true })
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
    val friend = if (vo.type == 2) {
      User("group_${vo.conversationId}", vo.name ?: "群聊", R.drawable.avatar_me)
    } else {
      User("user_${vo.conversationId}", vo.name ?: "用户", R.drawable.avatar_me)
    }

    val myUid = currentUserId?.toLongOrNull() ?: 0L // 【修正】获取当前用户ID
    // 【修正】查询时传入 myUid
    val dbMessages = messageDao.getMessagesByConversationId(vo.conversationId, myUid)

    val msgs = mutableStateListOf<Msg>()
    dbMessages.forEach { entity ->
      val user = resolveUser(entity.senderId)
      msgs.add(Msg(user, entity.content, entity.time, type = entity.msgType).apply { read = true })
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
        val cid = apiService.createConversation(userId, type, name)
        if (cid != null) {
          val others = selectedUserIds.filter { it.toString() != userId }
          if (others.isNotEmpty()) apiService.addMembersToConversation(userId, cid, others)
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
      val myUid = currentUserId?.toLongOrNull() ?: return // 【修正】获取当前用户ID

      val realContent = try {
        parser.decodeFromString<WsContent>(wsMsg.content).text
      } catch (e: Exception) { wsMsg.content }

      val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(wsMsg.createdTime))

      val entity = MessageEntity(
        msgId = wsMsg.messageId, conversationId = wsMsg.conversationId, senderId = wsMsg.senderId,
        ownerId = myUid, // 【修正】写入 ownerId
        content = realContent, time = timeStr, timestamp = wsMsg.createdTime, msgType = wsMsg.msgType
      )
      viewModelScope.launch { messageDao.insertMessage(entity) }

      if (chat != null) {
        chat.lastContent = when(wsMsg.msgType) {
          2 -> "[图片]"; 3 -> "[视频]"; 5 -> "[待办事项]"; else -> realContent
        }
        chat.lastTime = timeStr
        if (wsMsg.senderId.toString() != currentUserId) chat.unreadCount++

        val isDuplicate = wsMsg.senderId.toString() == currentUserId && chat.msgs.lastOrNull()?.text == realContent
        if (!isDuplicate) {
          viewModelScope.launch {
            val senderUser = resolveUser(wsMsg.senderId)
            chat.msgs.add(Msg(senderUser, realContent, timeStr, type = wsMsg.msgType).apply { read = true })
          }
        }
      } else { refreshConversationList() }
    } catch (e: Exception) {
      android.util.Log.e("WeViewModel", "WS Error", e)
    }
  }
}
