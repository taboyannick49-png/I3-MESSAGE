package com.example.ui.viewmodel

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ContactEntity
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application)

    private val _hasContactPermission = MutableStateFlow(
        ContextCompat.checkSelfPermission(
            application,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    )
    val hasContactPermission: StateFlow<Boolean> = _hasContactPermission.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _deviceContacts = MutableStateFlow<List<ContactEntity>>(emptyList())

    val contacts: StateFlow<List<ContactEntity>> = combine(
        repository.allContacts,
        _deviceContacts,
        _searchQuery
    ) { localContacts, deviceList, query ->
        val merged = (localContacts + deviceList)
            .distinctBy { it.phoneNumber.replace(" ", "").replace("-", "") }
            .sortedBy { it.name.lowercase() }
        if (query.isBlank()) {
            merged
        } else {
            merged.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.phoneNumber.contains(query)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        if (_hasContactPermission.value) {
            loadDeviceContacts()
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _hasContactPermission.value = granted
        if (granted) {
            loadDeviceContacts()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadDeviceContacts() {
        viewModelScope.launch {
            val list = fetchSystemContacts()
            _deviceContacts.value = list
        }
    }

    fun addContact(name: String, phoneNumber: String, email: String = "", onComplete: (ContactEntity) -> Unit = {}) {
        viewModelScope.launch {
            val created = repository.addCustomContact(name, phoneNumber, email)
            onComplete(created)
        }
    }

    fun deleteContact(id: String) {
        viewModelScope.launch {
            repository.deleteContact(id)
            _deviceContacts.value = _deviceContacts.value.filter { it.id != id }
        }
    }

    private suspend fun fetchSystemContacts(): List<ContactEntity> = withContext(Dispatchers.IO) {
        val result = mutableListOf<ContactEntity>()
        val context = getApplication<Application>()
        try {
            val cursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            cursor?.use {
                val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)

                val colors = listOf("#4F46E5", "#F95738", "#059669", "#7C3AED", "#D97706", "#DB2777")
                var colorIndex = 0

                while (it.moveToNext()) {
                    val name = if (nameIdx != -1) it.getString(nameIdx) ?: "Contact" else "Contact"
                    val number = if (numberIdx != -1) it.getString(numberIdx) ?: "" else ""
                    val id = if (idIdx != -1) it.getString(idIdx) ?: name else name

                    if (number.isNotBlank()) {
                        result.add(
                            ContactEntity(
                                id = "dev_$id",
                                name = name,
                                phoneNumber = number,
                                avatarColorHex = colors[colorIndex % colors.size],
                                isRcsActive = true,
                                statusMessage = "Contact de votre répertoire"
                            )
                        )
                        colorIndex++
                    }
                }
            }
        } catch (e: Exception) {
            // Silently handle any security exception or resolver error
        }
        result
    }

    suspend fun startChatWithContact(contact: ContactEntity): String {
        return repository.createOrGetConversationForContact(contact)
    }
}
