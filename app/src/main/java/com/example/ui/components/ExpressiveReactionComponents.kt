package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.data.model.EmojiReaction

val ExpressiveEmojiList = listOf("👍", "❤️", "😂", "😮", "😢", "🔥", "✨", "🚀")

/**
 * Floating Animated Reaction Bar displayed above message bubble upon long-press
 */
@Composable
fun ExpressiveReactionPopup(
    visible: Boolean,
    onDismiss: () -> Unit,
    onEmojiSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (visible) {
        Popup(
            alignment = Alignment.TopCenter,
            onDismissRequest = onDismiss,
            properties = PopupProperties(focusable = true)
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(32.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ExpressiveEmojiList.forEach { emoji ->
                        ExpressiveEmojiItem(
                            emoji = emoji,
                            onClick = {
                                onEmojiSelected(emoji)
                                onDismiss()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpressiveEmojiItem(
    emoji: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.45f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "emojiScale"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = 20.dp),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 22.sp
        )
    }
}

/**
 * Attached Emoji Reaction Badges attached to message bubbles
 * Shows emojis and user counts visible to all participants
 */
@Composable
fun ExpressiveReactionsRow(
    reactions: List<EmojiReaction>,
    onReactionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (reactions.isEmpty()) return

    Row(
        modifier = modifier.padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        reactions.forEach { reaction ->
            val isMine = reaction.isReactedByMe
            Surface(
                onClick = { onReactionClick(reaction.emoji) },
                shape = RoundedCornerShape(16.dp),
                color = if (isMine) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = if (isMine) 3.dp else 1.dp,
                modifier = Modifier.clip(RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = reaction.emoji,
                        fontSize = 14.sp
                    )
                    if (reaction.count > 1 || reaction.userNames.isNotEmpty()) {
                        Text(
                            text = "${reaction.count}",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isMine) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
