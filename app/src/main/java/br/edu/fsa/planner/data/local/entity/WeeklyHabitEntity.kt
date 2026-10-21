package br.edu.fsa.planner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.edu.fsa.planner.domain.model.WeeklyHabit
import java.time.DayOfWeek
import java.time.LocalDate

@Entity(tableName = "weekly_habits", indices = [Index("weekStart")])
data class WeeklyHabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStart: LocalDate, val title: String, val completedDays: Int,
)

fun WeeklyHabitEntity.toModel() = WeeklyHabit(id, weekStart, title,
    DayOfWeek.entries.filter { completedDays and (1 shl (it.value - 1)) != 0 }.toSet())

fun WeeklyHabit.toEntity() = WeeklyHabitEntity(id, weekStart, title,
    completedDays.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) })
