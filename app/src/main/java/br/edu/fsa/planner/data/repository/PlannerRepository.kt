package br.edu.fsa.planner.data.repository

import br.edu.fsa.planner.data.local.dao.PlannerDao
import br.edu.fsa.planner.data.local.entity.toEntity
import br.edu.fsa.planner.data.local.entity.toModel
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth

class PlannerRepository(private val dao: PlannerDao, private val clock: Clock = Clock.systemDefaultZone()) {
    fun observeDay(date: LocalDate) = dao.observeDay(date).map { rows -> rows.map { it.toModel() } }
    fun observeRange(interval: DateInterval) = dao.observeRange(interval.start, interval.endInclusive).map { rows -> rows.map { it.toModel() } }
    fun observeMonth(month: YearMonth) = observeRange(PlannerDates.month(month))
    fun observeCompletion(interval: DateInterval, completed: Boolean) =
        dao.observeCompletion(interval.start, interval.endInclusive, completed).map { rows -> rows.map { it.toModel() } }
    suspend fun get(id: Long) = dao.get(id)?.toModel()

    suspend fun save(item: PlannerItem): Long {
        val now = clock.millis()
        return if (item.id == 0L) dao.insert(item.copy(createdAt = now, updatedAt = now).toEntity())
        else {
            check(dao.update(item.copy(updatedAt = now).toEntity()) == 1) { "Item no longer exists" }
            item.id
        }
    }
    suspend fun delete(id: Long) { check(dao.delete(id) == 1) { "Item no longer exists" } }
    suspend fun setCompleted(item: PlannerItem, completed: Boolean) {
        check(dao.setCompleted(item.id, completed, clock.millis()) == 1) { "Item no longer exists" }
    }
    fun observeNote(key: String) = dao.observeNote(key).map { it?.toModel() ?: PlannerNote(key) }
    suspend fun saveNote(note: PlannerNote) { dao.saveNote(note.toEntity()) }
}
