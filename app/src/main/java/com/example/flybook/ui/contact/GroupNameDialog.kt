package com.example.flybook.ui.contact

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import com.example.flybook.viewmodel.HomeViewModel

@Composable
fun GroupNameDialog(
    viewModel: HomeViewModel,
    onConfirm: (String) -> Unit,  // 普通回调
    onCancel: () -> Unit
) {
    var groupName by remember { mutableStateOf(viewModel.newGroupName) }

    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text("输入群聊名称") },
        text = {
            TextField(
                value = groupName,
                onValueChange = { groupName = it },
                placeholder = { Text("群聊名称") },
                singleLine = true
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(groupName) }) {
                Text("确定")
            }
        },
        dismissButton = {
            Button(onClick = onCancel) {
                Text("取消")
            }
        }
    )
}
