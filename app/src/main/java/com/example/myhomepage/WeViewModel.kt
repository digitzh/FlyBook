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
import com.example.myhomepage.database.UserEntity
import com.example.myhomepage.network.ApiService
import com.example.myhomepage.network.ConversationVO
import com.example.myhomepage.ui.theme.TodoType
import com.example.myhomepage.ui.theme.WeComposeTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
  private val apiService = ApiService()

  // 用户列表（从数据库读取）
  val users = userDao.getAllUsers()
  
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

  val contacts by mutableStateOf(
    listOf(
        User("zhangsan", "张三", R.drawable.avatar_zhangsan),
        User("lisi", "李四", R.drawable.avatar_lisi),
    )
  )
  var chats by mutableStateOf(
    listOf(
      Chat(
        friend = User("zhangsan", "张三", R.drawable.avatar_zhangsan),
        mutableStateListOf(
          Msg(User("zhangsan", "张三", R.drawable.avatar_zhangsan), "锄禾日当午", "14:20"),
          Msg(User.Me, "汗滴禾下土", "14:20"),
          Msg(User("zhangsan", "张三", R.drawable.avatar_zhangsan), "谁知盘中餐", "14:20"),
          Msg(User.Me, "粒粒皆辛苦", "14:20"),
          Msg(User("zhangsan", "张三", R.drawable.avatar_zhangsan), "不聊了", "14:20"),
          Msg(User.Me, "吃饭吧？", "14:28"),
        )
      ),
      Chat(
        friend = User("lisi", "李四", R.drawable.avatar_lisi),
        mutableStateListOf(
          Msg(User("lisi", "李四", R.drawable.avatar_lisi), "你好", "13:48"),
          Msg(User.Me, "你好", "13:48"),
          Msg(User("lisi", "李四", R.drawable.avatar_lisi), "你是谁", "13:48").apply { read = false },
        )
      ),
    )
  )

//  val initbacklogList by mutableStateOf(
//      listOf(
//        Backlog("wenjian1", "周报", "完成周报", "2025-12-03", TodoType.FILE),
//        Backlog("wenjian2", "接口设计文档", "完成接口设计文档","2025-12-05",TodoType.FILE),
//        Backlog("wenjian3", "数据分析表", "完成数据分析表","2025-12-08",TodoType.FILE),
//        Backlog("trans1", "下午2点开会", "项目讨论","2025-12-01",TodoType.CONF),
//        Backlog("trans2", "下午2点开会", "项目讨论","2025-12-02",TodoType.CONF),
//        Backlog("other2", "原神启动", "原神启动！！！","2025-12-01",TodoType.OTHER),
//        Backlog("other1", "原神启动", "原神启动！！！","2025-12-02",TodoType.OTHER),
//      )
//  )

  fun switchTheme() {
    theme = when (theme) {
      WeComposeTheme.Theme.Light -> WeComposeTheme.Theme.Dark;
      WeComposeTheme.Theme.Dark -> WeComposeTheme.Theme.Light
    }
  }

  fun boom(chat: Chat,msg: String) {
    chat.msgs.add(Msg(User.Me, msg, "15:10").apply { read = true })
  }

  /**
   * 刷新会话列表（从服务端获取）
   */
  fun refreshConversationList() {
    val userId = currentUserId
    if (userId == null) return

    viewModelScope.launch {
      try {
        val conversationList = apiService.getConversationList(userId)
        chats = conversationList.map { vo ->
          convertConversationVOToChat(vo)
        }
      } catch (e: Exception) {
        android.util.Log.e("WeViewModel", "Refresh conversation list error", e)
      }
    }
  }

  /**
   * 将ConversationVO转换为Chat对象
   */
  private suspend fun convertConversationVOToChat(vo: ConversationVO): Chat {
    // 如果是群聊，使用群聊名称和头像
    // 如果是单聊，需要查询对方用户信息
    val friend = if (vo.type == 2) {
      // 群聊：使用默认用户，显示名称使用群聊名称
      User("group_${vo.conversationId}", vo.name ?: "群聊", R.drawable.avatar_me)
    } else {
      // 单聊：需要查询对方用户信息（这里简化处理，使用默认用户）
      // TODO: 实际应该查询对方用户信息
      User("user_${vo.conversationId}", vo.name ?: "用户", R.drawable.avatar_me)
    }

    // 创建消息列表（如果有最后一条消息）
    val msgs = mutableStateListOf<Msg>()
    if (!vo.lastMsgContent.isNullOrBlank()) {
      msgs.add(
        Msg(
          from = friend,
          text = vo.lastMsgContent,
          time = vo.lastMsgTime ?: ""
        ).apply { read = vo.unreadCount == 0 }
      )
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
   * @param name 群聊名称
   * @param selectedUserIds 选择的用户ID列表（包含创建者，因为对话框默认勾选了）
   * @param onSuccess 成功回调
   * @param onError 失败回调
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

        // 1. 创建会话（创建者会自动成为成员）
        val conversationId = apiService.createConversation(userId, 2, name)
        if (conversationId == null) {
          onError("创建会话失败")
          return@launch
        }

        // 2. 添加其他成员（排除创建者自己）
        val otherUserIds = selectedUserIds.filter { it != userIdLong }
        if (otherUserIds.isNotEmpty()) {
          val success = apiService.addMembersToConversation(userId, conversationId, otherUserIds)
          if (!success) {
            onError("添加成员失败")
            return@launch
          }
        }

        // 3. 刷新会话列表
        refreshConversationList()
        onSuccess()
      } catch (e: Exception) {
        onError("创建群聊失败: ${e.message}")
      }
    }
  }

//  fun changeBacklog(affair: Backlog){
//      if (affair.complete== false) affair.complete = true
//  }
}