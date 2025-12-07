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
import com.example.myhomepage.todolist.TodoAppContainer
import com.example.myhomepage.todolist.presentation.TodoDetailViewModel
import com.example.myhomepage.todolist.presentation.TodoListViewModel
import com.example.myhomepage.ui.AddTodoPage
import com.example.myhomepage.ui.ChatDetails
import com.example.myhomepage.ui.ChatDetailsPage
import com.example.myhomepage.ui.Home
import com.example.myhomepage.ui.HomePage
import com.example.myhomepage.ui.Login
import com.example.myhomepage.ui.LoginPage
import com.example.myhomepage.ui.TodoDetails
import com.example.myhomepage.ui.TodoDetailsPage
import com.example.myhomepage.ui.TodoEdit
import com.example.myhomepage.ui.theme.WeComposeTheme
import com.example.myhomepage.ui.todoAdd
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    // 您的 IM ViewModel
    val viewModel: WeViewModel by viewModels()

    // 远端的 Todo 依赖容器和 ViewModels
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
                NavHost(navController, Login) {
                    composable<Home> {
                        LaunchedEffect(Unit) {
                            viewModel.refreshConversationList()
                        }
                        // 合并：同时传入 viewModel (IM) 和 todolistViewModel (Todo)
                        HomePage(
                            viewModel,
                            todolistViewModel,
                            { navController.navigate(ChatDetails(it.friend.id)) },
                            { navController.navigate(TodoDetails(it.id)) },
                            { navController.navigate(Login) },
                            { navController.navigate(todoAdd) }
                        )
                    }
                    composable<ChatDetails>(
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                    ) {
                        ChatDetailsPage(viewModel, it.toRoute<ChatDetails>().userId)
                    }
                    // 使用远端的 Todo 详情页逻辑
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
                    // 使用远端的 AddTodo 逻辑
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
                            // 保留您的登录逻辑 (WebSocket 连接)
                            lifecycleScope.launch {
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