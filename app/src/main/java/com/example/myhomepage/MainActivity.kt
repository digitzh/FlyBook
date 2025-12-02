package com.example.myhomepage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.LaunchedEffect
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

import com.example.myhomepage.network.WebSocketManager
import com.example.myhomepage.ui.ChatDetails
import com.example.myhomepage.ui.ChatDetailsPage
import com.example.myhomepage.ui.Home
import com.example.myhomepage.ui.HomePage
import com.example.myhomepage.ui.Login
import com.example.myhomepage.ui.LoginPage
import com.example.myhomepage.ui.theme.WeComposeTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    val viewModel: WeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        lifecycleScope.launch {
            viewModel.isLightThemeFlow.collect {
                insetsController.isAppearanceLightStatusBars = it
            }
        }

        setContent {
            WeComposeTheme(viewModel.theme) {
                val navController = rememberNavController()
                NavHost(navController, Login) {
                    composable<Home> {
                        // 进入主页时刷新会话列表
                        LaunchedEffect(Unit) {
                            viewModel.refreshConversationList()
                        }
                        HomePage(viewModel,
                            { navController.navigate(ChatDetails(it.friend.id)) },
                            {navController.navigate(Login)})
                    }
                    composable<ChatDetails>(
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                    ) {
                        ChatDetailsPage(viewModel, it.toRoute<ChatDetails>().userId)
                    }
                    composable<Login> {
                        LoginPage { userId ->
                            // 设置当前用户ID并加载用户信息
                            lifecycleScope.launch {
                                viewModel.setCurrentUserIdAndLoadUser(userId)
                                // 建立WebSocket连接
                                WebSocketManager.getInstance().connect(userId)
                                // 登录成功后跳转到主页
                                navController.navigate(Home)
                            }
                        }
                    }
                }
            }
        }
    }
}