package com.example.flybook.ui.contact

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.flybook.model.User
import com.example.flybook.viewmodel.HomeViewModel

@Composable
fun ContactDialog(viewModel: HomeViewModel, onCreateGroup: (List<User>) -> Unit) {
    val currentUserId = 1000L  // 当前用户 ID

    AlertDialog(
        onDismissRequest = { viewModel.showContactDialog.value = false },
        title = { Text("选择联系人") },
        text = {
            LazyColumn {
                items(viewModel.contacts.size) { index ->
                    val user = viewModel.contacts[index]
                    val isCurrentUser = user.id == currentUserId
                    val selected = if (isCurrentUser) true else viewModel.selectedContacts.contains(user)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable(enabled = !isCurrentUser) {
                                if (!isCurrentUser) {
                                    if (selected) viewModel.selectedContacts.remove(user)
                                    else viewModel.selectedContacts.add(user)
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selected,
                            onCheckedChange = null, // 交互由 Row click 处理
                            enabled = !isCurrentUser
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(user.username + if (isCurrentUser) " (Me)" else "")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onCreateGroup(viewModel.selectedContacts) }) {
                Text("创建群聊")
            }
        },
        dismissButton = {
            Button(onClick = { viewModel.showContactDialog.value = false }) {
                Text("取消")
            }
        }
    )

}
