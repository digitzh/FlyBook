package com.example.myhomepage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.myhomepage.database.UserEntity
import com.example.myhomepage.ui.theme.WeComposeTheme

@Composable
fun CreateConversationDialog(
    users: List<UserEntity>,
    currentUserId: Long?,
    onDismiss: () -> Unit,
    onCreateConversation: (String, List<Long>, Int) -> Unit
) {
    var conversationName by remember { mutableStateOf("") }
    var isNameModified by remember { mutableStateOf(false) }

    var selectedUserIds by remember {
        mutableStateOf<Set<Long>>(
            if (currentUserId != null) setOf(currentUserId) else emptySet()
        )
    }

    val sortedUsers = remember(users, currentUserId) {
        if (currentUserId == null) {
            users.sortedBy { it.username }
        } else {
            val currentUserItem = users.find { it.userId == currentUserId }
            val otherUsers = users.filter { it.userId != currentUserId }.sortedBy { it.username }
            if (currentUserItem != null) {
                listOf(currentUserItem) + otherUsers
            } else {
                otherUsers
            }
        }
    }

    // 自动命名逻辑：拼接所有选中成员的名字
    LaunchedEffect(selectedUserIds) {
        if (!isNameModified) {
            // 找到所有选中的用户（按列表顺序）
            val selectedNames = sortedUsers
                .filter { selectedUserIds.contains(it.userId) }
                .map { it.username }

            if (selectedNames.isNotEmpty()) {
                conversationName = selectedNames.joinToString("、")
            } else {
                conversationName = ""
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(
                    WeComposeTheme.colors.background,
                    shape = RoundedCornerShape(16.dp)
                )
                .fillMaxWidth(0.9f)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "创建群聊",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = WeComposeTheme.colors.textPrimary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 会话名称输入框
            OutlinedTextField(
                value = conversationName,
                onValueChange = {
                    conversationName = it
                    isNameModified = true
                },
                label = { Text("群聊名称", color = WeComposeTheme.colors.meList) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = WeComposeTheme.colors.textPrimary,
                    unfocusedTextColor = WeComposeTheme.colors.textSecondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "选择成员",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = WeComposeTheme.colors.textPrimary,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(
                        WeComposeTheme.colors.listItem,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                items(sortedUsers) { user ->
                    val isCurrentUser = user.userId == currentUserId
                    UserSelectItem(
                        user = user,
                        isSelected = selectedUserIds.contains(user.userId),
                        isDisabled = isCurrentUser,
                        onToggle = {
                            if (!isCurrentUser) {
                                selectedUserIds = if (selectedUserIds.contains(user.userId)) {
                                    selectedUserIds - user.userId
                                } else {
                                    selectedUserIds + user.userId
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WeComposeTheme.colors.textSecondary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("取消", fontSize = 16.sp)
                }

                Button(
                    onClick = {
                        // 【修改】强制使用 Type=2 (群聊)
                        val type = 2
                        onCreateConversation(conversationName, selectedUserIds.toList(), type)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = conversationName.isNotBlank() && selectedUserIds.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WeComposeTheme.colors.meList
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("创建", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun UserSelectItem(
    user: UserEntity,
    isSelected: Boolean,
    isDisabled: Boolean = false,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isDisabled, onClick = onToggle)
            .padding(vertical = 8.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = user.avatarUrl ?: "",
            contentDescription = user.username,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop,
            error = androidx.compose.ui.res.painterResource(com.example.myhomepage.R.drawable.avatar_me)
        )

        Spacer(modifier = Modifier.size(12.dp))

        Text(
            text = user.username,
            fontSize = 16.sp,
            color = if (isDisabled) WeComposeTheme.colors.textSecondary else WeComposeTheme.colors.textPrimary,
            modifier = Modifier.weight(1f)
        )

        Checkbox(
            checked = isSelected,
            enabled = !isDisabled,
            onCheckedChange = { if (!isDisabled) onToggle() }
        )
    }
}
