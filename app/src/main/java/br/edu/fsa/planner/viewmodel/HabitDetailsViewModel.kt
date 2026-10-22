package br.edu.fsa.planner.viewmodel

import androidx.lifecycle.*
import br.edu.fsa.planner.data.repository.HabitRepository
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class HabitDetailsState(val habit: Habit? = null, val map: List<HeatmapDay> = emptyList(), val checks: List<HabitCheckIn> = emptyList(),
    val weeks: Int = 13, val availableWeeks: List<Int> = listOf(13), val consistency: Int = 0, val streak: Int = 0, val loading: Boolean = true, val error: Boolean = false)

class HabitDetailsViewModel(repository: HabitRepository, saved: SavedStateHandle) : ViewModel() {
    private val id = checkNotNull(saved.get<Long>("habitId"))
    private val weeks = saved.getStateFlow("habit_map_weeks", 13)
    private val handle = saved
    private val day = flow { while (currentCoroutineContext().isActive) { emit(LocalDate.now()); delay(60000) } }
    val state = combine(repository.habits, repository.history(id), weeks, day) { habits, checks, count, today ->
        val habit = habits.firstOrNull { it.id == id }
        if (habit == null) HabitDetailsState(loading = false) else {
            val interval = HeatmapRules.interval(today, count)
            val available = listOf(13, 26, 52).filter { it == 13 || ChronoUnit.WEEKS.between(habit.startsOn, today) >= it }
            HabitDetailsState(habit, HeatmapRules.days(listOf(habit), checks, interval, today), checks, count, available,
                HabitRules.consistency(habit, checks, DateInterval(interval.start, today)), HabitRules.streak(habit, checks, today), false)
        }
    }.catch { emit(HabitDetailsState(loading = false, error = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitDetailsState())
    fun selectWeeks(value: Int) { if (value in listOf(13, 26, 52)) handle["habit_map_weeks"] = value }
}
