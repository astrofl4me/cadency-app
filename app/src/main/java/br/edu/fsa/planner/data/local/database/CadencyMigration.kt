package br.edu.fsa.planner.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val Migration2To3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE planner_items ADD COLUMN originalDate INTEGER")
        db.execSQL("ALTER TABLE planner_items ADD COLUMN rolloverCount INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE planner_items ADD COLUMN isInbox INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE planner_items ADD COLUMN isDiscarded INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE planner_items ADD COLUMN completedOn INTEGER")
        db.execSQL("UPDATE planner_items SET originalDate = date")
        db.execSQL("CREATE TABLE habits (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, description TEXT NOT NULL, icon TEXT NOT NULL, category TEXT NOT NULL, frequencyType TEXT NOT NULL, targetPerPeriod INTEGER NOT NULL, daysMask INTEGER NOT NULL, createdAt INTEGER NOT NULL, startsOn INTEGER NOT NULL, endsOn INTEGER, isArchived INTEGER NOT NULL, position INTEGER NOT NULL, colorKey TEXT NOT NULL)")
        db.execSQL("CREATE INDEX index_habits_startsOn ON habits(startsOn)")
        db.execSQL("CREATE TABLE habit_check_ins (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, habitId INTEGER NOT NULL, date INTEGER NOT NULL, completed INTEGER NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(habitId) REFERENCES habits(id) ON DELETE CASCADE)")
        db.execSQL("CREATE UNIQUE INDEX index_habit_check_ins_habitId_date ON habit_check_ins(habitId,date)")
        db.execSQL("CREATE INDEX index_habit_check_ins_date ON habit_check_ins(date)")
        db.execSQL("INSERT INTO habits SELECT id,title,'','CHECK','PERSONAL','DAILY',1,127,weekStart*86400000,weekStart,weekStart+6,0,id,'primary' FROM weekly_habits")
        db.execSQL("INSERT INTO habit_check_ins(habitId,date,completed,createdAt) SELECT h.id,h.weekStart+d.n,1,h.weekStart*86400000 FROM weekly_habits h JOIN (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6) d WHERE (h.completedDays & (1 << d.n)) != 0")
        db.execSQL("CREATE TABLE subtasks (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, plannerItemId INTEGER NOT NULL, title TEXT NOT NULL, isCompleted INTEGER NOT NULL, position INTEGER NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(plannerItemId) REFERENCES planner_items(id) ON DELETE CASCADE)")
        db.execSQL("CREATE INDEX index_subtasks_plannerItemId ON subtasks(plannerItemId)")
        db.execSQL("CREATE TABLE focus_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, plannerItemId INTEGER, title TEXT NOT NULL, day INTEGER NOT NULL, startedAt INTEGER NOT NULL, endedAt INTEGER, plannedSeconds INTEGER NOT NULL, elapsedMillis INTEGER NOT NULL, resumedAt INTEGER, status TEXT NOT NULL, FOREIGN KEY(plannerItemId) REFERENCES planner_items(id) ON DELETE SET NULL)")
        db.execSQL("CREATE INDEX index_focus_sessions_plannerItemId ON focus_sessions(plannerItemId)")
        db.execSQL("CREATE INDEX index_focus_sessions_day ON focus_sessions(day)")
        db.execSQL("CREATE INDEX index_focus_sessions_status ON focus_sessions(status)")
    }
}
