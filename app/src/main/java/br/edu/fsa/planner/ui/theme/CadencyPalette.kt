package br.edu.fsa.planner.ui.theme

import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import br.edu.fsa.planner.domain.model.PlannerMood

fun cadencyColors(mood: PlannerMood, dark: Boolean): ColorScheme {
    val accent = when (mood) {
        PlannerMood.COZY -> if (dark) Color(0xFFE6B096) else Color(0xFF87553E)
        PlannerMood.SOFT -> if (dark) Color(0xFFCCBAEB) else Color(0xFF725991)
        PlannerMood.CLEAN -> if (dark) Color(0xFFBDC6B9) else Color(0xFF4D5C4B)
        PlannerMood.FOCUS -> if (dark) Color(0xFFA8C4D4) else Color(0xFF36596C)
    }
    val background = when (mood) {
        PlannerMood.COZY -> if (dark) Color(0xFF211B18) else Color(0xFFFFFAF3)
        PlannerMood.SOFT -> if (dark) Color(0xFF201D26) else Color(0xFFFCF9FF)
        PlannerMood.CLEAN -> if (dark) Color(0xFF181B18) else Color(0xFFFFFEFC)
        PlannerMood.FOCUS -> if (dark) Color(0xFF151C20) else Color(0xFFF7FAFB)
    }
    val panel = when (mood) {
        PlannerMood.COZY -> if (dark) Color(0xFF302721) else Color(0xFFF2E8DC)
        PlannerMood.SOFT -> if (dark) Color(0xFF2E2837) else Color(0xFFF0EAF6)
        PlannerMood.CLEAN -> if (dark) Color(0xFF282D28) else Color(0xFFF3F2EF)
        PlannerMood.FOCUS -> if (dark) Color(0xFF232E35) else Color(0xFFE8EFF2)
    }
    val ink = if (dark) Color(0xFFF0ECE6) else Color(0xFF30352F)
    val muted = if (dark) Color(0xFFCAC4BE) else Color(0xFF62685F)
    val rule = if (dark) Color(0xFF525951) else Color(0xFFCED2C9)
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(primary = accent, onPrimary = if (dark) Color(0xFF20251F) else Color.White,
        primaryContainer = panel, onPrimaryContainer = ink, secondary = accent, onSecondary = if (dark) Color.Black else Color.White,
        secondaryContainer = panel, onSecondaryContainer = ink, tertiary = accent, onTertiary = if (dark) Color.Black else Color.White,
        tertiaryContainer = panel, onTertiaryContainer = ink, background = background, onBackground = ink,
        surface = background, onSurface = ink, surfaceVariant = panel, onSurfaceVariant = muted,
        surfaceContainer = panel, surfaceContainerLow = background, surfaceContainerLowest = background,
        surfaceContainerHigh = panel, surfaceContainerHighest = panel, surfaceTint = accent,
        outline = muted, outlineVariant = rule)
}
