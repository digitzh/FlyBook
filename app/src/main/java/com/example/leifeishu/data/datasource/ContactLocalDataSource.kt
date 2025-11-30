package com.example.leifeishu.data.datasource

import com.example.leifeishu.data.model.Contact
import kotlinx.coroutines.flow.Flow

class ContactLocalDataSource(private val dao: ContactDao) {

    fun getContacts(): Flow<List<Contact>> = dao.getContacts()

    suspend fun addContact(contact: Contact) = dao.insert(contact)

    suspend fun removeContact(contact: Contact) = dao.delete(contact)

    suspend fun getContactById(id: String) = dao.getContactById(id)

    // =================== 新增 ===================
    // 初始化默认联系人（类飞书团队）
    suspend fun initDefaultContact() {
        val existing = dao.getContactById("system_team")
        if (existing == null) {
            val contact = Contact(
                id = "system_team",
                name = "类飞书团队",
                avatarUrl = null,  // 后续可改为本地资源
                lastChatId = "system_chat"
            )
            dao.insert(contact)
        }
    }
}