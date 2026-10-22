package br.edu.fsa.planner

import br.edu.fsa.planner.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class HeatmapRulesTest {
    private val monday = LocalDate.of(2026, 10, 19)
    private val daily = Habit(id = 1, name = "Ler", startsOn = monday, days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
    @Test fun overallMapCalculatesPercentageAndDeduplicatesCheckIns() {
        val habits = (1L..4L).map { daily.copy(id = it) }
        val checks = listOf(HabitCheckIn(1, monday), HabitCheckIn(1, monday), HabitCheckIn(2, monday), HabitCheckIn(3, monday))
        val cell = HeatmapRules.days(habits, checks, DateInterval(monday, monday), monday).single()
        assertEquals(4, cell.planned); assertEquals(3, cell.done); assertEquals(75, cell.percent); assertEquals(3, cell.intensity)
        assertEquals(listOf(0, 1, 2, 3, 4, 5), listOf(0 to 4, 1 to 4, 1 to 2, 3 to 4, 4 to 5, 5 to 5)
            .map { (done, total) -> HeatmapDay(monday, total, done, HeatmapStatus.RECORDED).intensity })
    }
    @Test fun futureUnscheduledAndBeforeCreationAreDifferentFromPending() {
        val interval = DateInterval(monday.minusDays(1), monday.plusDays(3))
        val days = HeatmapRules.days(listOf(daily), emptyList(), interval, monday.plusDays(2)).associateBy { it.date }
        assertEquals(HeatmapStatus.NOT_SCHEDULED, days[monday.minusDays(1)]!!.status)
        assertEquals(HeatmapStatus.OPEN, days[monday]!!.status)
        assertEquals(HeatmapStatus.NOT_SCHEDULED, days[monday.plusDays(1)]!!.status)
        assertEquals(HeatmapStatus.FUTURE, days[monday.plusDays(3)]!!.status)
    }
    @Test fun weeklyFlexibleDaysHaveNoDailyObligationAndMiniMapHas28Cells() {
        val weekly = daily.copy(id = 2, frequency = FrequencyType.WEEKLY, targetPerPeriod = 2)
        val map = HeatmapRules.days(listOf(weekly), listOf(HabitCheckIn(2, monday)), HeatmapRules.interval(monday.plusDays(3), 4), monday.plusDays(3))
        assertEquals(28, map.size)
        assertEquals(100, map.first { it.date == monday }.percent)
        assertEquals(HeatmapStatus.FLEXIBLE, map.first { it.date == monday.plusDays(2) }.status)
        assertEquals(0, map.first { it.date == monday.plusDays(2) }.planned)
        assertEquals(84, HeatmapRules.days(emptyList(), emptyList(), HeatmapRules.interval(monday, 12), monday).size)
    }
}
