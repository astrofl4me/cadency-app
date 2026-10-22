package br.edu.fsa.planner.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.PlannerPersonality

@OptIn(androidx.compose.ui.text.ExperimentalTextApi::class)
private fun variableFamily(resource: Int) = FontFamily(
    Font(resource, FontWeight.Normal, variationSettings = FontVariation.Settings(FontVariation.weight(400))),
    Font(resource, FontWeight.SemiBold, variationSettings = FontVariation.Settings(FontVariation.weight(600))))

object CadencyFonts {
    val Fraunces = variableFamily(R.font.fraunces)
    val Nunito = variableFamily(R.font.nunitosans)
    val Quicksand = variableFamily(R.font.quicksand)
    val Manrope = variableFamily(R.font.manrope)
    val Plex = variableFamily(R.font.ibmplexsans)
    val Mono = FontFamily(Font(R.font.ibmplexmono))
}

data class PersonalitySpacing(val page: Dp, val section: Dp, val small: Dp, val medium: Dp, val large: Dp)
data class CadencyPersonalityTokens(
    val typography: Typography, val shapes: Shapes, val spacing: PersonalitySpacing,
    val cardRadius: Dp, val controlRadius: Dp, val elevation: Dp, val border: Dp,
    val sectionMarker: Dp, val roundCheckbox: Boolean, val paperRules: Boolean,
    val prioritizeNow: Boolean, val quietNavigation: Boolean, val motionMillis: Int,
    val heatmapRadius: Dp, val heatmapGap: Dp, val timer: TextStyle, val quoteStyle: TextStyle,
) {
    val cardShape get() = RoundedCornerShape(cardRadius)
    val controlShape get() = RoundedCornerShape(controlRadius)
}

fun personalityTokens(personality: PlannerPersonality): CadencyPersonalityTokens {
    val headings = when (personality) {
        PlannerPersonality.COZY -> CadencyFonts.Fraunces; PlannerPersonality.SOFT -> CadencyFonts.Quicksand
        PlannerPersonality.CLEAN -> CadencyFonts.Manrope; PlannerPersonality.FOCUS -> CadencyFonts.Plex
    }
    val body = when (personality) {
        PlannerPersonality.COZY, PlannerPersonality.SOFT -> CadencyFonts.Nunito
        PlannerPersonality.CLEAN -> CadencyFonts.Manrope; PlannerPersonality.FOCUS -> CadencyFonts.Plex
    }
    val cozy = personality == PlannerPersonality.COZY
    val soft = personality == PlannerPersonality.SOFT
    val focus = personality == PlannerPersonality.FOCUS
    val radius = when (personality) { PlannerPersonality.COZY -> 18.dp; PlannerPersonality.SOFT -> 28.dp; PlannerPersonality.CLEAN -> 8.dp; PlannerPersonality.FOCUS -> 4.dp }
    val titleSize = when (personality) { PlannerPersonality.COZY -> 36; PlannerPersonality.SOFT -> 32; PlannerPersonality.CLEAN -> 28; PlannerPersonality.FOCUS -> 34 }
    fun style(font: FontFamily, size: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(fontFamily = font, fontSize = size.sp,
        lineHeight = (size + if (size >= 24) 8 else 6).sp, fontWeight = weight)
    val type = Typography(
        headlineLarge = style(headings, titleSize, FontWeight.SemiBold), headlineMedium = style(headings, titleSize - 4, FontWeight.SemiBold),
        headlineSmall = style(headings, 24, FontWeight.SemiBold), titleLarge = style(headings, 21, FontWeight.SemiBold),
        titleMedium = style(body, 16, FontWeight.SemiBold), titleSmall = style(body, 14, FontWeight.SemiBold),
        bodyLarge = style(body, 16), bodyMedium = style(body, 14), bodySmall = style(body, 12),
        labelLarge = style(body, 14, FontWeight.SemiBold), labelMedium = style(body, 12, FontWeight.SemiBold), labelSmall = style(body, 11, FontWeight.SemiBold))
    return CadencyPersonalityTokens(type, Shapes(RoundedCornerShape(radius / 2), RoundedCornerShape(radius), RoundedCornerShape(radius),
        RoundedCornerShape(radius), RoundedCornerShape(radius)),
        PersonalitySpacing(if (soft || focus) 28.dp else if (cozy) 24.dp else 20.dp,
            if (soft || focus) 36.dp else if (cozy) 32.dp else 24.dp, if (soft) 10.dp else 8.dp,
            if (soft || cozy) 14.dp else 12.dp, if (soft) 20.dp else 16.dp),
        radius, if (soft) 28.dp else radius, if (cozy) 1.dp else 0.dp, if (cozy || personality == PlannerPersonality.CLEAN) 1.dp else 0.dp,
        if (personality == PlannerPersonality.CLEAN) 2.dp else 0.dp, cozy, cozy, focus, focus,
        if (soft) 260 else if (cozy) 180 else 100,
        if (soft) 9.dp else if (cozy) 4.dp else 1.dp, if (cozy || soft) 5.dp else 3.dp,
        style(if (focus) CadencyFonts.Mono else headings, if (focus) 58 else 48),
        style(if (cozy || soft) headings else body, if (cozy) 23 else if (soft) 20 else 16))
}

val LocalCadencyPersonality = staticCompositionLocalOf { personalityTokens(PlannerPersonality.CLEAN) }
