package br.edu.fsa.planner.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import br.edu.fsa.planner.data.local.dao.PlannerDao
import br.edu.fsa.planner.data.local.entity.PlannerItemEntity
import br.edu.fsa.planner.data.local.entity.PlannerNoteEntity
import br.edu.fsa.planner.data.local.entity.WeeklyHabitEntity

@Database(entities = [PlannerItemEntity::class, PlannerNoteEntity::class, WeeklyHabitEntity::class], version = 2, exportSchema = true)
@TypeConverters(PlannerConverters::class)
abstract class PlannerDatabase : RoomDatabase() {
    abstract fun plannerDao(): PlannerDao

    companion object {
        val Migration1To2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE planner_notes ADD COLUMN waterGlasses INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE planner_notes ADD COLUMN gratitude TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE planner_notes ADD COLUMN selfCare TEXT NOT NULL DEFAULT ''")
                db.execSQL("CREATE TABLE IF NOT EXISTS weekly_habits (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, weekStart INTEGER NOT NULL, title TEXT NOT NULL, completedDays INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_weekly_habits_weekStart ON weekly_habits (weekStart)")
            }
        }
    }
}
