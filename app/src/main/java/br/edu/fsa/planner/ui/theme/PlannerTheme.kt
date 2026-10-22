package br.edu.fsa.planner.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.foundation.isSystemInDarkTheme
import br.edu.fsa.planner.domain.model.ThemeMode
import br.edu.fsa.planner.domain.model.PlannerMood
import androidx.compose.runtime.Composable
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
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val Page = 24.dp
    val Section = 32.dp
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
    val FocusTimer = 220.dp
    val ChartHeight = 132.dp
}

object PlannerFonts {
    val Heading = FontFamily.Serif
    val Body = FontFamily.SansSerif
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
    val Title = TextStyle(fontFamily = PlannerFonts.Heading, fontSize = 30.sp, lineHeight = 38.sp)
    val Label = TextStyle(fontFamily = PlannerFonts.Body, fontSize = 11.sp, lineHeight = 16.sp,
        fontWeight = FontWeight.Medium, letterSpacing = 1.sp)
    val Body = TextStyle(fontFamily = PlannerFonts.Body, fontSize = 14.sp, lineHeight = 22.sp)
    val Caption = TextStyle(fontFamily = PlannerFonts.Body, fontSize = 12.sp, lineHeight = 18.sp)
    val TimerText = TextStyle(fontFamily = PlannerFonts.Heading, fontSize = 48.sp, lineHeight = 56.sp)
}

private val Type = Typography(
    headlineLarge = TextStyle(fontFamily = PlannerFonts.Heading, fontWeight = FontWeight.Normal, fontSize = 34.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = PlannerFonts.Heading, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = PlannerFonts.Heading, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = PlannerFonts.Body, fontWeight = FontWeight.Medium, fontSize = 21.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = PlannerFonts.Body, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = PlannerFonts.Body, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = PlannerFonts.Body, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = PlannerFonts.Body, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = PlannerFonts.Body, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = PlannerFonts.Body, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = PlannerFonts.Body, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = PlannerFonts.Body, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)

@Composable
fun PlannerTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, mood: PlannerMood = PlannerMood.CLEAN, content: @Composable () -> Unit) {
    val dark = when (themeMode) { ThemeMode.SYSTEM -> isSystemInDarkTheme(); ThemeMode.LIGHT -> false; ThemeMode.DARK -> true }
    val corners = when (mood) { PlannerMood.COZY -> PlannerDimens.Medium; PlannerMood.SOFT -> PlannerDimens.Large;
        PlannerMood.CLEAN -> PlannerDimens.Tiny; PlannerMood.FOCUS -> PlannerDimens.Tiny }
    MaterialTheme(colorScheme = cadencyColors(mood, dark), typography = Type, shapes = Shapes(
        extraSmall = RoundedCornerShape(PlannerDimens.Tiny),
        small = RoundedCornerShape(corners),
        medium = RoundedCornerShape(corners),
        large = RoundedCornerShape(corners + PlannerDimens.Small),
        extraLarge = RoundedCornerShape(PlannerDimens.Page),
    ), content = content)
}
