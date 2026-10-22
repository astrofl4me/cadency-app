package br.edu.fsa.planner

import android.content.Context
import androidx.room.Room
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.*
import androidx.test.core.app.ApplicationProvider
import br.edu.fsa.planner.data.local.database.*
import br.edu.fsa.planner.data.remote.*
import br.edu.fsa.planner.data.repository.*
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okio.FileSystem
import okio.Path.Companion.toPath
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.io.IOException
import java.time.*
import java.util.UUID

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class QuoteRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val name = "quotes-${UUID.randomUUID()}.db"
    private val prefFile = File(context.filesDir, "$name.preferences_pb")
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val preferences = PreferencesRepository(PreferenceDataStoreFactory.create(scope = scope,
        storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer) { prefFile.absolutePath.toPath() }))
    private var db = open()
    private fun open() = Room.databaseBuilder(context, PlannerDatabase::class.java, name)
        .addMigrations(PlannerDatabase.Migration1To2, Migration2To3, Migration3To4, Migration4To5).build()
    private class TestClock(var millis: Long = Instant.parse("2026-10-22T15:00:00Z").toEpochMilli()) : Clock() {
        override fun getZone(): ZoneId = ZoneOffset.UTC
        override fun withZone(zone: ZoneId): Clock = this
        override fun instant(): Instant = Instant.ofEpochMilli(millis)
    }
    private val clock = TestClock()
    private class FakeRemote : QuoteRemoteDataSource {
        var calls = 0; var fail = false
        override suspend fun fetch(): List<RemoteQuote> { calls++; if (fail) throw IOException("Offline")
            return listOf(RemoteQuote("Respire e escolha um passo.", "Teste", "Paz"), RemoteQuote("A constância abre espaço.", "Teste", "Reflexão")) }
    }
    private val remote = FakeRemote()
    private fun repository() = QuoteRepository(db.quoteDao(), preferences, remote, { QuoteRepository.localCollection(context) }, clock)
    @After fun close() = runBlocking { db.close(); scope.coroutineContext.job.cancelAndJoin(); context.deleteDatabase(name); prefFile.delete(); Unit }

    @Test fun firstUseOfflineKeepsFallbackAndManualRotationWorks() = runBlocking {
        remote.fail = true
        val repo = repository()
        repo.rotate(onOpen = true)
        val first = repo.current.first()!!
        assertEquals(QuoteSource.FALLBACK, first.source)
        repo.refreshRemote()
        assertTrue(repo.hasRemoteError.value)
        assertNotNull(repo.current.first())
        clock.millis += 1800000
        repo.refreshRemote(); assertEquals(1, remote.calls)
        repo.rotate(manual = true)
        assertNotEquals(first.id, repo.current.first()!!.id)
    }
    @Test fun remoteCachePreservesFavoritesAndEveryOpenDoesNotFetchAgain() = runBlocking {
        val repo = repository()
        repo.rotate(); repo.refreshRemote()
        val current = repo.current.first()!!
        assertEquals(QuoteSource.API, current.source)
        repo.favorite(current.id)
        repeat(10) { repo.rotate(onOpen = true); repo.refreshRemote() }
        assertEquals(1, remote.calls)
        clock.millis += 25 * 3600000L
        repo.refreshRemote()
        assertEquals(2, remote.calls)
        assertTrue(repo.quotes.first().first { it.id == current.id }.isFavorite)
        preferences.setQuoteCategory(QuoteCategory.DISCIPLINE)
        repo.rotate()
        assertEquals(QuoteCategory.DISCIPLINE, repo.current.first()!!.category)
        assertEquals("Reflexão", repo.current.first()!!.rawCategory)
    }
    @Test fun manualFrequencyFavoritesAndPersonalQuotesSurviveDatabaseReopen() = runBlocking {
        var repo = repository()
        preferences.setQuoteMode(QuoteRefreshMode.MANUAL)
        preferences.setQuoteCategory(QuoteCategory.CALM)
        repo.rotate()
        val first = repo.current.first()!!.id
        clock.millis += 8 * 86400000L; repo.rotate(onOpen = true)
        assertEquals(first, repo.current.first()!!.id)
        repo.rotate(manual = true); assertNotEquals(first, repo.current.first()!!.id)
        repo.addOwn("Meu pequeno ritmo.", "")
        val own = repo.current.first()!!
        assertEquals(QuoteSource.USER, own.source); assertTrue(own.isFavorite)
        assertEquals(QuoteCategory.CALM, own.category)
        db.close(); db = open(); repo = repository()
        assertEquals(own.id, repo.current.first()!!.id)
        assertTrue(repo.quotes.first().first { it.id == own.id }.isFavorite)
        repo.favorite(own.id)
        assertFalse(repo.quotes.first().first { it.id == own.id }.isFavorite)
        repo.deleteOwn(own.id)
        assertFalse(repo.quotes.first().any { it.id == own.id })
        assertNotNull(repo.current.first())
    }
    @Test fun parserReadsCurrentDocumentedApiShape() {
        val data = JSONObject("""{"frases":[{"frase":"Um passo possível.","autor":"Teste","categoria":"Paz","data":"2026-10-22"}]}""")
        val rows = HttpQuoteRemoteDataSource.parseRows(data)
        assertEquals(1, rows.size); assertEquals("Paz", rows.single().rawCategory)
        assertEquals(rows.single().id, RemoteQuote("Um passo possível.", "Teste", "Paz").id)
    }
}
