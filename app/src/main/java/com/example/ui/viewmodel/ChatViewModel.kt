package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.EmojiReaction
import com.example.data.model.MessageEntity
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class ChatFilter {
    ALL, UNREAD, RCS, PINNED, CONTACTS
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    val repository = ChatRepository(application)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _activeFilter = MutableStateFlow(ChatFilter.ALL)
    val activeFilter: StateFlow<ChatFilter> = _activeFilter.asStateFlow()

    val activeConversations: StateFlow<List<ConversationEntity>> = combine(
        repository.activeConversations,
        _searchQuery,
        _activeFilter
    ) { conversations, query, filter ->
        if (filter == ChatFilter.CONTACTS && query.isNotBlank()) {
            emptyList()
        } else {
            conversations.filter { conv ->
                val matchesQuery = query.isBlank() ||
                        conv.title.contains(query, ignoreCase = true) ||
                        conv.phoneNumber.contains(query, ignoreCase = true) ||
                        conv.lastMessageText.contains(query, ignoreCase = true)
                val matchesFilter = when (filter) {
                    ChatFilter.ALL -> true
                    ChatFilter.UNREAD -> conv.unreadCount > 0
                    ChatFilter.RCS -> conv.isRcs
                    ChatFilter.PINNED -> conv.isPinned
                    ChatFilter.CONTACTS -> true
                }
                matchesQuery && matchesFilter
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredContacts: StateFlow<List<ContactEntity>> = combine(
        repository.allContacts,
        _searchQuery,
        _activeFilter
    ) { contacts, query, filter ->
        if (query.isBlank() && filter != ChatFilter.CONTACTS) {
            emptyList()
        } else {
            contacts.filter { contact ->
                query.isBlank() ||
                        contact.name.contains(query, ignoreCase = true) ||
                        contact.phoneNumber.contains(query, ignoreCase = true) ||
                        contact.email.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pinnedConversations: StateFlow<List<ConversationEntity>> = repository.pinnedConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedConversations: StateFlow<List<ConversationEntity>> = repository.archivedConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat State
    private val _selectedConversationId = MutableStateFlow<String?>(null)
    val selectedConversationId: StateFlow<String?> = _selectedConversationId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentConversation: StateFlow<ConversationEntity?> = _selectedConversationId
        .flatMapLatest { id ->
            if (id != null) repository.getConversation(id) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentMessages: StateFlow<List<MessageEntity>> = _selectedConversationId
        .flatMapLatest { id ->
            if (id != null) repository.getMessages(id) else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _smartReplies = MutableStateFlow<List<String>>(emptyList())
    val smartReplies: StateFlow<List<String>> = _smartReplies.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _activeFilter.value = ChatFilter.ALL
    }

    fun setFilter(filter: ChatFilter) {
        _activeFilter.value = filter
    }

    fun selectConversation(conversationId: String?) {
        _selectedConversationId.value = conversationId
        if (conversationId != null) {
            viewModelScope.launch {
                repository.markAsRead(conversationId)
                updateSmartRepliesForConv(conversationId)
            }
        }
    }

    fun startChatWithContact(contact: ContactEntity, onComplete: (String) -> Unit) {
        viewModelScope.launch {
            val convId = repository.createOrGetConversationForContact(contact)
            selectConversation(convId)
            onComplete(convId)
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            if (_selectedConversationId.value == conversationId) {
                _selectedConversationId.value = null
            }
            repository.deleteConversation(conversationId)
        }
    }

    private suspend fun updateSmartRepliesForConv(conversationId: String) {
        val conv = currentConversation.value
        val lastMsg = conv?.lastMessageText ?: ""
        _smartReplies.value = repository.getSmartReplies(lastMsg)
    }

    fun sendMessage(text: String) {
        val convId = _selectedConversationId.value ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.sendMessage(conversationId = convId, text = text)
            _smartReplies.value = emptyList()
        }
    }

    fun sendVoiceMessage(durationSec: Int, waveform: String, summary: String?) {
        val convId = _selectedConversationId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                text = "",
                isAudio = true,
                audioDurationSec = durationSec,
                audioWaveform = waveform,
                audioSummary = summary
            )
        }
    }

    fun toggleReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            repository.toggleEmojiReaction(messageId, emoji)
        }
    }

    fun togglePin(conversationId: String, currentPinned: Boolean) {
        viewModelScope.launch {
            repository.togglePin(conversationId, currentPinned)
        }
    }

    fun toggleArchive(conversationId: String, currentArchived: Boolean) {
        viewModelScope.launch {
            repository.toggleArchive(conversationId, currentArchived)
        }
    }

    fun toggleRcs(conversationId: String, currentRcs: Boolean) {
        viewModelScope.launch {
            repository.toggleRcs(conversationId, currentRcs)
        }
    }

    fun rewriteWithTone(text: String, tone: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            val rewritten = repository.rewriteTextWithAi(text, tone)
            onResult(rewritten)
        }
    }

    fun parseReactions(json: String): List<EmojiReaction> {
        return repository.parseReactions(json)
    }
}
