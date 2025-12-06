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
import com.example.myhomepage.todolist.TodoAppContainer
import com.example.myhomepage.todolist.presentation.TodoListViewModel
import com.example.myhomepage.todolist.presentation.TodoDetailViewModel
import com.example.myhomepage.network.WebSocketManager
import com.example.myhomepage.ui.AddTodoPage
import com.example.myhomepage.ui.ChatDetails
import com.example.myhomepage.ui.ChatDetailsPage
import com.example.myhomepage.ui.Home
import com.example.myhomepage.ui.HomePage
import com.example.myhomepage.ui.Login
import com.example.myhomepage.ui.LoginPage
import com.example.myhomepage.ui.TodoDetails
import com.example.myhomepage.ui.TodoDetailsPage
import com.example.myhomepage.ui.theme.WeComposeTheme
import com.example.myhomepage.ui.todoAdd
import com.example.myhomepage.ui.TodoEdit
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    val viewModel: WeViewModel by viewModels()


    // 在 Activity 里用 lazy 创建 ToDoList DI 容器
    private val todoAppContainer: TodoAppContainer by lazy {
        // 用 applicationContext 或 this 都可以
        TodoAppContainer(applicationContext)
    }
//    private val todoAppContainer: TodoAppContainer by lazy {
//        TodoAppContainer()
//    }
    // 用 Activity 的 viewModels + 自定义 factory 创建两个 VM
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
                        // 进入主页时刷新会话列表
                        LaunchedEffect(Unit) {
                            viewModel.refreshConversationList()
                        }
                        HomePage(viewModel,
                            todolistViewModel,
                            { navController.navigate(ChatDetails(it.friend.id)) },
                            {navController.navigate(TodoDetails(it.id))},
                            {navController.navigate(Login)},
                            {navController.navigate(todoAdd)})
                    }
                    composable<ChatDetails>(
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                    ) {
                        ChatDetailsPage(viewModel, it.toRoute<ChatDetails>().userId)
                    }
                    composable<TodoDetails>(
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                    ) {
                        TodoDetailsPage(todolistViewModel, it.toRoute<TodoDetails>().todoId,
                            onBack = { navController.popBackStack() }, onEditClick = {navController.navigate(
                                TodoEdit(it.toRoute<TodoDetails>().todoId))}
                        )
                    }
                    composable<todoAdd> {
                        AddTodoPage(addViewModel,false,null, onBack = {navController.popBackStack()}){}
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
                            // 设置当前用户ID并加载用户信息
                            lifecycleScope.launch {
                                viewModel.setCurrentUserIdAndLoadUser(userId)
                                // 建立WebSocket连接
                                WebSocketManager.getInstance().connect(userId)
                                // 登录成功后跳转到主页
                                navController.navigate(Home)
                            }
                        }
                    }
                }
            }
        }
    }
}