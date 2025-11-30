package com.example.leifeishu.ui.conversation.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import com.example.leifeishu.data.model.Message
import androidx.compose.ui.Alignment
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.layout.imePadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    backStackEntry: NavBackStackEntry,
    navController: NavHostController
) {
    val conversationId = backStackEntry.arguments?.getString("conversationId") ?: return
    val conversationName = backStackEntry.arguments?.getString("conversationName") ?: "聊天"
    val state by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(conversationId) {
        viewModel.loadMessages(conversationId)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(conversationName) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                } ,
//                modifier = Modifier.height(36.dp)  // 自定义高度
            )
        },
        bottomBar = { /* 聊天页隐藏底部导航 */ }
    ) { innerPadding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .imePadding()
            ) {
                items(state.messages) { msg ->
                    MessageItem(msg)
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("请输入消息…") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(conversationId, messageText)
                                messageText = ""
                            }
                        }
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Button(onClick = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(conversationId, messageText)
                        messageText = ""
                    }
                }) {
                    Text("发送")
                }
            }
        }
    }
}

@Composable
fun MessageItem(msg: Message) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (msg.isMine) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            color = if (msg.isMine) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.secondaryContainer,
            shape = MaterialTheme.shapes.medium
        ) {
            Text(msg.content, modifier = Modifier.padding(12.dp))
        }
    }
    Spacer(Modifier.height(4.dp))
}
