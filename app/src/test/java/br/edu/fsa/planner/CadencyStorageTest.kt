package br.edu.fsa.planner

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.edu.fsa.planner.data.local.database.*
import br.edu.fsa.planner.data.local.entity.FocusSessionEntity
import br.edu.fsa.planner.data.repository.*
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class CadencyStorageTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "cadency-${UUID.randomUUID()}.db"
    private var db = open()
    private fun open() = Room.databaseBuilder(context, PlannerDatabase::class.java, name)
        .addMigrations(PlannerDatabase.Migration1To2, Migration2To3).build()
    @After fun close() { db.close(); context.deleteDatabase(name) }
    @Test fun rolloverKeepsIdentityOriginAndSeparatesInboxFromAgenda() = runBlocking {
        val planner = PlannerRepository(db.plannerDao())
        val today = LocalDate.of(2026, 10, 21)
        val original = today.minusDays(2)
        val id = planner.save(PlannerItem(title = "Reorganizar", date = original))
        planner.save(PlannerItem(title = "Feito", date = original, isCompleted = true))
        planner.save(PlannerItem(title = "Evento", date = original, type = ItemType.EVENT))
        assertEquals(listOf(id), planner.observePending(today).first().map { it.id })
        planner.reschedule(id, original.plusDays(1))
        planner.reschedule(id, today)
        assertEquals(original, planner.get(id)!!.originalDate)
        assertEquals(2, planner.get(id)!!.rolloverCount)
        planner.toInbox(id)
        assertTrue(planner.observeDay(today).first().isEmpty())
        assertEquals(id, planner.observeInbox().first().single().id)
        planner.reschedule(id, today)
        assertEquals(id, planner.observeDay(today).first().single().id)
        assertTrue(planner.observeInbox().first().isEmpty())
        planner.discard(id)
        assertTrue(planner.observeDay(today).first().isEmpty())
        assertTrue(planner.get(id)!!.isDiscarded)
    }

    @Test fun deletingTaskCascadesStepsButRetainsFocusHistory() = runBlocking {
        val planner = PlannerRepository(db.plannerDao())
        val date = LocalDate.of(2026, 10, 21)
        val id = planner.save(PlannerItem(title = "Trabalho", date = date))
        planner.addSubtask(id, "Pesquisar")
        planner.addSubtask(id, "Escrever")
        val steps = planner.observeSubtasks(id).first()
        planner.moveSubtask(steps.last(), -1)
        assertEquals("Escrever", planner.observeSubtasks(id).first().first().title)
        planner.toggleSubtask(steps.first())
        assertEquals(1, planner.observeSubtaskProgress(date).first().single().done)
        val focus = db.focusDao().insert(FocusSessionEntity(plannerItemId = id, title = "Trabalho", day = date,
            startedAt = 1000, endedAt = 121000, plannedSeconds = 120, elapsedMillis = 120000, resumedAt = null, status = "FINISHED"))
        planner.delete(id)
        assertTrue(planner.observeSubtasks(id).first().isEmpty())
        assertNull(db.focusDao().get(focus)!!.plannerItemId)
        assertEquals(2, db.focusDao().observeMinutes(date, date).first().single().value.toInt())
    }

    @Test fun migrationFromV2KeepsWeeklyHabitNamesAndEveryCheckIn() = runBlocking {
        db.close()
        val schema = javaClass.getResourceAsStream("/br.edu.fsa.planner.data.local.database.PlannerDatabase/2.json")!!
            .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        val monday = LocalDate.of(2026, 10, 19)
        context.openOrCreateDatabase(name, Context.MODE_PRIVATE, null).use { old ->
            val entities = schema.getJSONArray("entities")
            for (index in 0 until entities.length()) {
                val entity = entities.getJSONObject(index)
                val table = entity.getString("tableName")
                old.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                entity.optJSONArray("indices")?.let { indices -> for (i in 0 until indices.length()) {
                    old.execSQL(indices.getJSONObject(i).getString("createSql").replace("\${TABLE_NAME}", table))
                } }
            }
            val setup = schema.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) old.execSQL(setup.getString(i))
            old.execSQL("INSERT INTO weekly_habits VALUES (5, ?, 'Ler', 5)", arrayOf(monday.toEpochDay()))
            old.version = 2
        }
        db = open()
        val habits = HabitRepository(db.habitDao())
        val migrated = habits.get(5)!!
        assertEquals("Ler", migrated.name)
        assertEquals(monday.plusDays(6), migrated.endsOn)
        assertEquals(listOf(monday.plusDays(2), monday), habits.history(5).first().map { it.date })
        habits.toggle(5, monday)
        assertEquals(1, habits.history(5).first().size)
        db.close(); db = open()
        assertEquals(1, HabitRepository(db.habitDao()).history(5).first().size)
    }
}
