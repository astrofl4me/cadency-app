package br.edu.fsa.planner.domain.model

import java.time.LocalDateTime

object NowRules {
    fun ordered(items: List<PlannerItem>, now: LocalDateTime, skipped: Set<Long> = emptySet()): List<PlannerItem> {
        val time = now.toLocalTime()
        fun rank(item: PlannerItem): Int {
            val start = item.startTime
            return when {
                start != null && item.endTime != null && time >= start && time < item.endTime -> 0
                start != null && start >= time && java.time.Duration.between(time, start).toMinutes() <= 30 -> 1
                item.priority == Priority.HIGH || item.type == ItemType.PRIORITY -> 2
                else -> 3
            }
        }
        return items.filter { it.date == now.toLocalDate() && !it.isCompleted && !it.isInbox && !it.isDiscarded && it.id !in skipped }
            .filterNot { it.type == ItemType.EVENT && it.endTime != null && it.endTime <= time }
            .sortedWith(compareBy<PlannerItem> { rank(it) }.thenBy { it.startTime ?: java.time.LocalTime.MAX }.thenByDescending { it.priority }.thenBy { it.id })
    }
}
