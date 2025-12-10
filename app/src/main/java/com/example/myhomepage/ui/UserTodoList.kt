package com.example.myhomepage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.myhomepage.R
import com.example.myhomepage.data.Backlog
import com.example.myhomepage.data.Chat
import com.example.myhomepage.data.toBacklog
import com.example.myhomepage.share.TodoShareCard
import com.example.myhomepage.todolist.data.toBacklog
import com.example.myhomepage.todolist.data.toShareCard
import com.example.myhomepage.todolist.presentation.TodoListViewModel
import com.example.myhomepage.ui.theme.TodoType
import com.example.myhomepage.ui.theme.WeComposeTheme

@Composable
fun TodoListTopBar() {
    WeTopBar(title = "待办事项")
}

@Composable
fun TodoListItem(
    backlog: Backlog,
    modifier: Modifier = Modifier,
    itemTodoClick: () -> Unit = {},
    onLongPress: (IntOffset) -> Unit = {}
) {
    var rowGlobalPosition by remember { mutableStateOf(Offset.Zero) }
    var rowSize by remember { mutableStateOf(IntOffset.Zero) }

    // Row 中心点（用来作为长按菜单的参考坐标）
    val rowCenterOffset by remember(rowGlobalPosition, rowSize) {
        mutableStateOf(
            IntOffset(
                x = rowGlobalPosition.x.toInt() + (rowSize.x / 2),
                y = rowGlobalPosition.y.toInt() + (rowSize.y / 2)
            )
        )
    }

    Surface(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                rowGlobalPosition = layoutCoordinates.positionInWindow()
                rowSize = IntOffset(layoutCoordinates.size.width, layoutCoordinates.size.height)
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { itemTodoClick() },
                    onLongPress = { _ ->
                        onLongPress(rowCenterOffset)
                    }
                )
            }
            .fillMaxWidth()
            .aspectRatio(1f) // 正方形卡片
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        color = getTodoColorByType(backlog.type),
        shadowElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 内部内容：图标 + 标题 + 时间，居中
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painterResource(
                            when (backlog.type) {
                                TodoType.FILE -> R.drawable.ic_contact_tag
                                TodoType.CONF -> R.drawable.ic_contact_official
                                TodoType.MSG -> backlog.avatar
                                TodoType.OTHER -> R.drawable.ic_photos
                            }
                        ),
                        "avatar",
                        Modifier
                            .padding(12.dp, 8.dp, 8.dp, 8.dp)
                            .size(50.dp)
                            .clip(RoundedCornerShape(6.dp)),
                    )
                    Text(
                        backlog.title,
                        fontSize = 20.sp,
                        color = WeComposeTheme.colors.textPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        backlog.time,
                        fontSize = 16.sp,
                        color = WeComposeTheme.colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // 完成状态小勾
            if (backlog.complete) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(24.dp)
                        .background(Color(0xFF4CAF50), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun getTodoColorByType(type: TodoType): Color {
    return when (type) {
        TodoType.FILE -> Color(0xDEEBF7FF)
        TodoType.CONF -> Color(0xFFFBE5D6)
        TodoType.MSG -> Color(0xFFD2F5BD)
        TodoType.OTHER -> Color(0xFFF8E899)
    }
}

@Composable
fun TodoList(
    chats: List<Chat>,
    listViewModel: TodoListViewModel,       // 列表页 VM
    onTodoClick: (Backlog) -> Unit,         // 点某个待办 → 去详情页
    addTodo: () -> Unit,                    // 点新增 → 去 AddTodoPage
    onShareTodo: (TodoShareCard) -> Unit    // 分享回调签名
) {
    val uiState by listViewModel.uiState.collectAsState()

    // 业务待办转成 Backlog
    val todoBacklogs = uiState.todos.map { it.toBacklog() }
    val showChatsList = chats.mapNotNull { it.toBacklog() }
    val backlogList = todoBacklogs + showChatsList

    var showMenu by remember { mutableStateOf(false) }
    var menuOffset by remember { mutableStateOf(IntOffset.Zero) }
    var longPressedTodoId by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = addTodo) {
                Text("+", fontSize = 20.sp)
            }
        }
    ) { padding ->
        Column(
            Modifier
                .background(WeComposeTheme.colors.background)
                .fillMaxSize()
                .padding(padding)
        ) {
            TodoListTopBar()

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(4.dp)
            ) {
                itemsIndexed(backlogList) { _, backlog ->
                    if (backlog.type != TodoType.MSG) {
                        TodoListItem(
                            backlog = backlog,
                            modifier = Modifier,
                            itemTodoClick = { onTodoClick(backlog) },
                            onLongPress = { offset ->
                                longPressedTodoId = backlog.id
                                menuOffset = IntOffset(
                                    x = offset.x - 205,
                                    y = offset.y + 50
                                )
                                showMenu = true
                            }
                        )
                    } else {
                        // 聊天产生的 Backlog，暂不支持长按菜单
                        TodoListItem(backlog)
                    }
                }
            }
        }
    }

    if (showMenu) {
        Popup(
            offset = menuOffset,
            onDismissRequest = { showMenu = false }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 完成
                TodoCompleteButton(
                    onClick = {
                        longPressedTodoId?.let { id ->
                            listViewModel.onToggleCompleted(id)
                        }
                        showMenu = false
                    }
                )

                // 分享到即时通信模块（使用当前回调 onShareTodo）
                TodoShareButton(
                    onClick = {
                        longPressedTodoId?.let { id ->
                            // 在 uiState 中找到对应 TodoTask，再转成 TodoShareCard
                            val todo = uiState.todos.firstOrNull { it.id == id }
                            if (todo != null) {
                                val card = todo.toShareCard()
                                onShareTodo(card)     // 这里接回 MainActivity 的实现
                            }
                        }
                        showMenu = false
                    }
                )

                // 删除
                TodoDeleteButton(
                    onClick = {
                        longPressedTodoId?.let { id ->
                            listViewModel.onDeleteTodo(id)
                        }
                        showMenu = false
                    }
                )
            }
        }
    }
}

@Composable
fun TodoCompleteButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(color = Color(0xFF90EE90), shape = CircleShape)
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "✓",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "完成",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TodoDeleteButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(color = Color(0xFFF8B4B4), shape = CircleShape)
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "✕",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "删除",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun TodoShareButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp)
            .background(color = Color(0xFFE1E15A), shape = CircleShape)
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "\u269D",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = "分享",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp
            )
        }
    }
}
