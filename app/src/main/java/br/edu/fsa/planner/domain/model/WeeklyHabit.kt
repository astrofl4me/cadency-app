package br.edu.fsa.planner.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

enum class DailyNoteField { NOTES, GRATITUDE, SELF_CARE }

data class WeeklyHabit(
    val id: Long = 0, val weekStart: LocalDate, val title: String,
    val completedDays: Set<DayOfWeek> = emptySet(),
)

object PlannerJournal {
    const val WaterGlasses = 8
    const val HabitTitleLimit = 60
}
