package br.edu.fsa.planner

import br.edu.fsa.planner.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class PomodoroRulesTest {
    private val config = PomodoroConfig(25, 5, 4, 15)
    private fun focus(cycle: Int = 1) = FocusSession(day = LocalDate.of(2026, 10, 22), startedAt = 0,
        plannedSeconds = 1500, resumedAt = 0, mode = FocusMode.POMODORO, pomodoro = config, cycleIndex = cycle)
    @Test fun focusTransitionsToShortBreakAndNextCycleRequiresAnExplicitStart() {
        val rest = PomodoroRules.next(focus(), 25 * 60000L)
        assertEquals(FocusPhase.SHORT_BREAK, rest.phase)
        assertEquals(FocusStatus.PAUSED, rest.status)
        assertTrue(rest.awaitingPhaseStart)
        assertEquals(25 * 60000L, rest.elapsedMillis)
        assertEquals(5 * 60000L, FocusRules.remaining(rest, 99_999_999))
        val resumed = rest.copy(status = FocusStatus.RUNNING, resumedAt = 30 * 60000L)
        val next = PomodoroRules.next(resumed, 35 * 60000L)
        assertEquals(FocusPhase.FOCUS, next.phase)
        assertEquals(2, next.cycleIndex)
        assertEquals(25 * 60000L, next.elapsedMillis)
    }
    @Test fun lastFocusCycleLeadsToLongBreakThenFinishes() {
        val last = focus(4).copy(elapsedMillis = 75 * 60000L)
        val rest = PomodoroRules.next(last, 25 * 60000L)
        assertEquals(FocusPhase.LONG_BREAK, rest.phase)
        assertEquals(100 * 60000L, rest.elapsedMillis)
        val finished = PomodoroRules.next(rest.copy(status = FocusStatus.RUNNING, resumedAt = 100000), 100000 + 15 * 60000L)
        assertEquals(FocusStatus.FINISHED, finished.status)
        assertEquals(100 * 60000L, finished.elapsedMillis)
        assertNotNull(finished.endedAt)
    }
    @Test fun customConfigurationRejectsInvalidDurationsAndCycles() {
        assertTrue(config.valid); assertTrue(PomodoroConfig(50, 10).valid)
        assertFalse(config.copy(cycles = 0).valid)
        assertFalse(config.copy(focusMinutes = 181).valid)
        assertFalse(config.copy(shortBreakMinutes = 0).valid)
        assertFalse(config.copy(longBreakMinutes = 121).valid)
    }
}
