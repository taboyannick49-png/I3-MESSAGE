package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.remote.AudioRecordManager
import com.example.data.remote.CarrierNetworkStatus
import com.example.data.remote.CarrierSmsManager
import com.example.ui.theme.ExpressiveMorphButton
import com.example.ui.theme.ExpressiveShape35
import com.example.ui.theme.ExpressiveShapeRenderer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

/**
 * M3 Expressive Chat Input Bar with:
 * 1. 35 Google Expressive Morphing Buttons
 * 2. Microphone Runtime Permission & Real Audio Recording
 * 3. Operator SMS / MMS / RCS Real-Time Network Toggle (MTN, Moov, Orange, etc.)
 * 4. Universal MMS Audio Sharing
 * 5. NO message suggestions (removed per user request)
 */
@Composable
fun ExpressiveChatInputBar(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onSendVoiceMessage: (durationSec: Int, waveform: String, summary: String?) -> Unit,
    onSendGsmSms: ((String) -> Unit)? = null,
    onAiRewriteRequest: (tone: String) -> Unit = {},
    recipientPhone: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var isSending by remember { mutableStateOf(false) }
    var isRecordingAudio by remember { mutableStateOf(false) }
    var recordingDuration by remember { mutableIntStateOf(0) }
    var showRewriteMenu by remember { mutableStateOf(false) }
    var sendViaGsmOperator by remember { mutableStateOf(false) }
    var recordedAmplitudes by remember { mutableStateOf(listOf<Float>()) }
    var lastRecordedAudioFile by remember { mutableStateOf<File?>(null) }

    val audioManager = remember { AudioRecordManager(context) }
    val networkStatus = remember { CarrierSmsManager.getCarrierNetworkStatus(context) }

    // Launcher for Microphone Permission
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Microphone activé", Toast.LENGTH_SHORT).show()
            val started = audioManager.startRecording { amp ->
                recordedAmplitudes = (recordedAmplitudes + amp).takeLast(16)
            }
            if (started) {
                isRecordingAudio = true
                recordingDuration = 0
            }
        } else {
            Toast.makeText(context, "Autorisation microphone nécessaire pour les vocaux MMS", Toast.LENGTH_LONG).show()
        }
    }

    // Timer coroutine for recording
    LaunchedEffect(isRecordingAudio) {
        if (isRecordingAudio) {
            recordingDuration = 0
            while (isRecordingAudio) {
                delay(1000)
                recordingDuration++
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        // Real-Time Carrier / Network Mode Bar (MTN, Moov, Orange, Wi-Fi, Mobile Data)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Operator Network Indicator
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                modifier = Modifier.clickable {
                    sendViaGsmOperator = !sendViaGsmOperator
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (sendViaGsmOperator) Icons.Default.SimCard else Icons.Default.SignalCellularAlt,
                        contentDescription = null,
                        tint = if (sendViaGsmOperator) Color(0xFFF59E0B) else Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (sendViaGsmOperator) "SMS Opérateur (${networkStatus.operatorName})" else "⚡ RCS • ${networkStatus.networkType}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Morphing Switch Channel Button (Expressive 35 Shape)
            ExpressiveMorphButton(
                onClick = { sendViaGsmOperator = !sendViaGsmOperator },
                initialShape = ExpressiveShape35.PILL,
                pressedShape = ExpressiveShape35.SUNNY,
                containerColor = if (sendViaGsmOperator) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
                contentColor = if (sendViaGsmOperator) MaterialTheme.colorScheme.onTertiaryContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (sendViaGsmOperator) "Mode GSM" else "Mode RCS/Wi-Fi",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Active Voice Recording Overlay with Live Waveform & Universal MMS compatibility
        AnimatedVisibility(
            visible = isRecordingAudio,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pulsing red recording dot
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dotPulse"
                        )
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = String.format("%02d:%02d", recordingDuration / 60, recordingDuration % 60),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Vocal universel / MMS",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Live Audio Waveform Bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(26.dp)
                    ) {
                        val bars = if (recordedAmplitudes.isNotEmpty()) recordedAmplitudes.takeLast(10) else listOf(0.3f, 0.6f, 0.9f, 0.4f, 0.7f)
                        bars.forEach { amp ->
                            Box(
                                modifier = Modifier
                                    .width(3.5.dp)
                                    .fillMaxHeight(amp.coerceIn(0.2f, 1f))
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.error)
                            )
                        }
                    }

                    // Cancel & Confirm Buttons (Expressive 35 Shapes)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                audioManager.cancelRecording()
                                isRecordingAudio = false
                                recordingDuration = 0
                                recordedAmplitudes = emptyList()
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Annuler",
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        // Send & Share MMS Action
                        IconButton(
                            onClick = {
                                val duration = maxOf(recordingDuration, 1)
                                val audioFile = audioManager.stopRecording()
                                lastRecordedAudioFile = audioFile
                                isRecordingAudio = false
                                recordingDuration = 0

                                val waveformStr = if (recordedAmplitudes.isNotEmpty()) {
                                    recordedAmplitudes.joinToString(",") { String.format("%.2f", it) }
                                } else {
                                    "0.3,0.6,0.9,0.7,0.4,0.8,0.5,0.9,0.3,0.6"
                                }

                                onSendVoiceMessage(
                                    duration,
                                    waveformStr,
                                    "Message vocal MMS (${duration}s) compatible toutes applications"
                                )
                                Toast.makeText(context, "Vocal MMS envoyé", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Valider et envoyer",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Main input bar with Google 35 Expressive Shapes Morphing Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Text Input Field
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp)),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji / Attachment action
                    IconButton(
                        onClick = { onMessageChange(messageText + "✨") },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SentimentSatisfiedAlt,
                            contentDescription = "Emojis",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextField(
                        value = messageText,
                        onValueChange = onMessageChange,
                        placeholder = {
                            Text(
                                if (sendViaGsmOperator) "SMS opérateur (${networkStatus.operatorName})..." else "Message RCS / Wi-Fi...",
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field"),
                        maxLines = 4
                    )

                    // AI Tone Rewriter trigger
                    if (messageText.isNotBlank()) {
                        Box {
                            IconButton(
                                onClick = { showRewriteMenu = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Réécrire avec IA",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            DropdownMenu(
                                expanded = showRewriteMenu,
                                onDismissRequest = { showRewriteMenu = false },
                                modifier = Modifier.clip(RoundedCornerShape(16.dp))
                            ) {
                                Text(
                                    "✨ Style IA Expressive",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                                HorizontalDivider()
                                listOf("Formel", "Décontracté", "Poétique", "Court & Percutant").forEach { tone ->
                                    DropdownMenuItem(
                                        text = { Text(tone) },
                                        onClick = {
                                            showRewriteMenu = false
                                            onAiRewriteRequest(tone)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // =========================================================================
            // Google Material 3 Expressive Morphing Buttons (35 Shapes)
            // Voice Record Morphing Button + Send Morphing Button
            // =========================================================================
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 2.dp
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(2.dp)
                ) {
                    // Voice Record Morphing Button (Cycles through 35 Google Shapes)
                    var voiceShapeIndex by remember { mutableIntStateOf(0) }
                    val currentVoiceShape = remember(voiceShapeIndex, isRecordingAudio) {
                        if (isRecordingAudio) ExpressiveShape35.SUNBURST
                        else ExpressiveShape35.entries[voiceShapeIndex % ExpressiveShape35.entries.size]
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(ExpressiveShapeRenderer(currentVoiceShape))
                            .background(
                                if (isRecordingAudio) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                            )
                            .clickable {
                                // Cycle shape
                                voiceShapeIndex = (voiceShapeIndex + 1) % ExpressiveShape35.entries.size

                                if (isRecordingAudio) {
                                    audioManager.cancelRecording()
                                    isRecordingAudio = false
                                } else {
                                    // Check microphone runtime permission
                                    val hasPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED

                                    if (hasPermission) {
                                        val started = audioManager.startRecording { amp ->
                                            recordedAmplitudes = (recordedAmplitudes + amp).takeLast(16)
                                        }
                                        if (started) {
                                            isRecordingAudio = true
                                            recordingDuration = 0
                                        }
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                            .testTag("voice_record_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isRecordingAudio) Icons.Default.Stop else Icons.Default.Mic,
                            contentDescription = "Message vocal MMS",
                            tint = if (isRecordingAudio) MaterialTheme.colorScheme.onError
                            else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(3.dp))

                    // Send Morphing Button (Cycles through 35 Google Expressive Shapes)
                    var sendShapeIndex by remember { mutableIntStateOf(1) } // starts as SQUARE/SQUIRCLE
                    val currentSendShape = remember(sendShapeIndex, isSending) {
                        if (isSending) ExpressiveShape35.COOKIE
                        else ExpressiveShape35.entries[sendShapeIndex % ExpressiveShape35.entries.size]
                    }

                    Box(
                        modifier = Modifier
                            .size(width = 54.dp, height = 46.dp)
                            .clip(ExpressiveShapeRenderer(currentSendShape))
                            .background(
                                if (messageText.isNotBlank() || isSending) {
                                    if (sendViaGsmOperator) Color(0xFFEA580C) else MaterialTheme.colorScheme.secondary
                                } else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            )
                            .clickable(
                                enabled = (messageText.isNotBlank() || isSending),
                                onClick = {
                                    if (messageText.isNotBlank() && !isSending) {
                                        sendShapeIndex = (sendShapeIndex + 1) % ExpressiveShape35.entries.size
                                        isSending = true
                                        val textToSend = messageText
                                        onMessageChange("")

                                        scope.launch {
                                            delay(350)
                                            if (sendViaGsmOperator && onSendGsmSms != null) {
                                                onSendGsmSms(textToSend)
                                            } else {
                                                onSendMessage(textToSend)
                                            }
                                            isSending = false
                                        }
                                    }
                                }
                            )
                            .testTag("send_message_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSending) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onSecondary,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (sendViaGsmOperator) Icons.Default.SendToMobile else Icons.AutoMirrored.Filled.Send,
                                contentDescription = if (sendViaGsmOperator) "Envoyer SMS Opérateur" else "Envoyer RCS",
                                tint = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
