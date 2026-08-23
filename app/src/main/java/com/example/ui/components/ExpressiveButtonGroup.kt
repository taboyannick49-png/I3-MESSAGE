package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * M3 Expressive: BoutonWithAnimatedShapeSample
 * Button whose shape morphs dynamically when pressed or active, using spring physics.
 */
@Composable
fun ExpressiveAnimatedShapeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    shape: Shape? = null,
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 12.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonShapeCorner"
    )

    val scale by animateDpAsState(
        targetValue = if (isPressed) 0.94f.dp else 1.0f.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "buttonScale"
    )

    val currentShape = shape ?: RoundedCornerShape(cornerRadius)

    Surface(
        onClick = onClick,
        modifier = modifier
            .scale(scale.value)
            .minimumInteractiveComponentSize(),
        shape = currentShape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = if (isPressed) 2.dp else 6.dp,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

/**
 * M3 Expressive: ButtonGroupSample with size contrasts (small, medium, large, extra-large)
 * Used to emphasize the key action among a group of connected actions.
 */
@Composable
fun <T> ExpressiveButtonGroup(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    getLabel: (T) -> String,
    getIcon: ((T) -> ImageVector)? = null,
    highlightKeyOption: ((T) -> Boolean)? = null
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(32.dp)),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .padding(4.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            options.forEach { option ->
                val isSelected = option == selectedOption
                val isKeyAction = highlightKeyOption?.invoke(option) ?: false

                val animatedColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (isKeyAction) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                    } else {
                        Color.Transparent
                    },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    label = "btnGroupColor"
                )

                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        if (isKeyAction) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    label = "btnGroupContentColor"
                )

                // Size contrast for Expressive design: key action or selected action is wider
                val weight = if (isSelected || isKeyAction) 1.3f else 1.0f

                Box(
                    modifier = Modifier
                        .weight(weight)
                        .clip(RoundedCornerShape(24.dp))
                        .background(animatedColor)
                        .clickable { onOptionSelected(option) }
                        .padding(vertical = if (isKeyAction) 12.dp else 10.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        if (getIcon != null) {
                            Icon(
                                imageVector = getIcon(option),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = contentColor
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = getLabel(option),
                            style = if (isKeyAction || isSelected) MaterialTheme.typography.labelLarge else MaterialTheme.typography.labelMedium,
                            color = contentColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
