package com.example.flybook.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.flybook.model.Message

@Composable
fun MessageItem(message: Message) {
    val isSender = message.senderId.toInt() == 1001 // 假设 1001 是自己
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSender) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .background(if (isSender) Color(0xFFDCF8C6) else Color.White, RoundedCornerShape(8.dp))
                .padding(8.dp)
                .padding(4.dp)
        ) {
            Text(message.content)
        }
    }
}
