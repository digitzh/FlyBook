package com.example.leifeishu.ui.contact

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.leifeishu.data.model.Contact
import com.example.leifeishu.data.repository.ContactRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class ContactListViewModel(private val repository: ContactRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<List<Contact>>(emptyList())
    val uiState: StateFlow<List<Contact>> = _uiState

    init {
        repository.getContacts().onEach {
            _uiState.value = it
        }.launchIn(viewModelScope)
    }

    fun addContact(contact: Contact) {
        viewModelScope.launch {
            repository.addContact(contact)
        }
    }

    fun removeContact(contact: Contact) {
        viewModelScope.launch {
            repository.removeContact(contact)
        }
    }
}