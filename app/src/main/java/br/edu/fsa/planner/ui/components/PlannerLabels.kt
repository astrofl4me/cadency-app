package br.edu.fsa.planner.ui.components

import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.theme.PlannerColors
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

val PlannerLocale: Locale = Locale.forLanguageTag("pt-BR")
fun LocalDate.longLabel(): String = format(DateTimeFormatter.ofPattern("d 'de' MMMM", PlannerLocale))
fun LocalDate.weekdayLabel(): String = format(DateTimeFormatter.ofPattern("EEEE", PlannerLocale))
fun LocalDate.shortLabel(): String = format(DateTimeFormatter.ofPattern("dd/MM", PlannerLocale))
fun YearMonth.titleLabel(): String = format(DateTimeFormatter.ofPattern("MMMM yyyy", PlannerLocale))
fun ItemType.label(): Int = when (this) {
    ItemType.TASK -> R.string.type_task; ItemType.EVENT -> R.string.type_event
    ItemType.PRIORITY -> R.string.type_priority; ItemType.TOMORROW -> R.string.type_tomorrow
}
fun Priority.label(): Int = when (this) { Priority.LOW -> R.string.priority_low; Priority.MEDIUM -> R.string.priority_medium; Priority.HIGH -> R.string.priority_high }
fun Category.label(): Int = when (this) { Category.PERSONAL -> R.string.category_personal; Category.STUDY -> R.string.category_study; Category.WORK -> R.string.category_work; Category.HEALTH -> R.string.category_health }
fun Category.color() = when (this) { Category.PERSONAL -> PlannerColors.PeachInk; Category.STUDY -> PlannerColors.LavenderInk; Category.WORK -> PlannerColors.BlueInk; Category.HEALTH -> PlannerColors.Sage }
fun Category.tint() = when (this) { Category.PERSONAL -> PlannerColors.Peach; Category.STUDY -> PlannerColors.Lavender; Category.WORK -> PlannerColors.BlueTint; Category.HEALTH -> PlannerColors.SageTint }
fun Mood.label(): Int = when (this) { Mood.DIFFICULT -> R.string.mood_difficult; Mood.QUIET -> R.string.mood_quiet; Mood.GOOD -> R.string.mood_good; Mood.GREAT -> R.string.mood_great }
