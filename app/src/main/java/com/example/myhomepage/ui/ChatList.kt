package com.example.myhomepage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.myhomepage.data.Chat
import com.example.myhomepage.ui.theme.WeComposeTheme

@Composable
fun ChatList(
    chats: List<Chat>,
    onChatClick: (Chat) -> Unit,
    viewModel: com.example.myhomepage.WeViewModel
) {
    var showCreateDialog by remember {
        mutableStateOf(false)
    }
    var selectedChatForMenu by remember {
        mutableStateOf<Chat?>(null)
    }
    val users by viewModel.users.collectAsState(initial = emptyList())

    Column(Modifier
        .background(WeComposeTheme.colors.background)
        .fillMaxSize()) {
        WeTopBar(
            title = "Flybook",
            onLeftAction = { showCreateDialog = true },
            leftActionIcon = com.example.myhomepage.R.drawable.ic_add
        )
        LazyColumn(Modifier.background(WeComposeTheme.colors.listItem)) {
            itemsIndexed(chats) { index, chat ->
                if (index > 0) {
                    HorizontalDivider(
                        Modifier.padding(start = 68.dp),
                        color = WeComposeTheme.colors.divider,
                        thickness = 0.8f.dp
                    )
                }
                ChatListItem(
                    chat, 
                    Modifier.combinedClickable(
                        onClick = { onChatClick(chat) },
                        onLongClick = { selectedChatForMenu = chat }
                    )
                )
            }
        }
    }

    if (showCreateDialog) {
        CreateConversationDialog(
            users = users,
            currentUserId = viewModel.currentUserId?.toLongOrNull(),
            onDismiss = { showCreateDialog = false },
            // 【修改】接收 type
            onCreateConversation = { name, selectedUserIds, type ->
                viewModel.createGroupConversation(
                    name = name,
                    selectedUserIds = selectedUserIds,
                    type = type, // 传递 type
                    onSuccess = {
                        showCreateDialog = false
                    },
                    onError = { error ->
                        android.util.Log.e("ChatList", "Create conversation error: $error")
                        showCreateDialog = false
                    }
                )
            }
        )
    }

    // 长按菜单对话框
    selectedChatForMenu?.let { chat ->
        ChatLongPressMenu(
            chat = chat,
            onDismiss = { selectedChatForMenu = null },
            onMarkAsRead = {
                chat.conversationId?.let { cid ->
                    viewModel.clearUnreadCount(cid)
                }
                selectedChatForMenu = null
            },
            onToggleTop = {
                chat.conversationId?.let { cid ->
                    viewModel.setConversationTop(cid, !chat.isTop)
                }
                selectedChatForMenu = null
            }
        )
    }
}

@Composable
private fun ChatListItem(chat: Chat, modifier: Modifier = Modifier) {
    val displayContent = chat.lastContent ?: (if (chat.msgs.isNotEmpty()) chat.msgs.last().text else "暂无消息")
    val displayTime = chat.lastTime ?: (if (chat.msgs.isNotEmpty()) chat.msgs.last().time else "")
    val hasUnread = chat.unreadCount > 0

    Row(modifier.fillMaxWidth()) {
        if (chat.isGroupChat) {
            // 群聊：使用静态图标
            Image(
                painterResource(com.example.myhomepage.R.drawable.ic_contact_group),
                chat.displayName,
                Modifier.padding(8.dp).size(48.dp).unread(hasUnread, WeComposeTheme.colors.badge).clip(RoundedCornerShape(4.dp))
            )
        } else {
            Image(painterResource(chat.friend.avatar), chat.friend.name, Modifier.padding(8.dp).size(48.dp).unread(hasUnread, WeComposeTheme.colors.badge).clip(RoundedCornerShape(4.dp)))
        }

        Column(Modifier.weight(1f).align(Alignment.CenterVertically)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    chat.displayName, 
                    fontSize = 17.sp, 
                    color = WeComposeTheme.colors.textPrimary
                )
                // 置顶标记
                if (chat.isTop) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = "置顶",
                        modifier = Modifier
                            .size(14.dp)
                            .padding(start = 4.dp),
                        tint = WeComposeTheme.colors.meList
                    )
                }
            }
            Text(text = displayContent, fontSize = 14.sp, color = WeComposeTheme.colors.textSecondary, maxLines = 1)
        }
        Text(displayTime, Modifier.padding(8.dp, 8.dp, 12.dp, 8.dp), fontSize = 11.sp, color = WeComposeTheme.colors.textSecondary)
    }
}

fun Modifier.unread(show: Boolean, color: Color) = drawWithContent {
    drawContent()
    if (show) {
        drawCircle(color, 5.dp.toPx(), Offset(size.width - 1.dp.toPx(), 1.dp.toPx()))
    }
}

@Composable
fun ChatLongPressMenu(
    chat: Chat,
    onDismiss: () -> Unit,
    onMarkAsRead: () -> Unit,
    onToggleTop: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = WeComposeTheme.colors.listItem
            )
        ) {
            Column {
                // 标为已读
                if (chat.unreadCount > 0) {
                    TextButton(
                        onClick = onMarkAsRead,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "标为已读",
                            fontSize = 16.sp,
                            color = WeComposeTheme.colors.textPrimary,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    HorizontalDivider(color = WeComposeTheme.colors.divider, thickness = 0.5f.dp)
                }
                
                // 置顶/取消置顶
                TextButton(
                    onClick = onToggleTop,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (chat.isTop) "取消置顶" else "置顶聊天",
                        fontSize = 16.sp,
                        color = WeComposeTheme.colors.textPrimary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}
