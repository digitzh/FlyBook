package com.example.myhomepage

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myhomepage.data.Backlog
import com.example.myhomepage.data.Chat
import com.example.myhomepage.data.Msg
import com.example.myhomepage.data.User
import com.example.myhomepage.ui.theme.TodoType
import com.example.myhomepage.ui.theme.WeComposeTheme
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class WeViewModel : ViewModel() {
  var theme by mutableStateOf(WeComposeTheme.Theme.Light)
  val isLightTheme by derivedStateOf { theme == WeComposeTheme.Theme.Light }
  val isLightThemeFlow = snapshotFlow { isLightTheme }
    .stateIn(viewModelScope, SharingStarted.Lazily, true)
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

//  fun changeBacklog(affair: Backlog){
//      if (affair.complete== false) affair.complete = true
//  }
}