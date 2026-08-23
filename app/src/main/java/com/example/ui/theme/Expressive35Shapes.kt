package com.example.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Google Material 3 Expressive Collection of 35 Distinct Shapes
 */
enum class ExpressiveShape35(val displayName: String, val category: String) {
    CIRCLE("Cercle", "Géométrique"),
    SQUARE("Carré", "Géométrique"),
    SLANTED_RECT("Rectangle Biseauté", "Expressif"),
    ARCH("Arche / Dôme", "Architectural"),
    SEMI_CIRCLE("Demi-Cercle", "Géométrique"),
    PILL("Pilule", "Classique"),
    OVAL("Ovale", "Classique"),
    SQUIRCLE("Squircle", "Moderne"),
    ROUNDED_RECT_SOFT("Rectangle Doux", "Coins"),
    ROUNDED_RECT_MEDIUM("Rectangle Moyen", "Coins"),
    ROUNDED_RECT_LARGE("Rectangle Large", "Coins"),
    CUT_CORNER("Coins Coupés", "Coins"),
    DIAMOND("Diamant / Losange", "Polygonal"),
    STAR_4("Étoile 4 Branches", "Étoilé"),
    STAR_5("Étoile 5 Branches", "Étoilé"),
    STAR_8("Étoile 8 Branches", "Étoilé"),
    STAR_12("Étoile 12 Branches", "Étoilé"),
    SUNBURST("Rayon de Soleil", "Solaire"),
    SUNNY("Sunny M3 (Étoile Adoucie)", "Solaire"),
    CLOVER_4("Trèfle 4 Pétales", "Floral"),
    FLOWER_5("Fleur 5 Pétales", "Floral"),
    FLOWER_8("Fleur 8 Pétales", "Floral"),
    SCALLOP_4("Festons 4", "Festons"),
    SCALLOP_8("Festons 8", "Festons"),
    SCALLOP_12("Festons 12", "Festons"),
    HEXAGON("Hexagone", "Polygonal"),
    OCTAGON("Octogone", "Polygonal"),
    PENTAGON("Pentagone", "Polygonal"),
    SHIELD("Bouclier", "Expressif"),
    BADGE_RIBBON("Ruban Badge", "Expressif"),
    LEMON("Citron / Marquise", "Organique"),
    COOKIE("Cookie Ondulé", "Organique"),
    TEARDROP("Goutte d'eau", "Organique"),
    SPEECH_BUBBLE("Bulle de Dialogue", "Messagerie"),
    ASYMMETRIC_LEAF("Feuille Asymétrique", "Organique")
}

/**
 * Custom Compose Shape generating Google's 35 Material 3 Expressive Shapes
 */
