package br.edu.fsa.planner.viewmodel

import androidx.lifecycle.*
import br.edu.fsa.planner.data.repository.PlannerRepository
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime

data class NowState(val now: LocalDateTime = LocalDateTime.now(), val candidates: List<PlannerItem> = emptyList(),
    val hasSkipped: Boolean = false, val error: Boolean = false, val loading: Boolean = true)

@OptIn(ExperimentalCoroutinesApi::class)
class NowModeViewModel(private val repository: PlannerRepository, private val saved: SavedStateHandle) : ViewModel() {
    private val clock = flow { while (currentCoroutineContext().isActive) { emit(LocalDateTime.now()); delay(1000) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocalDateTime.now())
    private val items = clock.map { it.toLocalDate() }.distinctUntilChanged().flatMapLatest(repository::observeDay)
    private val skipped = saved.getStateFlow("now_skipped", longArrayOf())
    private val error = MutableStateFlow(false)
    val state = combine(clock, items, skipped, error) { now, rows, ids, failed ->
        NowState(now, NowRules.ordered(rows, now, ids.toSet()), ids.isNotEmpty(), failed, false)
    }.catch { emit(NowState(error = true, loading = false)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NowState())
    fun skip() { state.value.candidates.firstOrNull()?.let { saved["now_skipped"] = skipped.value + it.id } }
    fun clearSkipped() { saved["now_skipped"] = longArrayOf() }
    fun complete() { viewModelScope.launch {
        try { state.value.candidates.firstOrNull()?.let { repository.setCompleted(it, true) }; error.value = false }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { error.value = true }
    } }
}
