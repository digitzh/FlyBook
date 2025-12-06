package com.example.myhomepage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import coil.compose.AsyncImage
import com.example.myhomepage.data.Chat
import com.example.myhomepage.ui.theme.WeComposeTheme

@Composable
fun ChatList(
    chats: List<Chat>,
    onChatClick: (Chat) -> Unit,
    viewModel: com.example.myhomepage.WeViewModel
) {
    var showCreateDialog by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(false)
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
                ChatListItem(chat, Modifier.clickable { onChatClick(chat) })
            }
        }
    }

    if (showCreateDialog) {
        CreateConversationDialog(
            users = users,
            // 【修改】这里改为传入 currentUserId (Long 类型)
            currentUserId = viewModel.currentUserId?.toLongOrNull(),
            onDismiss = { showCreateDialog = false },
            onCreateConversation = { name, selectedUserIds ->
                viewModel.createGroupConversation(
                    name = name,
                    selectedUserIds = selectedUserIds,
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
}

// ... ChatListItem 和 unread 函数保持不变 ...
//@Composable
//private fun ChatListItem(chat: Chat, modifier: Modifier = Modifier) {
//    val hasMessages = chat.msgs.isNotEmpty()
//    val lastMsg = if (hasMessages) chat.msgs.last() else null
//    val hasUnread = lastMsg?.read == false
//
//    Row(
//        modifier.fillMaxWidth()
//    ) {
//        // 群聊显示群头像，单聊显示好友头像
//        if (chat.isGroupChat && chat.avatarUrl != null) {
//            AsyncImage(
//                model = chat.avatarUrl,
//                contentDescription = chat.displayName,
//                modifier = Modifier
//                    .padding(8.dp)
//                    .size(48.dp)
//                    .unread(hasUnread, WeComposeTheme.colors.badge)
//                    .clip(RoundedCornerShape(4.dp)),
//                contentScale = ContentScale.Crop,
//                error = painterResource(chat.friend.avatar)
//            )
//        } else {
//            Image(
//                painterResource(chat.friend.avatar), chat.friend.name,
//                Modifier
//                    .padding(8.dp)
//                    .size(48.dp)
//                    .unread(hasUnread, WeComposeTheme.colors.badge)
//                    .clip(RoundedCornerShape(4.dp))
//            )
//        }
//        Column(
//            Modifier
//                .weight(1f)
//                .align(Alignment.CenterVertically)
//        ) {
//            Text(chat.displayName, fontSize = 17.sp, color = WeComposeTheme.colors.textPrimary)
//            Text(
//                text = lastMsg?.text ?: "暂无消息",
//                fontSize = 14.sp,
//                color = WeComposeTheme.colors.textSecondary
//            )
//        }
//        lastMsg?.let { msg ->
//            Text(
//                msg.time,
//                Modifier.padding(8.dp, 8.dp, 12.dp, 8.dp),
//                fontSize = 11.sp,
//                color = WeComposeTheme.colors.textSecondary
//            )
//        }
//    }
//}
// ... ChatList 函数不变 ...

//@Composable
// ... ChatList 函数不变 ...

@Composable
private fun ChatListItem(chat: Chat, modifier: Modifier = Modifier) {
    // 【修改】优先使用 lastContent，解决“只显示最后一条，中间的没了”问题
    val displayContent = chat.lastContent ?: (if (chat.msgs.isNotEmpty()) chat.msgs.last().text else "暂无消息")
    val displayTime = chat.lastTime ?: (if (chat.msgs.isNotEmpty()) chat.msgs.last().time else "")
    val hasUnread = chat.unreadCount > 0

    Row(modifier.fillMaxWidth()) {
        // 头像部分不变
        if (chat.isGroupChat && chat.avatarUrl != null) {
            AsyncImage(model = chat.avatarUrl, contentDescription = chat.displayName, modifier = Modifier.padding(8.dp).size(48.dp).unread(hasUnread, WeComposeTheme.colors.badge).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop, error = painterResource(chat.friend.avatar))
        } else {
            Image(painterResource(chat.friend.avatar), chat.friend.name, Modifier.padding(8.dp).size(48.dp).unread(hasUnread, WeComposeTheme.colors.badge).clip(RoundedCornerShape(4.dp)))
        }

        Column(Modifier.weight(1f).align(Alignment.CenterVertically)) {
            Text(chat.displayName, fontSize = 17.sp, color = WeComposeTheme.colors.textPrimary)
            Text(text = displayContent, fontSize = 14.sp, color = WeComposeTheme.colors.textSecondary, maxLines = 1)
        }
        Text(displayTime, Modifier.padding(8.dp, 8.dp, 12.dp, 8.dp), fontSize = 11.sp, color = WeComposeTheme.colors.textSecondary)
    }
}

// unread 函数不变 ...


fun Modifier.unread(show: Boolean, color: Color) = drawWithContent {
    drawContent()
    if (show) {
        drawCircle(color, 5.dp.toPx(), Offset(size.width - 1.dp.toPx(), 1.dp.toPx()))
    }
}
