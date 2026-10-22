package br.edu.fsa.planner.data.repository

import br.edu.fsa.planner.data.local.dao.FocusDao
import br.edu.fsa.planner.data.local.entity.*
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.LocalDate

class FocusRepository(private val dao: FocusDao, private val clock: Clock = Clock.systemDefaultZone()) {
    private val mutex = Mutex()
    val active = dao.observeActive().map { it?.toModel() }
    fun observe(id: Long) = dao.observe(id).map { it?.toModel() }
    suspend fun get(id: Long) = dao.get(id)?.toModel()
    fun minutes(interval: DateInterval) = dao.observeMinutes(interval.start, interval.endInclusive)
    suspend fun finishExpired() {
        val current = active.first()
        if (current?.status == FocusStatus.RUNNING && FocusRules.remaining(current, clock.millis()) == 0L) advance(current.id)
    }
    suspend fun start(itemId: Long?, title: String, minutes: Int, habitId: Long? = null,
                      mode: FocusMode = FocusMode.FREE, pomodoro: PomodoroConfig = PomodoroConfig()): Long = mutex.withLock {
        require(minutes in 1..180)
        require(pomodoro.valid && (itemId == null || habitId == null))
        active.first()?.let { return@withLock it.id }
        val now = clock.millis()
        dao.insert(FocusSession(plannerItemId = itemId, title = title, day = LocalDate.now(clock), startedAt = now,
            plannedSeconds = minutes * 60, resumedAt = now, habitId = habitId, mode = mode, pomodoro = pomodoro).toEntity())
    }
    suspend fun pause(id: Long) = mutex.withLock {
        val session = checkNotNull(get(id))
        if (session.status == FocusStatus.RUNNING) {
            val elapsed = FocusRules.elapsed(session, clock.millis())
            dao.update(if (FocusRules.remaining(session, clock.millis()) == 0L) endedPhase(session).toEntity()
                else session.copy(elapsedMillis = elapsed, phaseElapsedMillis = FocusRules.phaseElapsed(session, clock.millis()), resumedAt = null, status = FocusStatus.PAUSED).toEntity())
        }
    }
    suspend fun resume(id: Long) = mutex.withLock {
        val session = checkNotNull(get(id))
        if (session.status == FocusStatus.PAUSED) dao.update(session.copy(resumedAt = clock.millis(), status = FocusStatus.RUNNING, awaitingPhaseStart = false).toEntity())
    }
    suspend fun advance(id: Long) = mutex.withLock {
        val session = checkNotNull(get(id))
        if (session.status == FocusStatus.RUNNING && FocusRules.remaining(session, clock.millis()) == 0L) dao.update(endedPhase(session).toEntity())
    }
    private fun endedPhase(session: FocusSession) = if (session.mode == FocusMode.POMODORO) PomodoroRules.next(session, clock.millis())
        else finished(session, FocusRules.elapsed(session, clock.millis()))
    suspend fun finish(id: Long) = mutex.withLock {
        val session = checkNotNull(get(id))
        if (session.endedAt == null) {
            val elapsed = FocusRules.elapsed(session, clock.millis())
            dao.update(finished(session, elapsed).toEntity())
        }
    }
    private fun finished(session: FocusSession, elapsed: Long): FocusSession {
        val complete = if (session.mode == FocusMode.FREE) elapsed >= session.plannedSeconds * 1000L
            else elapsed >= session.pomodoro.focusMinutes * 60000L * session.pomodoro.cycles
        val end = if (session.mode == FocusMode.FREE && complete && session.resumedAt != null) session.resumedAt + session.plannedSeconds * 1000L - session.elapsedMillis else clock.millis()
        return session.copy(endedAt = end, elapsedMillis = elapsed, phaseElapsedMillis = FocusRules.phaseElapsed(session, clock.millis()), resumedAt = null, awaitingPhaseStart = false,
            status = if (complete) FocusStatus.FINISHED else FocusStatus.STOPPED)
    }
}
