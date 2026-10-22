package br.edu.fsa.planner.domain.model

import java.time.LocalDate

enum class HeatmapStatus { FUTURE, NOT_SCHEDULED, FLEXIBLE, OPEN, RECORDED }
data class HeatmapDay(val date: LocalDate, val planned: Int, val done: Int, val status: HeatmapStatus) {
    val percent get() = if (planned == 0) 0 else (done * 100 / planned).coerceIn(0, 100)
    val intensity get() = when { done == 0 -> 0; percent == 100 -> 5; percent <= 25 -> 1; percent <= 50 -> 2; percent <= 75 -> 3; else -> 4 }
}

object HeatmapRules {
    fun interval(today: LocalDate, weeks: Int) = DateInterval(PlannerDates.week(today).start.minusWeeks((weeks - 1).toLong()), PlannerDates.week(today).endInclusive)
    fun days(habits: List<Habit>, checks: List<HabitCheckIn>, interval: DateInterval, today: LocalDate): List<HeatmapDay> {
        val checked = checks.map { it.habitId to it.date }.toSet()
        return interval.days.map { date ->
            if (date > today) HeatmapDay(date, 0, 0, HeatmapStatus.FUTURE)
            else {
                val eligible = habits.filter { HabitRules.isPlanned(it, date) }
                val daily = eligible.filter { it.frequency == FrequencyType.DAILY }
                val flexibleDone = eligible.count { it.frequency == FrequencyType.WEEKLY && (it.id to date) in checked }
                val planned = daily.size + flexibleDone
                val done = daily.count { (it.id to date) in checked } + flexibleDone
                HeatmapDay(date, planned, done, when {
                    done > 0 -> HeatmapStatus.RECORDED
                    planned > 0 -> HeatmapStatus.OPEN
                    eligible.isNotEmpty() -> HeatmapStatus.FLEXIBLE
                    else -> HeatmapStatus.NOT_SCHEDULED
                })
            }
        }
    }
}
