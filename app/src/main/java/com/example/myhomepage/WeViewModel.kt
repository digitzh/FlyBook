package com.example.myhomepage

import android.app.Application
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
import com.example.myhomepage.network.WebSocketManager
import com.example.myhomepage.share.TodoShareCard
import com.example.myhomepage.ui.theme.TodoType
import com.example.myhomepage.ui.theme.WeComposeTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

  init {
    viewModelScope.launch {
      WebSocketManager.getInstance().receivedMessages.collect { jsonStr ->
        if (!jsonStr.isNullOrBlank()) handleWebSocketMessage(jsonStr)
      }
    }
  }

  /**
   * 同步用户数据从服务端到本地数据库
   */
  suspend fun syncUsersFromServer() {
    try {
      val userList = apiService.getUserList()
      if (userList.isNotEmpty()) {
        val userEntities = userList.map { userVO ->
          UserEntity(
            userId = userVO.userId,
            username = userVO.username,
            avatarUrl = userVO.avatarUrl,
            password = null, // 不保存密码
            createdTime = System.currentTimeMillis()
          )
        }
        userDao.insertUsers(userEntities)
        android.util.Log.d("WeViewModel", "同步了 ${userEntities.size} 个用户到本地数据库")
      }
    } catch (e: Exception) {
      android.util.Log.e("WeViewModel", "同步用户数据失败", e)
    }
  }

  suspend fun setCurrentUserIdAndLoadUser(userId: String) {
    currentUserId = userId
    val userIdLong = userId.toLongOrNull()
    if (userIdLong != null) {
      // 先从本地数据库查询
      currentUser = userDao.getUserById(userIdLong)
      // 如果本地数据库没有，尝试从服务端同步后再查询
      if (currentUser == null) {
        syncUsersFromServer()
        currentUser = userDao.getUserById(userIdLong)
      }
    }
  }

  var chats by mutableStateOf(listOf<Chat>())

  // 【修改】initbacklogList 使用 Long ID
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

  // 发送待办卡片
  fun sendTodoCard(conversationId: Long, card: TodoShareCard, onSuccess: () -> Unit) {
    val userId = currentUserId ?: return
    val cardJson = Json.encodeToString(card)
    viewModelScope.launch {
      // 【修改】传入 msgType=5
      val result = apiService.sendMessage(userId, conversationId, cardJson, msgType = 5)
      if (result != null) {
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val entity = MessageEntity(
          msgId = result.messageId,
          conversationId = conversationId,
          senderId = userId.toLong(),
          content = cardJson,
          time = timeStr,
          timestamp = System.currentTimeMillis(),
          msgType = 5 // 卡片类型
        )
        messageDao.insertMessage(entity)
        val chat = chats.find { it.conversationId == conversationId }
        // 【关键】更新内存时传入 type=5
        chat?.msgs?.add(Msg(User.Me, cardJson, timeStr, type = 5).apply { read = true })
        onSuccess()
      }
    }
  }

  // 同步历史消息
  fun syncChatHistory(conversationId: Long) {
    val userId = currentUserId ?: return
    viewModelScope.launch {
      val remoteMessages = apiService.getMessageHistory(userId, conversationId)
      if (remoteMessages.isNotEmpty()) {
        val parser = Json { ignoreUnknownKeys = true }
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val serverFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

        remoteMessages.forEach { msgVo ->
          // 【关键修复】同样统一先解 WsContent
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

          // 用 realContent 去重
          val existing = messageDao.getMessagesByConversationId(conversationId).find { it.msgId == msgVo.messageId }
          if (existing == null) {
            val entity = MessageEntity(
              msgId = msgVo.messageId,
              conversationId = conversationId,
              senderId = msgVo.senderId,
              content = realContent, // 存入清洗后的内容
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

    suspend fun clearUnreadMessage(conversationId: Long){
        val userId = currentUserId ?: return
        apiService.clearUnread(userId,conversationId)
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

  fun boom(chat: Chat, msg: String) {
    val cid = chat.conversationId
    val uid = currentUserId
    if (cid != null && uid != null) {
      viewModelScope.launch {
        // 【修改】传入 msgType=1
        val result = apiService.sendMessage(uid, cid, msg, msgType = 1)
        if (result != null) {
          val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
          chat.msgs.add(Msg(User.Me, msg, timeStr, type = 1).apply { read = true })
          val entity = MessageEntity(
            msgId = result.messageId,
            conversationId = cid,
            senderId = uid.toLong(),
            content = msg,
            time = timeStr,
            timestamp = System.currentTimeMillis(),
            msgType = 1
          )
          messageDao.insertMessage(entity)
        }
      }
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

      // 【关键修复】无论 type 是多少，服务端都套了一层 {"text": "..."}
      // 所以必须先解开 WsContent，取出 text
      val realContent = try {
        parser.decodeFromString<WsContent>(wsMsg.content).text
      } catch (e: Exception) {
        // 如果解不开（万一服务端改了），就用原串保底
        wsMsg.content
      }

      // 对于卡片(type=5)，realContent 就是 "{\"todoId\":1...}" (JSON字符串)
      // 对于文本(type=1)，realContent 就是 "你好"

      val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(wsMsg.createdTime))

      // 存库
      val entity = MessageEntity(
        msgId = wsMsg.messageId,
        conversationId = wsMsg.conversationId,
        senderId = wsMsg.senderId,
        content = realContent, // 存入清洗后的内容
        time = timeStr,
        timestamp = wsMsg.createdTime,
        msgType = wsMsg.msgType
      )
      viewModelScope.launch { messageDao.insertMessage(entity) }

      if (chat != null) {
        chat.lastContent = if (wsMsg.msgType == 5) "[待办事项]" else realContent
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
