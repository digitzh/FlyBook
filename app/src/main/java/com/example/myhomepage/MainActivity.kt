package com.example.myhomepage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.myhomepage.network.WebSocketManager
import com.example.myhomepage.share.OpenSharedTodoDetail
import com.example.myhomepage.share.ShareTodoToChat
import com.example.myhomepage.share.TodoShareCard
import com.example.myhomepage.todolist.TodoAppContainer
import com.example.myhomepage.todolist.presentation.TodoDetailViewModel
import com.example.myhomepage.todolist.presentation.TodoListViewModel
import com.example.myhomepage.ui.*
import com.example.myhomepage.ui.theme.WeComposeTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.URLDecoder
import java.net.URLEncoder

// 新增路由定义
@Serializable data class SelectConversation(val cardJson: String)
@Serializable data class SharedTodoDetails(val cardJson: String)

class MainActivity : ComponentActivity() {

    val viewModel: WeViewModel by viewModels()

    private val todoAppContainer: TodoAppContainer by lazy {
        TodoAppContainer(applicationContext)
    }
    private val todolistViewModel: TodoListViewModel by viewModels {
        todoAppContainer.todoListViewModelFactory
    }
    private val addViewModel: TodoDetailViewModel by viewModels {
        todoAppContainer.todoDetailViewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        lifecycleScope.launch {
            viewModel.isLightThemeFlow.collect {
                insetsController.isAppearanceLightStatusBars = it
            }
        }

        setContent {
            WeComposeTheme(viewModel.theme) {
                val navController = rememberNavController()

                // 【实现分享接口】
                val shareTodoToChat = object : ShareTodoToChat {
                    override fun share(card: TodoShareCard) {
                        val json = Json.encodeToString(card)
                        val encoded = URLEncoder.encode(json, "UTF-8")
                        navController.navigate(SelectConversation(encoded))
                    }
                }
                // 【实现点击卡片查看详情接口】
                val openSharedTodoDetail = object : OpenSharedTodoDetail {
                    override fun open(card: TodoShareCard) {
                        val json = Json.encodeToString(card)
                        val encoded = URLEncoder.encode(json, "UTF-8")
                        navController.navigate(SharedTodoDetails(encoded))
                    }
                }

                NavHost(navController, Login) {
                    composable<Home> {
                        LaunchedEffect(Unit) {
                            viewModel.refreshConversationList()
                        }
                        HomePage(
                            viewModel,
                            todolistViewModel,
                            { navController.navigate(ChatDetails(it.friend.id)) },
                            { navController.navigate(TodoDetails(it.id)) },
                            { navController.navigate(Login) },
                            { navController.navigate(todoAdd) },
                            // 传递分享逻辑
                            onShareTodo = { card -> shareTodoToChat.share(card) }
                        )
                    }
                    composable<ChatDetails>(
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                    ) {
                        ChatDetailsPage(
                            viewModel,
                            it.toRoute<ChatDetails>().userId,
                            // 点击卡片进入详情页
                            onTodoCardClick = openSharedTodoDetail::open
                        )
                    }

                    // 选择会话页面
                    composable<SelectConversation> { entry ->
                        val json = URLDecoder.decode(entry.toRoute<SelectConversation>().cardJson, "UTF-8")
                        val card = Json.decodeFromString<TodoShareCard>(json)
                        SelectConversationPage(
                            viewModel, card,
                            onBack = { navController.popBackStack() },
                            onSent = { navController.popBackStack() } // 发送成功后返回
                        )
                    }

                    // 卡片点击详情页面
                    composable<SharedTodoDetails> { entry ->
                        val json = URLDecoder.decode(entry.toRoute<SharedTodoDetails>().cardJson, "UTF-8")
                        val card = Json.decodeFromString<TodoShareCard>(json)
                        SharedTodoDetailsPage(card, onBack = { navController.popBackStack() }, onSaveClick = { shareCard -> addViewModel.saveSharedTodo(shareCard) })
                    }
                    composable<TodoDetails>(
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                    ) {
                        TodoDetailsPage(
                            todolistViewModel,
                            it.toRoute<TodoDetails>().todoId,
                            onBack = { navController.popBackStack() },
                            onEditClick = { navController.navigate(TodoEdit(it.toRoute<TodoDetails>().todoId)) }
                        )
                    }
                    composable<todoAdd> {
                        AddTodoPage(addViewModel, false, null, onBack = { navController.popBackStack() }) {}
                    }
                    composable<TodoEdit> { backStackEntry ->
                        val args = backStackEntry.toRoute<TodoEdit>()
                        AddTodoPage(
                            addViewModel = addViewModel,
                            isEdit = true,
                            todoId = args.todoId,
                            onBack = { navController.popBackStack() },
                            addTodo = {}
                        )
                    }
                    composable<Login> {
                        LoginPage { userId ->
                            lifecycleScope.launch {
                                // 先同步用户数据，再设置当前用户
                                viewModel.syncUsersFromServer()
                                viewModel.setCurrentUserIdAndLoadUser(userId)
                                WebSocketManager.getInstance().connect(userId)
                                navController.navigate(Home)
                            }
                        }
                    }
                }
            }
        }
    }
}