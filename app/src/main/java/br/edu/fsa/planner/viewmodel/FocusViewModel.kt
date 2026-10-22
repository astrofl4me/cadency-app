package br.edu.fsa.planner.viewmodel

import androidx.lifecycle.*
import br.edu.fsa.planner.data.repository.*
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class FocusState(val session: FocusSession? = null, val title: String = "", val now: Long = System.currentTimeMillis(),
    val minutes: Int = 25, val custom: Boolean = false, val customText: String = "30", val invalid: Boolean = false,
    val loading: Boolean = true, val busy: Boolean = false, val error: Boolean = false, val taskCompleted: Boolean = false,
    val mode: FocusMode = FocusMode.FREE, val pomodoro: PomodoroConfig = PomodoroConfig(), val offerHabit: String? = null)

@OptIn(ExperimentalCoroutinesApi::class)
class FocusViewModel(private val repository: FocusRepository, private val planner: PlannerRepository,
                     private val saved: SavedStateHandle, private val preferences: PreferencesRepository,
                     private val habits: HabitRepository) : ViewModel() {
    private val itemId = saved.get<Long>("itemId")?.takeIf { it > 0 }
    private val habitId = saved.get<Long>("habitId")?.takeIf { it > 0 }
    private val sessionId = saved.getStateFlow("focus_session_id", 0L)
    private val form = MutableStateFlow(FocusState(minutes = saved["focus_minutes"] ?: 25,
        custom = saved["focus_custom"] ?: false, customText = saved["focus_custom_text"] ?: "30"))
    private val clock = flow { while (currentCoroutineContext().isActive) { emit(System.currentTimeMillis()); delay(1000) } }
    private val session = sessionId.flatMapLatest { if (it == 0L) flowOf(null) else repository.observe(it) }
    val state = combine(session, form, clock) { current, ui, now -> ui.copy(session = current, now = now) }
        .catch { emit(form.value.copy(loading = false, error = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FocusState())
    init {
        change {
            val active = if (sessionId.value != 0L) repository.get(sessionId.value) else repository.active.first()
            if (active != null) saved["focus_session_id"] = active.id
            val title = itemId?.let { planner.get(it)?.title } ?: habitId?.let { habits.get(it)?.name }.orEmpty()
            val prefs = preferences.preferences.first()
            form.update { it.copy(title = title, loading = false, mode = prefs.defaultFocusMode, pomodoro = prefs.pomodoro) }
        }
        viewModelScope.launch {
            session.collectLatest { current ->
                if (current?.status == FocusStatus.RUNNING) {
                    delay(FocusRules.remaining(current, System.currentTimeMillis()))
                    try { repository.advance(current.id) }
                    catch (e: CancellationException) { throw e }
                    catch (_: Exception) { form.update { it.copy(error = true) } }
                }
                if (current?.endedAt != null && current.habitId != null && current.elapsedMillis > 0 && saved.get<Long>("habit_prompt_seen") != current.id) {
                    val habit = habits.get(current.habitId)
                    val today = java.time.LocalDate.now()
                    if (habit != null && HabitRules.isPlanned(habit, today) && habits.history(habit.id).first().none { it.date == today }) {
                        form.update { it.copy(offerHabit = habit.name) }
                    }
                }
            }
        }
    }
    fun choose(minutes: Int) { saved["focus_minutes"] = minutes; saved["focus_custom"] = false; form.update { it.copy(minutes = minutes, custom = false, invalid = false) } }
    fun custom() { saved["focus_custom"] = true; form.update { it.copy(custom = true) } }
    fun customText(value: String) { saved["focus_custom_text"] = value.take(3); form.update { it.copy(customText = value.take(3), invalid = false) } }
    fun mode(value: FocusMode) { form.update { it.copy(mode = value) }; change { preferences.setFocusMode(value) } }
    fun pomodoro(value: PomodoroConfig) { form.update { it.copy(pomodoro = value) }; change { preferences.setPomodoro(value) } }
    fun start() {
        val minutes = if (form.value.mode == FocusMode.POMODORO) form.value.pomodoro.focusMinutes
            else if (form.value.custom) form.value.customText.toIntOrNull() else form.value.minutes
        if (minutes == null || minutes !in 1..180) { form.update { it.copy(invalid = true) }; return }
        change { saved["focus_session_id"] = repository.start(itemId, form.value.title, minutes, habitId, form.value.mode, form.value.pomodoro) }
    }
    fun pause() = change { repository.pause(sessionId.value) }
    fun resume() = change { repository.resume(sessionId.value) }
    fun finish() = change { repository.finish(sessionId.value) }
    fun restart() = change {
        val previous = checkNotNull(state.value.session)
        saved["focus_session_id"] = repository.start(previous.plannerItemId, previous.title, previous.plannedSeconds / 60,
            previous.habitId, previous.mode, previous.pomodoro)
    }
    fun dismissHabit() { state.value.session?.let { saved["habit_prompt_seen"] = it.id }; form.update { it.copy(offerHabit = null) } }
    fun completeHabit() = change {
        state.value.session?.habitId?.let { habits.markDone(it, java.time.LocalDate.now()) }
        dismissHabit()
    }
    fun completeTask() = change {
        val current = checkNotNull(state.value.session)
        repository.finish(current.id)
        current.plannerItemId?.let { planner.get(it)?.let { item -> planner.setCompleted(item, true) } }
        form.update { it.copy(taskCompleted = true) }
    }
    private fun change(action: suspend () -> Unit) {
        if (form.value.busy) return
        form.update { it.copy(busy = true, error = false) }
        viewModelScope.launch { try { action(); form.update { it.copy(busy = false) } }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { form.update { it.copy(busy = false, loading = false, error = true) } } }
    }
}
