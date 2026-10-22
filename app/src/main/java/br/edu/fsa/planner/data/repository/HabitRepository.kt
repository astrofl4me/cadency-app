package br.edu.fsa.planner.data.repository

import br.edu.fsa.planner.data.local.dao.HabitDao
import br.edu.fsa.planner.data.local.entity.*
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Clock
import java.time.LocalDate

class HabitRepository(private val dao: HabitDao, private val clock: Clock = Clock.systemDefaultZone()) {
    private val mutex = Mutex()
    val habits = dao.observeAll().map { rows -> rows.map { it.toModel() } }
    fun checkIns(interval: DateInterval) = dao.observeCheckIns(interval.start, interval.endInclusive)
        .map { rows -> rows.map { HabitCheckIn(it.habitId, it.date) } }
    fun history(id: Long) = dao.observeHistory(id).map { rows -> rows.filter { it.completed }.map { HabitCheckIn(it.habitId, it.date) } }
    suspend fun get(id: Long) = dao.findHabit(id)?.toModel()
    suspend fun save(habit: Habit): Long {
        require(HabitRules.valid(habit))
        val normalized = habit.copy(name = habit.name.trim(),
            createdAt = habit.createdAt.takeIf { it > 0 } ?: clock.millis())
        return if (habit.id == 0L) dao.addHabit(normalized.toEntity())
        else { check(dao.editHabit(normalized.toEntity()) == 1); habit.id }
    }
    suspend fun toggle(id: Long, date: LocalDate) = mutex.withLock {
        val habit = checkNotNull(get(id))
        require(HabitRules.isPlanned(habit, date))
        val checked = dao.observeHistory(id).first().any { it.date == date && it.completed }
        if (checked) dao.undo(id, date) else dao.checkIn(HabitCheckInEntity(habitId = id, date = date, completed = true, createdAt = clock.millis()))
    }
    suspend fun delete(id: Long) = dao.removeHabit(id)
    suspend fun markDone(id: Long, date: LocalDate) = mutex.withLock {
        require(HabitRules.isPlanned(checkNotNull(get(id)), date))
        dao.checkIn(HabitCheckInEntity(habitId = id, date = date, completed = true, createdAt = clock.millis()))
    }
    fun weekly(weekStart: LocalDate) = combine(habits, checkIns(DateInterval(weekStart, weekStart.plusDays(6)))) { rows, checks ->
        rows.filter { habit -> (0L..6L).any { HabitRules.isPlanned(habit, weekStart.plusDays(it)) } }.map { habit ->
            WeeklyHabit(habit.id, weekStart, habit.name, checks.filter { it.habitId == habit.id }.map { it.date.dayOfWeek }.toSet())
        }
    }
    suspend fun saveWeekly(weekly: WeeklyHabit): Long {
        val existing = get(weekly.id)
        return save(existing?.copy(name = weekly.title) ?: Habit(name = weekly.title, startsOn = weekly.weekStart,
            endsOn = weekly.weekStart.plusDays(6)))
    }
}
