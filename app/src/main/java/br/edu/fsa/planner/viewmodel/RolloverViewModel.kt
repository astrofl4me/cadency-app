package br.edu.fsa.planner.viewmodel

import androidx.lifecycle.*
import br.edu.fsa.planner.data.repository.*
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate

data class RolloverState(val today: LocalDate = LocalDate.now(), val items: List<PlannerItem> = emptyList(),
    val inbox: List<PlannerItem> = emptyList(), val showing: Boolean = false, val error: Boolean = false)

@OptIn(ExperimentalCoroutinesApi::class)
class RolloverViewModel(private val planner: PlannerRepository, private val preferences: PreferencesRepository,
                        private val saved: SavedStateHandle) : ViewModel() {
    private val day = MutableStateFlow(LocalDate.now())
    private val showing = saved.getStateFlow("rollover_showing", false)
    private val error = MutableStateFlow(false)
    val state = day.flatMapLatest { today -> combine(planner.observePending(today), planner.observeInbox(), showing, error) { rows, inbox, show, failed ->
        RolloverState(today, rows, inbox, show, failed)
    } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), RolloverState())
    fun maybeOffer() = change {
        val today = LocalDate.now(); day.value = today
        val pending = planner.observePending(today).first()
        if (RolloverRules.shouldOffer(preferences.preferences.first(), today, pending)) {
            preferences.markRolloverReviewed(today)
            saved["rollover_showing"] = true
        }
    }
    fun open() { day.value = LocalDate.now(); saved["rollover_showing"] = true }
    fun dismiss() { saved["rollover_showing"] = false }
    fun schedule(item: PlannerItem, date: LocalDate) = change { planner.reschedule(item.id, date) }
    fun inbox(item: PlannerItem) = change { planner.toInbox(item.id) }
    fun discard(item: PlannerItem) = change { planner.discard(item.id) }
    fun allToday() = change { state.value.items.forEach { planner.reschedule(it.id, LocalDate.now()) }; dismiss() }
    private fun change(action: suspend () -> Unit) { viewModelScope.launch {
        try { action(); error.value = false } catch (e: CancellationException) { throw e }
        catch (_: Exception) { error.value = true }
    } }
}
