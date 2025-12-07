package com.example.myhomepage

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.derivedStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhomepage.data.Backlog
import com.example.myhomepage.data.Chat
import com.example.myhomepage.data.Msg
import com.example.myhomepage.data.User
import com.example.myhomepage.database.AppDatabase
import com.example.myhomepage.database.UserEntity
import com.example.myhomepage.database.MessageDao
import com.example.myhomepage.database.MessageEntity
import com.example.myhomepage.network.ApiService
import com.example.myhomepage.network.ConversationVO
import com.example.myhomepage.network.WebSocketManager
import com.example.myhomepage.ui.theme.TodoType
import com.example.myhomepage.ui.theme.WeComposeTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


// 【新增】消息内容结构
@Serializable
data class WsContent(val text: String)

// 【修改】补充 messageId 字段
@Serializable
data class WsMessage(
  val msgType: Int,
  val messageId: Long, // 必须加上这个，接口文档里有
  val conversationId: Long,
  val senderId: Long,
  val content: String,
  val createdTime: Long
)

class WeViewModel(application: Application) : AndroidViewModel(application) {
  var theme by mutableStateOf(WeComposeTheme.Theme.Light)
  val isLightTheme by derivedStateOf { theme == WeComposeTheme.Theme.Light }
  val isLightThemeFlow = snapshotFlow { isLightTheme }
    .stateIn(viewModelScope, SharingStarted.Lazily, true)

  // 当前登录的用户ID
  var currentUserId by mutableStateOf<String?>(null)

  // 当前用户信息
  var currentUser by mutableStateOf<UserEntity?>(null)
    private set

  // 数据库和API服务
  private val database = AppDatabase.getDatabase(application)
  private val userDao = database.userDao()
  // 【新增】获取 messageDao
  private val messageDao = database.messageDao()
  private val apiService = ApiService()

  // 用户列表（从数据库读取）
  val users = userDao.getAllUsers()

    init {
      // 1. WebSocket 监听
      viewModelScope.launch {
        WebSocketManager.getInstance().receivedMessages.collect { jsonStr ->
          if (!jsonStr.isNullOrBlank()) handleWebSocketMessage(jsonStr)
        }
      }
      // 【修改】已移除 loadUsersFromRemote()
    }

  /**
   * 设置当前用户ID并加载用户信息
   */
  suspend fun setCurrentUserIdAndLoadUser(userId: String) {
    currentUserId = userId
    val userIdLong = userId.toLongOrNull()
    if (userIdLong != null) {
      currentUser = userDao.getUserById(userIdLong)
    }
  }

  // 模拟数据 - 联系人
//  val contacts by mutableStateOf(
//    listOf(
//      User("1001", "ZhangSan", R.drawable.avatar_zhangsan),
//      User("1002", "LiSi", R.drawable.avatar_lisi),
////      User("1003", "WangWu", R.drawable.avatar_wangwu),
//    )
//  )

  // 聊天列表
  var chats by mutableStateOf(
    listOf<Chat>()
  )

  // 【修改】ID 改为 Long 类型 (使用负数模拟静态数据)
  val initbacklogList by mutableStateOf(
    listOf(
      Backlog(-1L, "周报", "完成周报", "2025-12-03", TodoType.FILE),
      Backlog(-2L, "接口设计文档", "完成接口设计文档", "2025-12-05", TodoType.FILE),
      Backlog(-3L, "数据分析表", "完成数据分析表", "2025-12-08", TodoType.FILE),
      Backlog(-4L, "下午2点开会", "项目讨论", "2025-12-01", TodoType.CONF),
      Backlog(-5L, "下午2点开会", "项目讨论", "2025-12-02", TodoType.CONF),
      Backlog(-6L, "原神启动", "原神启动！！！", "2025-12-01", TodoType.OTHER),
      Backlog(-7L, "原神启动", "原神启动！！！", "2025-12-02", TodoType.OTHER),
    )
  )


  fun switchTheme() {
    theme = when (theme) {
      WeComposeTheme.Theme.Light -> WeComposeTheme.Theme.Dark;
      WeComposeTheme.Theme.Dark -> WeComposeTheme.Theme.Light
    }
  }

// 【修改】同步历史消息：保存 messageId
fun syncChatHistory(conversationId: Long) {
  val userId = currentUserId ?: return
  viewModelScope.launch {
    val remoteMessages = apiService.getMessageHistory(userId, conversationId)
    if (remoteMessages.isNotEmpty()) {
      val parser = Json { ignoreUnknownKeys = true }
      val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
      val serverFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

      remoteMessages.forEach { msgVo ->
        val contentText = try {
          val contentObj = parser.decodeFromString<WsContent>(msgVo.content)
          contentObj.text
        } catch (e: Exception) { "[不支持]" }

        var timestamp = System.currentTimeMillis()
        var displayTime = timeFormat.format(Date())
        try {
          val date = serverFormat.parse(msgVo.createdTime)
          if (date != null) {
            timestamp = date.time
            displayTime = timeFormat.format(date)
          }
        } catch (e: Exception) { }

        // 【关键】使用 messageId (msgVo.messageId) 来判断是否存在
        val existing = messageDao.getMessagesByConversationId(conversationId).find {
          it.msgId == msgVo.messageId
        }

        if (existing == null) {
          val entity = MessageEntity(
            msgId = msgVo.messageId, // 保存服务端ID
            conversationId = conversationId,
            senderId = msgVo.senderId,
            content = contentText,
            time = displayTime,
            timestamp = timestamp
          )
          messageDao.insertMessage(entity)
        }
      }
    }
    reloadMessagesFromDb(conversationId)
  }
}

