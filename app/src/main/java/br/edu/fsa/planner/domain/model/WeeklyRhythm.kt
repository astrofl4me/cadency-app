package br.edu.fsa.planner.domain.model

object WeeklyRhythm {
    fun level(items: List<PlannerItem>): Int {
        val weight = items.filter { !it.isCompleted && !it.isInbox && !it.isDiscarded && it.type != ItemType.EVENT }
            .sumOf { when (it.priority) { Priority.LOW -> 1; Priority.MEDIUM -> 2; Priority.HIGH -> 3 } }
        return ((weight + 2) / 3).coerceIn(0, 5)
    }
}
