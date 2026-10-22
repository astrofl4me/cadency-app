package br.edu.fsa.planner.data.local.dao

import androidx.room.*
import br.edu.fsa.planner.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY position, id") fun observeAll(): Flow<List<HabitEntity>>
    @Query("SELECT * FROM habits WHERE id = :id") suspend fun findHabit(id: Long): HabitEntity?
    @Insert suspend fun addHabit(habit: HabitEntity): Long
    @Update suspend fun editHabit(habit: HabitEntity): Int
    @Query("DELETE FROM habits WHERE id = :id") suspend fun removeHabit(id: Long)
    @Query("SELECT * FROM habit_check_ins WHERE date BETWEEN :start AND :end AND completed = 1")
    fun observeCheckIns(start: LocalDate, end: LocalDate): Flow<List<HabitCheckInEntity>>
    @Query("SELECT * FROM habit_check_ins WHERE habitId = :id ORDER BY date DESC")
    fun observeHistory(id: Long): Flow<List<HabitCheckInEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun checkIn(checkIn: HabitCheckInEntity)
    @Query("DELETE FROM habit_check_ins WHERE habitId = :id AND date = :date") suspend fun undo(id: Long, date: LocalDate)
}
