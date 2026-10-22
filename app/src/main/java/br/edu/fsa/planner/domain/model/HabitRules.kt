package br.edu.fsa.planner.domain.model

import java.time.LocalDate
import kotlin.math.ceil

object HabitRules {
    fun isPlanned(habit: Habit, date: LocalDate) = date >= habit.startsOn &&
        (habit.endsOn == null || date <= habit.endsOn) && date.dayOfWeek in habit.days

    fun opportunities(habit: Habit, interval: DateInterval): Int {
        val days = interval.days.count { isPlanned(habit, it) }
        return if (habit.frequency == FrequencyType.DAILY) days
        else ceil(days.toDouble() * habit.targetPerPeriod / habit.days.size.coerceAtLeast(1)).toInt()
    }

    fun consistency(habit: Habit, checkIns: List<HabitCheckIn>, interval: DateInterval): Int {
        val planned = opportunities(habit, interval)
        if (planned == 0) return 0
        val done = checkIns.filter { it.habitId == habit.id && it.date in interval && isPlanned(habit, it.date) }
            .map { it.date }.distinct().size
        return (done.coerceAtMost(planned) * 100 / planned)
    }

    fun streak(habit: Habit, checkIns: List<HabitCheckIn>, today: LocalDate): Int {
        if (habit.frequency != FrequencyType.DAILY) return 0
        val dates = checkIns.filter { it.habitId == habit.id }.map { it.date }.toSet()
        var cursor = today
        if (cursor !in dates) cursor = cursor.minusDays(1)
        var count = 0
        while (cursor >= habit.startsOn) {
            if (isPlanned(habit, cursor)) {
                if (cursor !in dates) break
                count++
            }
            cursor = cursor.minusDays(1)
        }
        return count
    }
    fun valid(habit: Habit) = habit.name.trim().length in 1..60 && habit.description.length <= 2000 &&
        habit.days.isNotEmpty() && habit.targetPerPeriod in 1..habit.days.size
}
