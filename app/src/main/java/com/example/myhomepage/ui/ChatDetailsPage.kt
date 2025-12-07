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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhomepage.R
import com.example.myhomepage.WeViewModel
import com.example.myhomepage.data.Msg
import com.example.myhomepage.data.User
import com.example.myhomepage.ui.theme.WeComposeTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
data class ChatDetails(val userId: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailsPage(viewModel: WeViewModel, userId: String) {
    val chat = viewModel.chats.find { it.friend.id == userId }

    if (chat == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("会话不存在或加载中...")
        }
        return
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

    // 1. 【新增】列表状态，用于控制滚动
    val listState = rememberLazyListState()

    // 2. 【新增】当消息数量变化时（收到新消息/发消息），自动滚动到底部
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
                .weight(1f) // 占据剩余空间，当底部被键盘顶起时，这里会自动缩小
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
                state = listState, // 绑定状态
                modifier = Modifier
                    .fillMaxSize()
                    .offset(shakingOffset.value.dp, shakingOffset.value.dp)
            ) {
                items(chat.msgs.size) { index ->
                    val msg = chat.msgs[index]
                    msg.apply { read = true }
                    MessageItem(msg, shakingTime, chat.msgs.size - index - 1)
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
            .imePadding() // 3. 【关键新增】增加 imePadding，让输入栏随键盘顶起，而不是整个页面顶起
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
fun MessageItem(msg: Msg, shakingTime: Int, shakingLevel: Int) {
    val shakingAngleBubble = remember { Animatable(0f) }
    if (msg.from == User.Me) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            val bubbleColor = WeComposeTheme.colors.bubbleMe
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
                                10.dp.toPx(),
                                0f,
                                size.width - 10.dp.toPx(),
                                size.height,
                                4.dp.toPx(),
                                4.dp.toPx()
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
                color = WeComposeTheme.colors.textPrimaryMe)
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
                                10.dp.toPx(),
                                0f,
                                size.width - 10.dp.toPx(),
                                size.height,
                                4.dp.toPx(),
                                4.dp.toPx()
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
                color = WeComposeTheme.colors.textPrimary)
        }
    }
}

@Composable
fun EmojiGridView(
    emojiList: List<String>,
    onEmojiClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // 网格展示Emoji（4列布局，贴近微信）
        LazyVerticalGrid(
            columns = GridCells.Fixed(4), // 4列
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(emojiList) { emoji ->
                // Emoji项：可点击，放大显示
                Text(
                    text = emoji,
                    fontSize = 32.sp, // Emoji大小
                    modifier = Modifier
                        .clickable { onEmojiClick(emoji) }
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}