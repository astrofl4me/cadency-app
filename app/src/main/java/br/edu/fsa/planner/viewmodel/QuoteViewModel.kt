package br.edu.fsa.planner.viewmodel

import androidx.lifecycle.*
import br.edu.fsa.planner.data.repository.*
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class QuoteState(val current: CadencyQuote? = null, val quotes: List<CadencyQuote> = emptyList(),
    val preferences: QuotePreferences = QuotePreferences(), val loading: Boolean = true, val error: Boolean = false, val offline: Boolean = false)

class QuoteViewModel(private val repository: QuoteRepository, preferences: PreferencesRepository) : ViewModel() {
    private val error = MutableStateFlow(false)
    private var lastOpen = 0L
    val state = combine(repository.current, repository.quotes, preferences.preferences, error, repository.hasRemoteError) { quote, rows, prefs, failed, offline ->
        QuoteState(quote, rows, prefs.quote, false, failed, offline)
    }.catch { emit(QuoteState(loading = false, error = true)) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuoteState())
    init {
        change { repository.rotate() }
        viewModelScope.launch { preferences.preferences.map { it.quote.category }.distinctUntilChanged().drop(1).collect { change { repository.rotate() } } }
    }
    fun onOpen() {
        val now = System.currentTimeMillis()
        if (now - lastOpen < 2000) return
        lastOpen = now
        change { repository.rotate(onOpen = true) }
        viewModelScope.launch { repository.refreshRemote() }
    }
    fun checkDue() = change { repository.rotate() }
    fun next() = change { repository.rotate(manual = true) }
    fun favorite(id: String) = change { repository.favorite(id) }
    fun deleteOwn(id: String) = change { repository.deleteOwn(id) }
    suspend fun addOwn(text: String, author: String): Boolean = try { repository.addOwn(text, author); error.value = false; true }
        catch (e: CancellationException) { throw e } catch (_: Exception) { error.value = true; false }
    private fun change(action: suspend () -> Unit) { viewModelScope.launch {
        try { action(); error.value = false } catch (e: CancellationException) { throw e }
        catch (_: Exception) { error.value = true }
    } }
}
