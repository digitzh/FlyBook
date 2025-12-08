package com.example.myhomepage.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhomepage.R
import com.example.myhomepage.share.TodoShareCard
import com.example.myhomepage.ui.theme.TodoType
import com.example.myhomepage.ui.theme.WeComposeTheme

@Composable
fun SharedTodoDetailsPage(card: TodoShareCard, onBack: () -> Unit) {
    Column(
        Modifier
            .background(WeComposeTheme.colors.background)
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        WeTopBar("待办详情", onBack = onBack)

        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Image(
                    painter = painterResource(when (card.type) {
                        TodoType.FILE -> R.drawable.ic_contact_tag
                        TodoType.CONF -> R.drawable.ic_contact_official
                        TodoType.MSG -> R.drawable.ic_moments
                        TodoType.OTHER -> R.drawable.ic_photos
                    }),
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(card.title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = WeComposeTheme.colors.textPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text("类型：${card.type}", fontSize = 14.sp, color = WeComposeTheme.colors.textSecondary)
                    Text("截止：${card.deadline ?: "无"}", fontSize = 14.sp, color = WeComposeTheme.colors.textSecondary)
                    Text("状态：${if(card.done) "已完成" else "未完成"}", fontSize = 14.sp, color = if(card.done) WeComposeTheme.colors.meList else WeComposeTheme.colors.redletter)
                }
            }
            Spacer(Modifier.height(20.dp))
            Text("描述内容", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = WeComposeTheme.colors.textPrimary)
            Spacer(Modifier.height(8.dp))
            Text(card.description, fontSize = 16.sp, color = WeComposeTheme.colors.textPrimary)
        }
    }
}
