package br.edu.fsa.planner.domain.model

import java.time.LocalDate
import java.time.LocalTime

enum class ItemType { TASK, EVENT, PRIORITY, TOMORROW }
enum class Priority { LOW, MEDIUM, HIGH }
enum class Category { PERSONAL, STUDY, WORK, HEALTH }
enum class Mood { DIFFICULT, QUIET, GOOD, GREAT }

data class PlannerItem(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val date: LocalDate,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val type: ItemType = ItemType.TASK,
    val priority: Priority = Priority.MEDIUM,
    val category: Category = Category.PERSONAL,
    val isCompleted: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
)

data class PlannerNote(val key: String, val text: String = "", val highlight: String = "", val mood: Mood? = null)
