package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

enum class ExpressiveThemeType(val displayName: String, val description: String, val previewColor: Color) {
    EXPRESSIVE_CHROMATIC("Expressif Chromatique", "Style épuré, chromatique & nuances sombres", Color(0xFF6366F1)),
    HIGH_DENSITY("High Density", "Thème Haute Densité M3 Expressive", HighDensityPrimary),
    MONET("Monet Dynamic", "Thème dynamique Android 12+", ExpressivePrimary),
    INDIGO("Electric Indigo", "Bleu électrique & Cyan vibrant", IndigoPrimary),
    CORAL("Sunset Coral", "Tangerine chaud & Rose corail", CoralPrimary),
    EMERALD("Neon Emerald", "Menthe néon & Vert jade", EmeraldPrimary),
    VIOLET("Electric Violet", "Ultra violet & Magenta néon", VioletPrimary),
    AMBER("Cyber Amber", "Miel doré & Ambre solaire", AmberPrimary),
    SAKURA("Sakura Pink", "Rose bonbon & Lavande douce", SakuraPrimary),
    MIDNIGHT("Deep Midnight", "Bleu nuit profond & Cobalt néon", MidnightPrimary),
    BERRY("Expressive Berry", "Mûre sauvage & Baie pourpre", BerryPrimary)
}

