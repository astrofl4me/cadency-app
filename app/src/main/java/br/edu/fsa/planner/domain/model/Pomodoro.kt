package br.edu.fsa.planner.domain.model

enum class FocusMode { FREE, POMODORO }
enum class FocusPhase { FOCUS, SHORT_BREAK, LONG_BREAK }
data class PomodoroConfig(val focusMinutes: Int = 25, val shortBreakMinutes: Int = 5, val cycles: Int = 4, val longBreakMinutes: Int = 15) {
    val valid get() = focusMinutes in 1..180 && shortBreakMinutes in 1..60 && cycles in 1..12 && longBreakMinutes in 1..120
}

object PomodoroRules {
    fun next(session: FocusSession, now: Long): FocusSession {
        require(session.mode == FocusMode.POMODORO)
        require(FocusRules.remaining(session, now) == 0L)
        val elapsed = FocusRules.elapsed(session, now)
        val deadline = session.resumedAt?.plus(FocusRules.durationMillis(session) - session.phaseElapsedMillis) ?: now
        if (session.phase == FocusPhase.LONG_BREAK) return session.copy(elapsedMillis = elapsed, phaseElapsedMillis = FocusRules.durationMillis(session),
            resumedAt = null, endedAt = deadline, status = FocusStatus.FINISHED, awaitingPhaseStart = false)
        val next = when (session.phase) {
            FocusPhase.FOCUS -> if (session.cycleIndex >= session.pomodoro.cycles) FocusPhase.LONG_BREAK else FocusPhase.SHORT_BREAK
            else -> FocusPhase.FOCUS
        }
        return session.copy(elapsedMillis = elapsed, phaseElapsedMillis = 0, phase = next,
            cycleIndex = if (next == FocusPhase.FOCUS) session.cycleIndex + 1 else session.cycleIndex,
            resumedAt = null, status = FocusStatus.PAUSED, awaitingPhaseStart = true)
    }
}
