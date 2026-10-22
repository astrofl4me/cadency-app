package br.edu.fsa.planner.domain.model

object FocusRules {
    val presets = listOf(15, 25, 45, 60)
    fun elapsed(session: FocusSession, now: Long): Long = (session.elapsedMillis +
        if (session.status == FocusStatus.RUNNING && session.resumedAt != null) (now - session.resumedAt).coerceAtLeast(0) else 0)
        .coerceIn(0, session.plannedSeconds * 1000L)
    fun remaining(session: FocusSession, now: Long) = session.plannedSeconds * 1000L - elapsed(session, now)
    fun timer(millis: Long): String {
        val seconds = ((millis.coerceAtLeast(0) + 999) / 1000)
        return if (seconds >= 3600) "%d:%02d:%02d".format(seconds / 3600, seconds / 60 % 60, seconds % 60)
        else "%02d:%02d".format(seconds / 60, seconds % 60)
    }
}
