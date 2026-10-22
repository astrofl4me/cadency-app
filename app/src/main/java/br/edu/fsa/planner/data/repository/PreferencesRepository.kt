package br.edu.fsa.planner.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.io.IOException

class PreferencesRepository(private val store: DataStore<Preferences>) {
    private val theme = stringPreferencesKey("theme_mode")
    private val mood = stringPreferencesKey("planner_mood")
    private val personality = stringPreferencesKey("planner_personality")
    private val accent = stringPreferencesKey("accent_color")
    private val focusMode = stringPreferencesKey("default_focus_mode")
    private val focusMinutes = intPreferencesKey("pomodoro_focus_minutes")
    private val shortBreak = intPreferencesKey("pomodoro_short_break_minutes")
    private val cycles = intPreferencesKey("pomodoro_cycles")
    private val longBreak = intPreferencesKey("pomodoro_long_break_minutes")
    private val rollover = booleanPreferencesKey("gentle_rollover_enabled")
    private val reviewed = stringPreferencesKey("last_rollover_review_date")
    val preferences = store.data.map { values -> PlannerPreferences(ready = true,
        themeMode = values[theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
        plannerPersonality = (values[personality] ?: values[mood])?.let { runCatching { PlannerPersonality.valueOf(it) }.getOrNull() } ?: PlannerPersonality.CLEAN,
        accentColor = values[accent]?.let(AccentColor::parse) ?: AccentColor.Default,
        defaultFocusMode = values[focusMode]?.let { runCatching { FocusMode.valueOf(it) }.getOrNull() } ?: FocusMode.FREE,
        pomodoro = PomodoroConfig(values[focusMinutes] ?: 25, values[shortBreak] ?: 5, values[cycles] ?: 4, values[longBreak] ?: 15).let { if (it.valid) it else PomodoroConfig() },
        gentleRolloverEnabled = values[rollover] ?: true,
        lastRolloverReviewDate = values[reviewed]?.let { runCatching { LocalDate.parse(it) }.getOrNull() })
    }.catch { error ->
        if (error is IOException) emit(PlannerPreferences(ready = true, hasError = true)) else throw error
    }
    suspend fun setTheme(value: ThemeMode) { store.edit { it[theme] = value.name } }
    suspend fun setMood(value: PlannerPersonality) = setPersonality(value)
    suspend fun setPersonality(value: PlannerPersonality) { store.edit { it[personality] = value.name } }
    suspend fun setAccent(value: AccentColor) { store.edit { it[accent] = value.hex } }
    suspend fun setFocusMode(value: FocusMode) { store.edit { it[focusMode] = value.name } }
    suspend fun setPomodoro(value: PomodoroConfig) {
        require(value.valid)
        store.edit { it[focusMinutes] = value.focusMinutes; it[shortBreak] = value.shortBreakMinutes; it[cycles] = value.cycles; it[longBreak] = value.longBreakMinutes }
    }
    suspend fun setRollover(value: Boolean) { store.edit { it[rollover] = value } }
    suspend fun markRolloverReviewed(date: LocalDate) { store.edit { it[reviewed] = date.toString() } }
}
