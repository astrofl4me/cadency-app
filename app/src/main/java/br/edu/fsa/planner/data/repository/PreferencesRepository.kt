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
    private val rollover = booleanPreferencesKey("gentle_rollover_enabled")
    private val reviewed = stringPreferencesKey("last_rollover_review_date")
    val preferences = store.data.map { values -> PlannerPreferences(ready = true,
        themeMode = values[theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
        plannerMood = values[mood]?.let { runCatching { PlannerMood.valueOf(it) }.getOrNull() } ?: PlannerMood.CLEAN,
        gentleRolloverEnabled = values[rollover] ?: true,
        lastRolloverReviewDate = values[reviewed]?.let { runCatching { LocalDate.parse(it) }.getOrNull() })
    }.catch { error ->
        if (error is IOException) emit(PlannerPreferences(ready = true, hasError = true)) else throw error
    }
    suspend fun setTheme(value: ThemeMode) { store.edit { it[theme] = value.name } }
    suspend fun setMood(value: PlannerMood) { store.edit { it[mood] = value.name } }
    suspend fun setRollover(value: Boolean) { store.edit { it[rollover] = value } }
    suspend fun markRolloverReviewed(date: LocalDate) { store.edit { it[reviewed] = date.toString() } }
}
