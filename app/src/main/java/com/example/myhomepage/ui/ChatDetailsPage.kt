package com.example.myhomepage.ui

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhomepage.R
import com.example.myhomepage.WeViewModel
import com.example.myhomepage.data.Msg
import com.example.myhomepage.data.User
import com.example.myhomepage.network.ImageContent
import com.example.myhomepage.network.VideoContent
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
    onTodoCardClick: (TodoShareCard) -> Unit,
    onImageClick: () -> Unit // 【新增】回调
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
            viewModel.syncChatHistory(cid)
            // 清除未读消息数
            viewModel.clearUnreadCount(cid)
        }
    }

    var shakingTime by remember { mutableIntStateOf(0) }
    var bottomSheetType by remember { mutableIntStateOf(0) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val listState = rememberLazyListState()

    val photoPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { chat.conversationId?.let { cid -> viewModel.sendImage(cid, it) } }
    }
    val videoPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { chat.conversationId?.let { cid -> viewModel.sendVideo(cid, it) } }
    }

    LaunchedEffect(chat.msgs.size) {
        if (chat.msgs.isNotEmpty()) {
            listState.animateScrollToItem(chat.msgs.size - 1)
        }
    }

    Column(Modifier.background(WeComposeTheme.colors.background).fillMaxSize().statusBarsPadding()) {
        WeTopBar(chat.displayName, onBack = { backDispatcher?.onBackPressed() })

        Box(Modifier.background(WeComposeTheme.colors.chatPage).weight(1f)) {
            val shakingOffset = remember { Animatable(0f) }
            LaunchedEffect(shakingTime) {
                if (shakingTime != 0) {
                    shakingOffset.animateTo(0f, animationSpec = spring(0.3f, 600f), initialVelocity = -2000f) { }
                }
            }

            LazyColumn(state = listState, modifier = Modifier.fillMaxSize().offset(shakingOffset.value.dp, shakingOffset.value.dp)) {
                items(chat.msgs.size) { index ->
                    val msg = chat.msgs[index]
                    msg.apply { read = true }
                    // 【修改】传入 onImageClick
                    MessageItem(
                        msg, shakingTime, chat.msgs.size - index - 1,
                        onTodoCardClick,
                        onImageClick = { base64 ->
                            viewModel.currentPreviewImageBase64 = base64
                            onImageClick()
                        }
                    )
                }
            }
        }

        ChatBottomBar(
            onBombClicked = { editText -> viewModel.boom(chat, editText) },
            onEmojiClicked = { bottomSheetType = 1 },
            onMoreClicked = { bottomSheetType = 2 }
        )

        if (bottomSheetType != 0) {
            ModalBottomSheet(
                onDismissRequest = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion { bottomSheetType = 0 }
                },
                sheetState = sheetState,
                containerColor = Color(0xFFF5F5F5),
                content = {
                    if (bottomSheetType == 1) {
                        EmojiGridView(
                            emojiList = listOf("\uD83D\uDCA3", "\uD83D\uDC4C", "\uD83D\uDC4D", "\uD83D\uDC36"),
                            onEmojiClick = { emoji ->
                                viewModel.boom(chat, emoji)
                                if (emoji == "\uD83D\uDCA3") shakingTime++
                                bottomSheetType = 0
                            },
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        MediaSelectPanel(
                            onPhotoClick = {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                bottomSheetType = 0
                            },
                            onVideoClick = {
                                videoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                                bottomSheetType = 0
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun MediaSelectPanel(onPhotoClick: () -> Unit, onVideoClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(24.dp), horizontalArrangement = Arrangement.SpaceAround) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onPhotoClick() }) {
            Icon(painterResource(R.drawable.ic_photos), null, Modifier.size(48.dp), tint = WeComposeTheme.colors.textPrimary)
            Text("图片", fontSize = 14.sp, color = WeComposeTheme.colors.textSecondary)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable { onVideoClick() }) {
            Icon(painterResource(R.drawable.ic_moments), null, Modifier.size(48.dp), tint = WeComposeTheme.colors.textPrimary)
            Text("视频", fontSize = 14.sp, color = WeComposeTheme.colors.textSecondary)
        }
    }
}

@Composable
fun ChatBottomBar(onBombClicked: (String) -> Unit, onEmojiClicked: () -> Unit, onMoreClicked: () -> Unit) {
    var editingText by remember { mutableStateOf("") }
    Row(
        Modifier.fillMaxWidth().background(WeComposeTheme.colors.bottomBar).padding(4.dp, 0.dp).navigationBarsPadding().imePadding()
    ) {
        Icon(painterResource(R.drawable.ic_voice), null, Modifier.align(Alignment.CenterVertically).padding(4.dp).size(28.dp), tint = WeComposeTheme.colors.icon)
        BasicTextField(
            editingText, { editingText = it },
            Modifier.weight(1f).padding(4.dp, 8.dp).height(40.dp).clip(MaterialTheme.shapes.small).background(WeComposeTheme.colors.textFieldBackground).padding(start = 8.dp, top = 10.dp, end = 8.dp),
            cursorBrush = SolidColor(WeComposeTheme.colors.textPrimary)
        )
        if (editingText.isNotBlank()) {
            Text("发送", Modifier.clickable { onBombClicked(editingText); editingText = "" }.padding(8.dp).align(Alignment.CenterVertically), fontSize = 16.sp, color = WeComposeTheme.colors.meList)
        } else {
            Icon(painterResource(R.drawable.ic_stickers), null, Modifier.align(Alignment.CenterVertically).padding(4.dp).size(28.dp).clickable { onEmojiClicked() }, tint = WeComposeTheme.colors.icon)
            Icon(painterResource(R.drawable.ic_add), null, Modifier.align(Alignment.CenterVertically).padding(4.dp).size(28.dp).clickable { onMoreClicked() }, tint = WeComposeTheme.colors.icon)
        }
    }
}

@Composable
fun MessageItem(
    msg: Msg,
    shakingTime: Int,
    shakingLevel: Int,
    onTodoCardClick: (TodoShareCard) -> Unit = {},
    onImageClick: (String) -> Unit = {}
) {
    val shakingAngleBubble = remember { Animatable(0f) }
    val isMe = msg.from == User.Me
    val alignment = if (isMe) Arrangement.End else Arrangement.Start
    val bubbleColor = if (isMe) WeComposeTheme.colors.bubbleMe else WeComposeTheme.colors.bubbleOthers

    Row(Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = alignment) {
        if (!isMe) {
            Image(painterResource(msg.from.avatar), null, Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)))
            Spacer(Modifier.width(8.dp))
        }

        when (msg.type) {
            1 -> { // 文本
                Text(msg.text, Modifier.graphicsLayer(rotationZ = if(isMe) shakingAngleBubble.value else -shakingAngleBubble.value).drawBehind {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        addRoundRect(androidx.compose.ui.geometry.RoundRect(10.dp.toPx(), 0f, size.width - 10.dp.toPx(), size.height, 4.dp.toPx(), 4.dp.toPx()))
                        moveTo(if(isMe) size.width - 10.dp.toPx() else 10.dp.toPx(), 15.dp.toPx())
                        lineTo(if(isMe) size.width - 5.dp.toPx() else 5.dp.toPx(), 20.dp.toPx())
                        lineTo(if(isMe) size.width - 10.dp.toPx() else 10.dp.toPx(), 25.dp.toPx())
                        close()
                    }
                    drawPath(path, bubbleColor)
                }.padding(20.dp, 10.dp), color = if(isMe) WeComposeTheme.colors.textPrimaryMe else WeComposeTheme.colors.textPrimary)
            }
            2 -> { // 图片 (Base64)
                val base64Str = try {
                    val content = Json { ignoreUnknownKeys = true }.decodeFromString<ImageContent>(msg.text)
                    content.base64.substringAfter("base64,")
                } catch (e: Exception) { null }

                if (base64Str != null) {
                    val bitmap = remember(base64Str) {
                        try {
                            val decodedString = Base64.decode(base64Str, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size).asImageBitmap()
                        } catch (e: Exception) { null }
                    }
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap,
                            contentDescription = "Image",
                            modifier = Modifier
                                .widthIn(max = 200.dp)
                                .clip(RoundedCornerShape(8.dp))
                                // 【新增】点击回调
                                .clickable { onImageClick(base64Str) },
                            contentScale = ContentScale.FillWidth
                        )
                    } else {
                        Text("[图片数据损坏]", Modifier.background(bubbleColor, RoundedCornerShape(4.dp)).padding(8.dp))
                    }
                } else {
                    Text("[图片]", Modifier.background(bubbleColor, RoundedCornerShape(4.dp)).padding(8.dp))
                }
            }
            3 -> { // 视频
                val link = try { Json { ignoreUnknownKeys = true }.decodeFromString<VideoContent>(msg.text).link } catch (e: Exception) { "" }
                Box(Modifier.size(160.dp, 100.dp).background(Color.Black, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                    Text("VIDEO", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
            5 -> { // 待办
                TodoMessageCard(msg.text, bubbleColor, onTodoCardClick)
            }
        }

        if (isMe) {
            Spacer(Modifier.width(8.dp))
            Image(painterResource(msg.from.avatar), null, Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)))
        }
    }
}

