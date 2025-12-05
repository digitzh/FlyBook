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
import com.example.myhomepage.database.MessageEntity
import com.example.myhomepage.database.UserEntity
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

// 【新增】WebSocket 消息结构
@Serializable
data class WsMessage(
  val msgType: Int,
  val conversationId: Long,
  val senderId: Long,
  val content: String, // 这是一个嵌套的 JSON 字符串
  val createdTime: Long
)

// 【新增】消息内容结构
@Serializable
data class WsContent(val text: String)

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
    // 【新增】监听 WebSocket 消息
    viewModelScope.launch {
      WebSocketManager.getInstance().receivedMessages.collect { jsonStr ->
        if (!jsonStr.isNullOrBlank()) {
          handleWebSocketMessage(jsonStr)
        }
      }
    }
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

  val initbacklogList by mutableStateOf(
    listOf(
      Backlog("wenjian1", "周报", "完成周报", "2025-12-03", TodoType.FILE),
      Backlog("wenjian2", "接口设计文档", "完成接口设计文档", "2025-12-05", TodoType.FILE),
      Backlog("wenjian3", "数据分析表", "完成数据分析表", "2025-12-08", TodoType.FILE),
      Backlog("trans1", "下午2点开会", "项目讨论", "2025-12-01", TodoType.CONF),
      Backlog("trans2", "下午2点开会", "项目讨论", "2025-12-02", TodoType.CONF),
      Backlog("other2", "原神启动", "原神启动！！！", "2025-12-01", TodoType.OTHER),
      Backlog("other1", "原神启动", "原神启动！！！", "2025-12-02", TodoType.OTHER),
    )
  )

  fun switchTheme() {
    theme = when (theme) {
      WeComposeTheme.Theme.Light -> WeComposeTheme.Theme.Dark;
      WeComposeTheme.Theme.Dark -> WeComposeTheme.Theme.Light
    }
  }



  /**
   * 【修改】发送消息 (boom)
   * 调用 API 发送消息，并在成功后更新本地 UI
   */
//  fun boom(chat: Chat, msg: String) {
//    val cid = chat.conversationId
//    val uid = currentUserId
//
//    if (cid != null && uid != null) {
//      viewModelScope.launch {
//        // 1. 调用 API 发送消息
//        val result = apiService.sendMessage(uid, cid, msg)
//
//        if (result != null) {
//          // 2. 发送成功后，在本地 UI 显示
//          val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
//          // 添加到聊天列表 (User.Me 表示显示在右侧)
//          chat.msgs.add(Msg(User.Me, msg, timeStr).apply { read = true })
//        } else {
//          android.util.Log.e("WeViewModel", "Send message failed")
//        }
//      }
//    } else {
//      // 本地模拟逻辑（无网络时或测试用）
//      chat.msgs.add(Msg(User.Me, msg, "15:10").apply { read = true })
//    }
//  }
  /**
   * 【修改】发送消息
   * 发送成功后，保存到本地数据库
   */
  fun boom(chat: Chat, msg: String) {
    val cid = chat.conversationId
    val uid = currentUserId

    if (cid != null && uid != null) {
      viewModelScope.launch {
        val result = apiService.sendMessage(uid, cid, msg)
        if (result != null) {
          val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

          // 1. UI 上屏
          chat.msgs.add(Msg(User.Me, msg, timeStr).apply { read = true })

          // 2. 【新增】保存到本地数据库
          val entity = MessageEntity(
            conversationId = cid,
            senderId = uid.toLong(),
            content = msg,
            time = timeStr,
            timestamp = System.currentTimeMillis()
          )
          messageDao.insertMessage(entity)

        } else {
          android.util.Log.e("WeViewModel", "Send message failed")
        }
      }
    }
  }

//  /**
//   * 刷新会话列表（从服务端获取）
//   */
//  fun refreshConversationList() {
//    val userId = currentUserId
//    if (userId == null) return
//
//    viewModelScope.launch {
//      try {
//        val conversationList = apiService.getConversationList(userId)
//        chats = conversationList.map { vo ->
//          convertConversationVOToChat(vo)
//        }
//      } catch (e: Exception) {
//        android.util.Log.e("WeViewModel", "Refresh conversation list error", e)
//      }
//    }
//  }
  /**
   * 刷新会话列表（从服务端获取）
   * 【修改说明】这里加入了合并逻辑，防止覆盖掉本地已有的聊天记录
   */
  fun refreshConversationList() {
    val userId = currentUserId
    if (userId == null) return

    viewModelScope.launch {
      try {
        val conversationList = apiService.getConversationList(userId)

        // 使用 map 处理：如果本地已有该会话，则复用对象（保留聊天记录），否则创建新对象
        val newChats = conversationList.map { vo ->
          // 1. 查找内存中是否已有该会话
          val existingChat = chats.find { it.conversationId == vo.conversationId }

          if (existingChat != null) {
            // 2. 如果有，更新它的基础属性 (保留 existingChat.msgs 不变)
            existingChat.name = vo.name
            existingChat.avatarUrl = vo.avatarUrl

            // 3. 特殊处理：如果本地完全没消息，但服务端列表显示有消息（比如刚登录还没进过聊天页）
            // 我们可以把这“最后一条”补进去作为摘要
            if (existingChat.msgs.isEmpty() && !vo.lastMsgContent.isNullOrBlank()) {
              existingChat.msgs.add(
                Msg(
                  existingChat.friend,
                  vo.lastMsgContent,
                  vo.lastMsgTime ?: ""
                ).apply { read = vo.unreadCount == 0 }
              )
            }

            // 返回旧对象（关键！）
            existingChat
          } else {
            // 3. 如果没有，则是新会话，创建新对象
            convertConversationVOToChat(vo)
          }
        }

        // 更新列表状态
        chats = newChats

      } catch (e: Exception) {
        android.util.Log.e("WeViewModel", "Refresh conversation list error", e)
      }
    }
  }


  /**
   * 将ConversationVO转换为Chat对象
   */
