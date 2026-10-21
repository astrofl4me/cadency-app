package br.edu.fsa.planner.data.local.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import br.edu.fsa.planner.domain.model.Mood
import br.edu.fsa.planner.domain.model.PlannerNote

@Entity(tableName = "planner_notes")
data class PlannerNoteEntity(
    @PrimaryKey val key: String, val text: String, val highlight: String, val mood: Mood?,
    @ColumnInfo(defaultValue = "0") val waterGlasses: Int = 0,
    @ColumnInfo(defaultValue = "''") val gratitude: String = "",
    @ColumnInfo(defaultValue = "''") val selfCare: String = "",
)

fun PlannerNoteEntity.toModel() = PlannerNote(key, text, highlight, mood, waterGlasses, gratitude, selfCare)
fun PlannerNote.toEntity() = PlannerNoteEntity(key, text, highlight, mood, waterGlasses, gratitude, selfCare)
