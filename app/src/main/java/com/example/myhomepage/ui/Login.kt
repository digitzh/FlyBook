package com.example.myhomepage.ui

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhomepage.R
import com.example.myhomepage.WeViewModel
import com.example.myhomepage.data.User
import com.example.myhomepage.network.WebSocketManager
import com.example.myhomepage.ui.theme.WeComposeTheme
import kotlinx.serialization.Serializable

@Serializable
object Login

@Composable
fun LoginPage(onLoginClick : (String) -> Unit){
    Column(horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
        .background(WeComposeTheme.colors.background)
        .fillMaxSize()
        .statusBarsPadding()
    ) {
        val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        WeTopBar("Flybook", onBack = { backDispatcher?.onBackPressed() })

        Image(painterResource(id = R.drawable.icon), contentDescription = "Icon",
            modifier = Modifier.size(150.dp))
        Spacer(modifier = Modifier.size(15.dp))
        MeMessagesItem()
        LoginItem(onLoginClick)
    }
}

@Composable
fun MeMessagesItem(){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally, // 整体内容水平居中
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
    ) {
        Image(
            painterResource(id = R.drawable.avatar_me), contentDescription = "Me",
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.size(12.dp))
        Row(Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Text(
                text = User.Me.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.size(20.dp))
            Text(
                "${User.Me.id}",
                fontSize = 20.sp,
            )
        }

    }
}

@Composable
fun LoginItem(onLoginClick : (String) -> Unit){
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 用户ID输入框（可编辑）
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("用户ID", color = WeComposeTheme.colors.meList) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = WeComposeTheme.colors.textPrimary,
                    unfocusedTextColor = WeComposeTheme.colors.textSecondary
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it }, // 更新密码状态
                label = { Text("密码", color = WeComposeTheme.colors.meList) },
                visualTransformation = PasswordVisualTransformation(), // 密码隐藏
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),

            )

            Spacer(modifier = Modifier.height(24.dp))

            // 登录按钮
            Button(
                onClick = { onLoginClick(userId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WeComposeTheme.colors.meList, // 按钮深蓝色背景
                    contentColor = WeComposeTheme.colors.textFieldBackground // 文字白色
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = userId.isNotBlank()
            ) {
                Text(text = "登录", fontSize = 16.sp)
            }
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun LoginPagePreview() {
//    LoginPage()
//}