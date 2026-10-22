package br.edu.fsa.planner.domain.model

enum class ProgressPeriod(val days: Long) { WEEK(7), MONTH(30) }
data class HabitConsistency(val habit: Habit, val planned: Int, val done: Int) {
    val percent get() = if (planned == 0) 0 else done.coerceAtMost(planned) * 100 / planned
}
data class ProgressReport(val interval: DateInterval, val tasks: List<DailyCount> = emptyList(),
    val focus: List<DailyCount> = emptyList(), val habits: List<HabitConsistency> = emptyList(),
    val habitCheckIns: Int = 0, val plannedDays: Int = 0) {
    val completedTasks get() = tasks.sumOf { it.value }
    val focusMinutes get() = focus.sumOf { it.value }
    val opportunities get() = habits.sumOf { it.planned }
    val habitPercent get() = if (opportunities == 0) 0 else habits.sumOf { it.done.coerceAtMost(it.planned) } * 100 / opportunities
    val hasActivity get() = completedTasks > 0 || focusMinutes > 0 || habitCheckIns > 0
}
