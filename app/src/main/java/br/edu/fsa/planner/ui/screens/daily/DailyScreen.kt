package br.edu.fsa.planner.ui.screens.daily

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.ui.theme.PlannerPaper as Paper
import br.edu.fsa.planner.viewmodel.DailyUiState
import java.time.LocalDate

@Composable
fun DailyScreen(state: DailyUiState, onPrevious: () -> Unit, onNext: () -> Unit, onToday: () -> Unit,
                onAdd: (LocalDate, ItemType) -> Unit, onEdit: (PlannerItem) -> Unit, onToggle: (PlannerItem) -> Unit,
                onMood: (Mood) -> Unit, onNote: (String, DailyNoteField) -> Unit, onWater: (Int) -> Unit, onRetry: () -> Unit) {
    var editing by rememberSaveable(state.date.toString()) { mutableStateOf<DailyNoteField?>(null) }
    val priorities = state.items.filter { it.type == ItemType.PRIORITY || it.priority == Priority.HIGH }
    val agenda = state.items.filterNot { it in priorities }
    Box(Modifier.fillMaxSize().background(Paper.Background), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = D.PaperContentWidth).fillMaxSize().testTag("planner-paper-page"),
            contentPadding = PaddingValues(start = D.Page, end = D.Page, top = D.Page, bottom = D.Hero + D.Page),
            verticalArrangement = Arrangement.spacedBy(D.Page)) {
            item {
                PaperPageHeader(stringResource(R.string.daily_eyebrow),
                    stringResource(if (state.date == state.today) R.string.today_label else R.string.paper_daily_title),
                    state.date.longLabel())
                PaperWeekStrip(state.date)
                DateNavigator(stringResource(R.string.previous_day), stringResource(R.string.next_day), stringResource(R.string.back_today),
                    onPrevious, onNext, onToday)
            }
            if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            else if (state.hasError) item { DataError(onRetry) }
            else {
                item {
                    PaperColumns(primary = {
                        PaperItemSection(stringResource(R.string.priorities), priorities, shaded = true,
                            emptyMessage = stringResource(R.string.priorities_empty),
                            onAdd = { onAdd(state.date, ItemType.PRIORITY) }, onToggle = onToggle, onEdit = onEdit)
                        Spacer(Modifier.height(D.Large))
                        PaperItemSection(stringResource(R.string.day_agenda), agenda, minimumRows = 5,
                            emptyMessage = stringResource(R.string.daily_empty),
                            onAdd = { onAdd(state.date, ItemType.TASK) }, onToggle = onToggle, onEdit = onEdit)
                        if (state.items.isEmpty()) TextButton(onClick = { onAdd(state.date, ItemType.TASK) }) {
                            Text(stringResource(R.string.add_first), color = Paper.Ink)
                        }
                    }, secondary = {
                        PaperMoodSelector(state.note.mood, onMood)
                        Spacer(Modifier.height(D.Large))
                        PaperWaterTracker(state.note.waterGlasses, onWater)
                        Spacer(Modifier.height(D.Large))
                        PaperNoteArea(stringResource(R.string.paper_self_care), state.note.selfCare,
                            stringResource(R.string.paper_self_care_hint), { editing = DailyNoteField.SELF_CARE })
                    })
                }
                item {
                    PaperNoteArea(stringResource(R.string.paper_gratitude), state.note.gratitude,
                        stringResource(R.string.paper_gratitude_hint), { editing = DailyNoteField.GRATITUDE },
                        shaded = true, ruled = false)
                }
                item {
                    PaperNoteArea(stringResource(R.string.paper_activities), state.note.text,
                        stringResource(R.string.daily_note_hint), { editing = DailyNoteField.NOTES },
                        modifier = Modifier.heightIn(min = D.PaperNotesHeight), shaded = true, ruled = false)
                }
                item {
                    PaperItemSection(stringResource(R.string.tomorrow), state.tomorrowItems, minimumRows = 2,
                        emptyMessage = stringResource(R.string.tomorrow_empty),
                        onAdd = { onAdd(state.date.plusDays(1), ItemType.TOMORROW) }, onToggle = onToggle, onEdit = onEdit)
                    Text(stringResource(R.string.tomorrow_date, state.date.plusDays(1).shortLabel()),
                        style = Paper.Caption, color = Paper.Muted, modifier = Modifier.padding(horizontal = D.Medium))
                }
            }
        }
    }
    editing?.let { field ->
        val title = stringResource(when (field) {
            DailyNoteField.NOTES -> R.string.paper_activities
            DailyNoteField.GRATITUDE -> R.string.paper_gratitude
            DailyNoteField.SELF_CARE -> R.string.paper_self_care
        })
        val text = when (field) {
            DailyNoteField.NOTES -> state.note.text
            DailyNoteField.GRATITUDE -> state.note.gratitude
            DailyNoteField.SELF_CARE -> state.note.selfCare
        }
        NoteDialog(title, text, { editing = null }, { onNote(it, field) })
    }
}
