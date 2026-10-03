package br.edu.fsa.planner.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.edu.fsa.planner.domain.model.Mood
import br.edu.fsa.planner.domain.model.PlannerNote

@Entity(tableName = "planner_notes")
data class PlannerNoteEntity(@PrimaryKey val key: String, val text: String, val highlight: String, val mood: Mood?)

fun PlannerNoteEntity.toModel() = PlannerNote(key, text, highlight, mood)
fun PlannerNote.toEntity() = PlannerNoteEntity(key, text, highlight, mood)
