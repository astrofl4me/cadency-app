package br.edu.fsa.planner.ui.components

import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*

fun ThemeMode.label() = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system; ThemeMode.LIGHT -> R.string.theme_light; ThemeMode.DARK -> R.string.theme_dark
}
fun PlannerMood.label() = when (this) {
    PlannerMood.COZY -> R.string.style_cozy; PlannerMood.SOFT -> R.string.style_soft
    PlannerMood.CLEAN -> R.string.style_clean; PlannerMood.FOCUS -> R.string.style_focus
}
fun PlannerMood.description() = when (this) {
    PlannerMood.COZY -> R.string.style_cozy_hint; PlannerMood.SOFT -> R.string.style_soft_hint
    PlannerMood.CLEAN -> R.string.style_clean_hint; PlannerMood.FOCUS -> R.string.style_focus_hint
}
fun HabitIcon.label() = when (this) {
    HabitIcon.WATER -> R.string.habit_icon_water; HabitIcon.BOOK -> R.string.habit_icon_book
    HabitIcon.HEALTH -> R.string.habit_icon_health; HabitIcon.STUDY -> R.string.habit_icon_study
    HabitIcon.CALM -> R.string.habit_icon_calm; HabitIcon.CHECK -> R.string.habit_icon_check
}