  // 【新增】从数据库重载消息
  private suspend fun reloadMessagesFromDb(conversationId: Long) {
    val chat = chats.find { it.conversationId == conversationId } ?: return
    val dbMessages = messageDao.getMessagesByConversationId(conversationId)
    chat.msgs.clear()
    dbMessages.forEach { entity ->
      val senderUser = if (entity.senderId.toString() == currentUserId) User.Me else {
        User(entity.senderId.toString(), getNameForUser(entity.senderId), getAvatarForUser(entity.senderId))
      }
      chat.msgs.add(Msg(senderUser, entity.content, entity.time).apply { read = true })
    }
  }

  // 【修改】刷新列表（更新 lastContent）
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
            // 实时更新摘要
            existingChat.lastContent = vo.lastMsgContent
            existingChat.lastTime = vo.lastMsgTime
            existingChat.unreadCount = vo.unreadCount

            // 如果内存没消息但服务端有摘要，补一条假消息防止空白（可选，实际上有了 lastContent 后 UI 不再依赖这个）
            if (existingChat.msgs.isEmpty() && !vo.lastMsgContent.isNullOrBlank()) {
              existingChat.msgs.add(Msg(existingChat.friend, vo.lastMsgContent, vo.lastMsgTime?:"").apply { read = vo.unreadCount == 0 })
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

  // 【修改】转换 VO，并初始化 lastContent
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
      msgs.add(Msg(senderUser, entity.content, entity.time).apply { read = true })
    }
    if (msgs.isEmpty() && !vo.lastMsgContent.isNullOrBlank()) {
      msgs.add(Msg(friend, vo.lastMsgContent, vo.lastMsgTime ?: "").apply { read = vo.unreadCount == 0 })
    }
    return Chat(friend, msgs, vo.conversationId, vo.type, vo.name, vo.avatarUrl).apply {
      lastContent = vo.lastMsgContent
      lastTime = vo.lastMsgTime
      unreadCount = vo.unreadCount
    }
  }

// 【修改】发送消息：保存服务端返回的 messageId
fun boom(chat: Chat, msg: String) {
  val cid = chat.conversationId
  val uid = currentUserId
  if (cid != null && uid != null) {
    viewModelScope.launch {
      val result = apiService.sendMessage(uid, cid, msg)
      if (result != null) {
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        chat.msgs.add(Msg(User.Me, msg, timeStr).apply { read = true })

        val entity = MessageEntity(
          msgId = result.messageId, // 【关键】使用服务端返回的ID
          conversationId = cid,
          senderId = uid.toLong(),
          content = msg,
          time = timeStr,
          timestamp = System.currentTimeMillis()
        )
        messageDao.insertMessage(entity)
      }
    }
  }
}

  /**
   * 创建会话 (群聊或单聊)
   * 【修改】增加 type 参数
   */
  fun createGroupConversation(
    name: String,
    selectedUserIds: List<Long>,
    type: Int, // 新增参数
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    val userId = currentUserId
    if (userId == null) {
      onError("用户未登录")
      return
    }

    viewModelScope.launch {
      try {
        val userIdLong = userId.toLongOrNull()
        if (userIdLong == null) {
          onError("用户ID格式错误")
          return@launch
        }

        // 【修改】使用传入的 type
        val conversationId = apiService.createConversation(userId, type, name)
        if (conversationId == null) {
          onError("创建会话失败")
          return@launch
        }

        val otherUserIds = selectedUserIds.filter { it != userIdLong }
        if (otherUserIds.isNotEmpty()) {
          val success = apiService.addMembersToConversation(userId, conversationId, otherUserIds)
          if (!success) {
            onError("添加成员失败")
            return@launch
          }
        }

        refreshConversationList()
        onSuccess()
      } catch (e: Exception) {
        onError("创建群聊失败: ${e.message}")
      }
    }
  }

  // 【修改】WebSocket 接收：保存 messageId
  private fun handleWebSocketMessage(jsonStr: String) {
    try {
      val parser = Json { ignoreUnknownKeys = true }
      val wsMsg = parser.decodeFromString<WsMessage>(jsonStr)
      val chat = chats.find { it.conversationId == wsMsg.conversationId }
      if (wsMsg.msgType == 1) {
        val contentObj = parser.decodeFromString<WsContent>(wsMsg.content)
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(wsMsg.createdTime))

        // 存库
        val entity = MessageEntity(
          msgId = wsMsg.messageId, // 【关键】保存推送中的 messageId
          conversationId = wsMsg.conversationId,
          senderId = wsMsg.senderId,
          content = contentObj.text,
          time = timeStr,
          timestamp = wsMsg.createdTime
        )
        viewModelScope.launch { messageDao.insertMessage(entity) }

        if (chat != null) {
          chat.lastContent = contentObj.text
          chat.lastTime = timeStr
          if (wsMsg.senderId.toString() != currentUserId) chat.unreadCount++

          val isDuplicate = wsMsg.senderId.toString() == currentUserId && chat.msgs.lastOrNull()?.text == contentObj.text
          if (!isDuplicate) {
            val senderUser = if (wsMsg.senderId.toString() == currentUserId) User.Me else User(wsMsg.senderId.toString(), getNameForUser(wsMsg.senderId), getAvatarForUser(wsMsg.senderId))
            chat.msgs.add(Msg(senderUser, contentObj.text, timeStr).apply { read = true })
          }
        } else { refreshConversationList() }
      }
    } catch (e: Exception) { e.printStackTrace() }
  }

  private fun getAvatarForUser(userId: Long) = when (userId) { 1001L -> R.drawable.avatar_zhangsan; 1002L -> R.drawable.avatar_lisi; else -> R.drawable.avatar_me }
  private fun getNameForUser(userId: Long) = when (userId) { 1001L -> "ZhangSan"; 1002L -> "LiSi"; else -> "User $userId" }
}
