package com.example.myhomepage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.myhomepage.data.User
import com.example.myhomepage.ui.theme.WeComposeTheme
import com.example.myhomepage.R
import com.example.myhomepage.WeViewModel
import com.example.myhomepage.data.Backlog
import com.example.myhomepage.data.Chat
import com.example.myhomepage.data.toBacklog
import com.example.myhomepage.ui.theme.TodoType


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

    // 计算Row正中间的坐标（并调整按钮位置，避免按钮中心和Row中心重叠）
    val rowCenterOffset by remember(rowGlobalPosition, rowSize) {
        mutableStateOf(
            IntOffset(
                // Row左边界 + Row宽度/2 - 按钮宽度/2（让按钮水平居中）
                x = rowGlobalPosition.x.toInt() + (rowSize.x / 2),
                // Row上边界 + Row高度/2 - 按钮高度/2
                y = rowGlobalPosition.y.toInt() + (rowSize.y / 2)
            )
        )
    }
    // 外层用Surface实现圆角+正方形：
    // 1. aspectRatio(1f) → 宽高比1:1（正方形）
    // 2. shape → 圆角形状
    // 3. fillMaxWidth() → 填充列宽（网格中每列宽度一致，高度=宽度→正方形）
    Surface(
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                // 获取Row在屏幕中的全局坐标（IntOffset）
                rowGlobalPosition = layoutCoordinates.positionInWindow()
                // 获取Row的尺寸（宽度x, 高度y）
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
            .aspectRatio(1f) // 关键：宽高比1:1，实现正方形
            .padding(4.dp), // 可选：Item之间的间距
        shape = RoundedCornerShape(12.dp), // 圆角大小（按需调整）
        color = getTodoColorByType(backlog.type), // 可选：背景色
        shadowElevation = 2.dp // 可选：阴影提升质感
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically, // 垂直居中对齐
            horizontalArrangement = Arrangement.SpaceBetween // 左右控件贴边，中间占满
        ) {

            // 内部用Column/Row+居中布局，适配正方形空间
            Column(
                modifier = Modifier
                    .fillMaxSize() // 填充正方形空间
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally, // 水平居中
                verticalArrangement = Arrangement.Center // 垂直居中
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
                    maxLines = 1, // 最多两行
                    overflow = TextOverflow.Ellipsis, // 溢出省略
                    textAlign = TextAlign.Center // 文字居中
                )

                Spacer(modifier = Modifier.height(8.dp)) // 头像和文字间距

                // 文字：居中显示，限制行数避免溢出
                Text(
                    backlog.time,
                    fontSize = 16.sp,
                    color = WeComposeTheme.colors.textPrimary,
                    maxLines = 2, // 最多两行
                    overflow = TextOverflow.Ellipsis, // 溢出省略
                    textAlign = TextAlign.Center // 文字居中
                )
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
fun TodoList(chats: List<Chat>, initbacklogList: List<Backlog>, onTodoClick : (Backlog) -> Unit, addTodo : () -> Unit ) {
    val showChatsList = chats.mapNotNull{ it.toBacklog()}
    val backlogList = initbacklogList + showChatsList
    var showButton by remember { mutableStateOf(false) }
    var buttonOffsetLeft by remember { mutableStateOf(IntOffset.Zero) }
    var buttonOffsetRight by remember { mutableStateOf(IntOffset.Zero) }
    var buttonOffset by remember { mutableStateOf(IntOffset.Zero) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { addTodo() }) {
                Text("+",fontSize = 20.sp,) // 简单写个 "+"
            }
        }
    ) { padding ->
        Column(
            Modifier
                .background(WeComposeTheme.colors.background)
                .fillMaxSize()
        ) {
            TodoListTopBar()
            LazyVerticalGrid(
                columns = GridCells.Fixed(2), // 固定2列 → 一行两个待办项
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp), // 列之间的间距
                verticalArrangement = Arrangement.spacedBy(8.dp),   // 行之间的间距
                contentPadding = PaddingValues(4.dp) // 列表整体内边距
            ) {

                itemsIndexed(backlogList) { index, backlog ->
                    if (backlog.type != TodoType.MSG)
                        TodoListItem(backlog, Modifier,
                            {onTodoClick(backlog)},{offset->
                                buttonOffsetLeft = IntOffset(offset.x-200,offset.y)
                                buttonOffset = IntOffset(offset.x-66,offset.y)
                                buttonOffsetRight = IntOffset(offset.x+70,offset.y)
                                showButton = true
                            })
                    else
                        TodoListItem(backlog)

                    if (index < backlogList.size - 1) {
                        HorizontalDivider(
                            Modifier.padding(start = 56.dp),
                            color = WeComposeTheme.colors.divider,
                            thickness = 0.8f.dp
                        )
                    }
                }
            }
        }
    }

    if (showButton) {

        Popup(
            // 按钮偏移位置（基于长按坐标）
            offset = buttonOffsetLeft,
            // 点击Popup外部不自动隐藏（靠外层空白点击隐藏）
            onDismissRequest = { showButton = false }
        ) {
            TodoCompleteButton(
                onClick = {
                    // 完成按钮点击逻辑（实现）TODO
                    showButton = false
                }
            )
        }

        Popup(
            // 按钮偏移位置（基于长按坐标）
            offset = buttonOffset,
            // 点击Popup外部不自动隐藏（靠外层空白点击隐藏）
            onDismissRequest = { showButton = false }
        ) {
            TodoShareButton(
                onClick = {
                    // 完成按钮点击逻辑（实现）TODO
                    showButton = false
                }
            )
        }

        Popup(
            // 按钮偏移位置（基于长按坐标）
            offset = buttonOffsetRight,
            // 点击Popup外部不自动隐藏（靠外层空白点击隐藏）
            onDismissRequest = { showButton = false }
        ) {
            TodoDeleteButton(
                onClick = {
                    // 完成按钮点击逻辑（实现）TODO
                    showButton = false
                }
            )
        }
    }

}


@Composable
fun TodoCompleteButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(50.dp) // 圆形按钮大小
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
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            // 完成文字
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
            .size(50.dp) // 圆形按钮大小
            .background(
                color = Color(0xFFF8B4B4),
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
                text = "\u2718",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            // 完成文字
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
            .size(50.dp) // 圆形按钮大小
            .background(
                color = Color(0xFFE1E15A),
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
                text = "\u269D",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.width(2.dp))
            // 完成文字
            Text(
                text = "分享",
                color = Color.White,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 12.sp
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun ContactListItemPreview() {
//    WeComposeTheme {
//        Box {
//            TodoListItem(
//                Backlog("wenjian1", "周报","完成周报","2025-12-03", TodoType.MSG ),
//            )
//        }
//    }
//}

//@Preview(showBackground = true)
//@Composable
//fun ContactListPreview() {
//    val contacts = listOf<User>(
//        User("zhangsan", "张三", R.drawable.avatar_zhangsan),
//        User("lisi", "李四", R.drawable.avatar_lisi),
//    )
//    TodoList(contacts)
//}