class ExpressiveShapeRenderer(val type: ExpressiveShape35) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = minOf(w, h) / 2f

        val path = Path()

        when (type) {
            ExpressiveShape35.CIRCLE -> {
                path.addOval(Rect(0f, 0f, w, h))
            }
            ExpressiveShape35.SQUARE -> {
                path.addRect(Rect(0f, 0f, w, h))
            }
            ExpressiveShape35.SLANTED_RECT -> {
                val slant = w * 0.18f
                path.moveTo(slant, 0f)
                path.lineTo(w, 0f)
                path.lineTo(w - slant, h)
                path.lineTo(0f, h)
                path.close()
            }
            ExpressiveShape35.ARCH -> {
                path.moveTo(0f, h)
                path.lineTo(0f, cy)
                path.arcTo(Rect(0f, 0f, w, h), 180f, 180f, false)
                path.lineTo(w, h)
                path.close()
            }
            ExpressiveShape35.SEMI_CIRCLE -> {
                path.moveTo(0f, cy)
                path.arcTo(Rect(0f, 0f, w, h), 180f, 180f, false)
                path.close()
            }
            ExpressiveShape35.PILL -> {
                val radius = minOf(w, h) / 2f
                if (w >= h) {
                    path.moveTo(radius, 0f)
                    path.lineTo(w - radius, 0f)
                    path.arcTo(Rect(w - 2 * radius, 0f, w, h), -90f, 180f, false)
                    path.lineTo(radius, h)
                    path.arcTo(Rect(0f, 0f, 2 * radius, h), 90f, 180f, false)
                } else {
                    path.moveTo(0f, radius)
                    path.lineTo(0f, h - radius)
                    path.arcTo(Rect(0f, h - 2 * radius, w, h), 180f, -180f, false)
                    path.lineTo(w, radius)
                    path.arcTo(Rect(0f, 0f, w, 2 * radius), 0f, -180f, false)
                }
                path.close()
            }
            ExpressiveShape35.OVAL -> {
                path.addOval(Rect(0f, 0f, w, h))
            }
            ExpressiveShape35.SQUIRCLE -> {
                val corner = r * 0.55f
                path.moveTo(corner, 0f)
                path.lineTo(w - corner, 0f)
                path.cubicTo(w - corner * 0.3f, 0f, w, corner * 0.3f, w, corner)
                path.lineTo(w, h - corner)
                path.cubicTo(w, h - corner * 0.3f, w - corner * 0.3f, h, w - corner, h)
                path.lineTo(corner, h)
                path.cubicTo(corner * 0.3f, h, 0f, h - corner * 0.3f, 0f, h - corner)
                path.lineTo(0f, corner)
                path.cubicTo(0f, corner * 0.3f, corner * 0.3f, 0f, corner, 0f)
                path.close()
            }
            ExpressiveShape35.ROUNDED_RECT_SOFT -> {
                val cr = minOf(w, h) * 0.2f
                path.addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(0f, 0f, w, h), androidx.compose.ui.geometry.CornerRadius(cr, cr)))
            }
            ExpressiveShape35.ROUNDED_RECT_MEDIUM -> {
                val cr = minOf(w, h) * 0.35f
                path.addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(0f, 0f, w, h), androidx.compose.ui.geometry.CornerRadius(cr, cr)))
            }
            ExpressiveShape35.ROUNDED_RECT_LARGE -> {
                val cr = minOf(w, h) * 0.48f
                path.addRoundRect(androidx.compose.ui.geometry.RoundRect(Rect(0f, 0f, w, h), androidx.compose.ui.geometry.CornerRadius(cr, cr)))
            }
            ExpressiveShape35.CUT_CORNER -> {
                val cut = minOf(w, h) * 0.28f
                path.moveTo(cut, 0f)
                path.lineTo(w - cut, 0f)
                path.lineTo(w, cut)
                path.lineTo(w, h - cut)
                path.lineTo(w - cut, h)
                path.lineTo(cut, h)
                path.lineTo(0f, h - cut)
                path.lineTo(0f, cut)
                path.close()
            }
            ExpressiveShape35.DIAMOND -> {
                path.moveTo(cx, 0f)
                path.lineTo(w, cy)
                path.lineTo(cx, h)
                path.lineTo(0f, cy)
                path.close()
            }
            ExpressiveShape35.STAR_4 -> {
                drawStar(path, cx, cy, 4, r, r * 0.42f)
            }
            ExpressiveShape35.STAR_5 -> {
                drawStar(path, cx, cy, 5, r, r * 0.48f)
            }
            ExpressiveShape35.STAR_8 -> {
                drawStar(path, cx, cy, 8, r, r * 0.65f)
            }
            ExpressiveShape35.STAR_12 -> {
                drawStar(path, cx, cy, 12, r, r * 0.78f)
            }
            ExpressiveShape35.SUNBURST -> {
                drawStar(path, cx, cy, 16, r, r * 0.72f)
            }
            ExpressiveShape35.SUNNY -> {
                // 8-point smooth rounded star
                drawRoundedStar(path, cx, cy, 8, r, r * 0.75f)
            }
            ExpressiveShape35.CLOVER_4 -> {
                drawPetals(path, cx, cy, 4, r, r * 0.55f)
            }
            ExpressiveShape35.FLOWER_5 -> {
                drawPetals(path, cx, cy, 5, r, r * 0.52f)
            }
            ExpressiveShape35.FLOWER_8 -> {
                drawPetals(path, cx, cy, 8, r, r * 0.7f)
            }
            ExpressiveShape35.SCALLOP_4 -> {
                drawScallop(path, cx, cy, 4, r, r * 0.6f)
            }
            ExpressiveShape35.SCALLOP_8 -> {
                drawScallop(path, cx, cy, 8, r, r * 0.78f)
            }
            ExpressiveShape35.SCALLOP_12 -> {
                drawScallop(path, cx, cy, 12, r, r * 0.85f)
            }
            ExpressiveShape35.HEXAGON -> {
                drawPolygon(path, cx, cy, 6, r)
            }
            ExpressiveShape35.OCTAGON -> {
                drawPolygon(path, cx, cy, 8, r)
            }
            ExpressiveShape35.PENTAGON -> {
                drawPolygon(path, cx, cy, 5, r)
            }
            ExpressiveShape35.SHIELD -> {
                path.moveTo(0f, 0f)
                path.lineTo(w, 0f)
                path.lineTo(w, h * 0.55f)
                path.cubicTo(w, h * 0.85f, cx, h, cx, h)
                path.cubicTo(cx, h, 0f, h * 0.85f, 0f, h * 0.55f)
                path.close()
            }
            ExpressiveShape35.BADGE_RIBBON -> {
                path.moveTo(0f, 0f)
                path.lineTo(w, 0f)
                path.lineTo(w, h * 0.82f)
                path.lineTo(cx, h)
                path.lineTo(0f, h * 0.82f)
                path.close()
            }
            ExpressiveShape35.LEMON -> {
                path.moveTo(0f, cy)
                path.cubicTo(0f, 0f, w, 0f, w, cy)
                path.cubicTo(w, h, 0f, h, 0f, cy)
                path.close()
            }
            ExpressiveShape35.COOKIE -> {
                drawPetals(path, cx, cy, 10, r, r * 0.82f)
            }
            ExpressiveShape35.TEARDROP -> {
                path.moveTo(cx, 0f)
                path.cubicTo(w, cy * 0.6f, w, h, cx, h)
                path.cubicTo(0f, h, 0f, cy * 0.6f, cx, 0f)
                path.close()
            }
            ExpressiveShape35.SPEECH_BUBBLE -> {
                val cr = r * 0.45f
                path.moveTo(cr, 0f)
                path.lineTo(w - cr, 0f)
                path.cubicTo(w, 0f, w, cr, w, cr)
                path.lineTo(w, h - cr * 1.5f)
                path.cubicTo(w, h - cr * 0.5f, w - cr, h - cr * 0.5f, w - cr, h - cr * 0.5f)
                path.lineTo(w * 0.4f, h - cr * 0.5f)
                path.lineTo(w * 0.2f, h)
                path.lineTo(w * 0.25f, h - cr * 0.5f)
                path.lineTo(cr, h - cr * 0.5f)
                path.cubicTo(0f, h - cr * 0.5f, 0f, h - cr * 1.5f, 0f, h - cr * 1.5f)
                path.lineTo(0f, cr)
                path.cubicTo(0f, 0f, cr, 0f, cr, 0f)
                path.close()
            }
            ExpressiveShape35.ASYMMETRIC_LEAF -> {
                path.moveTo(0f, 0f)
                path.cubicTo(w * 0.8f, 0f, w, h * 0.3f, w, h)
                path.cubicTo(w * 0.2f, h, 0f, h * 0.7f, 0f, 0f)
                path.close()
            }
        }

        return Outline.Generic(path)
    }

    private fun drawStar(path: Path, cx: Float, cy: Float, points: Int, outerRadius: Float, innerRadius: Float) {
        val step = (PI / points).toFloat()
        var angle = -PI.toFloat() / 2f
        path.moveTo(cx + outerRadius * cos(angle), cy + outerRadius * sin(angle))
        for (i in 0 until points) {
            angle += step
            path.lineTo(cx + innerRadius * cos(angle), cy + innerRadius * sin(angle))
            angle += step
            path.lineTo(cx + outerRadius * cos(angle), cy + outerRadius * sin(angle))
        }
        path.close()
    }

    private fun drawRoundedStar(path: Path, cx: Float, cy: Float, points: Int, outerRadius: Float, innerRadius: Float) {
        val step = (2 * PI / points).toFloat()
        var angle = -PI.toFloat() / 2f
        for (i in 0 until points) {
            val px1 = cx + outerRadius * cos(angle)
            val py1 = cy + outerRadius * sin(angle)
            val midAngle = angle + step / 2f
            val pxMid = cx + innerRadius * cos(midAngle)
            val pyMid = cy + innerRadius * sin(midAngle)
            val nextAngle = angle + step
            val px2 = cx + outerRadius * cos(nextAngle)
            val py2 = cy + outerRadius * sin(nextAngle)

            if (i == 0) {
                path.moveTo(px1, py1)
            }
            path.quadraticTo(pxMid, pyMid, px2, py2)
            angle += step
        }
        path.close()
    }

    private fun drawPetals(path: Path, cx: Float, cy: Float, count: Int, outerRadius: Float, innerRadius: Float) {
        val step = (2 * PI / count).toFloat()
        var angle = -PI.toFloat() / 2f
        for (i in 0 until count) {
            val startAngle = angle
            val endAngle = angle + step
            val midAngle = angle + step / 2f
            val tipX = cx + outerRadius * cos(midAngle)
            val tipY = cy + outerRadius * sin(midAngle)
            val endX = cx + innerRadius * cos(endAngle)
            val endY = cy + innerRadius * sin(endAngle)

            if (i == 0) {
                path.moveTo(cx + innerRadius * cos(startAngle), cy + innerRadius * sin(startAngle))
            }
            path.quadraticTo(tipX, tipY, endX, endY)
            angle += step
        }
        path.close()
    }

    private fun drawScallop(path: Path, cx: Float, cy: Float, count: Int, outerRadius: Float, innerRadius: Float) {
        val step = (2 * PI / count).toFloat()
        var angle = -PI.toFloat() / 2f
        for (i in 0 until count) {
            val startAngle = angle
            val endAngle = angle + step
            val midAngle = angle + step / 2f
            val tipX = cx + outerRadius * cos(midAngle)
            val tipY = cy + outerRadius * sin(midAngle)
            val endX = cx + innerRadius * cos(endAngle)
            val endY = cy + innerRadius * sin(endAngle)

            if (i == 0) {
                path.moveTo(cx + innerRadius * cos(startAngle), cy + innerRadius * sin(startAngle))
            }
            path.cubicTo(
                cx + outerRadius * cos(startAngle + step * 0.25f),
                cy + outerRadius * sin(startAngle + step * 0.25f),
                cx + outerRadius * cos(startAngle + step * 0.75f),
                cy + outerRadius * sin(startAngle + step * 0.75f),
                endX, endY
            )
            angle += step
        }
        path.close()
    }

    private fun drawPolygon(path: Path, cx: Float, cy: Float, sides: Int, radius: Float) {
        val step = (2 * PI / sides).toFloat()
        var angle = -PI.toFloat() / 2f
        path.moveTo(cx + radius * cos(angle), cy + radius * sin(angle))
        for (i in 1 until sides) {
            angle += step
            path.lineTo(cx + radius * cos(angle), cy + radius * sin(angle))
        }
        path.close()
    }
}

