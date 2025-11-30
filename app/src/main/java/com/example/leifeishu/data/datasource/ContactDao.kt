package com.example.leifeishu.data.datasource

import androidx.room.*
import com.example.leifeishu.data.model.Contact
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getContacts(): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: Contact)

    @Delete
    suspend fun delete(contact: Contact)

    @Query("SELECT * FROM contacts WHERE id = :contactId LIMIT 1")
    suspend fun getContactById(contactId: String): Contact?
}