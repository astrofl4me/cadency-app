package br.edu.fsa.planner.data.local.dao

import androidx.room.*
import br.edu.fsa.planner.data.local.entity.PlannerItemEntity
import br.edu.fsa.planner.data.local.entity.PlannerNoteEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface PlannerDao {
    @Query("SELECT * FROM planner_items WHERE date = :date ORDER BY isCompleted, CASE WHEN startTime IS NULL THEN 1 ELSE 0 END, startTime, priority DESC, id")
    fun observeDay(date: LocalDate): Flow<List<PlannerItemEntity>>

    @Query("SELECT * FROM planner_items WHERE date BETWEEN :start AND :end ORDER BY date, isCompleted, CASE WHEN startTime IS NULL THEN 1 ELSE 0 END, startTime, id")
    fun observeRange(start: LocalDate, end: LocalDate): Flow<List<PlannerItemEntity>>

    @Query("SELECT * FROM planner_items WHERE date BETWEEN :start AND :end AND isCompleted = :completed ORDER BY date, id")
    fun observeCompletion(start: LocalDate, end: LocalDate, completed: Boolean): Flow<List<PlannerItemEntity>>

    @Query("SELECT * FROM planner_items WHERE id = :id")
    suspend fun get(id: Long): PlannerItemEntity?

    @Insert suspend fun insert(item: PlannerItemEntity): Long
    @Update suspend fun update(item: PlannerItemEntity): Int
    @Query("DELETE FROM planner_items WHERE id = :id") suspend fun delete(id: Long): Int
    @Query("UPDATE planner_items SET isCompleted = :completed, updatedAt = :timestamp WHERE id = :id")
    suspend fun setCompleted(id: Long, completed: Boolean, timestamp: Long): Int

    @Query("SELECT * FROM planner_notes WHERE `key` = :key")
    fun observeNote(key: String): Flow<PlannerNoteEntity?>

    @Upsert suspend fun saveNote(note: PlannerNoteEntity)
}
