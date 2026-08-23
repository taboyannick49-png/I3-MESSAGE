package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

data class FabMenuItem(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val containerColor: Color? = null
)

/**
 * M3 Expressive: FAB Menu with Morphing Shapes and Spring Transitions
 */
@Composable
fun ExpressiveFabMenu(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 135f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fabRotation"
    )

    val fabCorner by animateDpAsState(
        targetValue = if (expanded) 16.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "fabShapeMorph"
    )

    val items = listOf(
        FabMenuItem("new_rcs", "Nouveau message RCS", Icons.Default.ChatBubble, null),
        FabMenuItem("new_group", "Nouveau groupe RCS", Icons.Default.GroupAdd, null),
        FabMenuItem("ai_chat", "Discuter avec l'IA I3", Icons.Default.AutoAwesome, null),
        FabMenuItem("scan_qr", "Scanner un contact QR", Icons.Default.QrCodeScanner, null)
    )

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.padding(16.dp)
    ) {
        // Expanded Menu Items with staggered spring animations
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMedium)) + expandVertically(
                spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
            ),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items.forEach { item ->
                    Surface(
                        onClick = {
                            onExpandedChange(false)
                            onItemClick(item.id)
                        },
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        tonalElevation = 6.dp,
                        shadowElevation = 4.dp,
                        modifier = Modifier.clip(RoundedCornerShape(20.dp))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Primary Morphing FAB (High Density Accent)
        FloatingActionButton(
            onClick = { onExpandedChange(!expanded) },
            shape = RoundedCornerShape(fabCorner),
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
            modifier = Modifier
                .size(56.dp)
                .testTag("expressive_fab_button")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Actions",
                modifier = Modifier
                    .size(28.dp)
                    .rotate(rotation)
            )
        }
    }
}
