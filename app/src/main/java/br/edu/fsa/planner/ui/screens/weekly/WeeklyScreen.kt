package br.edu.fsa.planner.ui.screens.weekly

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.ui.theme.PlannerPaper as Paper
import br.edu.fsa.planner.viewmodel.WeeklyUiState
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun WeeklyScreen(state: WeeklyUiState, onPrevious: () -> Unit, onNext: () -> Unit, onCurrent: () -> Unit,
                 onAdd: (LocalDate, ItemType) -> Unit, onEdit: (PlannerItem) -> Unit, onToggle: (PlannerItem) -> Unit,
                 onOpenDay: (LocalDate) -> Unit, onNote: (String, Boolean) -> Unit,
                 onSaveHabit: (WeeklyHabit) -> Unit, onToggleHabit: (WeeklyHabit, DayOfWeek) -> Unit,
                 onDeleteHabit: (WeeklyHabit) -> Unit, onRetry: () -> Unit) {
    var editing by rememberSaveable(state.interval.start.toString()) { mutableStateOf<Boolean?>(null) }
    val priorities = state.items.filter { it.type == ItemType.PRIORITY || it.priority == Priority.HIGH }
    val tasks = state.items.filter { it.type != ItemType.EVENT && it !in priorities }
    val addDate = if (state.today in state.interval) state.today else state.interval.start
    Box(Modifier.fillMaxSize().background(Paper.Background), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = D.PaperContentWidth).fillMaxSize().testTag("planner-paper-page"),
            contentPadding = PaddingValues(start = D.Page, end = D.Page, top = D.Page, bottom = D.Hero + D.Page),
            verticalArrangement = Arrangement.spacedBy(D.Page)) {
            item {
                PaperPageHeader(stringResource(R.string.weekly_eyebrow), stringResource(R.string.paper_weekly_title),
                    stringResource(R.string.paper_week_number, state.interval.start.get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear())))
                Spacer(Modifier.height(D.Small))
                Text(stringResource(R.string.week_interval, state.interval.start.shortLabel(), state.interval.endInclusive.shortLabel(),
                    state.interval.endInclusive.year.toString()), style = Paper.Caption, color = Paper.Muted)
                DateNavigator(stringResource(R.string.previous_week), stringResource(R.string.next_week), stringResource(R.string.current_week),
                    onPrevious, onNext, onCurrent)
            }
            if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            else if (state.hasError) item { DataError(onRetry) }
            else {
                item {
                    SectionTitle(stringResource(R.string.week_rhythm))
                    Text(stringResource(R.string.week_rhythm_hint), style = Paper.Caption, color = Paper.Muted)
                }
                item {
                    PaperColumns(primary = {
                        Column(verticalArrangement = Arrangement.spacedBy(D.Small)) {
                            state.interval.days.forEach { date ->
                                WeeklyDay(date, date == state.today, state.items.filter { it.date == date },
                                    { onOpenDay(date) }, { onAdd(date, ItemType.TASK) }, onToggle, onEdit)
                            }
                        }
                    }, secondary = {
                        PaperItemSection(stringResource(R.string.priorities), priorities, shaded = true, minimumRows = 4,
                            emptyMessage = stringResource(R.string.priorities_empty),
                            onAdd = { onAdd(addDate, ItemType.PRIORITY) }, onToggle = onToggle, onEdit = onEdit)
                        Spacer(Modifier.height(D.Large))
                        PaperItemSection(stringResource(R.string.paper_week_todo), tasks, minimumRows = 5,
                            emptyMessage = stringResource(R.string.paper_add_here),
                            onAdd = { onAdd(addDate, ItemType.TASK) }, onToggle = onToggle, onEdit = onEdit)
                        Spacer(Modifier.height(D.Large))
                        PaperHabitTracker(state.interval.start, state.habits, onSaveHabit, onToggleHabit, onDeleteHabit)
                    })
                }
                item {
                    PaperNoteArea(stringResource(R.string.week_highlight), state.note.highlight,
                        stringResource(R.string.week_highlight_hint), { editing = true }, ruled = false)
                }
                item {
                    PaperNoteArea(stringResource(R.string.week_notes), state.note.text,
                        stringResource(R.string.week_notes_hint), { editing = false }, shaded = true, ruled = false)
                }
            }
        }
    }
    editing?.let { highlight -> NoteDialog(stringResource(if (highlight) R.string.week_highlight else R.string.week_notes),
        if (highlight) state.note.highlight else state.note.text, { editing = null }, { onNote(it, highlight) }) }
}

@Composable
private fun WeeklyDay(date: LocalDate, today: Boolean, items: List<PlannerItem>, onOpen: () -> Unit,
                      onAdd: () -> Unit, onToggle: (PlannerItem) -> Unit, onEdit: (PlannerItem) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = D.PaperDayHeight).background(Paper.Panel, Paper.Shape)) {
        Column(Modifier.width(D.PaperWeekdayWidth)
            .clickable(onClickLabel = stringResource(R.string.open_daily), onClick = onOpen).padding(D.Medium),
            verticalArrangement = Arrangement.spacedBy(D.Tiny)) {
            Text(date.weekdayLabel().removeSuffix("-feira"), style = Paper.Label, color = Paper.Ink)
            Text(date.dayOfMonth.toString(), style = Paper.Title, color = Paper.Ink)
            val level = WeeklyRhythm.level(items)
            val rhythmLabel = stringResource(R.string.week_rhythm_level, level)
            Row(Modifier.semantics { contentDescription = rhythmLabel }, horizontalArrangement = Arrangement.spacedBy(D.Tiny)) {
                repeat(5) { index -> Box(Modifier.size(D.Indicator).background(
                    if (index < level) MaterialTheme.colorScheme.primary else Paper.Rule, CircleShape)) }
            }
            if (today) Text(stringResource(R.string.today_label), style = Paper.Caption, color = MaterialTheme.colorScheme.primary)
        }
        Column(Modifier.weight(1f).padding(end = D.Small, bottom = D.Small)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                HorizontalDivider(Modifier.weight(1f), color = Paper.Rule)
                IconButton(onClick = onAdd) { Icon(Icons.Outlined.Add, stringResource(R.string.add_day, date.shortLabel()),
                    tint = Paper.Muted, modifier = Modifier.size(D.Icon)) }
            }
            if (items.isEmpty()) Text(stringResource(R.string.week_day_empty), style = Paper.Caption, color = Paper.Muted,
                modifier = Modifier.fillMaxWidth().heightIn(min = D.Touch).clickable(onClick = onAdd).padding(top = D.Small))
            items.forEach { item -> PaperItemRow(item, { onToggle(item) }, { onEdit(item) }) }
        }
    }
}
