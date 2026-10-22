package br.edu.fsa.planner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.fsa.planner.data.repository.PreferencesRepository
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: PreferencesRepository) : ViewModel() {
    val preferences = repository.preferences.stateIn(viewModelScope, SharingStarted.Eagerly, PlannerPreferences())
    private val _writeError = MutableStateFlow(false)
    val writeError = _writeError.asStateFlow()
    fun setTheme(value: ThemeMode) = change { repository.setTheme(value) }
    fun setMood(value: PlannerPersonality) = change { repository.setMood(value) }
    fun setPersonality(value: PlannerPersonality) = change { repository.setPersonality(value) }
    fun setAccent(value: AccentColor) = change { repository.setAccent(value) }
    fun setFocusMode(value: FocusMode) = change { repository.setFocusMode(value) }
    fun setPomodoro(value: PomodoroConfig) = change { repository.setPomodoro(value) }
    fun setQuoteCategory(value: QuoteCategory) = change { repository.setQuoteCategory(value) }
    fun setQuoteMode(value: QuoteRefreshMode) = change { repository.setQuoteMode(value) }
    fun setRollover(value: Boolean) = change { repository.setRollover(value) }
    private fun change(action: suspend () -> Unit) { viewModelScope.launch {
        try { action(); _writeError.value = false }
        catch (error: CancellationException) { throw error }
        catch (_: Exception) { _writeError.value = true }
    } }
}
