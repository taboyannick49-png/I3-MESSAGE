package com.example.ui.theme

import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

// High Density / M3 Expressive asymmetrical message bubbles
val SentBubbleShape = RoundedCornerShape(
    topStart = 24.dp,
    topEnd = 4.dp,
    bottomStart = 24.dp,
    bottomEnd = 24.dp
)

val ReceivedBubbleShape = RoundedCornerShape(
    topStart = 4.dp,
    topEnd = 24.dp,
    bottomStart = 24.dp,
    bottomEnd = 24.dp
)

val VoiceNoteShape = RoundedCornerShape(24.dp)
val PillShape = RoundedCornerShape(50)
val SquircleShape = RoundedCornerShape(CornerSize(35))
val HighDensityFabShape = RoundedCornerShape(18.dp)
