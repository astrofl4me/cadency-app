package br.edu.fsa.planner.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.fsa.planner.data.repository.PlannerRepository
import br.edu.fsa.planner.domain.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

enum class PlannerNotice { SAVED, DELETED, COMPLETED, REOPENED, NOTE_SAVED, ERROR }
data class DailyUiState(
    val date: LocalDate = LocalDate.now(), val today: LocalDate = LocalDate.now(),
    val items: List<PlannerItem> = emptyList(), val tomorrowItems: List<PlannerItem> = emptyList(),
    val note: PlannerNote = PlannerNote(""), val loading: Boolean = true, val hasError: Boolean = false,
)
data class WeeklyUiState(
    val interval: DateInterval = PlannerDates.week(LocalDate.now()), val today: LocalDate = LocalDate.now(),
    val items: List<PlannerItem> = emptyList(), val note: PlannerNote = PlannerNote(""),
    val loading: Boolean = true, val hasError: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModel(private val repository: PlannerRepository, private val savedState: SavedStateHandle,
                       private val clock: Clock = Clock.systemDefaultZone()) : ViewModel() {
    private val today = MutableStateFlow(LocalDate.now(clock))
    private val retry = MutableStateFlow(0)
    private val day = savedState.getStateFlow("selected_day", today.value.toString())
    private val week = savedState.getStateFlow("selected_week", PlannerDates.week(today.value).start.toString())
    private val noticeChannel = Channel<PlannerNotice>(Channel.BUFFERED)
    val notices = noticeChannel.receiveAsFlow()

    val daily = combine(day, today, retry) { date, current, _ -> LocalDate.parse(date) to current }
        .flatMapLatest { (date, current) ->
            combine(repository.observeDay(date), repository.observeDay(date.plusDays(1)), repository.observeNote("day:$date")) { items, next, note ->
                DailyUiState(date, current, items, next.filterNot { it.isCompleted }, note, loading = false)
            }.onStart { emit(DailyUiState(date, current)) }
                .catch { emit(DailyUiState(date, current, loading = false, hasError = true)) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DailyUiState(today.value, today.value))

    val weekly = combine(week, today, retry) { date, current, _ -> PlannerDates.week(LocalDate.parse(date)) to current }
        .flatMapLatest { (interval, current) ->
            combine(repository.observeRange(interval), repository.observeNote("week:${interval.start}")) { items, note ->
                WeeklyUiState(interval, current, items, note, loading = false)
            }.onStart { emit(WeeklyUiState(interval, current)) }
                .catch { emit(WeeklyUiState(interval, current, loading = false, hasError = true)) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), WeeklyUiState(PlannerDates.week(today.value), today.value))

    fun refreshToday() { today.value = LocalDate.now(clock) }
    fun retryData() { retry.update { it + 1 } }
    fun goToday() { refreshToday(); selectDay(today.value) }
    fun selectDay(date: LocalDate) { savedState["selected_day"] = date.toString() }
    fun moveDay(amount: Long) { selectDay(LocalDate.parse(day.value).plusDays(amount)) }
    fun currentDay(): LocalDate = LocalDate.parse(day.value)
    fun moveWeek(amount: Long) { savedState["selected_week"] = LocalDate.parse(week.value).plusWeeks(amount).toString() }
    fun goCurrentWeek() { refreshToday(); savedState["selected_week"] = PlannerDates.week(today.value).start.toString() }
    fun weekAddDate(): LocalDate = PlannerDates.week(LocalDate.parse(week.value)).let { if (today.value in it) today.value else it.start }

    fun toggle(item: PlannerItem) { mutate(if (item.isCompleted) PlannerNotice.REOPENED else PlannerNotice.COMPLETED) {
        repository.setCompleted(item, !item.isCompleted)
    } }
    fun saveDailyNote(text: String) { mutate(PlannerNotice.NOTE_SAVED) { repository.saveNote(daily.value.note.copy(text = text.take(2000))) } }
    fun setMood(mood: Mood) { mutate(null) { repository.saveNote(daily.value.note.copy(mood = mood)) } }
    fun saveWeeklyNote(text: String, highlight: Boolean) { mutate(PlannerNotice.NOTE_SAVED) {
        val note = weekly.value.note
        repository.saveNote(if (highlight) note.copy(highlight = text.take(2000)) else note.copy(text = text.take(2000)))
    } }
    fun editorFinished(result: EditorResult) { viewModelScope.launch { noticeChannel.send(if (result == EditorResult.SAVED) PlannerNotice.SAVED else PlannerNotice.DELETED) } }

    private fun mutate(success: PlannerNotice?, operation: suspend () -> Unit) {
        viewModelScope.launch {
            try { operation(); success?.let { noticeChannel.send(it) }
            } catch (error: CancellationException) { throw error
            } catch (_: Exception) { noticeChannel.send(PlannerNotice.ERROR) }
        }
    }
}
