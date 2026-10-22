package br.edu.fsa.planner.data.repository

import android.content.Context
import br.edu.fsa.planner.data.local.dao.QuoteDao
import br.edu.fsa.planner.data.local.entity.*
import br.edu.fsa.planner.data.remote.*
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import java.time.Clock
import java.util.UUID

class QuoteRepository(private val dao: QuoteDao, private val preferences: PreferencesRepository,
    private val remote: QuoteRemoteDataSource, private val fallback: () -> List<CadencyQuote>, private val clock: Clock = Clock.systemDefaultZone()) {
    private val mutex = Mutex()
    private val networkMutex = Mutex()
    private val local by lazy(fallback)
    private val remoteError = MutableStateFlow(false)
    val hasRemoteError = remoteError.asStateFlow()
    val quotes = dao.observe().map { it.map(QuoteEntity::toModel) }
    val current = combine(quotes, preferences.preferences) { quotes, prefs ->
        quotes.firstOrNull { it.id == prefs.quote.currentId && QuoteRules.eligible(it, prefs.quote.category) }
            ?: QuoteRules.choose(quotes, prefs.quote.category, null)
    }
    suspend fun rotate(onOpen: Boolean = false, manual: Boolean = false) = mutex.withLock {
        dao.seed(withContext(Dispatchers.IO) { local.map(CadencyQuote::toEntity) })
        val prefs = preferences.preferences.first().quote
        val rows = dao.all().map(QuoteEntity::toModel)
        val current = rows.firstOrNull { it.id == prefs.currentId }
        if (manual || current == null || !QuoteRules.eligible(current, prefs.category) || QuoteRules.due(prefs, clock.millis(), onOpen, clock.zone)) {
            QuoteRules.choose(rows, prefs.category, prefs.currentId)?.let { preferences.selectQuote(it.id, clock.millis()) }
        }
    }
    suspend fun refreshRemote() = networkMutex.withLock {
        val now = clock.millis()
        val prefs = preferences.preferences.first().quote
        if (prefs.lastRemoteSuccessAt > 0 && now - prefs.lastRemoteSuccessAt < 24 * 3600000L) return@withLock
        if (prefs.lastRemoteAttemptAt > 0 && now - prefs.lastRemoteAttemptAt < 3600000L) return@withLock
        preferences.quoteRemoteAttempt(now)
        try {
            val rows = remote.fetch().map { row -> CadencyQuote(row.id, row.text, row.author, QuoteRules.classify(row.rawCategory, row.text), row.rawCategory, now, source = QuoteSource.API).toEntity() }
            check(rows.isNotEmpty())
            mutex.withLock {
                dao.merge(rows)
                preferences.quoteRemoteSuccess(now)
                val latest = preferences.preferences.first().quote
                dao.trimCache(latest.currentId.orEmpty())
                val old = latest.currentId?.let { dao.get(it) }
                if (latest.refreshMode != QuoteRefreshMode.MANUAL && old?.source == QuoteSource.FALLBACK.name) {
                    QuoteRules.choose(dao.all().map(QuoteEntity::toModel), latest.category, latest.currentId)?.let { preferences.selectQuote(it.id, now) }
                }
            }
            remoteError.value = false
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { remoteError.value = true }
    }
    suspend fun favorite(id: String) = dao.toggleFavorite(id)
    suspend fun select(id: String) { checkNotNull(dao.get(id)); preferences.selectQuote(id, clock.millis()) }
    suspend fun addOwn(text: String, author: String) = mutex.withLock {
        require(text.trim().length in 1..1000 && author.trim().length <= 120)
        val category = preferences.preferences.first().quote.category
        val quote = CadencyQuote("user-${UUID.randomUUID()}", text.trim(), author.trim(), category, "", clock.millis(), true, QuoteSource.USER)
        dao.put(quote.toEntity()); preferences.selectQuote(quote.id, clock.millis())
    }
    suspend fun deleteOwn(id: String) { dao.deleteOwn(id); rotate() }
    companion object {
        fun localCollection(context: Context): List<CadencyQuote> = context.assets.open("quotes_pt_br.json").bufferedReader().use { reader ->
            val data = JSONArray(reader.readText())
            (0 until data.length()).map { index -> data.getJSONObject(index).let { row ->
                CadencyQuote("fallback-${row.getString("id")}", row.getString("text"), "Cadency", QuoteCategory.valueOf(row.getString("category")), "", 0, source = QuoteSource.FALLBACK)
            } }
        }
    }
}