//  private suspend fun convertConversationVOToChat(vo: ConversationVO): Chat {
//    val friend = if (vo.type == 2) {
//      // 群聊：使用群名称
//      User("group_${vo.conversationId}", vo.name ?: "群聊", R.drawable.avatar_me)
//    } else {
//      // 单聊：简单的头像/名字映射
//      val friendIdLong = if (vo.name == "ZhangSan") 1001L else if (vo.name == "LiSi") 1002L else 0L
//      val avatar = getAvatarForUser(friendIdLong)
//      User("user_${vo.conversationId}", vo.name ?: "用户", avatar)
//    }
//
//    // 创建消息列表
//    val msgs = mutableStateListOf<Msg>()
//    if (!vo.lastMsgContent.isNullOrBlank()) {
//      msgs.add(
//        Msg(
//          from = friend,
//          text = vo.lastMsgContent,
//          time = vo.lastMsgTime ?: ""
//        ).apply { read = vo.unreadCount == 0 }
//      )
//    }
//
//
//    return Chat(
//      friend = friend,
//      msgs = msgs,
//      conversationId = vo.conversationId,
//      type = vo.type,
//      name = vo.name,
//      avatarUrl = vo.avatarUrl
//    )
//  }
  /**
   * 【修改】将ConversationVO转换为Chat对象
   * 现在需要从数据库加载历史消息
   */
  private suspend fun convertConversationVOToChat(vo: ConversationVO): Chat {
    val friend = if (vo.type == 2) {
      User("group_${vo.conversationId}", vo.name ?: "群聊", R.drawable.avatar_me)
    } else {
      val friendIdLong = if (vo.name == "ZhangSan") 1001L else if (vo.name == "LiSi") 1002L else 0L
      val avatar = getAvatarForUser(friendIdLong)
      User("user_${vo.conversationId}", vo.name ?: "用户", avatar)
    }

    // 1. 从本地数据库加载该会话的历史消息
    val dbMessages = messageDao.getMessagesByConversationId(vo.conversationId)

    val msgs = mutableStateListOf<Msg>()

    // 2. 将数据库实体转换为 UI 模型 (Msg)
    dbMessages.forEach { entity ->
      val senderUser = if (entity.senderId.toString() == currentUserId) {
        User.Me
      } else {
        User(
          entity.senderId.toString(),
          getNameForUser(entity.senderId),
          getAvatarForUser(entity.senderId)
        )
      }
      msgs.add(Msg(senderUser, entity.content, entity.time).apply { read = true })
    }

    // 3. 如果数据库是空的（可能是第一次加载），但服务端列表里有摘要，把摘要补进去
    // 注意：这里最好也把摘要存入数据库，避免下次还是空的
    if (msgs.isEmpty() && !vo.lastMsgContent.isNullOrBlank()) {
      val timeStr = vo.lastMsgTime ?: ""
      msgs.add(
        Msg(friend, vo.lastMsgContent, timeStr).apply { read = vo.unreadCount == 0 }
      )
      // 可选：将这条摘要也存入库，防止丢失
    }

    return Chat(
      friend = friend,
      msgs = msgs,
      conversationId = vo.conversationId,
      type = vo.type,
      name = vo.name,
      avatarUrl = vo.avatarUrl
    )
  }

  /**
   * 创建群聊
   */
  fun createGroupConversation(
    name: String,
    selectedUserIds: List<Long>,
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

        // 1. 创建会话
        val conversationId = apiService.createConversation(userId, 2, name)
        if (conversationId == null) {
          onError("创建会话失败")
          return@launch
        }

        // 2. 添加成员
        val otherUserIds = selectedUserIds.filter { it != userIdLong }
        if (otherUserIds.isNotEmpty()) {
          val success = apiService.addMembersToConversation(userId, conversationId, otherUserIds)
          if (!success) {
            onError("添加成员失败")
            return@launch
          }
        }

        // 3. 刷新
        refreshConversationList()
        onSuccess()
      } catch (e: Exception) {
        onError("创建群聊失败: ${e.message}")
      }
    }
  }

  fun changeBacklog(affair: Backlog) {
    if (affair.complete == false) affair.complete = true
  }

  /**
   * 【新增】处理 WebSocket 消息
   */
