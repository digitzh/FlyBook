package com.example.flybook.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flybook.model.Message
import com.example.flybook.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    viewModel: ChatViewModel,
    groupName: String,
    conversationId: Long
) {
    var inputText by remember { mutableStateOf("") }

    // 直接从 viewModel 读取消息（因为是 StateList，会自动刷新）
    val messages = viewModel.getMessages(conversationId)

    Column(modifier = Modifier.fillMaxSize()) {

        TopAppBar(
            title = {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(groupName, modifier = Modifier.align(Alignment.Center))
                }
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
            }
        )

        LazyColumn(
            modifier = Modifier.weight(1f).padding(8.dp),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                MessageItem(message)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            TextField(
                value = inputText,
                onValueChange = { inputText = it },
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(
                            conversationId,
                            Message(
                                id = System.currentTimeMillis(),
                                senderId = 1001,
                                content = inputText,
                                timestamp = System.currentTimeMillis()
                            )
                        )
                        inputText = ""  // 清空输入框
                    }
                }
            ) {
                Text("发送")
            }
        }
    }
}