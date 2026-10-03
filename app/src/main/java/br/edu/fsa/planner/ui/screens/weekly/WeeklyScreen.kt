package br.edu.fsa.planner.ui.screens.weekly

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
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
import br.edu.fsa.planner.viewmodel.WeeklyUiState
import java.time.LocalDate

@Composable
fun WeeklyScreen(state: WeeklyUiState, onPrevious: () -> Unit, onNext: () -> Unit, onCurrent: () -> Unit,
                 onAdd: (LocalDate, ItemType) -> Unit, onEdit: (PlannerItem) -> Unit, onToggle: (PlannerItem) -> Unit,
                 onOpenDay: (LocalDate) -> Unit, onNote: (String, Boolean) -> Unit, onRetry: () -> Unit) {
    var editing by rememberSaveable(state.interval.start.toString()) { mutableStateOf<String?>(null) }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = D.ContentWidth).fillMaxSize(),
            contentPadding = PaddingValues(start = D.Page, end = D.Page, top = D.Page, bottom = D.Hero + D.Page)) {
            item {
                PlannerHeader(stringResource(R.string.weekly_eyebrow), stringResource(R.string.weekly_title),
                    stringResource(R.string.week_interval, state.interval.start.shortLabel(), state.interval.endInclusive.shortLabel(), state.interval.endInclusive.year.toString()))
                DateNavigator(stringResource(R.string.previous_week), stringResource(R.string.next_week), stringResource(R.string.current_week),
                    onPrevious, onNext, onCurrent)
                HorizontalDivider()
                Spacer(Modifier.height(D.Page))
            }
            if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            else if (state.hasError) item { DataError(onRetry) }
            else {
                item {
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.large,
                        modifier = Modifier.fillMaxWidth().clickable { editing = "highlight" }) {
                        Column(Modifier.padding(D.Large), verticalArrangement = Arrangement.spacedBy(D.Small)) {
                            Text(stringResource(R.string.week_highlight), style = MaterialTheme.typography.titleSmall)
                            Text(state.note.highlight.ifBlank { stringResource(R.string.week_highlight_hint) }, style = MaterialTheme.typography.bodyMedium)
                            Text(stringResource(R.string.edit_highlight), style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Spacer(Modifier.height(D.Page))
                }
                state.interval.days.forEach { date ->
                    val dayItems = PlannerDates.itemsIn(state.items, DateInterval(date, date))
                    item(key = date.toString()) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = if (date == state.today) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                shape = MaterialTheme.shapes.medium, modifier = Modifier.weight(1f).clickable(
                                    onClickLabel = stringResource(R.string.open_daily), onClick = { onOpenDay(date) })) {
                                Row(Modifier.padding(D.Small), verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(D.Medium)) {
                                    Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.headlineMedium)
                                    Column {
                                        Text(date.weekdayLabel(), style = MaterialTheme.typography.titleSmall)
                                        Text(if (date == state.today) stringResource(R.string.today_label) else date.shortLabel(),
                                            style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            IconButton(onClick = { onAdd(date, ItemType.TASK) }) {
                                Icon(Icons.Outlined.Add, stringResource(R.string.add_day, date.shortLabel()))
                            }
                        }
                        if (dayItems.isEmpty()) EmptyState(stringResource(R.string.week_day_empty))
                        else dayItems.forEach { item -> PlannerItemRow(item, { onToggle(item) }, { onEdit(item) }, compact = true) }
                        Spacer(Modifier.height(D.Page))
                        HorizontalDivider()
                        Spacer(Modifier.height(D.Large))
                    }
                }
                item {
                    SectionTitle(stringResource(R.string.week_notes))
                    Text(state.note.text.ifBlank { stringResource(R.string.week_notes_hint) }, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { editing = "text" }) { Text(stringResource(R.string.edit_note)) }
                }
            }
        }
    }
    editing?.let { field -> NoteDialog(stringResource(if (field == "highlight") R.string.week_highlight else R.string.week_notes),
        if (field == "highlight") state.note.highlight else state.note.text, { editing = null }, { onNote(it, field == "highlight") }) }
}
