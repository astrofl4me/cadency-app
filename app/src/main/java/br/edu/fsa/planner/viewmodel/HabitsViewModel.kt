package br.edu.fsa.planner.viewmodel

import androidx.lifecycle.*
import br.edu.fsa.planner.data.repository.HabitRepository
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate

data class HabitsState(val today: LocalDate = LocalDate.now(), val week: DateInterval = PlannerDates.week(LocalDate.now()),
    val habits: List<Habit> = emptyList(), val checks: List<HabitCheckIn> = emptyList(),
    val loading: Boolean = true, val error: Boolean = false, val archived: Boolean = false)

@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModel(private val repository: HabitRepository, private val saved: SavedStateHandle) : ViewModel() {
    private val day = MutableStateFlow(LocalDate.now())
    private val week = saved.getStateFlow("habit_week", PlannerDates.week(day.value).start.toString())
    private val archived = MutableStateFlow(false)
    private val error = MutableStateFlow(false)
    val state = combine(day, week, archived) { today, value, show -> Triple(today, PlannerDates.week(LocalDate.parse(value)), show) }
        .flatMapLatest { (today, interval, show) ->
            combine(repository.habits, repository.checkIns(DateInterval(minOf(today.minusDays(365), interval.start), maxOf(today, interval.endInclusive))), error) { habits, checks, failed ->
                HabitsState(today, interval, habits.filter { (it.isArchived || (it.endsOn != null && it.endsOn < today)) == show }, checks, false, failed, show)
            }.catch { emit(HabitsState(today, interval, loading = false, error = true)) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HabitsState())
    fun refreshToday() { day.value = LocalDate.now() }
    fun moveWeek(amount: Long) { saved["habit_week"] = LocalDate.parse(week.value).plusWeeks(amount).toString() }
    fun currentWeek() { refreshToday(); saved["habit_week"] = PlannerDates.week(day.value).start.toString() }
    fun showArchived(value: Boolean) { archived.value = value }
    fun toggle(habit: Habit, date: LocalDate) { viewModelScope.launch {
        try { repository.toggle(habit.id, date); error.value = false }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { error.value = true }
    } }
}

data class HabitEditorState(val habit: Habit = Habit(name = ""), val loading: Boolean = true,
    val invalid: Boolean = false, val error: Boolean = false, val busy: Boolean = false, val finished: Boolean = false)

class HabitEditorViewModel(private val repository: HabitRepository, private val saved: SavedStateHandle) : ViewModel() {
    private val id = saved.get<Long>("habitId") ?: 0L
    private val _state = MutableStateFlow(HabitEditorState())
    val state = _state.asStateFlow()
    val history = repository.history(id).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    init { viewModelScope.launch {
        try {
            val initial = if (id == 0L) Habit(name = "") else checkNotNull(repository.get(id))
            val habit = initial.copy(name = saved["habit_name"] ?: initial.name, description = saved["habit_description"] ?: initial.description,
                frequency = saved.get<String>("habit_frequency")?.let(FrequencyType::valueOf) ?: initial.frequency,
                icon = saved.get<String>("habit_icon")?.let(HabitIcon::valueOf) ?: initial.icon,
                category = saved.get<String>("habit_category")?.let(Category::valueOf) ?: initial.category,
                isArchived = saved["habit_archived"] ?: initial.isArchived,
                targetPerPeriod = saved["habit_target"] ?: initial.targetPerPeriod,
                days = saved.get<Int>("habit_days")?.let { mask -> java.time.DayOfWeek.entries.filter { mask and (1 shl (it.value - 1)) != 0 }.toSet() } ?: initial.days)
            _state.value = HabitEditorState(habit, loading = false)
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { _state.value = _state.value.copy(loading = false, error = true) }
    } }
    fun update(habit: Habit) {
        _state.value = _state.value.copy(habit = habit, invalid = false)
        saved["habit_name"] = habit.name; saved["habit_description"] = habit.description
        saved["habit_frequency"] = habit.frequency.name; saved["habit_target"] = habit.targetPerPeriod
        saved["habit_icon"] = habit.icon.name; saved["habit_category"] = habit.category.name; saved["habit_archived"] = habit.isArchived
        saved["habit_days"] = habit.days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }
    }
    fun save() {
        if (!HabitRules.valid(state.value.habit)) { _state.value = state.value.copy(invalid = true); return }
        mutate { repository.save(state.value.habit.let { if (it.isArchived && it.endsOn == null) it.copy(endsOn = LocalDate.now())
            else if (!it.isArchived) it.copy(endsOn = null) else it }) }
    }
    fun delete() = mutate { repository.delete(id) }
    private fun mutate(action: suspend () -> Unit) {
        if (state.value.busy) return
        _state.value = state.value.copy(busy = true, error = false)
        viewModelScope.launch { try { action(); _state.value = state.value.copy(busy = false, finished = true) }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { _state.value = state.value.copy(busy = false, error = true) } }
    }
}
