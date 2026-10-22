package br.edu.fsa.planner.ui.theme

import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import br.edu.fsa.planner.domain.model.AccentColor
import br.edu.fsa.planner.domain.model.PlannerPersonality

fun cadencyColors(mood: PlannerPersonality, dark: Boolean, accentColor: AccentColor = AccentColor.Default): ColorScheme {
    val background = when (mood) {
        PlannerPersonality.COZY -> if (dark) Color(0xFF211B18) else Color(0xFFFFFAF3)
        PlannerPersonality.SOFT -> if (dark) Color(0xFF201D26) else Color(0xFFFCF9FF)
        PlannerPersonality.CLEAN -> if (dark) Color(0xFF181B18) else Color(0xFFFFFEFC)
        PlannerPersonality.FOCUS -> if (dark) Color(0xFF151C20) else Color(0xFFF7FAFB)
    }
    val panel = when (mood) {
        PlannerPersonality.COZY -> if (dark) Color(0xFF302721) else Color(0xFFF2E8DC)
        PlannerPersonality.SOFT -> if (dark) Color(0xFF2E2837) else Color(0xFFF0EAF6)
        PlannerPersonality.CLEAN -> if (dark) Color(0xFF282D28) else Color(0xFFF3F2EF)
        PlannerPersonality.FOCUS -> if (dark) Color(0xFF232E35) else Color(0xFFE8EFF2)
    }
    val ink = if (dark) Color(0xFFF0ECE6) else Color(0xFF30352F)
    val muted = if (dark) Color(0xFFCAC4BE) else Color(0xFF62685F)
    val rule = if (dark) Color(0xFF525951) else Color(0xFFCED2C9)
    val seed = Color(0xFF000000L or accentColor.rgb.toLong())
    val accent = (0..100).asSequence().map { lerp(seed, if (dark) Color.White else Color.Black, it / 100f) }
        .first { colorContrast(it, background) >= 4.5f }
    val onAccent = if (colorContrast(accent, Color.White) >= colorContrast(accent, Color.Black)) Color.White else Color.Black
    val tint = lerp(background, accent, if (dark) 0.18f else 0.10f)
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(primary = accent, onPrimary = onAccent,
        primaryContainer = tint, onPrimaryContainer = ink, secondary = accent, onSecondary = onAccent,
        secondaryContainer = tint, onSecondaryContainer = ink, tertiary = accent, onTertiary = onAccent,
        tertiaryContainer = tint, onTertiaryContainer = ink, background = background, onBackground = ink,
        surface = background, onSurface = ink, surfaceVariant = panel, onSurfaceVariant = muted,
        surfaceContainer = panel, surfaceContainerLow = background, surfaceContainerLowest = background,
        surfaceContainerHigh = panel, surfaceContainerHighest = panel, surfaceTint = accent,
        outline = muted, outlineVariant = rule)
}

fun colorContrast(first: Color, second: Color) = (maxOf(first.luminance(), second.luminance()) + 0.05f) /
    (minOf(first.luminance(), second.luminance()) + 0.05f)
