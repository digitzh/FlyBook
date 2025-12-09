package com.example.myhomepage.ui

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhomepage.R
import com.example.myhomepage.WeViewModel
import com.example.myhomepage.data.Msg
import com.example.myhomepage.data.User
import com.example.myhomepage.share.TodoShareCard
import com.example.myhomepage.ui.theme.WeComposeTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ChatDetails(val userId: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailsPage(
    viewModel: WeViewModel,
    userId: String,
    onTodoCardClick: (TodoShareCard) -> Unit // 【新增】卡片点击回调
) {
    val chat = viewModel.chats.find { it.friend.id == userId }

    if (chat == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("会话不存在或加载中...")
        }
        return
    }

    LaunchedEffect(chat.conversationId) {
        chat.conversationId?.let { cid ->
            viewModel.clearUnreadMessage(cid)
        }
    }

    LaunchedEffect(chat.conversationId) {
        chat.conversationId?.let { cid ->
            viewModel.syncChatHistory(cid)
        }
    }

    LaunchedEffect(Unit) {
        chat.unreadCount = 0
    }

    var shakingTime by remember { mutableIntStateOf(0) }
    var showEmojiSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    val coroutineScope = rememberCoroutineScope()
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    val listState = rememberLazyListState()

    LaunchedEffect(chat.msgs.size) {
        if (chat.msgs.isNotEmpty()) {
            listState.animateScrollToItem(chat.msgs.size - 1)
        }
    }

    Column(
        Modifier
            .background(WeComposeTheme.colors.background)
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        WeTopBar(chat.displayName, onBack = { backDispatcher?.onBackPressed() })

        Box(
            Modifier
                .background(WeComposeTheme.colors.chatPage)
                .weight(1f)
        ) {
            val shakingOffset = remember { Animatable(0f) }
            LaunchedEffect(shakingTime) {
                if (shakingTime != 0) {
                    shakingOffset.animateTo(
                        0f,
                        animationSpec = spring(0.3f, 600f),
                        initialVelocity = -2000f
                    ) { }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .offset(shakingOffset.value.dp, shakingOffset.value.dp)
            ) {
                items(chat.msgs.size) { index ->
                    val msg = chat.msgs[index]
                    msg.apply { read = true }
                    MessageItem(msg, shakingTime, chat.msgs.size - index - 1, onTodoCardClick)
                }
            }
        }
        ChatBottomBar(onBombClicked = { editText -> viewModel.boom(chat, editText) },
            onOtherClicked = { showEmojiSheet = true })

        if (showEmojiSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    coroutineScope.launch { sheetState.hide() }
                        .invokeOnCompletion { showEmojiSheet = false }
                },
                sheetState = sheetState,
                containerColor = Color(0xFFF5F5F5),
                content = {
                    EmojiGridView(
                        emojiList = listOf("\uD83D\uDCA3", "\uD83D\uDC4C", "\uD83D\uDC4D", "\uD83D\uDC36"),
                        onEmojiClick = { emoji ->
                            viewModel.boom(chat, emoji)
                            if (emoji == "\uD83D\uDCA3") shakingTime++
                            showEmojiSheet = false
                        },
                        modifier = Modifier.padding(16.dp)
                    )
                }
            )
        }
    }
}

