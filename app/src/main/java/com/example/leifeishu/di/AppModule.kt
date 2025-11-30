package com.example.leifeishu.di

import com.example.leifeishu.data.datasource.ChatLocalDataSource
import com.example.leifeishu.data.repository.ChatRepository
import com.example.leifeishu.ui.conversation.chat.ChatViewModel
import com.example.leifeishu.ui.conversation.conversationList.ConversationListViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { ChatLocalDataSource() }
    single { ChatRepository(get()) }
    viewModel { ConversationListViewModel(get()) }
    viewModel { ChatViewModel(get()) }
}
