package br.edu.fsa.planner.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import br.edu.fsa.planner.domain.model.*
import java.time.LocalDate
import java.time.LocalTime

@Entity(tableName = "planner_items", indices = [Index("date"), Index(value = ["date", "isCompleted"])])
data class PlannerItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val date: LocalDate,
    val startTime: LocalTime?,
    val endTime: LocalTime?,
    val type: ItemType,
    val priority: Priority,
    val category: Category,
    val isCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val originalDate: LocalDate? = null,
    @ColumnInfo(defaultValue = "0") val rolloverCount: Int = 0,
    @ColumnInfo(defaultValue = "0") val isInbox: Boolean = false,
    @ColumnInfo(defaultValue = "0") val isDiscarded: Boolean = false,
    val completedOn: LocalDate? = null,
)

fun PlannerItemEntity.toModel() = PlannerItem(id, title, description, date, startTime, endTime, type,
    priority, category, isCompleted, createdAt, updatedAt, originalDate, rolloverCount, isInbox, isDiscarded, completedOn)

fun PlannerItem.toEntity() = PlannerItemEntity(id, title, description, date, startTime, endTime, type,
    priority, category, isCompleted, createdAt, updatedAt, originalDate, rolloverCount, isInbox, isDiscarded, completedOn)
