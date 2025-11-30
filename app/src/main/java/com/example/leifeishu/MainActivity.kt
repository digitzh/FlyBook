package com.example.leifeishu

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.leifeishu.ui.conversation.chat.ChatViewModel
import com.example.leifeishu.ui.conversation.conversationList.ConversationListViewModel
import com.example.leifeishu.ui.navigation.AppNavGraph
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val conversationListViewModel: ConversationListViewModel by viewModel()
            val chatViewModel: ChatViewModel by viewModel()
            var selectedTab by remember { mutableStateOf(0) }

            // 安全获取当前路由
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route ?: "conversationList"

            Scaffold(
                topBar = {
                    if (currentRoute == "conversationList") {
                        CenterAlignedTopAppBar(
                            title = { Text("类飞书") }
                        )
                    }
                },
                bottomBar = {
                    if (currentRoute == "conversationList") {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = {
                                    selectedTab = 0
                                    navController.navigate("conversationList") {
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                label = { Text("即时通信") },
                                icon = { Icon(Icons.Default.Chat, contentDescription = null) }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                label = { Text("待办任务") },
                                icon = { Icon(Icons.Default.List, contentDescription = null) }
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    AppNavGraph(navController, conversationListViewModel, chatViewModel)
                }
            }
        }
    }
}
