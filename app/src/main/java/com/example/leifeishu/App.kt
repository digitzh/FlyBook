package com.example.leifeishu

import android.app.Application
import com.example.leifeishu.data.datasource.ChatRoomDataSource
import com.example.leifeishu.data.datasource.ContactLocalDataSource
import com.example.leifeishu.di.appModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.android.ext.android.get

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(appModule)
        }

        val chatDataSource: ChatRoomDataSource = get()
        val contactDataSource: ContactLocalDataSource = get()

        CoroutineScope(Dispatchers.IO).launch {
            // 初始化系统欢迎会话
            chatDataSource.initWelcomeConversation()

            // 初始化默认联系人
            contactDataSource.initDefaultContact()
        }
    }
}

//class App : Application() {
//    override fun onCreate() {
//        super.onCreate()
//
//        // 启动 Koin
//        startKoin {
//            androidContext(this@App)
//            modules(appModule)
//        }
//
//        // 获取单例
//        val chatRoomDataSource: ChatRoomDataSource = get()
//        val contactDataSource: ContactLocalDataSource = get()
//
//        CoroutineScope(Dispatchers.IO).launch {
//            val defaultChatId = "system_welcome"
//            // 初始化默认联系人
//            contactDataSource.initDefaultContact(defaultChatId)
//
//            // 初始化系统默认会话
//            chatRoomDataSource.initWelcomeConversation()
//        }
//    }
//}

//class App : Application() {
//    override fun onCreate() {
//        super.onCreate()
//
//        // 启动 Koin
//        startKoin {
//            androidContext(this@App)
//            modules(appModule)
//        }
//
//        // 获取 ChatRoomDataSource 单例
//        val chatRoomDataSource: ChatRoomDataSource = get()
//
//        // 初始化欢迎会话
//        CoroutineScope(Dispatchers.IO).launch {
//            chatRoomDataSource.initWelcomeConversation()
//        }
//    }
//}

//class App : Application() {
//    override fun onCreate() {
//        super.onCreate()
//        startKoin {
//            androidContext(this@App)
//            modules(appModule)
//        }
//    }
//}
