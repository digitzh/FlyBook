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

    // 【修改】自动命名逻辑
    LaunchedEffect(selectedUserIds) {
        if (!isNameModified) {
            // 1. 找出所有被选中的用户对象
            val selectedUsers = users.filter { selectedUserIds.contains(it.userId) }

            // 2. 计算人数（排除自己）
            val otherUsers = selectedUsers.filter { it.userId != currentUserId }

            if (otherUsers.size == 1) {
                // 情况A：双人聊天 (P2P) -> 默认名为对方名字
                // 注意：对于P2P，服务端通常会忽略这个name，而是动态显示对方名字
                conversationName = otherUsers.first().username
            } else if (selectedUsers.isNotEmpty()) {
                // 情况B：多人聊天 (Group) -> 包含【自己】的所有人名拼接
                // 【修改】这里不再排除 currentUserId，而是使用 selectedUsers 全部
                // 为了让名字好看点，可以把“自己”放在第一个或者最后一个，或者按原列表顺序
                // 这里按 sortedUsers 的顺序来拼，体验更好
                val sortedSelectedNames = sortedUsers
                    .filter { selectedUserIds.contains(it.userId) }
                    .map { it.username }

                conversationName = sortedSelectedNames.joinToString("、")
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
                        // 强制使用群聊模式 (Type=2) 以兼容服务端限制
                        val type = 2

                        // 【关键】对于双人聊天，如果不希望写死群名导致“双方都看对方叫李四”，
                        // 可以在这里做个判断：如果是双人且没改过名，传空字符串？
                        // 但由于我们暂时强制 Type=2，服务端会把它当群聊处理，所以名字必须固定。
                        // 如果您希望完美的 P2P 体验（双方看对方名字），服务端必须支持 Type=1。
                        // 在当前 Type=2 的限制下，这是最优解：群名就是“张三、李四”。

                        // 修正逻辑：如果只有两个人，也用“张三、李四”这种组合名，
                        // 这样张三看到群名叫“张三、李四”，李四看到也叫“张三、李四”，这是群聊的正常表现。
                        // 之前的问题是只用了“李四”做群名。

                        // 重新生成一次名字以确保逻辑一致 (如果用户没改)
                        val finalName = if (!isNameModified) {
                            val sortedSelectedNames = sortedUsers
                                .filter { selectedUserIds.contains(it.userId) }
                                .map { it.username }
                            sortedSelectedNames.joinToString("、")
                        } else {
                            conversationName
                        }

                        onCreateConversation(finalName, selectedUserIds.toList(), type)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    enabled = selectedUserIds.isNotEmpty(), // 只要有人就能建（自己也算人）
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