/**
 * Composable Morphing Button that shifts between Google's 35 Expressive Shapes
 */
@Composable
fun ExpressiveMorphButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialShape: ExpressiveShape35 = ExpressiveShape35.ROUNDED_RECT_MEDIUM,
    pressedShape: ExpressiveShape35 = ExpressiveShape35.SUNNY,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    var currentShapeIndex by remember { mutableIntStateOf(initialShape.ordinal) }

    val currentShape = remember(isPressed, currentShapeIndex) {
        if (isPressed) pressedShape else ExpressiveShape35.entries[currentShapeIndex % ExpressiveShape35.entries.size]
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "morphScale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(ExpressiveShapeRenderer(currentShape))
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = {
                    currentShapeIndex = (currentShapeIndex + 1) % ExpressiveShape35.entries.size
                    onClick()
                }
            ),
        shape = ExpressiveShapeRenderer(currentShape),
        color = if (enabled) containerColor else containerColor.copy(alpha = 0.4f),
        contentColor = contentColor,
        tonalElevation = if (isPressed) 6.dp else 2.dp,
        shadowElevation = if (isPressed) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            content = content
        )
    }
}

/**
 * Composable Morphing Icon Button
 */
@Composable
fun ExpressiveMorphIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialShape: ExpressiveShape35 = ExpressiveShape35.SUNNY,
    pressedShape: ExpressiveShape35 = ExpressiveShape35.CLOVER_4,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    size: Dp = 44.dp,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var shapeIndex by remember { mutableIntStateOf(initialShape.ordinal) }

    val activeShape = remember(isPressed, shapeIndex) {
        if (isPressed) pressedShape else ExpressiveShape35.entries[shapeIndex % ExpressiveShape35.entries.size]
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "iconScale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(ExpressiveShapeRenderer(activeShape))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = {
                    shapeIndex = (shapeIndex + 1) % ExpressiveShape35.entries.size
                    onClick()
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = ExpressiveShapeRenderer(activeShape),
            color = containerColor,
            contentColor = contentColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                content()
            }
        }
    }
}
