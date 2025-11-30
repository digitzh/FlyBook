package com.example.leifeishu.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.navigation.NavHostController
import com.example.leifeishu.ui.conversation.chat.ChatScreen
import com.example.leifeishu.ui.conversation.chat.ChatViewModel
import com.example.leifeishu.ui.conversation.conversationList.ConversationListScreen
import com.example.leifeishu.ui.conversation.conversationList.ConversationListViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    conversationListViewModel: ConversationListViewModel,
    chatViewModel: ChatViewModel
) {
    NavHost(navController, startDestination = "conversationList") {

        composable("conversationList") {
            ConversationListScreen(conversationListViewModel, navController)
        }

        composable(
            "chat/{conversationId}/{conversationName}",
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("conversationName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            ChatScreen(chatViewModel, backStackEntry, navController)
        }
    }
}
