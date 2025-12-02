package com.example.flybook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.flybook.database.AppDatabase
import com.example.flybook.database.ChatDao
import com.example.flybook.ui.chat.ChatScreen
import com.example.flybook.ui.home.HomeScreen
import com.example.flybook.viewmodel.ChatViewModel
import com.example.flybook.viewmodel.ChatViewModelFactory
import com.example.flybook.viewmodel.HomeViewModel
import com.example.flybook.viewmodel.HomeViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var db: AppDatabase
    private lateinit var chatDao: ChatDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化 Room
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "flybook.db"
        )
            .fallbackToDestructiveMigration()    // ← 自动重建数据库
            .build()

        chatDao = db.chatDao()

        setContent {
            val navController = rememberNavController()
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(chatDao)
            )
            val chatViewModel: ChatViewModel = viewModel(
                factory = ChatViewModelFactory(chatDao)
            )
            MaterialTheme {
                NavHost(navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(navController, homeViewModel)
                    }
                    composable(
                        "chat/{conversationId}/{groupName}",
                        arguments = listOf(
                            navArgument("conversationId") { type = NavType.LongType },
                            navArgument("groupName") { type = NavType.StringType }
                        )
                    ) { backStackEntry ->
                        val groupName = backStackEntry.arguments?.getString("groupName") ?: "聊天"
                        val conversationId = backStackEntry.arguments!!.getLong("conversationId")

                        // 加载历史消息
                        chatViewModel.loadMessages(conversationId)

                        ChatScreen(
                            navController = navController,
                            viewModel = chatViewModel,
                            groupName = groupName,
                            conversationId = conversationId
                        )
                    }
                }
            }
        }
    }
}
