package br.edu.fsa.planner

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.edu.fsa.planner.data.local.database.PlannerDatabase
import br.edu.fsa.planner.data.repository.PlannerRepository
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneOffset
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class PlannerPersistenceTest {
    private lateinit var context: Context
    private lateinit var database: PlannerDatabase
    private lateinit var repository: PlannerRepository
    private val name = "test-${UUID.randomUUID()}.db"
    private val date = LocalDate.of(2026, 10, 3)
    private val clock = Clock.fixed(Instant.ofEpochMilli(1000), ZoneOffset.UTC)

    @Before fun open() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.databaseBuilder(context, PlannerDatabase::class.java, name).build()
        repository = PlannerRepository(database.plannerDao(), clock)
    }
    @After fun close() { database.close(); context.deleteDatabase(name) }

    @Test fun itemSurvivesDatabaseCloseAndReopenWithTypedFields() = runBlocking {
        val id = repository.save(PlannerItem(title = "Prova", description = "Revisar", date = date,
            startTime = LocalTime.of(9, 0), endTime = LocalTime.of(11, 0), type = ItemType.EVENT,
            priority = Priority.HIGH, category = Category.STUDY))
        database.close()
        database = Room.databaseBuilder(context, PlannerDatabase::class.java, name).build()
        repository = PlannerRepository(database.plannerDao(), clock)
        val item = repository.get(id)!!
        assertEquals("Prova", item.title)
        assertEquals(LocalTime.of(9, 0), item.startTime)
        assertEquals(ItemType.EVENT, item.type)
        assertEquals(Priority.HIGH, item.priority)
        assertEquals(Category.STUDY, item.category)
        assertEquals(1000L, item.createdAt)
        assertEquals(listOf(id), repository.observeDay(date).first().map { it.id })
    }

    @Test fun editCompletionAndDeletionEmitThroughSameFlow() = runBlocking {
        val updates = Channel<List<PlannerItem>>(Channel.UNLIMITED)
        val collector = launch(Dispatchers.IO) { repository.observeDay(date).collect { updates.send(it) } }
        suspend fun next() = withTimeout(10_000) { updates.receive() }
        try {
            assertTrue(next().isEmpty())
            val id = repository.save(PlannerItem(title = "Ler", date = date))
            assertEquals("Ler", next().single().title)
            val item = repository.get(id)!!
            repository.save(item.copy(title = "Ler capítulo 2"))
            assertEquals("Ler capítulo 2", next().single().title)
            repository.setCompleted(item, true)
            assertTrue(next().single().isCompleted)
            repository.setCompleted(item.copy(isCompleted = true), false)
            assertFalse(next().single().isCompleted)
            repository.delete(id)
            assertTrue(next().isEmpty())
        } finally { collector.cancelAndJoin(); updates.close() }
    }

    @Test fun roomDateMonthAndCompletionQueriesRespectBoundaries() = runBlocking {
        val first = LocalDate.of(2026, 10, 1)
        val last = LocalDate.of(2026, 10, 31)
        repository.save(PlannerItem(title = "Antes", date = first.minusDays(1)))
        val startId = repository.save(PlannerItem(title = "Início", date = first))
        val endId = repository.save(PlannerItem(title = "Fim", date = last, isCompleted = true))
        repository.save(PlannerItem(title = "Depois", date = last.plusDays(1)))
        assertEquals(listOf(startId, endId), repository.observeMonth(YearMonth.of(2026, 10)).first().map { it.id })
        assertEquals(listOf(endId), repository.observeCompletion(DateInterval(first, last), true).first().map { it.id })
        assertEquals(listOf(startId), repository.observeCompletion(DateInterval(first, last), false).first().map { it.id })
    }

    @Test fun tomorrowItemBelongsToItsActualScheduledDate() = runBlocking {
        val id = repository.save(PlannerItem(title = "Amanhã", date = date.plusDays(1), type = ItemType.TOMORROW))
        assertTrue(repository.observeDay(date).first().isEmpty())
        assertEquals(id, repository.observeDay(date.plusDays(1)).first().single().id)
        assertEquals(id, repository.observeRange(PlannerDates.week(date)).first().single().id)
    }

    @Test fun noteAndMoodUpdatesDoNotOverwriteEachOther() = runBlocking {
        val key = "day:$date"
        coroutineScope {
            launch { repository.updateNote(key) { it.copy(text = "Uma boa ideia") } }
            launch { repository.updateNote(key) { it.copy(mood = Mood.GOOD) } }
        }
        database.close()
        database = Room.databaseBuilder(context, PlannerDatabase::class.java, name).build()
        repository = PlannerRepository(database.plannerDao(), clock)
        val note = repository.observeNote(key).first()
        assertEquals("Uma boa ideia", note.text)
        assertEquals(Mood.GOOD, note.mood)
    }
}
