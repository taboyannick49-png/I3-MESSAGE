package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.model.ContactEntity
import com.example.data.model.ConversationEntity
import com.example.data.model.EmojiReaction
import com.example.data.model.MessageEntity
import com.example.data.remote.GeminiClient
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID

class ChatRepository(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "i3_messaging.db"
    ).build()

    private val conversationDao = database.conversationDao()
    private val messageDao = database.messageDao()
    private val contactDao = database.contactDao()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val reactionListType = Types.newParameterizedType(List::class.java, EmojiReaction::class.java)
    private val reactionAdapter = moshi.adapter<List<EmojiReaction>>(reactionListType)

    val activeConversations: Flow<List<ConversationEntity>> = conversationDao.getActiveConversations()
    val pinnedConversations: Flow<List<ConversationEntity>> = conversationDao.getPinnedConversations()
    val archivedConversations: Flow<List<ConversationEntity>> = conversationDao.getArchivedConversations()
    val allContacts: Flow<List<ContactEntity>> = contactDao.getAllContacts()

    init {
        // Pre-seed sample data if empty
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    fun getConversation(id: String): Flow<ConversationEntity?> = conversationDao.getConversationById(id)

    fun getMessages(conversationId: String): Flow<List<MessageEntity>> =
        messageDao.getMessagesForConversation(conversationId)

    suspend fun togglePin(conversationId: String, currentPinned: Boolean) {
        conversationDao.setPinned(conversationId, !currentPinned)
    }

    suspend fun toggleArchive(conversationId: String, currentArchived: Boolean) {
        conversationDao.setArchived(conversationId, !currentArchived)
    }

    suspend fun toggleRcs(conversationId: String, currentRcs: Boolean) {
        conversationDao.setRcs(conversationId, !currentRcs)
    }

    suspend fun markAsRead(conversationId: String) {
        conversationDao.markAsRead(conversationId)
    }

    suspend fun deleteConversation(conversationId: String) {
        conversationDao.deleteById(conversationId)
        messageDao.deleteMessagesForConversation(conversationId)
    }

    suspend fun sendMessage(
        conversationId: String,
        text: String,
        isAudio: Boolean = false,
        audioDurationSec: Int = 0,
        audioWaveform: String = "",
        audioSummary: String? = null
    ) {
        val messageId = "msg_${UUID.randomUUID()}"
        val now = System.currentTimeMillis()

        val message = MessageEntity(
            id = messageId,
            conversationId = conversationId,
            senderName = "Moi",
            isFromMe = true,
            text = text,
            isAudio = isAudio,
            audioDurationSec = audioDurationSec,
            audioWaveform = audioWaveform,
            audioSummary = audioSummary,
            timestamp = now,
            status = "SENDING",
            reactionsJson = ""
        )

        messageDao.insertMessage(message)
        val preview = if (isAudio) "🎤 Message vocal (${audioDurationSec}s)" else text
        conversationDao.updateLastMessage(conversationId, preview, now)

        // Simulate sending transition (SENDING -> SENT -> DELIVERED -> READ)
        CoroutineScope(Dispatchers.IO).launch {
            delay(350)
            messageDao.updateStatus(messageId, "SENT")
            delay(400)
            messageDao.updateStatus(messageId, "DELIVERED")
            delay(500)
            messageDao.updateStatus(messageId, "READ")

            // If chat is with AI Assistant or active contact, simulate smart response
            handleIncomingReplyIfNeeded(conversationId, text)
        }
    }

    private suspend fun handleIncomingReplyIfNeeded(conversationId: String, userText: String) {
        val conv = conversationDao.getConversationByIdOnce(conversationId) ?: return
        if (conv.isAiAssistant) {
            delay(600)
            val prompt = "Tu es l'assistant IA de l'application de messagerie I3. Réponds de manière concise, élégante, expressive et amicale en français au message suivant de l'utilisateur: \"$userText\""
            val fallback = when {
                userText.contains("salut", ignoreCase = true) || userText.contains("bonjour", ignoreCase = true) ->
                    "Bonjour ! Ravi de discuter avec vous sur I3. Comment puis-je vous assister aujourd'hui ?"
                userText.contains("merci", ignoreCase = true) ->
                    "Avec grand plaisir ! N'hésitez pas si vous avez d'autres questions."
                userText.contains("thème", ignoreCase = true) || userText.contains("couleur", ignoreCase = true) ->
                    "Vous pouvez personnaliser les 9 thèmes Material 3 Expressive dans l'onglet Paramètres !"
                else ->
                    "J'ai bien reçu votre message ! Avec les fonctionnalités RCS d'I3, vous bénéficiez de communications ultra-rapides et sécurisées."
            }

            val replyText = GeminiClient.generateText(prompt, fallback)
            val replyMessage = MessageEntity(
                id = "msg_${UUID.randomUUID()}",
                conversationId = conversationId,
                senderName = conv.title,
                isFromMe = false,
                text = replyText,
                timestamp = System.currentTimeMillis(),
                status = "READ",
                reactionsJson = ""
            )
            messageDao.insertMessage(replyMessage)
            conversationDao.updateLastMessage(conversationId, replyText, replyMessage.timestamp)
        }
    }

    suspend fun toggleEmojiReaction(messageId: String, emoji: String, currentUserName: String = "Moi") {
        val message = messageDao.getMessageById(messageId) ?: return
        val existingReactions = parseReactions(message.reactionsJson).toMutableList()

        val index = existingReactions.indexOfFirst { it.emoji == emoji }
        if (index >= 0) {
            val item = existingReactions[index]
            if (item.isReactedByMe) {
                // Remove my reaction
                val newCount = item.count - 1
                val newUsers = item.userNames.filter { it != currentUserName }
                if (newCount <= 0) {
                    existingReactions.removeAt(index)
                } else {
                    existingReactions[index] = item.copy(
                        count = newCount,
                        userNames = newUsers,
                        isReactedByMe = false
                    )
                }
            } else {
                // Add my reaction to existing
                existingReactions[index] = item.copy(
                    count = item.count + 1,
                    userNames = item.userNames + currentUserName,
                    isReactedByMe = true
                )
            }
        } else {
            // New emoji reaction
            existingReactions.add(
                EmojiReaction(
                    emoji = emoji,
                    count = 1,
                    userNames = listOf(currentUserName),
                    isReactedByMe = true
                )
            )
        }

        val json = reactionAdapter.toJson(existingReactions)
        messageDao.updateReactions(messageId, json)
    }

    fun parseReactions(json: String): List<EmojiReaction> {
        if (json.isBlank()) return emptyList()
        return try {
            reactionAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun rewriteTextWithAi(text: String, tone: String): String {
        if (text.isBlank()) return text
        val prompt = "Réécris ce court message de messagerie dans un style $tone en français. Ne retourne QUE le texte réécrit, sans explications ni guillemets:\n\"$text\""
        val fallback = when (tone) {
            "Formel" -> "Bonjour, je vous confirme la bonne réception et vous tiendrai informé dans les plus brefs délais."
            "Décontracté" -> "Hey ! C'est tout bon de mon côté, on se capte vite 😎"
            "Poétique" -> "Que ces quelques mots portent l'éclat d'une belle harmonie ✨"
            "Court & Percutant" -> "Parfait, validé !"
            else -> text
        }
        return GeminiClient.generateText(prompt, fallback)
    }

    suspend fun getSmartReplies(lastMessage: String): List<String> {
        if (lastMessage.isBlank()) return listOf("Oui !", "D'accord 👍", "À plus tard")
        val prompt = "Génère 3 suggestions de réponses courtes et naturelles en français pour répondre à ce message de chat: \"$lastMessage\". Retourne uniquement 3 lignes avec une suggestion par ligne sans numérotation."
        val fallback = "Super ! 👍\nJe regarde ça de suite\nOn se voit bientôt ! ✨"
        val response = GeminiClient.generateText(prompt, fallback)
        return response.lines().map { it.trim().trim('"', '-', '*', '1', '2', '3', '.') }.filter { it.isNotBlank() }.take(3)
    }

    suspend fun summarizeAudio(durationSec: Int): String {
        val prompt = "Génère un résumé court et expressif en une phrase d'un message vocal de $durationSec secondes."
        val fallback = "Note vocale : Confirmation du rendez-vous et partage des derniers détails du projet."
        return GeminiClient.generateText(prompt, fallback)
    }

    suspend fun createOrGetConversationForContact(contact: ContactEntity): String {
        val convId = "conv_${contact.phoneNumber.replace("+", "").replace(" ", "")}"
        val existing = conversationDao.getConversationByIdOnce(convId)
        if (existing == null) {
            val newConv = ConversationEntity(
                id = convId,
                title = contact.name,
                phoneNumber = contact.phoneNumber,
                avatarColorHex = contact.avatarColorHex,
                avatarInitial = contact.name.take(1).uppercase(),
                lastMessageText = "Discussion RCS démarrée",
                lastMessageTime = System.currentTimeMillis(),
                unreadCount = 0,
                isPinned = false,
                isArchived = false,
                isRcs = contact.isRcsActive
            )
            conversationDao.insertOrUpdate(newConv)
        }
        return convId
    }

    suspend fun addCustomContact(name: String, phoneNumber: String, email: String = ""): ContactEntity {
        val colors = listOf("#4F46E5", "#F95738", "#059669", "#7C3AED", "#D97706", "#DB2777", "#0284C7")
        val randomColor = colors.random()
        val contact = ContactEntity(
            id = "custom_${UUID.randomUUID()}",
            name = name.trim(),
            phoneNumber = phoneNumber.trim(),
            email = email.trim(),
            avatarColorHex = randomColor,
            isRcsActive = true,
            statusMessage = "Contact I3"
        )
        contactDao.insertContact(contact)
        return contact
    }

    suspend fun deleteContact(id: String) {
        contactDao.deleteContact(id)
    }

    private suspend fun seedInitialDataIfNeeded() {
        // Purge any previously seeded fake test contacts & conversations
        contactDao.deleteFakeContacts()
        conversationDao.deleteFakeConversations()
        messageDao.deleteFakeMessages()

        // Initialize AI Assistant conversation only if it does not exist
        val convAi = conversationDao.getConversationByIdOnce("conv_ai")
        if (convAi == null) {
            val aiConv = ConversationEntity(
                id = "conv_ai",
                title = "Assistant IA I3",
                phoneNumber = "IA-3000",
                avatarColorHex = "#7C3AED",
                avatarInitial = "✨",
                lastMessageText = "Bonjour ! Je suis votre copilote intelligent. Comment puis-je vous aider ?",
                lastMessageTime = System.currentTimeMillis(),
                unreadCount = 0,
                isPinned = false,
                isArchived = false,
                isRcs = true,
                isAiAssistant = true
            )
            conversationDao.insertOrUpdate(aiConv)

            val aiWelcome = MessageEntity(
                id = "m_ai_welcome",
                conversationId = "conv_ai",
                senderName = "Assistant IA I3",
                isFromMe = false,
                text = "Bienvenue sur I3 ! Je suis votre copilote intelligent. Je peux résumer vos notes vocales, réécrire vos messages ou répondre à vos questions.",
                timestamp = System.currentTimeMillis(),
                status = "READ",
                reactionsJson = ""
            )
            messageDao.insertMessage(aiWelcome)
        }
    }
}
