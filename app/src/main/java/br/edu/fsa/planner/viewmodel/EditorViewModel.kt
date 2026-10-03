package br.edu.fsa.planner.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.fsa.planner.data.repository.PlannerRepository
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class EditorResult { SAVED, DELETED }
data class EditorUiState(
    val form: PlannerForm = PlannerForm(), val loading: Boolean = false, val busy: Boolean = false,
    val errors: FormErrors = FormErrors(), val storageError: Boolean = false,
    val missingItem: Boolean = false, val result: EditorResult? = null, val isEditing: Boolean = false,
)

class EditorViewModel(private val repository: PlannerRepository, private val savedState: SavedStateHandle) : ViewModel() {
    private val itemId: Long = savedState.get<Long>("itemId") ?: 0L
    private var original: PlannerItem? = null
    private fun initialForm() = PlannerForm(
        date = savedState.get<String>("date") ?: java.time.LocalDate.now().toString(),
        type = savedState.get<String>("type")?.let { runCatching { ItemType.valueOf(it) }.getOrNull() } ?: ItemType.TASK,
    )
    private val _state = MutableStateFlow(EditorUiState(form = initialForm(), loading = itemId != 0L, isEditing = itemId != 0L))
    val state = _state.asStateFlow()

    init { if (itemId != 0L) load() else restoreDraft() }

    private fun restoreDraft() {
        val draft = _state.value.form.copy(
            title = savedState["draft_title"] ?: _state.value.form.title,
            description = savedState["draft_description"] ?: _state.value.form.description,
            date = savedState["draft_date"] ?: _state.value.form.date,
            startTime = savedState["draft_start"] ?: _state.value.form.startTime,
            endTime = savedState["draft_end"] ?: _state.value.form.endTime,
            type = savedState.get<String>("draft_type")?.let(ItemType::valueOf) ?: _state.value.form.type,
            priority = savedState.get<String>("draft_priority")?.let(Priority::valueOf) ?: _state.value.form.priority,
            category = savedState.get<String>("draft_category")?.let(Category::valueOf) ?: _state.value.form.category,
            isCompleted = savedState["draft_completed"] ?: _state.value.form.isCompleted,
        )
        _state.value = _state.value.copy(form = draft)
    }

    fun load() { viewModelScope.launch {
        _state.value = _state.value.copy(loading = true, storageError = false)
        try {
            original = repository.get(itemId)
            _state.value = _state.value.copy(loading = false, missingItem = original == null,
                form = original?.toForm() ?: initialForm())
            restoreDraft()
        } catch (error: CancellationException) { throw error
        } catch (_: Exception) { _state.value = _state.value.copy(loading = false, storageError = true) }
    } }

    fun update(transform: (PlannerForm) -> PlannerForm) {
        if (_state.value.busy || _state.value.loading) return
        val form = transform(_state.value.form)
        _state.value = _state.value.copy(form = form, errors = FormErrors(), storageError = false)
        savedState["draft_title"] = form.title
        savedState["draft_description"] = form.description
        savedState["draft_date"] = form.date
        savedState["draft_start"] = form.startTime
        savedState["draft_end"] = form.endTime
        savedState["draft_type"] = form.type.name
        savedState["draft_priority"] = form.priority.name
        savedState["draft_category"] = form.category.name
        savedState["draft_completed"] = form.isCompleted
    }

    fun save() {
        if (_state.value.busy || _state.value.loading || _state.value.missingItem) return
        val errors = validatePlannerForm(_state.value.form)
        _state.value = _state.value.copy(errors = errors)
        if (!errors.isValid) return
        mutate(EditorResult.SAVED) { repository.save(_state.value.form.toItem(original)) }
    }

    fun delete() { if (original != null && !_state.value.busy) mutate(EditorResult.DELETED) { repository.delete(itemId) } }

    private fun mutate(result: EditorResult, operation: suspend () -> Unit) {
        _state.value = _state.value.copy(busy = true, storageError = false)
        viewModelScope.launch {
            try { operation(); _state.value = _state.value.copy(busy = false, result = result)
            } catch (error: CancellationException) { throw error
            } catch (_: Exception) { _state.value = _state.value.copy(busy = false, storageError = true) }
        }
    }
}
