package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * M3 Expressive Draggable Split Divider (inspired by Play Store & Android Dual-Pane / Foldable Split Handle)
 * Allows smooth horizontal dragging to resize the left and right panes on 10-inch tablets and foldables.
 */
@Composable
fun ExpressiveSplitDivider(
    splitRatio: Float,
    onSplitRatioChange: (Float) -> Unit,
    totalWidthPx: Float,
    modifier: Modifier = Modifier,
    minRatio: Float = 0.22f,
    maxRatio: Float = 0.78f,
    onResetToDefault: () -> Unit = { onSplitRatioChange(0.38f) }
) {
    var isDragging by remember { mutableStateOf(false) }

    val handleWidth by animateDpAsState(
        targetValue = if (isDragging) 8.dp else 5.dp,
        animationSpec = spring(),
        label = "handleWidth"
    )

    val handleColor by animateColorAsState(
        targetValue = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = spring(),
        label = "handleColor"
    )

    val containerBg by animateColorAsState(
        targetValue = if (isDragging) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainerLow,
        animationSpec = spring(),
        label = "containerBg"
    )

    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(24.dp)
            .background(containerBg)
            .pointerInput(totalWidthPx) {
                detectHorizontalDragGestures(
                    onDragStart = { isDragging = true },
                    onDragEnd = { isDragging = false },
                    onDragCancel = { isDragging = false },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        if (totalWidthPx > 0) {
                            val deltaRatio = dragAmount / totalWidthPx
                            val newRatio = (splitRatio + deltaRatio).coerceIn(minRatio, maxRatio)
                            onSplitRatioChange(newRatio)
                        }
                    }
                )
            }
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        onResetToDefault()
                    }
                )
            }
            .testTag("expressive_split_divider"),
        contentAlignment = Alignment.Center
    ) {
        // Vertical guideline line
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        )

        // Floating Grip Pill Handle (PlayStore / Tablet style)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isDragging) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest,
            tonalElevation = if (isDragging) 8.dp else 3.dp,
            shadowElevation = if (isDragging) 6.dp else 2.dp,
            modifier = Modifier
                .width(14.dp)
                .height(68.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Drag Grip 3 dots
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(3.5.dp)
                            .clip(CircleShape)
                            .background(handleColor)
                    )
                    if (it < 2) {
                        Spacer(modifier = Modifier.height(5.dp))
                    }
                }
            }
        }
    }
}