// ... TodoMessageCard & EmojiGridView 保持不变 ...
@Composable
fun TodoMessageCard(jsonStr: String, bgColor: Color, onClick: (TodoShareCard) -> Unit) {
    val card = remember(jsonStr) {
        try {
            Json { ignoreUnknownKeys = true }.decodeFromString<TodoShareCard>(jsonStr)
        } catch (e1: Exception) {
            try {
                val unescaped = Json.decodeFromString<String>(jsonStr)
                Json { ignoreUnknownKeys = true }.decodeFromString<TodoShareCard>(unescaped)
            } catch (e2: Exception) { null }
        }
    }
    if (card != null) {
        Column(Modifier.width(220.dp).background(bgColor, RoundedCornerShape(8.dp)).clickable { onClick(card) }.padding(12.dp)) {
            Text(card.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = WeComposeTheme.colors.textPrimary)
            Text(card.description, maxLines = 2, fontSize = 12.sp, color = WeComposeTheme.colors.textSecondary)
            Text(if(card.done)"已完成" else "进行中", fontSize = 10.sp, color = WeComposeTheme.colors.textSecondary)
        }
    } else {
        Text("[卡片解析错误]", Modifier.background(bgColor).padding(8.dp))
    }
}

@Composable
fun EmojiGridView(emojiList: List<String>, onEmojiClick: (String) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        LazyVerticalGrid(GridCells.Fixed(4)) {
            items(emojiList) { emoji ->
                Text(emoji, fontSize = 32.sp, modifier = Modifier.clickable { onEmojiClick(emoji) }.padding(8.dp))
            }
        }
    }
}