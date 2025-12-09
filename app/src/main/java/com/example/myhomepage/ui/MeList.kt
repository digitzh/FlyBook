package com.example.myhomepage.ui

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhomepage.R
import com.example.myhomepage.WeViewModel
import com.example.myhomepage.ui.theme.WeComposeTheme

@Composable
fun MeListTopBar(viewModel: WeViewModel) {
  Row(
    Modifier
      .background(WeComposeTheme.colors.meList)
      .fillMaxWidth()
      .height(224.dp)
  ) {
    Image(
      painterResource(id = R.drawable.avatar_me), contentDescription = "Me",
      Modifier
        .align(Alignment.CenterVertically)
        .padding(start = 24.dp)
        .clip(RoundedCornerShape(6.dp))
        .size(75.dp)
    )
    Column(
      Modifier
        .weight(1f)
        .padding(start = 12.dp)
    ) {
      // 动态显示用户名
      Text(
        viewModel.currentUser?.let { user -> "${user.username}" } ?: "${viewModel.currentUserId ?: ""}",
        Modifier.padding(top = 75.dp),
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = WeComposeTheme.colors.textPrimaryMe
      )
      Text(
        "ID：${viewModel.currentUser?.userId ?: viewModel.currentUserId ?: "未登录"}",
        Modifier.padding(top = 21.dp),
        fontSize = 14.sp,
        color = WeComposeTheme.colors.textPrimaryMe
      )
    }
    Icon(
      painterResource(id = R.drawable.ic_qrcode), contentDescription = "qrcode",
      Modifier
        .align(Alignment.CenterVertically)
        .padding(end = 20.dp)
        .size(25.dp),
      tint = WeComposeTheme.colors.textPrimaryMe
    )
    Icon(
      painterResource(R.drawable.ic_arrow_more), contentDescription = "更多",
      Modifier
        .align(Alignment.CenterVertically)
        .padding(end = 16.dp)
        .size(20.dp),
      tint = WeComposeTheme.colors.textPrimaryMe
    )
  }
}

// MeListItem 和 LogoutItem 保持不变... (请保留原代码)
@Composable
fun MeListItem(@DrawableRes icon: Int, title: String, modifier: Modifier = Modifier, badge: @Composable (() -> Unit)? = null, endBadge: @Composable (() -> Unit)? = null) {
  Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Image(painterResource(icon), "title", Modifier.padding(12.dp, 8.dp, 8.dp, 8.dp).size(36.dp).padding(8.dp))
    Text(title, fontSize = 17.sp, color = WeComposeTheme.colors.textSecondary)
    badge?.invoke()
    Spacer(Modifier.weight(1f))
    endBadge?.invoke()
    Icon(painterResource(R.drawable.ic_arrow_more), contentDescription = "更多", Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp).size(16.dp), tint = WeComposeTheme.colors.more)
  }
}

@Composable
fun LogoutItem(modifier: Modifier = Modifier) {
  Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
    Icon(painterResource(R.drawable.logout), "title", Modifier.size(38.dp).padding(5.dp), tint = WeComposeTheme.colors.redletter)
    Text("退出登录", fontSize = 20.sp, color = WeComposeTheme.colors.redletter)
  }
}

@Composable
fun MeList(viewModel: WeViewModel, myLogout : () -> Unit) {
  Box(Modifier.background(WeComposeTheme.colors.background).fillMaxSize()) {
    Column(Modifier.background(WeComposeTheme.colors.chatPage)
        .fillMaxWidth().fillMaxHeight()
        .verticalScroll(rememberScrollState())
        .imePadding()
    ) {
      MeListTopBar(viewModel)
      Spacer(Modifier.background(WeComposeTheme.colors.background).fillMaxWidth().height(8.dp))
      MeListItem(R.drawable.ic_moments, "朋友圈")
      HorizontalDivider(Modifier.padding(start = 56.dp), color = WeComposeTheme.colors.divider, thickness = 0.8f.dp)
      MeListItem(R.drawable.ic_stickers, "表情")
      Spacer(Modifier.background(WeComposeTheme.colors.background).fillMaxWidth().height(8.dp))
      MeListItem(R.drawable.ic_settings, "设置")
      Spacer(Modifier.background(WeComposeTheme.colors.background).fillMaxWidth().height(300.dp))
      LogoutItem(Modifier.clickable { myLogout() })
    }
  }
}
