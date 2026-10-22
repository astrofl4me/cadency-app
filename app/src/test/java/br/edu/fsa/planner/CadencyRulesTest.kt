package br.edu.fsa.planner

import br.edu.fsa.planner.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class CadencyRulesTest {
    private val monday = LocalDate.of(2026, 10, 19)
    @Test fun focusTimerHandlesPauseAndDeadlineWithoutNegativeTime() {
        val session = FocusSession(day = monday, startedAt = 1000, plannedSeconds = 60, resumedAt = 1000)
        assertEquals(30000, FocusRules.remaining(session, 31000))
        assertEquals("00:30", FocusRules.timer(30000))
        assertEquals(0, FocusRules.remaining(session, 999999))
        assertEquals(30000, FocusRules.remaining(session.copy(status = FocusStatus.PAUSED, elapsedMillis = 30000), 999999))
    }
    @Test fun nowModePrefersOngoingThenSoonThenPriorityAndSkipsCompleted() {
        val now = monday.atTime(14, 0)
        val ongoing = PlannerItem(id = 1, title = "Em andamento", date = monday, startTime = java.time.LocalTime.of(13, 30), endTime = java.time.LocalTime.of(14, 30))
        val soon = PlannerItem(id = 2, title = "Em breve", date = monday, startTime = java.time.LocalTime.of(14, 20))
        val priority = PlannerItem(id = 3, title = "Prioridade", date = monday, priority = Priority.HIGH)
        val done = priority.copy(id = 4, isCompleted = true)
        assertEquals(listOf(1L, 2L, 3L), NowRules.ordered(listOf(priority, soon, done, ongoing), now).map { it.id })
        assertEquals(2L, NowRules.ordered(listOf(ongoing, soon), now, setOf(1)).first().id)
    }
    @Test fun rolloverExcludesCompletedEventsInboxAndCurrentDates() {
        val pending = PlannerItem(title = "Ontem", date = monday.minusDays(1))
        assertTrue(RolloverRules.eligible(pending, monday))
        listOf(pending.copy(isCompleted = true), pending.copy(type = ItemType.EVENT), pending.copy(isInbox = true),
            pending.copy(isDiscarded = true), pending.copy(date = monday)).forEach { assertFalse(RolloverRules.eligible(it, monday)) }
        val prefs = PlannerPreferences(ready = true)
        assertTrue(RolloverRules.shouldOffer(prefs, monday, listOf(pending)))
        assertFalse(RolloverRules.shouldOffer(prefs.copy(lastRolloverReviewDate = monday), monday, listOf(pending)))
        assertFalse(RolloverRules.shouldOffer(prefs.copy(gentleRolloverEnabled = false), monday, listOf(pending)))
    }
    @Test fun subtaskProgressHandlesEmptyPartialAndCompleteLists() {
        assertEquals(0f, SubtaskProgress(1, 0, 0).fraction)
        assertFalse(SubtaskProgress(1, 0, 0).allComplete)
        assertEquals(0.5f, SubtaskProgress(1, 4, 2).fraction)
        assertTrue(SubtaskProgress(1, 4, 4).allComplete)
    }
    @Test fun dailyHabitCountsOnlyPlannedDays() {
        val habit = Habit(id = 1, name = "Ler", startsOn = monday,
            days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY))
        val interval = PlannerDates.week(monday)
        assertTrue(HabitRules.isPlanned(habit, monday))
        assertFalse(HabitRules.isPlanned(habit, monday.plusDays(1)))
        assertEquals(3, HabitRules.opportunities(habit, interval))
        assertEquals(66, HabitRules.consistency(habit, listOf(HabitCheckIn(1, monday), HabitCheckIn(1, monday.plusDays(2)),
            HabitCheckIn(1, monday.plusDays(1))), interval))
    }
    @Test fun weeklyHabitUsesItsTargetAndCapsConsistency() {
        val habit = Habit(id = 1, name = "Caminhar", startsOn = monday, frequency = FrequencyType.WEEKLY, targetPerPeriod = 3)
        val interval = PlannerDates.week(monday)
        assertEquals(3, HabitRules.opportunities(habit, interval))
        assertEquals(66, HabitRules.consistency(habit, listOf(HabitCheckIn(1, monday), HabitCheckIn(1, monday.plusDays(1))), interval))
        assertEquals(100, HabitRules.consistency(habit, interval.days.map { HabitCheckIn(1, it) }, interval))
    }
    @Test fun habitsExcludeDatesBeforeCreationAndAfterArchive() {
        val habit = Habit(id = 1, name = "Água", startsOn = monday.plusDays(2), endsOn = monday.plusDays(4))
        assertEquals(3, HabitRules.opportunities(habit, PlannerDates.week(monday)))
        assertEquals(0, HabitRules.consistency(habit, emptyList(), PlannerDates.week(monday)))
        assertFalse(HabitRules.valid(habit.copy(days = emptySet())))
        assertFalse(HabitRules.valid(habit.copy(targetPerPeriod = 8)))
    }
    @Test fun dailyStreakSkipsUnscheduledDaysWithoutPunishingToday() {
        val habit = Habit(id = 1, name = "Ler", startsOn = monday, days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY))
        assertEquals(2, HabitRules.streak(habit, listOf(HabitCheckIn(1, monday), HabitCheckIn(1, monday.plusDays(2))), monday.plusDays(3)))
    }
}
