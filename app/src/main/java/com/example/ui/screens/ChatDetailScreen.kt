package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.MessageEntity
import com.example.data.remote.CarrierSmsManager
import com.example.ui.components.ExpressiveChatInputBar
import com.example.ui.components.ExpressiveReactionPopup
import com.example.ui.components.ExpressiveReactionsRow
import com.example.ui.theme.*
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    chatViewModel: ChatViewModel,
    onBack: () -> Unit,
    showBackNavigation: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LaunchedEffect(conversationId) {
        chatViewModel.selectConversation(conversationId)
    }

    val conversation by chatViewModel.currentConversation.collectAsStateWithLifecycle()
    val messages by chatViewModel.currentMessages.collectAsStateWithLifecycle()

    var inputMessageText by remember { mutableStateOf("") }
    var selectedMessageForReaction by remember { mutableStateOf<String?>(null) }
    var showBubbleMenu by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val carrierStatus = remember { CarrierSmsManager.getCarrierNetworkStatus(context) }

    // Auto scroll to bottom on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val avatarColor = remember(conversation?.avatarColorHex) {
        try {
            Color(android.graphics.Color.parseColor(conversation?.avatarColorHex ?: "#4F46E5"))
        } catch (e: Exception) {
            Color(0xFF4F46E5)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { /* info */ }
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(avatarColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = conversation?.avatarInitial ?: "I",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (conversation?.isRcs == true) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = conversation?.title ?: "Discussion",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = if (conversation?.isRcs == true) "RCS • ${carrierStatus.operatorName} • Chiffré" else "SMS Opérateur • ${carrierStatus.operatorName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (conversation?.isRcs == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (showBackNavigation) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("chat_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Retour"
                            )
                        }
                    }
                },
                actions = {
                    // Google Phone Action
                    IconButton(
                        onClick = {
                            val phoneNumber = conversation?.phoneNumber?.takeIf { it.isNotBlank() } ?: "+33612345678"
                            try {
                                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${Uri.encode(phoneNumber)}")
                                }
                                context.startActivity(dialIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "Numéro : $phoneNumber", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("action_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Appel Google Téléphone",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Google Meet Action
                    IconButton(
                        onClick = {
                            try {
                                val meetIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://meet.google.com/new")).apply {
                                    setPackage("com.google.android.apps.meetings")
                                }
                                context.startActivity(meetIntent)
                            } catch (e: Exception) {
                                try {
                                    val webMeetIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://meet.google.com/new"))
                                    context.startActivity(webMeetIntent)
                                } catch (e2: Exception) {
                                    Toast.makeText(context, "Lancement de Google Meet...", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.testTag("action_videocam_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Google Meet",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Three Dots: Expressive Bubble Popover Menu
                    IconButton(
                        onClick = { showBubbleMenu = true },
                        modifier = Modifier.testTag("action_more_bubble_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options de la discussion",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            ExpressiveChatInputBar(
                messageText = inputMessageText,
                onMessageChange = { inputMessageText = it },
                onSendMessage = { text ->
                    chatViewModel.sendMessage(text)
                },
                onSendGsmSms = { text ->
                    val phone = conversation?.phoneNumber ?: ""
                    CarrierSmsManager.sendRealGsmSms(
                        context = context,
                        destinationAddress = phone,
                        text = text,
                        onSuccess = {
                            chatViewModel.sendMessage("[SMS ${carrierStatus.operatorName}] $text")
                            Toast.makeText(context, "SMS envoyé via ${carrierStatus.operatorName}", Toast.LENGTH_SHORT).show()
                        },
                        onError = { err ->
                            chatViewModel.sendMessage(text)
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                onSendVoiceMessage = { durationSec, waveform, summary ->
                    chatViewModel.sendVoiceMessage(durationSec, waveform, summary)
                },
                onAiRewriteRequest = { tone ->
                    chatViewModel.rewriteWithTone(inputMessageText, tone) { rewritten ->
                        inputMessageText = rewritten
                    }
                },
                recipientPhone = conversation?.phoneNumber ?: ""
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Network & Encryption status badge banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.8f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Réseau ${carrierStatus.operatorName} • Messages RCS & Vocaux MMS universels",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                items(messages, key = { it.id }) { message ->
                    ExpressiveMessageBubble(
                        message = message,
                        reactions = chatViewModel.parseReactions(message.reactionsJson),
                        recipientPhone = conversation?.phoneNumber ?: "",
                        onLongPress = {
                            selectedMessageForReaction = message.id
                        },
                        onReactionClick = { emoji ->
                            chatViewModel.toggleReaction(message.id, emoji)
                        }
                    )
                }
            }

            // Emoji Reaction Popup triggered on long-press
            ExpressiveReactionPopup(
                visible = selectedMessageForReaction != null,
                onDismiss = { selectedMessageForReaction = null },
                onEmojiSelected = { emoji ->
                    selectedMessageForReaction?.let { msgId ->
                        chatViewModel.toggleReaction(msgId, emoji)
                    }
                },
                modifier = Modifier.align(Alignment.Center)
            )

            // Custom Expressive Bubble Menu Dialog (Requested by user)
            if (showBubbleMenu) {
                Dialog(onDismissRequest = { showBubbleMenu = false }) {
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        tonalElevation = 10.dp,
                        shadowElevation = 14.dp,
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .clip(RoundedCornerShape(26.dp))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(26.dp)
                            )
                            .testTag("expressive_bubble_menu_dialog")
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {
                            // Header of the Bubble Menu
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.ChatBubble,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = conversation?.title ?: "Discussion",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Actions rapides",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            Spacer(modifier = Modifier.height(12.dp))

                            // Choice 1: Supprimer la discussion
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showBubbleMenu = false
                                        showDeleteConfirmDialog = true
                                    }
                                    .testTag("bubble_menu_delete_option")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteForever,
                                        contentDescription = "Supprimer la discussion",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Supprimer la discussion",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                        Text(
                                            text = "Efface tous les messages de cette conversation",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Choice 2: Archiver / Désarchiver
                            val isArchived = conversation?.isArchived == true
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showBubbleMenu = false
                                        conversation?.let {
                                            chatViewModel.toggleArchive(it.id, it.isArchived)
                                             Toast.makeText(
                                                context,
                                                if (isArchived) "Discussion désarchivée" else "Discussion archivée",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            onBack()
                                        }
                                    }
                                    .testTag("bubble_menu_archive_option")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isArchived) Icons.Outlined.Unarchive else Icons.Outlined.Archive,
                                        contentDescription = if (isArchived) "Désarchiver" else "Archiver",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (isArchived) "Désarchiver la discussion" else "Archiver la discussion",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isArchived) "Replacer dans la boîte de réception" else "Masquer de la liste principale",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Choice 3: Activer / Désactiver le chat RCS
                            val isRcs = conversation?.isRcs == true
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        showBubbleMenu = false
                                        conversation?.let {
                                            chatViewModel.toggleRcs(it.id, it.isRcs)
                                            Toast.makeText(
                                                context,
                                                if (isRcs) "Chat RCS désactivé (Mode SMS standard)" else "Chat RCS activé ⚡",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    .testTag("bubble_menu_rcs_option")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "RCS",
                                        tint = if (isRcs) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isRcs) "Désactiver le chat RCS" else "Activer le chat RCS",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isRcs) "Basculer vers les SMS standards" else "Profitez du chiffrement et des accusés de lecture",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = isRcs,
                                        onCheckedChange = {
                                            showBubbleMenu = false
                                            conversation?.let { conv ->
                                                chatViewModel.toggleRcs(conv.id, conv.isRcs)
                                            }
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Close Button (Google Expressive 35 Shape)
                            ExpressiveMorphButton(
                                onClick = { showBubbleMenu = false },
                                initialShape = ExpressiveShape35.ROUNDED_RECT_MEDIUM,
                                pressedShape = ExpressiveShape35.SUNNY,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Fermer", style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    }
                }
            }

            // Confirmation Dialog for Deletion
            if (showDeleteConfirmDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirmDialog = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.DeleteForever,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    title = {
                        Text(
                            text = "Supprimer la discussion ?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = "Cette action supprimera définitivement tous les messages de cette conversation.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteConfirmDialog = false
                                conversation?.let { conv ->
                                    chatViewModel.deleteConversation(conv.id)
                                    Toast.makeText(context, "Discussion supprimée", Toast.LENGTH_SHORT).show()
                                    onBack()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("Supprimer")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirmDialog = false }) {
                            Text("Annuler")
                        }
                    },
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpressiveMessageBubble(
    message: MessageEntity,
    reactions: List<com.example.data.model.EmojiReaction>,
    recipientPhone: String = "",
    onLongPress: () -> Unit,
    onReactionClick: (String) -> Unit
) {
    val isMine = message.isFromMe
    val alignment = if (isMine) Alignment.End else Alignment.Start
    val bubbleShape = if (isMine) SentBubbleShape else ReceivedBubbleShape

    val timeFormatted = remember(message.timestamp) {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        sdf.format(Date(message.timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        // Message Content Bubble
        Surface(
            shape = bubbleShape,
            color = if (isMine) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
            tonalElevation = if (isMine) 3.dp else 1.dp,
            modifier = Modifier
                .widthIn(max = 310.dp)
                .clip(bubbleShape)
                .combinedClickable(
                    onClick = { /* tap */ },
                    onLongClick = onLongPress
                )
                .testTag("message_bubble_${message.id}")
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (message.isAudio) {
                    // Expressive Audio Message Component
                    ExpressiveAudioBubbleContent(
                        durationSec = message.audioDurationSec,
                        waveformStr = message.audioWaveform,
                        summary = message.audioSummary,
                        recipientPhone = recipientPhone,
                        isMine = isMine
                    )
                } else {
                    // Text Message
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Time and Status
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isMine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )

                    if (isMine) {
                        val statusIcon = when (message.status) {
                            "SENDING" -> Icons.Default.Schedule
                            "SENT" -> Icons.Default.Check
                            "DELIVERED" -> Icons.Default.DoneAll
                            "READ" -> Icons.Default.DoneAll
                            else -> Icons.Default.DoneAll
                        }
                        val tint = if (message.status == "READ") {
                            Color(0xFF67E8F9) // Cyan double check
                        } else {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        }

                        Icon(
                            imageVector = statusIcon,
                            contentDescription = message.status,
                            tint = tint,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // Attached Reaction Badges
        ExpressiveReactionsRow(
            reactions = reactions,
            onReactionClick = onReactionClick,
            modifier = Modifier.align(alignment)
        )
    }
}

/**
 * Expressive Voice Note Bubble with scrubber, Playback, and MMS Universal Sharing
 */
@Composable
fun ExpressiveAudioBubbleContent(
    durationSec: Int,
    waveformStr: String,
    summary: String?,
    recipientPhone: String = "",
    isMine: Boolean
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var currentProgress by remember { mutableFloatStateOf(0f) }

    val waveformValues = remember(waveformStr) {
        if (waveformStr.isNotBlank()) {
            waveformStr.split(",").mapNotNull { it.trim().toFloatOrNull() }
        } else {
            listOf(0.3f, 0.6f, 0.9f, 0.4f, 0.7f, 0.8f, 0.5f, 0.9f, 0.3f, 0.6f)
        }
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val totalSteps = 20
            for (i in 0..totalSteps) {
                currentProgress = i.toFloat() / totalSteps
                kotlinx.coroutines.delay((durationSec * 1000L) / totalSteps)
            }
            isPlaying = false
            currentProgress = 0f
        }
    }

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Play / Pause Button
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isMine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.primaryContainer
                    )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Lecture audio",
                    tint = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Waveform Graphic Bars
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(28.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                waveformValues.forEachIndexed { index, amp ->
                    val fraction = (index.toFloat() / waveformValues.size)
                    val isPast = isPlaying && currentProgress >= fraction

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(amp.coerceIn(0.25f, 1f))
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (isMine) {
                                    if (isPast) Color.White else Color.White.copy(alpha = 0.45f)
                                } else {
                                    if (isPast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                }
                            )
                    )
                }
            }

            // Duration
            Text(
                text = String.format("%02d:%02d", durationSec / 60, durationSec % 60),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isMine) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )

            // MMS Share / Export Button (universal compatibility)
            IconButton(
                onClick = {
                    val dummyAudio = File(context.cacheDir, "sample_voice.m4a").apply {
                        if (!exists()) createNewFile()
                    }
                    CarrierSmsManager.shareVoiceAsMms(
                        context = context,
                        audioFile = dummyAudio,
                        phoneNumber = recipientPhone,
                        messageText = "Message vocal MMS I3 (${durationSec}s)"
                    )
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Share,
                    contentDescription = "Partager MMS",
                    tint = if (isMine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // AI Summary Banner if present
        if (!summary.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isMine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (isMine) Color.White else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isMine) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
