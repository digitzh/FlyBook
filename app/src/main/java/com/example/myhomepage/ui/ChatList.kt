package com.example.myhomepage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
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
    var showCompleteButton by remember { mutableStateOf(false) }
    var buttonOffset by remember { mutableStateOf(IntOffset.Zero) }
    var unShowIndex by remember { mutableStateOf(-1) }

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
                if(chat.isshow) {
                    if (index > 0) {
                        HorizontalDivider(
                            Modifier.padding(start = 68.dp),
                            color = WeComposeTheme.colors.divider,
                            thickness = 0.8f.dp
                        )
                    }
                    ChatListItem(chat, Modifier,{onChatClick(chat)},{offset ->
                        unShowIndex = index
                        buttonOffset = offset
                        showCompleteButton = true
                    })
                }
            }
        }
    }

    if (showCompleteButton) {
        Popup(
            // 按钮偏移位置（基于长按坐标）
            offset = buttonOffset,
            // 点击Popup外部不自动隐藏（靠外层空白点击隐藏）
            onDismissRequest = { showCompleteButton = false }
        ) {
            CompleteButton(
                onClick = {
                    // 完成按钮点击逻辑（实现）
                    showCompleteButton = false
                    if(chats[unShowIndex].msgs.last().read)
                        chats[unShowIndex].isshow = false
                }
            )
        }
    }

    if (showCreateDialog) {
        CreateConversationDialog(
            users = users,
            currentUser = viewModel.currentUser,
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


@Composable
private fun ChatListItem(chat: Chat, modifier: Modifier = Modifier,
                         onClick : () -> Unit,
                         onLongPress : (IntOffset)->Unit) {
    val hasMessages = chat.msgs.isNotEmpty()
    val lastMsg = if (hasMessages) chat.msgs.last() else null
    val hasUnread = lastMsg?.read == false

    // 保存Row的全局布局信息（位置+尺寸）
    var rowGlobalPosition by remember { mutableStateOf(Offset.Zero) }
    var rowSize by remember { mutableStateOf(IntOffset.Zero) }

    // 计算Row正中间的坐标（并调整按钮位置，避免按钮中心和Row中心重叠）
    val rowCenterOffset by remember(rowGlobalPosition, rowSize) {
        mutableStateOf(
            IntOffset(
                // Row左边界 + Row宽度/2 - 按钮宽度/2（让按钮水平居中）
                x = rowGlobalPosition.x.toInt() + (rowSize.x / 2) - ( 60 / 2),
                // Row上边界 + Row高度/2 - 按钮高度/2
                y = rowGlobalPosition.y.toInt() -33 //+ (rowSize.x / 2)
            )
        )
    }
    Row(
        modifier.fillMaxWidth()
        .onGloballyPositioned { layoutCoordinates ->
            // 获取Row在屏幕中的全局坐标（IntOffset）
            rowGlobalPosition = layoutCoordinates.positionInWindow()
            // 获取Row的尺寸（宽度x, 高度y）
            rowSize = IntOffset(layoutCoordinates.size.width, layoutCoordinates.size.height)
        }
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {onClick()},
                onLongPress = { offset ->
                    onLongPress(rowCenterOffset)
                }
            )
        },
    ) {
        // 群聊显示群头像，单聊显示好友头像
        if (chat.isGroupChat && chat.avatarUrl != null) {
            AsyncImage(
                model = chat.avatarUrl,
                contentDescription = chat.displayName,
                modifier = Modifier
                    .padding(8.dp)
                    .size(48.dp)
                    .unread(hasUnread, WeComposeTheme.colors.badge)
                    .clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(chat.friend.avatar)
            )
        } else {
            Image(
                painterResource(chat.friend.avatar), chat.friend.name,
                Modifier
                    .padding(8.dp)
                    .size(48.dp)
                    .unread(hasUnread, WeComposeTheme.colors.badge)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
        Column(
            Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                chat.displayName,
                fontSize = 17.sp,
                color = WeComposeTheme.colors.textPrimary
            )
            Text(
                text = lastMsg?.text ?: "暂无消息",
                fontSize = 14.sp,
                color = WeComposeTheme.colors.textSecondary
            )
        }
        lastMsg?.let { msg ->
            Text(
                msg.time,
                Modifier.padding(8.dp, 8.dp, 12.dp, 8.dp),
                fontSize = 11.sp,
                color = WeComposeTheme.colors.textSecondary
            )
        }
    }
}


@Composable
fun CompleteButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(58.dp) // 圆形按钮大小
            .background(
                color = Color(0xFF90EE90), // 浅绿色（淡绿）
                shape = CircleShape // 圆形
            )
            .clickable { onClick() } // 点击事件
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // 对号图标
            Text(
                text = "\u2714",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
            Spacer(modifier = Modifier.width(2.dp))
            // 完成文字
            Text(
                text = "完成",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}


fun Modifier.unread(show: Boolean, color: Color) = drawWithContent {
  drawContent()
  if (show) {
    drawCircle(color, 5.dp.toPx(), Offset(size.width - 1.dp.toPx(), 1.dp.toPx()))
  }
}