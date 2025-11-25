package com.example.myhomepage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhomepage.data.User
import com.example.myhomepage.ui.theme.WeComposeTheme
import com.example.myhomepage.R


@Composable
fun TodoListTopBar(){
    WeTopBar(title = "待办事项")
}

@Composable
fun TodoListItem(
    contact: User,
    modifier: Modifier = Modifier,
) {
    Row(Modifier.fillMaxWidth()) {
        Image(
            painterResource(contact.avatar), "avatar", Modifier
                .padding(12.dp, 8.dp, 8.dp, 8.dp)
                .size(36.dp)
                .clip(RoundedCornerShape(4.dp))
        )
        Text(
            contact.name,
            Modifier.weight(1f).align(Alignment.CenterVertically),
            fontSize = 17.sp,
            color = WeComposeTheme.colors.textPrimary
        )
    }
}

@Composable
fun TodoList(contacts: List<User>) {
    Column(Modifier.fillMaxSize()) {
        TodoListTopBar()
        Box(
            modifier = Modifier.background(WeComposeTheme.colors.background).fillMaxSize()
        ) {
            LazyColumn(
                Modifier
                    .background(WeComposeTheme.colors.listItem)
                    .fillMaxWidth()
            ) {
                item {
                    Text(
                        "文件",
                        Modifier.background(WeComposeTheme.colors.background).fillMaxWidth().padding(12.dp, 8.dp),
                        fontSize = 14.sp,
                        color = WeComposeTheme.colors.onBackground
                    )
                }

                val buttons = listOf(
                    User("wenjian1", "周报", R.drawable.ic_contact_tag),
                    User("wenjian2", "接口设计文档", R.drawable.ic_contact_tag),
                    User("wenjian3", "数据分析表", R.drawable.ic_contact_tag),
                )
                itemsIndexed(buttons) { index, contact ->
                    TodoListItem(contact)
                    if (index < buttons.size - 1) {
                        HorizontalDivider(
                            Modifier.padding(start = 56.dp),
                            color = WeComposeTheme.colors.divider,
                            thickness = 0.8f.dp
                        )
                    }
                }

                item {
                    Text(
                        "通知",
                        Modifier.background(WeComposeTheme.colors.background).fillMaxWidth().padding(12.dp, 8.dp),
                        fontSize = 14.sp,
                        color = WeComposeTheme.colors.onBackground
                    )
                }
                itemsIndexed(contacts) { index, contact ->
                    TodoListItem(contact)
                    if (index < contacts.size - 1) {
                        HorizontalDivider(
                            Modifier.padding(start = 56.dp),
                            color = WeComposeTheme.colors.divider,
                            thickness = 0.8f.dp
                        )
                    }
                }

                item {
                    Text(
                        "事务",
                        Modifier.background(WeComposeTheme.colors.background).fillMaxWidth().padding(12.dp, 8.dp),
                        fontSize = 14.sp,
                        color = WeComposeTheme.colors.onBackground
                    )
                }
                val transactions = listOf(
                    User("trans1", "下午2点开会", R.drawable.ic_contact_official),
                )
                itemsIndexed(transactions) { index, contact ->
                    TodoListItem(contact)
                    if (index < transactions.size - 1) {
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
}

@Preview(showBackground = true)
@Composable
fun ContactListItemPreview() {
    WeComposeTheme {
        Box {
            TodoListItem(
                User("zhangsan", "张三", R.drawable.avatar_zhangsan)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactListPreview() {
    val contacts = listOf<User>(
        User("zhangsan", "今天下午开会，请全员出席", R.drawable.avatar_zhangsan),
        User("lisi", "今天下班前给我数据分析表", R.drawable.avatar_lisi),
    )
    TodoList(contacts)
}

