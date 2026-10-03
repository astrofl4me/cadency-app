package br.edu.fsa.planner.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object PlannerColors {
    val Paper = Color(0xFFFAF8F4)
    val Ink = Color(0xFF292E2A)
    val Sage = Color(0xFF49634D)
    val SageTint = Color(0xFFE3EBDF)
    val Peach = Color(0xFFF4E3D9)
    val PeachInk = Color(0xFF805239)
    val Lavender = Color(0xFFEAE4F2)
    val LavenderInk = Color(0xFF68537D)
    val BlueTint = Color(0xFFE1EBF0)
    val BlueInk = Color(0xFF476678)
    val Subtle = Color(0xFFF0EEE8)
    val Muted = Color(0xFF646960)
    val Line = Color(0xFFD9DDD3)
}

object PlannerDimens {
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
}

private val Colors = lightColorScheme(
    primary = PlannerColors.Sage, onPrimary = Color.White,
    primaryContainer = PlannerColors.SageTint, onPrimaryContainer = PlannerColors.Ink,
    secondary = PlannerColors.PeachInk, secondaryContainer = PlannerColors.Peach,
    onSecondaryContainer = PlannerColors.PeachInk,
    tertiary = PlannerColors.LavenderInk, tertiaryContainer = PlannerColors.Lavender,
    background = PlannerColors.Paper, onBackground = PlannerColors.Ink,
    surface = PlannerColors.Paper, onSurface = PlannerColors.Ink,
    surfaceVariant = PlannerColors.Subtle, onSurfaceVariant = PlannerColors.Muted,
    surfaceContainer = PlannerColors.Subtle, surfaceContainerLow = PlannerColors.Paper,
    outline = PlannerColors.Muted, outlineVariant = PlannerColors.Line,
)

private val Type = Typography(
    headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal, fontSize = 34.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal, fontSize = 28.sp, lineHeight = 34.sp),
    headlineSmall = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Normal, fontSize = 24.sp, lineHeight = 30.sp),
    titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 21.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontSize = 12.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 14.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 11.sp, lineHeight = 16.sp),
)

@Composable
fun PlannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Colors, typography = Type, shapes = Shapes(
        extraSmall = RoundedCornerShape(PlannerDimens.Tiny),
        small = RoundedCornerShape(PlannerDimens.Small),
        medium = RoundedCornerShape(PlannerDimens.Medium),
        large = RoundedCornerShape(PlannerDimens.Large),
        extraLarge = RoundedCornerShape(PlannerDimens.Page),
    ), content = content)
}
