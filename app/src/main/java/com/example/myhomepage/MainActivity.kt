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

@Serializable data class SelectConversation(val cardJson: String)
@Serializable data class SharedTodoDetails(val cardJson: String)
@Serializable object ImagePreview

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

                val shareTodoToChat = object : ShareTodoToChat {
                    override fun share(card: TodoShareCard) {
                        val json = Json.encodeToString(card)
                        val encoded = URLEncoder.encode(json, "UTF-8")
                        navController.navigate(SelectConversation(encoded))
                    }
                }

                val openSharedTodoDetail = object : com.example.myhomepage.share.OpenSharedTodoDetail {
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
                            onTodoCardClick = { card -> openSharedTodoDetail.open(card) },
                            // 【新增】图片预览跳转
                            onImageClick = {
                                navController.navigate(ImagePreview)
                            }
                        )
                    }

                    // 【新增】图片预览页
                    composable<ImagePreview> {
                        val base64 = viewModel.currentPreviewImageBase64
                        if (base64 != null) {
                            ImagePreviewPage(base64) {
                                navController.popBackStack()
                            }
                        }
                    }

                    composable<SelectConversation> { entry ->
                        val json = URLDecoder.decode(entry.toRoute<SelectConversation>().cardJson, "UTF-8")
                        val card = Json.decodeFromString<TodoShareCard>(json)
                        SelectConversationPage(
                            viewModel, card,
                            onBack = { navController.popBackStack() },
                            onSent = { navController.popBackStack() }
                        )
                    }

                    composable<SharedTodoDetails> { entry ->
                        val json = URLDecoder.decode(entry.toRoute<SharedTodoDetails>().cardJson, "UTF-8")
                        val card = Json.decodeFromString<TodoShareCard>(json)
                        SharedTodoDetailsPage(card, onBack = { navController.popBackStack() }, onSaveClick = {
                            addViewModel.saveSharedTodo(it)
                            navController.popBackStack()
                        })
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
                                // 我们现在通过 ownerId 进行数据隔离，不需要清库了
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