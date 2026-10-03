package br.edu.fsa.planner.ui.screens.daily

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.viewmodel.DailyUiState
import java.time.LocalDate

@Composable
fun DailyScreen(state: DailyUiState, onPrevious: () -> Unit, onNext: () -> Unit, onToday: () -> Unit,
                onAdd: (LocalDate, ItemType) -> Unit, onEdit: (PlannerItem) -> Unit, onToggle: (PlannerItem) -> Unit,
                onMood: (Mood) -> Unit, onNote: (String) -> Unit, onRetry: () -> Unit) {
    var editNote by rememberSaveable(state.date.toString()) { mutableStateOf(false) }
    val priorities = state.items.filter { it.type == ItemType.PRIORITY || it.priority == Priority.HIGH }
    val agenda = state.items.filterNot { it in priorities }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = D.ContentWidth).fillMaxSize(),
            contentPadding = PaddingValues(start = D.Page, end = D.Page, top = D.Page, bottom = D.Hero + D.Page)) {
            item {
                PlannerHeader(stringResource(R.string.daily_eyebrow), state.date.longLabel(),
                    "${state.date.weekdayLabel()} · ${state.date.year}")
                DateNavigator(stringResource(R.string.previous_day), stringResource(R.string.next_day), stringResource(R.string.back_today),
                    onPrevious, onNext, onToday)
                HorizontalDivider()
                Spacer(Modifier.height(D.Large))
                Text(stringResource(R.string.daily_quote), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(D.Large))
                if (!state.loading && !state.hasError && state.items.isNotEmpty()) Text(
                    stringResource(R.string.daily_progress, state.items.count { it.isCompleted }, state.items.size),
                    style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical = D.Page)) }
            else if (state.hasError) item { DataError(onRetry) }
            else {
                item { SectionTitle(stringResource(R.string.priorities), priorities.size) { onAdd(state.date, ItemType.PRIORITY) } }
                if (priorities.isEmpty()) item { EmptyState(stringResource(R.string.priorities_empty)) }
                items(priorities, key = { "priority:${it.id}" }) { item -> PlannerItemRow(item, { onToggle(item) }, { onEdit(item) }) }
                item {
                    Spacer(Modifier.height(D.Large))
                    SectionTitle(stringResource(R.string.day_agenda), agenda.size) { onAdd(state.date, ItemType.TASK) }
                }
                if (agenda.isEmpty()) item { EmptyState(stringResource(if (state.items.isEmpty()) R.string.daily_empty else R.string.agenda_empty),
                    stringResource(R.string.add_first), { onAdd(state.date, ItemType.TASK) }) }
                items(agenda, key = { "agenda:${it.id}" }) { item -> PlannerItemRow(item, { onToggle(item) }, { onEdit(item) }) }
                item {
                    Spacer(Modifier.height(D.Section))
                    SectionTitle(stringResource(R.string.tomorrow)) { onAdd(state.date.plusDays(1), ItemType.TOMORROW) }
                    Text(stringResource(R.string.tomorrow_date, state.date.plusDays(1).shortLabel()), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (state.tomorrowItems.isEmpty()) item { EmptyState(stringResource(R.string.tomorrow_empty)) }
                items(state.tomorrowItems, key = { "next:${it.id}" }) { item -> PlannerItemRow(item, { onToggle(item) }, { onEdit(item) }, compact = true) }
                item {
                    Spacer(Modifier.height(D.Section))
                    SectionTitle(stringResource(R.string.mood_today))
                    MoodSelector(state.note.mood, onMood)
                    Spacer(Modifier.height(D.Large))
                    Surface(color = MaterialTheme.colorScheme.tertiaryContainer, shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth().clickable(onClickLabel = stringResource(R.string.edit_note)) { editNote = true }) {
                        Column(Modifier.padding(D.Large), verticalArrangement = Arrangement.spacedBy(D.Small)) {
                            Text(stringResource(R.string.daily_note), style = MaterialTheme.typography.titleSmall)
                            Text(state.note.text.ifBlank { stringResource(R.string.daily_note_hint) },
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text(stringResource(R.string.edit_note), style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }
        }
    }
    if (editNote) NoteDialog(stringResource(R.string.daily_note), state.note.text, { editNote = false }, onNote)
}
