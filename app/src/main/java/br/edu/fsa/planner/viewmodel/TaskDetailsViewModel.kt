package br.edu.fsa.planner.viewmodel

import androidx.lifecycle.*
import br.edu.fsa.planner.data.repository.PlannerRepository
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class TaskDetailsState(val item: PlannerItem? = null, val steps: List<Subtask> = emptyList(),
    val loading: Boolean = true, val error: Boolean = false, val offerCompletion: Boolean = false,
    val invalidStep: Boolean = false)

class TaskDetailsViewModel(private val repository: PlannerRepository, private val saved: SavedStateHandle) : ViewModel() {
    private val id = checkNotNull(saved.get<Long>("itemId"))
    private val prompt = MutableStateFlow(false)
    private val failure = MutableStateFlow(false)
    private val invalid = MutableStateFlow(false)
    val draft = saved.getStateFlow("subtask_draft", "")
    val state = combine(repository.observeItem(id), repository.observeSubtasks(id), prompt, failure, invalid) { item, steps, ask, failed, invalid ->
        TaskDetailsState(item, steps, false, failed, ask, invalid)
    }.catch { emit(TaskDetailsState(loading = false, error = true)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskDetailsState())
    fun updateDraft(text: String) { saved["subtask_draft"] = text.take(120); invalid.value = false }
    fun addStep() {
        if (draft.value.isBlank()) { invalid.value = true; return }
        change { repository.addSubtask(id, draft.value); saved["subtask_draft"] = "" }
    }
    fun toggleStep(step: Subtask) = change {
        repository.toggleSubtask(step)
        val steps = repository.observeSubtasks(id).first()
        if (!step.isCompleted && steps.isNotEmpty() && steps.all { it.isCompleted } && state.value.item?.isCompleted == false) prompt.value = true
    }
    fun removeStep(step: Subtask) = change { repository.removeSubtask(step.id) }
    fun move(step: Subtask, direction: Int) = change { repository.moveSubtask(step, direction) }
    fun dismissCompletion() { prompt.value = false }
    fun toggleParent() = change { state.value.item?.let { repository.setCompleted(it, !it.isCompleted) }; prompt.value = false }
    private fun change(action: suspend () -> Unit) { viewModelScope.launch {
        try { action(); failure.value = false } catch (e: CancellationException) { throw e }
        catch (_: Exception) { failure.value = true }
    } }
}
