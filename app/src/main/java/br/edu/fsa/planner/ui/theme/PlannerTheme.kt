package br.edu.fsa.planner.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import br.edu.fsa.planner.domain.model.ThemeMode
import br.edu.fsa.planner.domain.model.PlannerPersonality
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import br.edu.fsa.planner.domain.model.AccentColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object PlannerColors {
    val Paper @Composable get() = MaterialTheme.colorScheme.background
    val Ink @Composable get() = MaterialTheme.colorScheme.onBackground
    val Sage @Composable get() = MaterialTheme.colorScheme.primary
    val SageTint @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val Peach @Composable get() = MaterialTheme.colorScheme.secondaryContainer
    val PeachInk @Composable get() = MaterialTheme.colorScheme.secondary
    val Lavender @Composable get() = MaterialTheme.colorScheme.tertiaryContainer
    val LavenderInk @Composable get() = MaterialTheme.colorScheme.tertiary
    val BlueTint @Composable get() = MaterialTheme.colorScheme.secondaryContainer
    val BlueInk @Composable get() = MaterialTheme.colorScheme.secondary
    val Subtle @Composable get() = MaterialTheme.colorScheme.surfaceContainer
    val Muted @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val Line @Composable get() = MaterialTheme.colorScheme.outlineVariant
}

object PlannerDimens {
    val None = 0.dp
    val Tiny = 4.dp
    val Small @Composable get() = LocalCadencyPersonality.current.spacing.small
    val Medium @Composable get() = LocalCadencyPersonality.current.spacing.medium
    val Large @Composable get() = LocalCadencyPersonality.current.spacing.large
    val Page @Composable get() = LocalCadencyPersonality.current.spacing.page
    val Section @Composable get() = LocalCadencyPersonality.current.spacing.section
    val Hero = 48.dp
    val Touch = 48.dp
    val CalendarCell = 52.dp
    val Indicator = 5.dp
    val Icon = 20.dp
    val Brand = 64.dp
    val ContentWidth = 640.dp
    val Hairline = 1.dp
    val AccentLine = 3.dp
    val PaperContentWidth = 1000.dp
    val PaperColumnsBreakpoint = 600.dp
    val PaperRuleHeight = 36.dp
    val PaperDayHeight = 108.dp
    val PaperNotesHeight = 160.dp
    val PaperHabitLabelWidth = 96.dp
    val PaperWeekdayWidth = 84.dp
    val PaperDot = 10.dp
    val PaperMood = 28.dp
    val FocusTimer @Composable get() = if (LocalCadencyPersonality.current.quietNavigation) 260.dp else 240.dp
    val ChartHeight = 132.dp
}

object PlannerFonts {
    val Heading @Composable get() = MaterialTheme.typography.headlineMedium.fontFamily
    val Body @Composable get() = MaterialTheme.typography.bodyMedium.fontFamily
}

object PlannerPaper {
    val Background @Composable get() = MaterialTheme.colorScheme.background
    val Panel @Composable get() = MaterialTheme.colorScheme.surfaceContainer
    val Ink @Composable get() = MaterialTheme.colorScheme.onSurface
    val Muted @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val Rule @Composable get() = MaterialTheme.colorScheme.outlineVariant
    val Marker @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val Selected @Composable get() = MaterialTheme.colorScheme.primaryContainer
    val Shape @Composable get() = MaterialTheme.shapes.small
    val Title @Composable get() = MaterialTheme.typography.headlineMedium
    val Label @Composable get() = MaterialTheme.typography.labelSmall
    val Body @Composable get() = MaterialTheme.typography.bodyMedium
    val Caption @Composable get() = MaterialTheme.typography.bodySmall
    val TimerText @Composable get() = LocalCadencyPersonality.current.timer
}

@Composable
fun PlannerTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, mood: PlannerPersonality = PlannerPersonality.CLEAN,
                 accentColor: AccentColor = AccentColor.Default, content: @Composable () -> Unit) {
    val dark = when (themeMode) { ThemeMode.SYSTEM -> isSystemInDarkTheme(); ThemeMode.LIGHT -> false; ThemeMode.DARK -> true }
    val tokens = remember(mood) { personalityTokens(mood) }
    CompositionLocalProvider(LocalCadencyPersonality provides tokens) {
        MaterialTheme(colorScheme = cadencyColors(mood, dark, accentColor), typography = tokens.typography, shapes = tokens.shapes, content = content)
    }
}