@Composable
fun ChatBottomBar(onBombClicked: (String) -> Unit, onOtherClicked: () -> Unit) {
    var editingText by remember { mutableStateOf("") }
    Row(
        Modifier
            .fillMaxWidth()
            .background(WeComposeTheme.colors.bottomBar)
            .padding(4.dp, 0.dp)
            .navigationBarsPadding()
            .imePadding()
    ) {
        Icon(
            painterResource(R.drawable.ic_voice),
            contentDescription = null,
            Modifier
                .align(Alignment.CenterVertically)
                .padding(4.dp)
                .size(28.dp),
            tint = WeComposeTheme.colors.icon
        )
        BasicTextField(
            editingText, { editingText = it },
            Modifier
                .weight(1f)
                .padding(4.dp, 8.dp)
                .height(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(WeComposeTheme.colors.textFieldBackground)
                .padding(start = 8.dp, top = 10.dp, end = 8.dp),
            cursorBrush = SolidColor(WeComposeTheme.colors.textPrimary)
        )
        Text(
            "发送",
            Modifier
                .clickable {
                    if (editingText.isNotBlank()) {
                        onBombClicked(editingText)
                        editingText = ""
                    }
                }
                .padding(4.dp)
                .align(Alignment.CenterVertically),
            fontSize = 20.sp,
            color = WeComposeTheme.colors.textPrimary
        )
        Icon(
            painterResource(R.drawable.ic_add),
            contentDescription = null,
            Modifier
                .align(Alignment.CenterVertically)
                .padding(4.dp)
                .size(28.dp)
                .clickable { onOtherClicked() },
            tint = WeComposeTheme.colors.icon
        )
    }
}

@Composable
fun MessageItem(msg: Msg, shakingTime: Int, shakingLevel: Int, onTodoCardClick: (TodoShareCard) -> Unit = {}) {
    val shakingAngleBubble = remember { Animatable(0f) }
    val isMe = msg.from == User.Me

    if (isMe) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            val bubbleColor = WeComposeTheme.colors.bubbleMe

            if (msg.type == 5) {
                // 【新增】渲染卡片消息
                TodoMessageCard(msg.text, bubbleColor, onTodoCardClick)
            } else {
                // 渲染文本消息 (保留原有气泡效果)
                Text(
                    "${msg.text}",
                    Modifier
                        .graphicsLayer(
                            rotationZ = shakingAngleBubble.value,
                            transformOrigin = TransformOrigin(1f, 0f)
                        )
                        .drawBehind {
                            val bubble = Path().apply {
                                val rect = RoundRect(
                                    10.dp.toPx(), 0f, size.width - 10.dp.toPx(), size.height, 4.dp.toPx(), 4.dp.toPx()
                                )
                                addRoundRect(rect)
                                moveTo(size.width - 10.dp.toPx(), 15.dp.toPx())
                                lineTo(size.width - 5.dp.toPx(), 20.dp.toPx())
                                lineTo(size.width - 10.dp.toPx(), 25.dp.toPx())
                                close()
                            }
                            drawPath(bubble, bubbleColor)
                        }
                        .padding(20.dp, 10.dp),
                    color = WeComposeTheme.colors.textPrimaryMe
                )
            }

            Image(
                painterResource(msg.from.avatar),
                contentDescription = msg.from.name,
                Modifier
                    .graphicsLayer(
                        rotationZ = shakingAngleBubble.value * 0.6f,
                        transformOrigin = TransformOrigin(1f, 0f)
                    )
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    } else {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Image(
                painterResource(msg.from.avatar),
                contentDescription = msg.from.name,
                Modifier
                    .graphicsLayer(
                        rotationZ = -shakingAngleBubble.value * 0.6f,
                        transformOrigin = TransformOrigin(0f, 0f)
                    )
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
            val bubbleColor = WeComposeTheme.colors.bubbleOthers

            if (msg.type == 5) {
                // 【新增】渲染卡片消息
                TodoMessageCard(msg.text, bubbleColor, onTodoCardClick)
            } else {
                // 渲染文本消息
                Text(
                    "${msg.text}",
                    Modifier
                        .graphicsLayer(
                            rotationZ = -shakingAngleBubble.value,
                            transformOrigin = TransformOrigin(0f, 0f)
                        )
                        .drawBehind {
                            val bubble = Path().apply {
                                val rect = RoundRect(
                                    10.dp.toPx(), 0f, size.width - 10.dp.toPx(), size.height, 4.dp.toPx(), 4.dp.toPx()
                                )
                                addRoundRect(rect)
                                moveTo(10.dp.toPx(), 15.dp.toPx())
                                lineTo(5.dp.toPx(), 20.dp.toPx())
                                lineTo(10.dp.toPx(), 25.dp.toPx())
                                close()
                            }
                            drawPath(bubble, bubbleColor)
                        }
                        .padding(20.dp, 10.dp),
                    color = WeComposeTheme.colors.textPrimary
                )
            }
        }
    }
}

// 【新增】待办卡片 UI 组件
@Composable
fun TodoMessageCard(jsonStr: String, bgColor: Color, onClick: (TodoShareCard) -> Unit) {
    // 尝试解析 JSON
    val card = remember(jsonStr) { // 使用 remember 避免重组时重复解析
        try {
            // 第一次尝试：直接解析
            Json { ignoreUnknownKeys = true }.decodeFromString<TodoShareCard>(jsonStr)
        } catch (e1: Exception) {
            try {
                // 第二次尝试：如果是被转义的字符串 (e.g. "{\"id\":1}"), 先解成 String 再解对象
                val unescaped = Json.decodeFromString<String>(jsonStr)
                Json { ignoreUnknownKeys = true }.decodeFromString<TodoShareCard>(unescaped)
            } catch (e2: Exception) {
                null
            }
        }
    }

    if (card != null) {
        // ... 渲染逻辑保持不变 ...
        Column(
            Modifier
                .width(220.dp)
                .background(bgColor, RoundedCornerShape(8.dp))
                .clickable { onClick(card) }
                .padding(12.dp)
        ) {
            Text(card.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WeComposeTheme.colors.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(card.description, maxLines = 2, fontSize = 12.sp, overflow = TextOverflow.Ellipsis, color = WeComposeTheme.colors.textSecondary)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(8.dp).clip(CircleShape).background(if(card.done) Color.Green else Color.Red)
                )
                Spacer(Modifier.width(4.dp))
                Text(if(card.done) "已完成" else "进行中", fontSize = 10.sp, color = WeComposeTheme.colors.textSecondary)
            }
        }
    } else {
        // 调试用：显示原始字符串，方便看哪里不对
        Text("[解析错误] $jsonStr", Modifier.background(bgColor).padding(8.dp), color = Color.Red, fontSize = 10.sp)
    }
}

@Composable
fun EmojiGridView(
    emojiList: List<String>,
    onEmojiClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(emojiList) { emoji ->
                Text(
                    text = emoji,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .clickable { onEmojiClick(emoji) }
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}