package br.edu.fsa.planner.ui.components

import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*

fun ThemeMode.label() = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system; ThemeMode.LIGHT -> R.string.theme_light; ThemeMode.DARK -> R.string.theme_dark
}
fun PlannerPersonality.label() = when (this) {
    PlannerPersonality.COZY -> R.string.style_cozy; PlannerPersonality.SOFT -> R.string.style_soft
    PlannerPersonality.CLEAN -> R.string.style_clean; PlannerPersonality.FOCUS -> R.string.style_focus
}
fun PlannerPersonality.description() = when (this) {
    PlannerPersonality.COZY -> R.string.style_cozy_hint; PlannerPersonality.SOFT -> R.string.style_soft_hint
    PlannerPersonality.CLEAN -> R.string.style_clean_hint; PlannerPersonality.FOCUS -> R.string.style_focus_hint
}
fun HabitIcon.label() = when (this) {
    HabitIcon.WATER -> R.string.habit_icon_water; HabitIcon.BOOK -> R.string.habit_icon_book
    HabitIcon.HEALTH -> R.string.habit_icon_health; HabitIcon.STUDY -> R.string.habit_icon_study
    HabitIcon.CALM -> R.string.habit_icon_calm; HabitIcon.CHECK -> R.string.habit_icon_check
}
