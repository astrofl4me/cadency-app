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

class PlannerRepository(private val dao: PlannerDao, private val clock: Clock = Clock.systemDefaultZone(),
                        private val habits: HabitRepository = HabitRepository(dao, clock)) {
    private val noteMutex = Mutex()
    private val habitMutex = Mutex()
    fun observeDay(date: LocalDate) = dao.observeDay(date).map { rows -> rows.map { it.toModel() } }
    fun observeRange(interval: DateInterval) = dao.observeRange(interval.start, interval.endInclusive).map { rows -> rows.map { it.toModel() } }
    fun observeMonth(month: YearMonth) = observeRange(PlannerDates.month(month))
    fun observeCompletion(interval: DateInterval, completed: Boolean) =
        dao.observeCompletion(interval.start, interval.endInclusive, completed).map { rows -> rows.map { it.toModel() } }
    suspend fun get(id: Long) = dao.get(id)?.toModel()
    fun observeItem(id: Long) = dao.observeItem(id).map { it?.toModel() }
    fun observeSubtasks(id: Long) = dao.observeSubtasks(id).map { rows -> rows.map { it.toModel() } }
    fun observeSubtaskProgress(date: LocalDate) = dao.observeSubtaskProgress(date)
    suspend fun addSubtask(parentId: Long, title: String) {
        require(title.trim().length in 1..120)
        val position = (dao.subtaskRows(parentId).maxOfOrNull { it.position } ?: -1) + 1
        dao.addSubtask(br.edu.fsa.planner.data.local.entity.SubtaskEntity(plannerItemId = parentId,
            title = title.trim(), isCompleted = false, position = position, createdAt = clock.millis()))
    }
    suspend fun toggleSubtask(step: Subtask) = dao.setSubtaskCompleted(step.id, !step.isCompleted)
    suspend fun removeSubtask(id: Long) = dao.removeSubtask(id)
    suspend fun moveSubtask(step: Subtask, direction: Int) = dao.moveSubtask(step.plannerItemId, step.id, direction)

    suspend fun save(item: PlannerItem): Long {
        val now = clock.millis()
        val previous = if (item.id == 0L) null else get(item.id)
        val rescheduled = previous != null && previous.date != item.date
        val value = item.copy(originalDate = item.originalDate ?: previous?.originalDate ?: previous?.date ?: item.date,
            rolloverCount = if (rescheduled) previous!!.rolloverCount + 1 else item.rolloverCount,
            isInbox = if (rescheduled) false else item.isInbox,
            completedOn = if (item.isCompleted) item.completedOn ?: LocalDate.now(clock) else null)
        return if (item.id == 0L) dao.insert(value.copy(createdAt = now, updatedAt = now).toEntity())
        else {
            check(dao.update(value.copy(updatedAt = now).toEntity()) == 1) { "Item no longer exists" }
            item.id
        }
    }
    suspend fun delete(id: Long) { check(dao.delete(id) == 1) { "Item no longer exists" } }
    suspend fun setCompleted(item: PlannerItem, completed: Boolean) {
        check(dao.setCompletedWithDate(item.id, completed, clock.millis(), if (completed) LocalDate.now(clock) else null) == 1) { "Item no longer exists" }
    }
    fun observePending(today: LocalDate) = dao.observePending(today).map { rows -> rows.map { it.toModel() } }
    fun observeInbox() = dao.observeInbox().map { rows -> rows.map { it.toModel() } }
    suspend fun reschedule(id: Long, date: LocalDate) = dao.reschedule(id, date, clock.millis())
    suspend fun toInbox(id: Long) = dao.moveToInbox(id, clock.millis())
    suspend fun discard(id: Long) = dao.discard(id, clock.millis())
    fun observeNote(key: String) = dao.observeNote(key).map { it?.toModel() ?: PlannerNote(key) }
    suspend fun saveNote(note: PlannerNote) { dao.saveNote(note.toEntity()) }
    suspend fun updateNote(key: String, transform: (PlannerNote) -> PlannerNote) {
        noteMutex.withLock {
            val current = dao.observeNote(key).first()?.toModel() ?: PlannerNote(key)
            dao.saveNote(transform(current).toEntity())
        }
    }

    fun observeHabits(weekStart: LocalDate) = habits.weekly(weekStart)
    suspend fun saveHabit(habit: WeeklyHabit): Long {
        return habits.saveWeekly(habit.copy(weekStart = PlannerDates.week(habit.weekStart).start))
    }
    suspend fun toggleHabit(id: Long, day: DayOfWeek, weekStart: LocalDate? = null) {
        val start = weekStart ?: PlannerDates.week(checkNotNull(habits.get(id)).startsOn).start
        habits.toggle(id, start.plusDays((day.value - 1).toLong()))
    }
    suspend fun deleteHabit(id: Long) = habits.delete(id)
}
