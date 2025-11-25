package com.example.myhomepage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

import com.example.myhomepage.ui.ChatDetails
import com.example.myhomepage.ui.ChatDetailsPage
import com.example.myhomepage.ui.Home
import com.example.myhomepage.ui.HomePage
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
            WeComposeTheme {
                val navController = rememberNavController()
                NavHost(navController, Home) {
                    composable<Home> {
                        HomePage(viewModel) { navController.navigate(ChatDetails(it.friend.id)) }
                    }
                    composable<ChatDetails>(
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                    ) {
                        ChatDetailsPage(viewModel, it.toRoute<ChatDetails>().userId)
                    }
                }
            }
        }
    }
}