package br.edu.fsa.planner.data.repository

import br.edu.fsa.planner.data.local.dao.PlannerDao
import br.edu.fsa.planner.data.local.entity.toEntity
import br.edu.fsa.planner.data.local.entity.toModel
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

class PlannerRepository(private val dao: PlannerDao, private val clock: Clock = Clock.systemDefaultZone()) {
    private val noteMutex = Mutex()
    private val habitMutex = Mutex()
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
    suspend fun updateNote(key: String, transform: (PlannerNote) -> PlannerNote) {
        noteMutex.withLock {
            val current = dao.observeNote(key).first()?.toModel() ?: PlannerNote(key)
            dao.saveNote(transform(current).toEntity())
        }
    }

    fun observeHabits(weekStart: LocalDate) = dao.observeHabits(weekStart).map { rows -> rows.map { it.toModel() } }
    suspend fun saveHabit(habit: WeeklyHabit): Long {
        val title = habit.title.trim()
        require(title.isNotEmpty() && title.length <= PlannerJournal.HabitTitleLimit)
        val normalized = habit.copy(title = title, weekStart = PlannerDates.week(habit.weekStart).start)
        return if (habit.id == 0L) dao.insertHabit(normalized.toEntity())
        else { check(dao.updateHabit(normalized.toEntity()) == 1); habit.id }
    }
    suspend fun toggleHabit(id: Long, day: DayOfWeek) {
        habitMutex.withLock {
            val habit = checkNotNull(dao.getHabit(id)).toModel()
            val days = if (day in habit.completedDays) habit.completedDays - day else habit.completedDays + day
            check(dao.updateHabit(habit.copy(completedDays = days).toEntity()) == 1)
        }
    }
    suspend fun deleteHabit(id: Long) = dao.deleteHabit(id)
}
