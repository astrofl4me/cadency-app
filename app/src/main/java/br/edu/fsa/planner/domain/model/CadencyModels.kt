package br.edu.fsa.planner.domain.model

import java.time.DayOfWeek
import java.time.LocalDate

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class PlannerMood { COZY, SOFT, CLEAN, FOCUS }
data class PlannerPreferences(val ready: Boolean = false, val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val plannerMood: PlannerMood = PlannerMood.CLEAN, val gentleRolloverEnabled: Boolean = true,
    val lastRolloverReviewDate: LocalDate? = null, val hasError: Boolean = false)

enum class FrequencyType { DAILY, WEEKLY }
enum class HabitIcon { WATER, BOOK, HEALTH, STUDY, CALM, CHECK }
data class Habit(val id: Long = 0, val name: String, val description: String = "",
    val icon: HabitIcon = HabitIcon.CHECK, val category: Category = Category.PERSONAL,
    val frequency: FrequencyType = FrequencyType.DAILY, val targetPerPeriod: Int = 1,
    val days: Set<DayOfWeek> = DayOfWeek.entries.toSet(), val createdAt: Long = 0,
    val startsOn: LocalDate = LocalDate.now(), val endsOn: LocalDate? = null,
    val isArchived: Boolean = false, val position: Int = 0, val colorKey: String = "primary")
data class HabitCheckIn(val habitId: Long, val date: LocalDate)
data class Subtask(val id: Long = 0, val plannerItemId: Long, val title: String,
    val isCompleted: Boolean = false, val position: Int = 0, val createdAt: Long = 0)
enum class FocusStatus { RUNNING, PAUSED, FINISHED, STOPPED }
data class FocusSession(val id: Long = 0, val plannerItemId: Long? = null, val title: String = "",
    val day: LocalDate, val startedAt: Long, val endedAt: Long? = null, val plannedSeconds: Int,
    val elapsedMillis: Long = 0, val resumedAt: Long? = null, val status: FocusStatus = FocusStatus.RUNNING)
data class DailyCount(val date: LocalDate, val value: Long)
