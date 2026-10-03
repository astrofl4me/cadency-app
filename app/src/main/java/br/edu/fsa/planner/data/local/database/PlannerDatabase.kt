package br.edu.fsa.planner.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.edu.fsa.planner.data.local.dao.PlannerDao
import br.edu.fsa.planner.data.local.entity.PlannerItemEntity
import br.edu.fsa.planner.data.local.entity.PlannerNoteEntity

@Database(entities = [PlannerItemEntity::class, PlannerNoteEntity::class], version = 1, exportSchema = true)
@TypeConverters(PlannerConverters::class)
abstract class PlannerDatabase : RoomDatabase() { abstract fun plannerDao(): PlannerDao }
