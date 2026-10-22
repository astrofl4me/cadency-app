package br.edu.fsa.planner.data.local.dao

import androidx.room.*
import br.edu.fsa.planner.data.local.entity.PlannerItemEntity
import br.edu.fsa.planner.data.local.entity.PlannerNoteEntity
import br.edu.fsa.planner.data.local.entity.WeeklyHabitEntity
import br.edu.fsa.planner.data.local.entity.SubtaskEntity
import br.edu.fsa.planner.domain.model.SubtaskProgress
import br.edu.fsa.planner.domain.model.DailyCount
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PlannerDao : HabitDao {
    @Query("SELECT * FROM planner_items WHERE date = :date AND isInbox = 0 AND isDiscarded = 0 ORDER BY isCompleted, CASE WHEN startTime IS NULL THEN 1 ELSE 0 END, startTime, CASE priority WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END, id")
    fun observeDay(date: LocalDate): Flow<List<PlannerItemEntity>>

    @Query("SELECT * FROM planner_items WHERE date BETWEEN :start AND :end AND isInbox = 0 AND isDiscarded = 0 ORDER BY date, isCompleted, CASE WHEN startTime IS NULL THEN 1 ELSE 0 END, startTime, id")
    fun observeRange(start: LocalDate, end: LocalDate): Flow<List<PlannerItemEntity>>

    @Query("SELECT * FROM planner_items WHERE date BETWEEN :start AND :end AND isCompleted = :completed AND isInbox = 0 AND isDiscarded = 0 ORDER BY date, id")
    fun observeCompletion(start: LocalDate, end: LocalDate, completed: Boolean): Flow<List<PlannerItemEntity>>

    @Query("SELECT * FROM planner_items WHERE id = :id")
    suspend fun get(id: Long): PlannerItemEntity?
    @Query("SELECT * FROM planner_items WHERE id = :id") fun observeItem(id: Long): Flow<PlannerItemEntity?>
    @Query("SELECT * FROM subtasks WHERE plannerItemId = :id ORDER BY position, id") fun observeSubtasks(id: Long): Flow<List<SubtaskEntity>>
    @Query("SELECT * FROM subtasks WHERE plannerItemId = :id ORDER BY position, id") suspend fun subtaskRows(id: Long): List<SubtaskEntity>
    @Insert suspend fun addSubtask(step: SubtaskEntity): Long
    @Query("UPDATE subtasks SET isCompleted = :completed WHERE id = :id") suspend fun setSubtaskCompleted(id: Long, completed: Boolean)
    @Query("DELETE FROM subtasks WHERE id = :id") suspend fun removeSubtask(id: Long)
    @Query("UPDATE subtasks SET position = :position WHERE id = :id") suspend fun setSubtaskPosition(id: Long, position: Int)
    @Query("SELECT s.plannerItemId, COUNT(*) AS total, SUM(s.isCompleted) AS done FROM subtasks s JOIN planner_items p ON p.id = s.plannerItemId WHERE p.date = :date GROUP BY s.plannerItemId")
    fun observeSubtaskProgress(date: LocalDate): Flow<List<SubtaskProgress>>
    @Transaction suspend fun moveSubtask(parentId: Long, id: Long, amount: Int) {
        val rows = subtaskRows(parentId)
        val index = rows.indexOfFirst { it.id == id }
        val other = index + amount
        if (index >= 0 && other in rows.indices) {
            setSubtaskPosition(id, rows[other].position)
            setSubtaskPosition(rows[other].id, rows[index].position)
        }
    }

    @Insert suspend fun insert(item: PlannerItemEntity): Long
    @Update suspend fun update(item: PlannerItemEntity): Int
    @Query("DELETE FROM planner_items WHERE id = :id") suspend fun delete(id: Long): Int
    @Query("UPDATE planner_items SET isCompleted = :completed, updatedAt = :timestamp WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean, timestamp: Long): Int
    @Query("UPDATE planner_items SET isCompleted = :completed, completedOn = :date, updatedAt = :timestamp WHERE id = :id")
    suspend fun setCompletedWithDate(id: Long, completed: Boolean, timestamp: Long, date: LocalDate?): Int
    @Query("SELECT * FROM planner_items WHERE date < :today AND isCompleted = 0 AND isInbox = 0 AND isDiscarded = 0 AND type != 'EVENT' ORDER BY date, id")
    fun observePending(today: LocalDate): Flow<List<PlannerItemEntity>>
    @Query("SELECT * FROM planner_items WHERE isInbox = 1 AND isDiscarded = 0 AND isCompleted = 0 ORDER BY createdAt, id")
    fun observeInbox(): Flow<List<PlannerItemEntity>>
    @Query("SELECT COALESCE(completedOn,date) AS date, COUNT(*) AS value FROM planner_items WHERE isCompleted = 1 AND isDiscarded = 0 AND type != 'EVENT' AND COALESCE(completedOn,date) BETWEEN :start AND :end GROUP BY COALESCE(completedOn,date) ORDER BY date")
    fun observeCompletedCounts(start: LocalDate, end: LocalDate): Flow<List<DailyCount>>
    @Query("SELECT COUNT(DISTINCT date) FROM planner_items WHERE isInbox = 0 AND isDiscarded = 0 AND date BETWEEN :start AND :end")
    fun observePlannedDays(start: LocalDate, end: LocalDate): Flow<Int>
    @Query("UPDATE planner_items SET originalDate = COALESCE(originalDate,date), date = :date, rolloverCount = rolloverCount + 1, isInbox = 0, updatedAt = :timestamp WHERE id = :id AND isCompleted = 0 AND isDiscarded = 0")
    suspend fun reschedule(id: Long, date: LocalDate, timestamp: Long)
    @Query("UPDATE planner_items SET originalDate = COALESCE(originalDate,date), rolloverCount = rolloverCount + 1, isInbox = 1, updatedAt = :timestamp WHERE id = :id AND isCompleted = 0")
    suspend fun moveToInbox(id: Long, timestamp: Long)
    @Query("UPDATE planner_items SET isDiscarded = 1, updatedAt = :timestamp WHERE id = :id AND isCompleted = 0")
    suspend fun discard(id: Long, timestamp: Long)

    @Query("SELECT * FROM planner_notes WHERE `key` = :key")
    fun observeNote(key: String): Flow<PlannerNoteEntity?>

    @Upsert suspend fun saveNote(note: PlannerNoteEntity)

    @Query("SELECT * FROM weekly_habits WHERE weekStart = :weekStart ORDER BY id")
    fun observeHabits(weekStart: LocalDate): Flow<List<WeeklyHabitEntity>>
    @Query("SELECT * FROM weekly_habits WHERE id = :id")
    suspend fun getHabit(id: Long): WeeklyHabitEntity?
    @Insert suspend fun insertHabit(habit: WeeklyHabitEntity): Long
    @Update suspend fun updateHabit(habit: WeeklyHabitEntity): Int
    @Query("DELETE FROM weekly_habits WHERE id = :id") suspend fun deleteHabit(id: Long)
}
