package br.edu.fsa.planner.data.local.dao

import androidx.room.*
import br.edu.fsa.planner.data.local.entity.PlannerItemEntity
import br.edu.fsa.planner.data.local.entity.PlannerNoteEntity
import br.edu.fsa.planner.data.local.entity.WeeklyHabitEntity
import br.edu.fsa.planner.data.local.entity.SubtaskEntity
import br.edu.fsa.planner.domain.model.SubtaskProgress
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PlannerDao : HabitDao {
    @Query("SELECT * FROM planner_items WHERE date = :date ORDER BY isCompleted, CASE WHEN startTime IS NULL THEN 1 ELSE 0 END, startTime, CASE priority WHEN 'HIGH' THEN 0 WHEN 'MEDIUM' THEN 1 ELSE 2 END, id")
    fun observeDay(date: LocalDate): Flow<List<PlannerItemEntity>>

    @Query("SELECT * FROM planner_items WHERE date BETWEEN :start AND :end ORDER BY date, isCompleted, CASE WHEN startTime IS NULL THEN 1 ELSE 0 END, startTime, id")
    fun observeRange(start: LocalDate, end: LocalDate): Flow<List<PlannerItemEntity>>

    @Query("SELECT * FROM planner_items WHERE date BETWEEN :start AND :end AND isCompleted = :completed ORDER BY date, id")
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
