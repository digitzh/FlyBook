package com.example.flybook.viewmodel

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flybook.database.ChatDao
import com.example.flybook.database.ConversationEntity
import com.example.flybook.database.ConversationMemberEntity
import com.example.flybook.model.Conversation
import com.example.flybook.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class HomeViewModel(private val chatDao: ChatDao) : ViewModel() {

    private val currentUserId = 1000L  // 当前用户 ID

    val showContactDialog = mutableStateOf(false)
    val showGroupNameDialog = mutableStateOf(false)

    val selectedContacts = mutableStateListOf<User>()
    val conversations = mutableStateListOf<Conversation>()

    var newGroupName by mutableStateOf("")

    val contacts = listOf(
        User(1000, "Me", "https://api.dicebear.com/..."),
        User(1001, "ZhangSan", "..."),
        User(1002, "LiSi", "..."),
        User(1003, "WangWu", "...")
    )

    // 🔹 新增：StateFlow 通知 UI 新建会话 ID
    val _createdConversationId = MutableStateFlow<Long?>(null)
    val createdConversationId: StateFlow<Long?> = _createdConversationId

    init {
        loadAllConversations()
    }

    private fun loadAllConversations() {
        viewModelScope.launch {
            chatDao.getAllConversations().collect { list ->
                conversations.clear()
                list.forEach { cw ->
                    val members = cw.members.map { m ->
                        contacts.find { it.id == m.userId } ?: User(m.userId, "Unknown", "")
                    }
                    conversations.add(
                        Conversation(
                            id = cw.conversation.id,
                            name = cw.conversation.name,
                            members = members
                        )
                    )
                }
            }
        }
    }

    /**
     * 创建或获取已有会话
     */
    suspend fun createOrGetConversation(providedName: String? = null): Long {
        val currentUserId = 1000L  // 当前用户 ID

        // 确保当前用户总是在选中列表中
        if (selectedContacts.none { it.id == currentUserId }) {
            selectedContacts.add(contacts.first { it.id == currentUserId })
        }

        val memberIds = selectedContacts.map { it.id }

        // 查询是否已有相同成员的会话
        val existIds = chatDao.findConversationByMembers(memberIds, memberIds.size)
        val id = if (existIds.isNotEmpty()) {
            existIds.first()   // 直接复用已有会话
        } else {
            // 自动生成名称
            val groupName = providedName ?: run {
                val others = selectedContacts.filter { it.id != currentUserId }
                when {
                    others.size == 1 -> others.first().username           // 1 对 1
                    else -> others.joinToString("、") { it.username }     // 群聊
                }
            }

            // 创建新会话
            val newId = System.currentTimeMillis()
            chatDao.insertConversation(ConversationEntity(newId, groupName))
            val members = memberIds.map { ConversationMemberEntity(conversationId = newId, userId = it) }
            chatDao.insertMembers(members)
            newId
        }

        // 通知 UI 跳转
        _createdConversationId.value = id

        return id
    }



    fun resetCreatedConversationId() {
        _createdConversationId.value = null
    }
}
