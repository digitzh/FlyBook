package com.example.myhomepage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhomepage.data.User
import com.example.myhomepage.ui.theme.WeComposeTheme
import com.example.myhomepage.R
import com.example.myhomepage.data.Backlog
import com.example.myhomepage.data.toBacklog
import com.example.myhomepage.ui.theme.TodoType


@Composable
fun TodoListTopBar(){
    WeTopBar(title = "待办事项")
}

@Composable
fun TodoListItem(
    contact: Backlog,
    modifier: Modifier = Modifier,
) {
    // 外层用Surface实现圆角+正方形：
    // 1. aspectRatio(1f) → 宽高比1:1（正方形）
    // 2. shape → 圆角形状
    // 3. fillMaxWidth() → 填充列宽（网格中每列宽度一致，高度=宽度→正方形）
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // 关键：宽高比1:1，实现正方形
            .padding(4.dp), // 可选：Item之间的间距
        shape = RoundedCornerShape(12.dp), // 圆角大小（按需调整）
        color = getTodoColorByType(contact.type), // 可选：背景色
        shadowElevation = 2.dp // 可选：阴影提升质感
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
                    when (contact.type) {
                        TodoType.FILE -> R.drawable.ic_contact_tag
                        TodoType.CONF -> R.drawable.ic_contact_official
                        TodoType.MSG -> contact.avatar
                        TodoType.OTHER -> R.drawable.ic_photos
                    }
                ), "avatar", Modifier
                    .padding(12.dp, 8.dp, 8.dp, 8.dp)
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp)),
            )
            Text(
                contact.title,
                fontSize = 20.sp,
                color = WeComposeTheme.colors.textPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 2, // 最多两行
                overflow = TextOverflow.Ellipsis, // 溢出省略
                textAlign = TextAlign.Center // 文字居中
            )

            Spacer(modifier = Modifier.height(8.dp)) // 头像和文字间距

            // 文字：居中显示，限制行数避免溢出
            Text(
                contact.time,
                fontSize = 16.sp,
                color = WeComposeTheme.colors.textPrimary,
                maxLines = 2, // 最多两行
                overflow = TextOverflow.Ellipsis, // 溢出省略
                textAlign = TextAlign.Center // 文字居中
            )
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
fun TodoList(contacts: List<User>, initbacklogList: List<Backlog>, onTodoClick : (Backlog) -> Unit) {
    val contactsAsBacklogs = contacts.map { it.toBacklog() }
    val backlogList = initbacklogList + contactsAsBacklogs
    Column(Modifier
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

            itemsIndexed(backlogList) { index, contact ->
                if (contact.type != TodoType.MSG)
                    TodoListItem(contact,  Modifier.clickable{ onTodoClick(contact) })
                else
                    TodoListItem(contact)
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

@Preview(showBackground = true)
@Composable
fun ContactListItemPreview() {
    WeComposeTheme {
        Box {
            TodoListItem(
                Backlog("wenjian1", "周报","完成周报","2025-12-03", TodoType.MSG ),
            )
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun ContactListPreview() {
//    val contacts = listOf<User>(
//        User("zhangsan", "张三", R.drawable.avatar_zhangsan),
//        User("lisi", "李四", R.drawable.avatar_lisi),
//    )
//    TodoList(contacts)
//}

