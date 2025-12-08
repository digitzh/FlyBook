package com.example.myhomepage.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myhomepage.WeViewModel
import com.example.myhomepage.data.Chat
import com.example.myhomepage.share.TodoShareCard
import com.example.myhomepage.ui.theme.WeComposeTheme
import androidx.compose.foundation.Image

@Composable
fun SelectConversationPage(
    viewModel: WeViewModel,
    card: TodoShareCard,
    onBack: () -> Unit,
    onSent: () -> Unit
) {
    Column(
        Modifier
            .background(WeComposeTheme.colors.background)
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        WeTopBar(title = "选择发送给谁", onBack = onBack)

        LazyColumn(Modifier.background(WeComposeTheme.colors.listItem).weight(1f)) {
            items(viewModel.chats) { chat ->
                SelectChatListItem(chat) {
                    // 点击发送
                    chat.conversationId?.let { cid ->
                        viewModel.sendTodoCard(cid, card) {
                            onSent()
                        }
                    }
                }
                HorizontalDivider(Modifier.padding(start = 68.dp), color = WeComposeTheme.colors.divider, thickness = 0.8f.dp)
            }
        }
    }
}

@Composable
private fun SelectChatListItem(chat: Chat, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (chat.isGroupChat && chat.avatarUrl != null) {
            AsyncImage(model = chat.avatarUrl, contentDescription = null, modifier = Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        } else {
            Image(painterResource(chat.friend.avatar), null, Modifier.size(48.dp).clip(RoundedCornerShape(4.dp)))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(chat.displayName, fontSize = 17.sp, color = WeComposeTheme.colors.textPrimary)
    }
}
