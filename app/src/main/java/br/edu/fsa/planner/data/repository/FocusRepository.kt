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
        if (current?.status == FocusStatus.RUNNING && FocusRules.remaining(current, clock.millis()) == 0L) finish(current.id)
    }
    suspend fun start(itemId: Long?, title: String, minutes: Int): Long = mutex.withLock {
        require(minutes in 1..180)
        active.first()?.let { return@withLock it.id }
        val now = clock.millis()
        dao.insert(FocusSession(plannerItemId = itemId, title = title, day = LocalDate.now(clock), startedAt = now,
            plannedSeconds = minutes * 60, resumedAt = now).toEntity())
    }
    suspend fun pause(id: Long) = mutex.withLock {
        val session = checkNotNull(get(id))
        if (session.status == FocusStatus.RUNNING) dao.update(session.copy(elapsedMillis = FocusRules.elapsed(session, clock.millis()),
            resumedAt = null, status = FocusStatus.PAUSED).toEntity())
    }
    suspend fun resume(id: Long) = mutex.withLock {
        val session = checkNotNull(get(id))
        if (session.status == FocusStatus.PAUSED) dao.update(session.copy(resumedAt = clock.millis(), status = FocusStatus.RUNNING).toEntity())
    }
    suspend fun finish(id: Long) = mutex.withLock {
        val session = checkNotNull(get(id))
        if (session.endedAt == null) {
            val elapsed = FocusRules.elapsed(session, clock.millis())
            dao.update(session.copy(endedAt = clock.millis(), elapsedMillis = elapsed, resumedAt = null,
                status = if (elapsed >= session.plannedSeconds * 1000L) FocusStatus.FINISHED else FocusStatus.STOPPED).toEntity())
        }
    }
}
