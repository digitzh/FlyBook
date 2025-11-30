package com.example.leifeishu.ui.conversation.conversationList

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.leifeishu.data.model.Conversation

@Composable
fun ConversationListScreen(
    viewModel: ConversationListViewModel,
    navController: NavController
) {
    val state = viewModel.uiState.collectAsState().value

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(state.conversations) { conversation ->
            ConversationItem(conversation) {
                navController.navigate("chat/${conversation.id}/${conversation.name}")
            }
        }
    }
}

@Composable
fun ConversationItem(conversation: Conversation, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(conversation.name, style = MaterialTheme.typography.titleMedium)
        Text(conversation.lastMessage, style = MaterialTheme.typography.bodyMedium)
        if (conversation.unreadCount > 0) {
            Text("未读: ${conversation.unreadCount}", color = MaterialTheme.colorScheme.primary)
        }
    }
    Divider()
}
