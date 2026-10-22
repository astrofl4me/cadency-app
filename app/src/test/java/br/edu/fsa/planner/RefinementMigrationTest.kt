package br.edu.fsa.planner

import android.content.Context
import androidx.room.Room
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.*
import androidx.test.core.app.ApplicationProvider
import br.edu.fsa.planner.data.local.database.*
import br.edu.fsa.planner.data.repository.*
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okio.FileSystem
import okio.Path.Companion.toPath
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.time.LocalDate
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RefinementMigrationTest {
    @Test fun databaseV3KeepsTasksStepsHabitsAndPausedFocusWhenUpgradedToV5() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "v3-${UUID.randomUUID()}.db"
        val day = LocalDate.of(2026, 10, 22)
        val schema = javaClass.getResourceAsStream("/br.edu.fsa.planner.data.local.database.PlannerDatabase/3.json")!!
            .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { old ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val row = entities.getJSONObject(i); val table = row.getString("tableName")
                old.execSQL(row.getString("createSql").replace("\${TABLE_NAME}", table))
                row.optJSONArray("indices")?.let { indices -> for (j in 0 until indices.length())
                    old.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table)) }
            }
            val setup = schema.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) old.execSQL(setup.getString(i))
            old.execSQL("INSERT INTO planner_items VALUES (1,'Antiga','',?,NULL,NULL,'TASK','HIGH','STUDY',0,100,200,?,2,0,0,NULL)", arrayOf(day.toEpochDay(), day.toEpochDay()))
            old.execSQL("INSERT INTO habits VALUES (7,'Ler','','BOOK','STUDY','DAILY',1,127,100,?,NULL,0,0,'primary')", arrayOf(day.toEpochDay()))
            old.execSQL("INSERT INTO habit_check_ins VALUES (1,7,?,1,100)", arrayOf(day.toEpochDay()))
            old.execSQL("INSERT INTO subtasks VALUES (5,1,'Primeiro passo',0,0,100)")
            old.execSQL("INSERT INTO focus_sessions VALUES (12,1,'Antiga',?,1000,NULL,1500,60000,NULL,'PAUSED')", arrayOf(day.toEpochDay()))
            old.version = 3
        }
        val db = Room.databaseBuilder(context, PlannerDatabase::class.java, name).addMigrations(Migration3To4, Migration4To5).build()
        try {
            val planner = PlannerRepository(db.plannerDao())
            assertEquals(2, planner.get(1)!!.rolloverCount)
            assertEquals("Primeiro passo", planner.observeSubtasks(1).first().single().title)
            assertEquals(1, HabitRepository(db.habitDao()).history(7).first().size)
            val focus = FocusRepository(db.focusDao()).get(12)!!
            assertEquals(FocusMode.FREE, focus.mode); assertEquals(FocusStatus.PAUSED, focus.status)
            assertEquals(60000, focus.elapsedMillis)
            assertEquals(1440000, FocusRules.remaining(focus, 99999999))
            assertTrue(db.quoteDao().all().isEmpty())
        } finally { db.close(); context.deleteDatabase(name) }
    }

    @Test fun oldPlannerMoodIsReadAndPersonalityChangesPreserveIndependentAccent() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.filesDir, "legacy-${UUID.randomUUID()}.preferences_pb")
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val store = PreferenceDataStoreFactory.create(scope = scope, storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer) { file.absolutePath.toPath() })
        try {
            store.edit { it[stringPreferencesKey("planner_mood")] = "SOFT"; it[stringPreferencesKey("theme_mode")] = "DARK" }
            val prefs = PreferencesRepository(store)
            assertEquals(PlannerPersonality.SOFT, prefs.preferences.first().plannerPersonality)
            assertEquals(ThemeMode.DARK, prefs.preferences.first().themeMode)
            prefs.setAccent(AccentColor(0x8F315A)); prefs.setPersonality(PlannerPersonality.COZY)
            assertEquals(AccentColor(0x8F315A), prefs.preferences.first().accentColor)
            assertEquals(PlannerPersonality.COZY, prefs.preferences.first().plannerPersonality)
        } finally { scope.coroutineContext.job.cancelAndJoin(); file.delete() }
    }
}
