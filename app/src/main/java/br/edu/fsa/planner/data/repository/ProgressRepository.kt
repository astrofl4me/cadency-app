package br.edu.fsa.planner.data.repository

import br.edu.fsa.planner.data.local.dao.PlannerDao
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*

class ProgressRepository(private val planner: PlannerDao, private val habits: HabitRepository,
                         private val focus: FocusRepository) {
    fun observe(interval: DateInterval) = combine(planner.observeCompletedCounts(interval.start, interval.endInclusive),
        planner.observePlannedDays(interval.start, interval.endInclusive), habits.habits, habits.checkIns(interval), focus.minutes(interval)) { tasks, days, habits, checks, focus ->
        val planned = habits.mapNotNull { habit ->
            val count = HabitRules.opportunities(habit, interval)
            if (count == 0) null else HabitConsistency(habit, count,
                checks.filter { it.habitId == habit.id && HabitRules.isPlanned(habit, it.date) }.map { it.date }.distinct().size)
        }
        ProgressReport(interval, interval.days.map { date -> DailyCount(date, tasks.firstOrNull { it.date == date }?.value ?: 0) },
            interval.days.map { date -> DailyCount(date, focus.firstOrNull { it.date == date }?.value ?: 0) }, planned,
            planned.sumOf { it.done }, days)
    }.flowOn(Dispatchers.Default)
}
