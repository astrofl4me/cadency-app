package br.edu.fsa.planner.data.local.dao

import androidx.room.*
import br.edu.fsa.planner.data.local.entity.FocusSessionEntity
import br.edu.fsa.planner.domain.model.DailyCount
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface FocusDao {
    @Query("SELECT * FROM focus_sessions WHERE id = :id") fun observe(id: Long): Flow<FocusSessionEntity?>
    @Query("SELECT * FROM focus_sessions WHERE id = :id") suspend fun get(id: Long): FocusSessionEntity?
    @Query("SELECT * FROM focus_sessions WHERE status IN ('RUNNING','PAUSED') ORDER BY id DESC LIMIT 1")
    fun observeActive(): Flow<FocusSessionEntity?>
    @Insert suspend fun insert(session: FocusSessionEntity): Long
    @Update suspend fun update(session: FocusSessionEntity)
    @Query("SELECT day AS date, SUM(elapsedMillis) / 60000 AS value FROM focus_sessions WHERE endedAt IS NOT NULL AND day BETWEEN :start AND :end GROUP BY day ORDER BY day")
    fun observeMinutes(start: LocalDate, end: LocalDate): Flow<List<DailyCount>>
}