fun getExpressiveColorScheme(
    themeType: ExpressiveThemeType,
    darkTheme: Boolean,
    dynamicColor: Boolean,
    context: android.content.Context?
): ColorScheme {
    if (themeType == ExpressiveThemeType.MONET && dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && context != null) {
        return if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }

    return when (themeType) {
        ExpressiveThemeType.EXPRESSIVE_CHROMATIC -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFF818CF8),
                    onPrimary = Color(0xFF0F172A),
                    primaryContainer = Color(0xFF312E81),
                    onPrimaryContainer = Color(0xFFE0E7FF),
                    secondary = Color(0xFF38BDF8),
                    onSecondary = Color(0xFF082F49),
                    secondaryContainer = Color(0xFF075985),
                    onSecondaryContainer = Color(0xFFE0F2FE),
                    tertiary = Color(0xFF34D399),
                    onTertiary = Color(0xFF022C22),
                    tertiaryContainer = Color(0xFF065F46),
                    onTertiaryContainer = Color(0xFFA7F3D0),
                    background = Color(0xFF0A0D14), // Deep subtle obsidian nuance
                    surface = Color(0xFF10141E), // Slate dark nuance
                    surfaceVariant = Color(0xFF1E2433),
                    surfaceContainer = Color(0xFF141926),
                    surfaceContainerHigh = Color(0xFF1B2232),
                    surfaceContainerHighest = Color(0xFF242C40),
                    onSurface = Color(0xFFF1F5F9),
                    onSurfaceVariant = Color(0xFF94A3B8)
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF4F46E5),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFEEF2FF),
                    onPrimaryContainer = Color(0xFF312E81),
                    secondary = Color(0xFF0284C7),
                    onSecondary = Color.White,
                    secondaryContainer = Color(0xFFE0F2FE),
                    onSecondaryContainer = Color(0xFF0369A1),
                    tertiary = Color(0xFF059669),
                    onTertiary = Color.White,
                    tertiaryContainer = Color(0xFFD1FAE5),
                    onTertiaryContainer = Color(0xFF065F46),
                    background = Color(0xFFF8FAFC),
                    surface = Color(0xFFFFFFFF),
                    surfaceVariant = Color(0xFFE2E8F0),
                    surfaceContainer = Color(0xFFF1F5F9),
                    surfaceContainerHigh = Color(0xFFE2E8F0),
                    surfaceContainerHighest = Color(0xFFCBD5E1),
                    onSurface = Color(0xFF0F172A),
                    onSurfaceVariant = Color(0xFF475569)
                )
            }
        }
        ExpressiveThemeType.HIGH_DENSITY -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFD0BCFF),
                    onPrimary = Color(0xFF381E72),
                    primaryContainer = Color(0xFF4F378B),
                    onPrimaryContainer = Color(0xFFEADDFF),
                    secondary = Color(0xFFCCC2DC),
                    onSecondary = Color(0xFF332D41),
                    secondaryContainer = Color(0xFF4A4458),
                    onSecondaryContainer = Color(0xFFE8DEF8),
                    tertiary = Color(0xFFFFB0C8),
                    onTertiary = Color(0xFF5E1128),
                    tertiaryContainer = Color(0xFF7B293E),
                    onTertiaryContainer = Color(0xFFFFD8E4),
                    background = Color(0xFF141218),
                    surface = Color(0xFF141218),
                    surfaceVariant = Color(0xFF49454F),
                    surfaceContainer = Color(0xFF1D1B20),
                    surfaceContainerHigh = Color(0xFF2B2930),
                    surfaceContainerHighest = Color(0xFF36343B),
                    onSurface = Color(0xFFE6E0E9),
                    onSurfaceVariant = Color(0xFFCAC4D0)
                )
            } else {
                lightColorScheme(
                    primary = HighDensityPrimary,
                    onPrimary = HighDensityOnPrimary,
                    primaryContainer = HighDensityPrimaryContainer,
                    onPrimaryContainer = HighDensityOnPrimaryContainer,
                    secondary = HighDensitySecondary,
                    onSecondary = HighDensityOnSecondary,
                    secondaryContainer = HighDensitySecondaryContainer,
                    onSecondaryContainer = HighDensityOnSecondaryContainer,
                    tertiary = HighDensityTertiary,
                    onTertiary = HighDensityOnTertiary,
                    tertiaryContainer = HighDensityTertiaryContainer,
                    onTertiaryContainer = HighDensityOnTertiaryContainer,
                    background = HighDensityBackground,
                    surface = HighDensitySurface,
                    surfaceVariant = HighDensitySurfaceVariant,
                    surfaceContainer = HighDensitySurfaceContainer,
                    surfaceContainerHigh = HighDensitySurfaceContainerHigh,
                    surfaceContainerHighest = HighDensitySurfaceContainerHighest,
                    onSurface = HighDensityOnSurface,
                    onSurfaceVariant = HighDensityOnSurfaceVariant
                )
            }
        }
        ExpressiveThemeType.MONET -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFD0BCFF),
                    onPrimary = Color(0xFF381E72),
                    primaryContainer = Color(0xFF4F378B),
                    onPrimaryContainer = Color(0xFFEADDFF),
                    secondary = Color(0xFFCCC2DC),
                    tertiary = Color(0xFFEFB8C8),
                    background = Color(0xFF141218),
                    surface = Color(0xFF141218),
                    surfaceVariant = Color(0xFF49454F)
                )
            } else {
                lightColorScheme(
                    primary = ExpressivePrimary,
                    onPrimary = Color.White,
                    primaryContainer = ExpressivePrimaryContainer,
                    onPrimaryContainer = ExpressiveOnPrimaryContainer,
                    secondary = ExpressiveSecondary,
                    tertiary = ExpressiveTertiary,
                    background = ExpressiveBackground,
                    surface = ExpressiveSurface,
                    surfaceVariant = ExpressiveSurfaceVariant
                )
            }
        }
        ExpressiveThemeType.INDIGO -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFF818CF8),
                    onPrimary = Color(0xFF1E1B4B),
                    primaryContainer = Color(0xFF312E81),
                    onPrimaryContainer = Color(0xFFE0E7FF),
                    secondary = IndigoSecondary,
                    tertiary = IndigoTertiary,
                    background = Color(0xFF0F172A),
                    surface = Color(0xFF1E293B),
                    surfaceVariant = Color(0xFF334155)
                )
            } else {
                lightColorScheme(
                    primary = IndigoPrimary,
                    onPrimary = Color.White,
                    primaryContainer = IndigoPrimaryContainer,
                    onPrimaryContainer = Color(0xFF1E1B4B),
                    secondary = IndigoSecondary,
                    tertiary = IndigoTertiary,
                    background = IndigoBackground,
                    surface = IndigoSurface,
                    surfaceVariant = Color(0xFFE2E8F0)
                )
            }
        }
        ExpressiveThemeType.CORAL -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFFF8A65),
                    onPrimary = Color(0xFF4E1500),
                    primaryContainer = Color(0xFF7E2A0D),
                    onPrimaryContainer = Color(0xFFFFCCBC),
                    secondary = CoralSecondary,
                    tertiary = CoralTertiary,
                    background = Color(0xFF1C1412),
                    surface = Color(0xFF2C1F1C),
                    surfaceVariant = Color(0xFF3E2D28)
                )
            } else {
                lightColorScheme(
                    primary = CoralPrimary,
                    onPrimary = Color.White,
                    primaryContainer = CoralPrimaryContainer,
                    onPrimaryContainer = Color(0xFF4A1005),
                    secondary = CoralSecondary,
                    tertiary = CoralTertiary,
                    background = CoralBackground,
                    surface = CoralSurface,
                    surfaceVariant = Color(0xFFFDE8E4)
                )
            }
        }
        ExpressiveThemeType.EMERALD -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFF34D399),
                    onPrimary = Color(0xFF064E3B),
                    primaryContainer = Color(0xFF065F46),
                    onPrimaryContainer = Color(0xFFA7F3D0),
                    secondary = EmeraldSecondary,
                    tertiary = EmeraldTertiary,
                    background = Color(0xFF0B1B14),
                    surface = Color(0xFF122B20),
                    surfaceVariant = Color(0xFF1E3A2E)
                )
            } else {
                lightColorScheme(
                    primary = EmeraldPrimary,
                    onPrimary = Color.White,
                    primaryContainer = EmeraldPrimaryContainer,
                    onPrimaryContainer = Color(0xFF022C22),
                    secondary = EmeraldSecondary,
                    tertiary = EmeraldTertiary,
                    background = EmeraldBackground,
                    surface = EmeraldSurface,
                    surfaceVariant = Color(0xFFE0F2FE)
                )
            }
        }
        ExpressiveThemeType.VIOLET -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFA78BFA),
                    onPrimary = Color(0xFF2E1065),
                    primaryContainer = Color(0xFF4C1D95),
                    onPrimaryContainer = Color(0xFFDDD6FE),
                    secondary = VioletSecondary,
                    tertiary = VioletTertiary,
                    background = Color(0xFF130E20),
                    surface = Color(0xFF221A36),
                    surfaceVariant = Color(0xFF32264D)
                )
            } else {
                lightColorScheme(
                    primary = VioletPrimary,
                    onPrimary = Color.White,
                    primaryContainer = VioletPrimaryContainer,
                    onPrimaryContainer = Color(0xFF2E1065),
                    secondary = VioletSecondary,
                    tertiary = VioletTertiary,
                    background = VioletBackground,
                    surface = VioletSurface,
                    surfaceVariant = Color(0xFFF3E8FF)
                )
            }
        }
        ExpressiveThemeType.AMBER -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFFBBF24),
                    onPrimary = Color(0xFF451A03),
                    primaryContainer = Color(0xFF78350F),
                    onPrimaryContainer = Color(0xFFFDE68A),
                    secondary = AmberSecondary,
                    tertiary = AmberTertiary,
                    background = Color(0xFF1A1408),
                    surface = Color(0xFF2A2010),
                    surfaceVariant = Color(0xFF3E311A)
                )
            } else {
                lightColorScheme(
                    primary = AmberPrimary,
                    onPrimary = Color.White,
                    primaryContainer = AmberPrimaryContainer,
                    onPrimaryContainer = Color(0xFF451A03),
                    secondary = AmberSecondary,
                    tertiary = AmberTertiary,
                    background = AmberBackground,
                    surface = AmberSurface,
                    surfaceVariant = Color(0xFFFEF3C7)
                )
            }
        }
        ExpressiveThemeType.SAKURA -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFF472B6),
                    onPrimary = Color(0xFF500724),
                    primaryContainer = Color(0xFF831843),
                    onPrimaryContainer = Color(0xFFFCE7F3),
                    secondary = SakuraSecondary,
                    tertiary = SakuraTertiary,
                    background = Color(0xFF1C0D15),
                    surface = Color(0xFF2C1522),
                    surfaceVariant = Color(0xFF402033)
                )
            } else {
                lightColorScheme(
                    primary = SakuraPrimary,
                    onPrimary = Color.White,
                    primaryContainer = SakuraPrimaryContainer,
                    onPrimaryContainer = Color(0xFF500724),
                    secondary = SakuraSecondary,
                    tertiary = SakuraTertiary,
                    background = SakuraBackground,
                    surface = SakuraSurface,
                    surfaceVariant = Color(0xFFFCE7F3)
                )
            }
        }
        ExpressiveThemeType.MIDNIGHT -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = MidnightPrimary,
                    onPrimary = Color(0xFF082F49),
                    primaryContainer = MidnightPrimaryContainer,
                    onPrimaryContainer = Color(0xFFE0F2FE),
                    secondary = MidnightSecondary,
                    tertiary = MidnightTertiary,
                    background = MidnightBackground,
                    surface = MidnightSurface,
                    surfaceVariant = MidnightSurfaceVariant
                )
            } else {
                lightColorScheme(
                    primary = Color(0xFF0284C7),
                    onPrimary = Color.White,
                    primaryContainer = Color(0xFFE0F2FE),
                    onPrimaryContainer = Color(0xFF0369A1),
                    secondary = Color(0xFF6366F1),
                    tertiary = Color(0xFF10B981),
                    background = Color(0xFFF0F9FF),
                    surface = Color(0xFFFFFFFF),
                    surfaceVariant = Color(0xFFE0F2FE)
                )
            }
        }
        ExpressiveThemeType.BERRY -> {
            if (darkTheme) {
                darkColorScheme(
                    primary = Color(0xFFF43F5E),
                    onPrimary = Color(0xFF4C0519),
                    primaryContainer = Color(0xFF881337),
                    onPrimaryContainer = Color(0xFFFFE4E6),
                    secondary = BerrySecondary,
                    tertiary = BerryTertiary,
                    background = Color(0xFF1C0A10),
                    surface = Color(0xFF2B1019),
                    surfaceVariant = Color(0xFF411726)
                )
            } else {
                lightColorScheme(
                    primary = BerryPrimary,
                    onPrimary = Color.White,
                    primaryContainer = BerryPrimaryContainer,
                    onPrimaryContainer = Color(0xFF4C0519),
                    secondary = BerrySecondary,
                    tertiary = BerryTertiary,
                    background = BerryBackground,
                    surface = BerrySurface,
                    surfaceVariant = Color(0xFFFFE4E6)
                )
            }
        }
    }
}

@Composable
fun I3Theme(
    selectedTheme: ExpressiveThemeType = ExpressiveThemeType.HIGH_DENSITY,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = getExpressiveColorScheme(selectedTheme, darkTheme, dynamicColor, context)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
