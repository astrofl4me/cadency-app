package br.edu.fsa.planner.viewmodel

import androidx.lifecycle.*
import br.edu.fsa.planner.data.repository.ProgressRepository
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDate

data class ProgressState(val report: ProgressReport = ProgressReport(DateInterval(LocalDate.now().minusDays(6), LocalDate.now())),
    val period: ProgressPeriod = ProgressPeriod.WEEK, val loading: Boolean = true, val error: Boolean = false)

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressViewModel(repository: ProgressRepository, private val saved: SavedStateHandle) : ViewModel() {
    private val period = saved.getStateFlow("progress_period", ProgressPeriod.WEEK.name)
    private val today = flow { while (currentCoroutineContext().isActive) { emit(LocalDate.now()); delay(60_000) } }.distinctUntilChanged()
    val state = combine(today, period) { date, value -> date to ProgressPeriod.valueOf(value) }.flatMapLatest { (date, selected) ->
        val interval = DateInterval(date.minusDays(selected.days - 1), date)
        repository.observe(interval).map { ProgressState(it, selected, false) }
            .onStart { emit(ProgressState(ProgressReport(interval), selected)) }
            .catch { emit(ProgressState(ProgressReport(interval), selected, false, true)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressState())
    fun select(period: ProgressPeriod) { saved["progress_period"] = period.name }
}
