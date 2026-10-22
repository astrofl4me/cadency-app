package br.edu.fsa.planner.data.local.entity

import androidx.room.*
import br.edu.fsa.planner.domain.model.*
import java.time.DayOfWeek
import java.time.LocalDate

@Entity(tableName = "habits", indices = [Index("startsOn")])
data class HabitEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String,
    val description: String, val icon: String, val category: Category, val frequencyType: String,
    val targetPerPeriod: Int, val daysMask: Int, val createdAt: Long, val startsOn: LocalDate,
    val endsOn: LocalDate?, val isArchived: Boolean, val position: Int, val colorKey: String)

@Entity(tableName = "habit_check_ins", foreignKeys = [ForeignKey(entity = HabitEntity::class,
    parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["habitId", "date"], unique = true), Index("date")])
data class HabitCheckInEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val habitId: Long,
    val date: LocalDate, val completed: Boolean, val createdAt: Long)

@Entity(tableName = "subtasks", foreignKeys = [ForeignKey(entity = PlannerItemEntity::class,
    parentColumns = ["id"], childColumns = ["plannerItemId"], onDelete = ForeignKey.CASCADE)], indices = [Index("plannerItemId")])
data class SubtaskEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val plannerItemId: Long,
    val title: String, val isCompleted: Boolean, val position: Int, val createdAt: Long)

@Entity(tableName = "focus_sessions", foreignKeys = [ForeignKey(entity = PlannerItemEntity::class,
    parentColumns = ["id"], childColumns = ["plannerItemId"], onDelete = ForeignKey.SET_NULL),
    ForeignKey(entity = HabitEntity::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.SET_NULL)],
    indices = [Index("plannerItemId"), Index("habitId"), Index("day"), Index("status")])
data class FocusSessionEntity(@PrimaryKey(autoGenerate = true) val id: Long = 0, val plannerItemId: Long?,
    val title: String, val day: LocalDate, val startedAt: Long, val endedAt: Long?, val plannedSeconds: Int,
    val elapsedMillis: Long, val resumedAt: Long?, val status: String,
    val habitId: Long? = null,
    @ColumnInfo(defaultValue = "'FREE'") val mode: String = "FREE",
    @ColumnInfo(defaultValue = "'FOCUS'") val phase: String = "FOCUS",
    @ColumnInfo(defaultValue = "1") val cycleIndex: Int = 1,
    @ColumnInfo(defaultValue = "25") val workMinutes: Int = 25,
    @ColumnInfo(defaultValue = "5") val shortBreakMinutes: Int = 5,
    @ColumnInfo(defaultValue = "4") val cycles: Int = 4,
    @ColumnInfo(defaultValue = "15") val longBreakMinutes: Int = 15,
    @ColumnInfo(defaultValue = "0") val phaseElapsedMillis: Long = 0,
    @ColumnInfo(defaultValue = "0") val awaitingPhaseStart: Boolean = false)

fun HabitEntity.toModel() = Habit(id, name, description, HabitIcon.valueOf(icon), category,
    FrequencyType.valueOf(frequencyType), targetPerPeriod,
    DayOfWeek.entries.filter { daysMask and (1 shl (it.value - 1)) != 0 }.toSet(), createdAt,
    startsOn, endsOn, isArchived, position, colorKey)
fun Habit.toEntity() = HabitEntity(id, name, description, icon.name, category, frequency.name,
    targetPerPeriod, days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }, createdAt,
    startsOn, endsOn, isArchived, position, colorKey)
fun SubtaskEntity.toModel() = Subtask(id, plannerItemId, title, isCompleted, position, createdAt)
fun FocusSessionEntity.toModel() = FocusSession(id, plannerItemId, title, day, startedAt, endedAt,
    plannedSeconds, elapsedMillis, resumedAt, FocusStatus.valueOf(status), habitId, FocusMode.valueOf(mode), FocusPhase.valueOf(phase),
    cycleIndex, PomodoroConfig(workMinutes, shortBreakMinutes, cycles, longBreakMinutes), phaseElapsedMillis, awaitingPhaseStart)
fun FocusSession.toEntity() = FocusSessionEntity(id, plannerItemId, title, day, startedAt, endedAt,
    plannedSeconds, elapsedMillis, resumedAt, status.name, habitId, mode.name, phase.name, cycleIndex,
    pomodoro.focusMinutes, pomodoro.shortBreakMinutes, pomodoro.cycles, pomodoro.longBreakMinutes, phaseElapsedMillis, awaitingPhaseStart)
