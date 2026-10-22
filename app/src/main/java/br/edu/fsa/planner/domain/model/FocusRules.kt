package br.edu.fsa.planner.domain.model

object FocusRules {
    val presets = listOf(15, 25, 45, 60)
    fun durationMillis(session: FocusSession) = if (session.mode == FocusMode.FREE) session.plannedSeconds * 1000L else when (session.phase) {
        FocusPhase.FOCUS -> session.pomodoro.focusMinutes; FocusPhase.SHORT_BREAK -> session.pomodoro.shortBreakMinutes
        FocusPhase.LONG_BREAK -> session.pomodoro.longBreakMinutes
    } * 60000L
    private fun storedPhase(session: FocusSession) = if (session.mode == FocusMode.FREE) session.elapsedMillis else session.phaseElapsedMillis
    private fun increment(session: FocusSession, now: Long) = if (session.status == FocusStatus.RUNNING && session.resumedAt != null)
        (now - session.resumedAt).coerceIn(0, (durationMillis(session) - storedPhase(session)).coerceAtLeast(0)) else 0L
    fun phaseElapsed(session: FocusSession, now: Long) = storedPhase(session) + increment(session, now)
    fun elapsed(session: FocusSession, now: Long) = session.elapsedMillis + if (session.phase == FocusPhase.FOCUS) increment(session, now) else 0L
    fun remaining(session: FocusSession, now: Long) = (durationMillis(session) - phaseElapsed(session, now)).coerceAtLeast(0)
    fun timer(millis: Long): String {
        val seconds = ((millis.coerceAtLeast(0) + 999) / 1000)
        return if (seconds >= 3600) "%d:%02d:%02d".format(seconds / 3600, seconds / 60 % 60, seconds % 60)
        else "%02d:%02d".format(seconds / 60, seconds % 60)
    }
}
