package com.example.myhomepage.ui

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myhomepage.R
import com.example.myhomepage.data.User
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
    ) {
        Image(
            painterResource(id = R.drawable.avatar_me), contentDescription = "Me",
            modifier = Modifier.size(160.dp).clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.size(12.dp))
        Row(Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ){
            Text(text = "欢迎使用，请先登录", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LoginItem(onLoginClick : (String) -> Unit){
    var userId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPasswordField by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.fillMaxWidth(0.85f).padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            OutlinedTextField(
                value = userId,
                onValueChange = { userId = it },
                label = { Text("用户ID", color = WeComposeTheme.colors.meList) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(focusedTextColor = WeComposeTheme.colors.textPrimary, unfocusedTextColor = WeComposeTheme.colors.textSecondary),
            )
            Text(
                text = if (showPasswordField) "跳过密码" else "使用密码",
                Modifier.align(Alignment.End).clickable{ showPasswordField = !showPasswordField},
                color = WeComposeTheme.colors.meList,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            if(showPasswordField) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("密码", color = WeComposeTheme.colors.meList) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onLoginClick(userId) },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WeComposeTheme.colors.meList, contentColor = WeComposeTheme.colors.textFieldBackground),
                shape = RoundedCornerShape(12.dp),
                enabled = userId.isNotBlank()
            ) {
                Text(text = "登录", fontSize = 16.sp)
            }
        }
    }
}
