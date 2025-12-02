package com.example.flybook.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.flybook.ui.contact.ContactDialog
import com.example.flybook.ui.contact.GroupNameDialog
import com.example.flybook.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel) {

    val conversations = viewModel.conversations
    val createdId by viewModel.createdConversationId.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    // 🔹 当新的会话 ID 有值且 conversations 已更新时跳转
    LaunchedEffect(createdId) {
        createdId?.let { id ->
            val conv = viewModel.conversations.find { it.id == id } ?: return@let
            navController.navigate("chat/${conv.id}/${conv.name}")
            viewModel.selectedContacts.clear()
            viewModel.newGroupName = ""
            viewModel.resetCreatedConversationId()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("飞书") },
                actions = {
                    IconButton(onClick = { viewModel.showContactDialog.value = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add")
                    }
                }
            )
        }
    ) { padding ->

        // 会话列表
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(conversations) { conversation ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            navController.navigate("chat/${conversation.id}/${conversation.name}")
                        }
                        .padding(16.dp)
                ) {
                    Text(
                        text = conversation.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        // 联系人选择弹窗
        if (viewModel.showContactDialog.value) {
            ContactDialog(
                viewModel = viewModel,
                onCreateGroup = {
                    viewModel.showContactDialog.value = false

                    // 先计算人数（包含自己）
                    val total = viewModel.selectedContacts.size + 1 // 加上当前用户自己

                    if (total == 2) { // 1 对 1
                        coroutineScope.launch {
                            viewModel.createOrGetConversation()
                        }
                    } else if (total > 2) { // 群聊
                        // 自动生成群名：自己不用显示
                        val others = viewModel.selectedContacts
                        viewModel.newGroupName = others.joinToString("、") { it.username }

                        viewModel.showGroupNameDialog.value = true
                    }
                }
            )
        }

        // 群聊名称弹窗
        if (viewModel.showGroupNameDialog.value) {
            GroupNameDialog(
                viewModel = viewModel,
                onConfirm = { groupName ->
                    viewModel.showGroupNameDialog.value = false
                    coroutineScope.launch {
                        viewModel.createOrGetConversation(groupName)
                    }
                },
                onCancel = { viewModel.showGroupNameDialog.value = false }
            )
        }
    }
}
