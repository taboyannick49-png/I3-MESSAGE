package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.ui.theme.ExpressiveThemeType

enum class MessageStatus {
    SENDING, SENT, DELIVERED, READ
}

data class EmojiReaction(
    val emoji: String,
    val count: Int = 1,
    val userNames: List<String> = emptyList(),
    val isReactedByMe: Boolean = false
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey val id: String,
    val title: String,
    val phoneNumber: String = "",
    val avatarColorHex: String = "#4F46E5",
    val avatarInitial: String = "I",
    val lastMessageText: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isArchived: Boolean = false,
    val isRcs: Boolean = true,
    val isAiAssistant: Boolean = false,
    val isOnline: Boolean = true,
    val typingStatus: String? = null
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderName: String,
    val isFromMe: Boolean,
    val text: String,
    val isAudio: Boolean = false,
    val audioDurationSec: Int = 0,
    val audioWaveform: String = "", // Comma-separated floats e.g. "0.2,0.6,0.8..."
    val audioSummary: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "READ", // SENDING, SENT, DELIVERED, READ
    val reactionsJson: String = "" // Serialized list of EmojiReaction
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String = "",
    val avatarColorHex: String = "#7C3AED",
    val isRcsActive: Boolean = true,
    val statusMessage: String = "Disponible sur I3 RCS"
)

data class GoogleUserProfile(
    val email: String,
    val displayName: String,
    val avatarInitial: String = "G",
    val isSignedIn: Boolean = true
)
