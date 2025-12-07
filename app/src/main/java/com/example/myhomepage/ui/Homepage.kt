package com.example.myhomepage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.myhomepage.WeViewModel
import com.example.myhomepage.data.Backlog
import com.example.myhomepage.data.Chat
import com.example.myhomepage.todolist.presentation.TodoListViewModel
import com.example.myhomepage.ui.theme.WeComposeTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object Home

@Composable
fun HomePage(
    viewModel: WeViewModel,
    todolistViewModel: TodoListViewModel, // 【新增】
    onOpenChat: (Chat) -> Unit,
    onOpenTodo: (Backlog) -> Unit,
    myLogout: () -> Unit,
    addTodo : () -> Unit
) {
    Column(Modifier
        .background(WeComposeTheme.colors.background)
        .statusBarsPadding()) {
        val pagerState = rememberPagerState { 3 }
        HorizontalPager(pagerState, Modifier.weight(1f)) { page ->
            when (page) {
                0 -> ChatList(viewModel.chats, onOpenChat, viewModel)
                // 【修改】传入 todolistViewModel
                1 -> TodoList(viewModel.chats, todolistViewModel, onOpenTodo, addTodo)
                // 【修改】MeList 使用远端的逻辑 (支持显示用户名)
                2 -> MeList(viewModel, myLogout)
            }
        }
        val scope = rememberCoroutineScope()
        WeNavigationBar(pagerState.currentPage) {
            scope.launch { pagerState.animateScrollToPage(it) }
        }
    }
}