//  private fun handleWebSocketMessage(jsonStr: String) {
//    try {
//      val parser = Json { ignoreUnknownKeys = true }
//      val wsMsg = parser.decodeFromString<WsMessage>(jsonStr)
//
//      // 找到对应的会话
//      val chat = chats.find { it.conversationId == wsMsg.conversationId }
//
//      if (chat == null) {
//        // 新会话或未加载的会话，刷新列表
//        refreshConversationList()
//        return
//      }
//
//      if (wsMsg.msgType == 1) {
//        // 文本消息
//        val contentObj = parser.decodeFromString<WsContent>(wsMsg.content)
//
//        // 判断是否是自己发的（多端同步）
//        val isMe = wsMsg.senderId.toString() == currentUserId
//
//        val senderUser = if (isMe) {
//          User.Me
//        } else {
//          // 构造发送者用户
//          val name = getNameForUser(wsMsg.senderId)
//          val avatar = getAvatarForUser(wsMsg.senderId)
//          User(wsMsg.senderId.toString(), name, avatar)
//        }
//
//        // 格式化时间
//        val date = Date(wsMsg.createdTime)
//        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
//
//        // 添加消息到 UI
//        // 简单去重：如果是自己发的且最后一条消息内容相同，则不重复添加（避免 boom 接口添加后 ws 又推一次）
//        val lastMsg = chat.msgs.lastOrNull()
//        val isDuplicate = isMe && lastMsg?.text == contentObj.text && lastMsg?.from == User.Me
//
//        if (!isDuplicate) {
//          chat.msgs.add(Msg(senderUser, contentObj.text, timeStr).apply { read = true })
//        }
//      }
//    } catch (e: Exception) {
//      android.util.Log.e("WeViewModel", "WebSocket handle error", e)
//    }
//  }
  /**
   * 【修改】处理 WebSocket 消息
   * 收到消息后，保存到本地数据库
   */
  private fun handleWebSocketMessage(jsonStr: String) {
    try {
      val parser = Json { ignoreUnknownKeys = true }
      val wsMsg = parser.decodeFromString<WsMessage>(jsonStr)

      val chat = chats.find { it.conversationId == wsMsg.conversationId }

      // 如果当前内存里没有这个会话，刷新列表（刷新时会自动从DB加载，但我们需要先把这条消息存DB）
      // 所以无论 chat 是否为空，都应该先存库

      if (wsMsg.msgType == 1) {
        val contentObj = parser.decodeFromString<WsContent>(wsMsg.content)
        val isMe = wsMsg.senderId.toString() == currentUserId

        val date = Date(wsMsg.createdTime)
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)

        // 1. 【新增】保存到本地数据库
        // 只有当不是“自己发给自己的回执”时才存，或者根据 messageId 去重
        // 这里简单处理：都存（onConflict=Replace会根据ID覆盖，但我们这里MessageEntity主键是自增的）
        // 为了避免重复，可以在 MessageEntity 增加 messageId 字段作为唯一键，这里简化处理
        val entity = MessageEntity(
          conversationId = wsMsg.conversationId,
          senderId = wsMsg.senderId,
          content = contentObj.text,
          time = timeStr,
          timestamp = wsMsg.createdTime
        )
        // 启动协程写入数据库
        viewModelScope.launch {
          messageDao.insertMessage(entity)
        }

        // 2. 更新 UI (如果当前正好在这个会话窗口)
        if (chat != null) {
          val lastMsg = chat.msgs.lastOrNull()
          // 简单防抖：如果是自己发的，且最后一条内容一样，就不重复加 UI 了
          val isDuplicate = isMe && lastMsg?.text == contentObj.text

          if (!isDuplicate) {
            val senderUser = if (isMe) User.Me else {
              User(wsMsg.senderId.toString(), getNameForUser(wsMsg.senderId), getAvatarForUser(wsMsg.senderId))
            }
            chat.msgs.add(Msg(senderUser, contentObj.text, timeStr).apply { read = true })
          }
        } else {
          // 如果 chat 为空（新会话），刷新列表，列表刷新时会从 DB 读出刚才存进去的消息
          refreshConversationList()
        }
      }
    } catch (e: Exception) {
      android.util.Log.e("WeViewModel", "WebSocket handle error", e)
    }
  }

  // 【新增】辅助方法：根据 ID 获取头像
  private fun getAvatarForUser(userId: Long): Int {
    return when (userId) {
      1001L -> R.drawable.avatar_zhangsan
      1002L -> R.drawable.avatar_lisi
      else -> R.drawable.avatar_me
    }
  }

  // 【新增】辅助方法：根据 ID 获取名字
  private fun getNameForUser(userId: Long): String {
    return when (userId) {
      1001L -> "ZhangSan"
      1002L -> "LiSi"
      else -> "User $userId"
    }
  }
}
