package br.edu.fsa.planner

import br.edu.fsa.planner.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

class CadencyRulesTest {
    private val monday = LocalDate.of(2026, 10, 19)
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
