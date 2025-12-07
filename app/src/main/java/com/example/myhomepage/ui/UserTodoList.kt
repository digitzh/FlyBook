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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.myhomepage.R
import com.example.myhomepage.data.Backlog
import com.example.myhomepage.data.Chat
import com.example.myhomepage.data.toBacklog
import com.example.myhomepage.todolist.data.toBacklog
import com.example.myhomepage.todolist.presentation.TodoListViewModel
import com.example.myhomepage.ui.theme.TodoType
import com.example.myhomepage.ui.theme.WeComposeTheme

@Composable
fun TodoListTopBar(){
    WeTopBar(title = "待办事项")
}

@Composable
fun TodoListItem(
    backlog: Backlog,
    modifier: Modifier = Modifier,
    itemTodoClick: () -> Unit = {},
    onLongPress: (IntOffset)->Unit = {}
) {
    var rowGlobalPosition by remember { mutableStateOf(Offset.Zero) }
    var rowSize by remember { mutableStateOf(IntOffset.Zero) }

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
                    onTap = {itemTodoClick()},
                    onLongPress = { offset ->
                        onLongPress(rowCenterOffset)
                    }
                )
            }
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(4.dp),
        shape = RoundedCornerShape(12.dp),
        color = getTodoColorByType(backlog.type),
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
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
    listViewModel: TodoListViewModel,
    onTodoClick: (Backlog) -> Unit,
    addTodo: () -> Unit
) {
    val uiState by listViewModel.uiState.collectAsState()

    // 合并数据库任务和聊天未读
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
                itemsIndexed(backlogList) { index, backlog ->
                    if (backlog.type != TodoType.MSG) {
                        TodoListItem(
                            backlog = backlog,
                            modifier = Modifier,
                            itemTodoClick = { onTodoClick(backlog) },
                            onLongPress = { offset ->
                                longPressedTodoId = backlog.id
                                menuOffset = IntOffset(
                                    x = offset.x.toInt() - 205,
                                    y = offset.y.toInt() + 50
                                )
                                showMenu = true
                            }
                        )
                    } else {
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
                TodoCompleteButton(
                    onClick = {
                        longPressedTodoId?.let { id ->
                            listViewModel.onToggleCompleted(id)
                        }
                        showMenu = false
                    }
                )
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
        Text(text = "✓", color = Color.White, fontSize = 18.sp)
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
        Text(text = "✕", color = Color.White, fontSize = 18.sp)
    }
}
