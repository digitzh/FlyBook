package com.example.leifeishu.data.repository

import com.example.leifeishu.data.datasource.ContactLocalDataSource
import com.example.leifeishu.data.model.Contact

class ContactRepository(private val dataSource: ContactLocalDataSource) {

    fun getContacts() = dataSource.getContacts()

    suspend fun addContact(contact: Contact) = dataSource.addContact(contact)

    suspend fun removeContact(contact: Contact) = dataSource.removeContact(contact)

    suspend fun getContactById(id: String) = dataSource.getContactById(id)

    // =================== 新增 ===================
    suspend fun initDefaultContact() = dataSource.initDefaultContact()

}