package com.example.leifeishu.di
//
//import com.example.leifeishu.data.datasource.ChatLocalDataSource
//import com.example.leifeishu.data.repository.ChatRepository
//import com.example.leifeishu.ui.conversation.chat.ChatViewModel
//import com.example.leifeishu.ui.conversation.conversationList.ConversationListViewModel
//import org.koin.androidx.viewmodel.dsl.viewModel
//import org.koin.dsl.module
//
//val appModule = module {
//    single { ChatLocalDataSource() }
//    single { ChatRepository(get()) }
//    viewModel { ConversationListViewModel(get()) }
//    viewModel { ChatViewModel(get()) }
//}

import androidx.room.Room
import com.example.leifeishu.data.datasource.AppDatabase
import com.example.leifeishu.data.datasource.ChatRoomDataSource
import com.example.leifeishu.data.datasource.ContactLocalDataSource
import com.example.leifeishu.data.repository.ChatRepository
import com.example.leifeishu.data.repository.ContactRepository
import com.example.leifeishu.ui.contact.ContactListViewModel
import com.example.leifeishu.ui.conversation.chat.ChatViewModel
import com.example.leifeishu.ui.conversation.conversationList.ConversationListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Room database
    single {
        Room.databaseBuilder(get(), AppDatabase::class.java, "chat-db")
            .fallbackToDestructiveMigration()  // 避免版本冲突崩溃
            .build()
    }

    // Chat
    single { ChatRoomDataSource(get<AppDatabase>().chatDao()) }
    single { ChatRepository(get()) }

    // Contact
    single { ContactLocalDataSource(get<AppDatabase>().contactDao()) }
    single { ContactRepository(get()) }

    // ViewModels
    viewModel { ConversationListViewModel(get()) }
    viewModel { ChatViewModel(get()) }
    viewModel { ContactListViewModel(get()) }
}

//val appModule = module {
//    single {
//        Room.databaseBuilder(get(), AppDatabase::class.java, "chat-db").build()
//    }
//    single { ChatRoomDataSource(get<AppDatabase>().chatDao()) }
//    single { ChatRepository(get()) }
//
//    // Contact
//    single { ContactLocalDataSource(get<AppDatabase>().contactDao()) }  // 新增
//    single { ContactRepository(get()) }  // 新增
//
//    viewModel { ConversationListViewModel(get()) }
//    viewModel { ChatViewModel(get()) }
//    viewModel { ContactListViewModel(get()) }  // 新增
//}