package br.edu.fsa.planner.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration3To4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE focus_sessions ADD COLUMN habitId INTEGER REFERENCES habits(id) ON DELETE SET NULL")
        db.execSQL("CREATE INDEX index_focus_sessions_habitId ON focus_sessions(habitId)")
        db.execSQL("ALTER TABLE focus_sessions ADD COLUMN mode TEXT NOT NULL DEFAULT 'FREE'")
        db.execSQL("ALTER TABLE focus_sessions ADD COLUMN phase TEXT NOT NULL DEFAULT 'FOCUS'")
        db.execSQL("ALTER TABLE focus_sessions ADD COLUMN cycleIndex INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE focus_sessions ADD COLUMN workMinutes INTEGER NOT NULL DEFAULT 25")
        db.execSQL("ALTER TABLE focus_sessions ADD COLUMN shortBreakMinutes INTEGER NOT NULL DEFAULT 5")
        db.execSQL("ALTER TABLE focus_sessions ADD COLUMN cycles INTEGER NOT NULL DEFAULT 4")
        db.execSQL("ALTER TABLE focus_sessions ADD COLUMN longBreakMinutes INTEGER NOT NULL DEFAULT 15")
        db.execSQL("ALTER TABLE focus_sessions ADD COLUMN phaseElapsedMillis INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE focus_sessions ADD COLUMN awaitingPhaseStart INTEGER NOT NULL DEFAULT 0")
    }
}
