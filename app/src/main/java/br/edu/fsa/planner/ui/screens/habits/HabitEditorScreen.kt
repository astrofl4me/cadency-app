package br.edu.fsa.planner.ui.screens.habits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.viewmodel.HabitEditorState
import java.time.DayOfWeek

@Composable
fun HabitEditorScreen(state: HabitEditorState, history: List<HabitCheckIn>, onUpdate: (Habit) -> Unit,
                      onSave: () -> Unit, onDelete: () -> Unit, onBack: () -> Unit) {
    var confirm by rememberSaveable { mutableStateOf(false) }
    var showHistory by rememberSaveable { mutableStateOf(false) }
    val habit = state.habit
    Scaffold(topBar = { CadencyTopBar(stringResource(if (habit.id == 0L) R.string.paper_add_habit else R.string.paper_edit_habit), onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).imePadding().padding(D.Page),
            verticalArrangement = Arrangement.spacedBy(D.Large)) {
            if (state.loading) { LinearProgressIndicator(Modifier.fillMaxWidth()); return@Column }
            if (state.error) Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error)
            OutlinedTextField(habit.name, { onUpdate(habit.copy(name = it.take(60))) }, label = { Text(stringResource(R.string.paper_habit_name)) },
                modifier = Modifier.fillMaxWidth(), singleLine = true, isError = state.invalid && habit.name.isBlank())
            OutlinedTextField(habit.description, { onUpdate(habit.copy(description = it.take(2000))) }, label = { Text(stringResource(R.string.description)) }, modifier = Modifier.fillMaxWidth())
            Text(stringResource(R.string.habit_frequency), style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(D.Small)) {
                FrequencyType.entries.forEach { frequency -> FilterChip(habit.frequency == frequency,
                    { onUpdate(habit.copy(frequency = frequency, targetPerPeriod = 1)) },
                    label = { Text(stringResource(if (frequency == FrequencyType.DAILY) R.string.habit_daily else R.string.habit_weekly)) }) }
            }
            Text(stringResource(R.string.habit_days), style = MaterialTheme.typography.titleSmall)
            LazyRow {
                items(DayOfWeek.entries) { day ->
                    val label = day.getDisplayName(java.time.format.TextStyle.FULL, PlannerLocale)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(day.getDisplayName(java.time.format.TextStyle.SHORT, PlannerLocale), style = MaterialTheme.typography.labelSmall)
                        Checkbox(day in habit.days, { checked -> onUpdate(habit.copy(days = if (checked) habit.days + day else habit.days - day)) },
                            modifier = Modifier.semantics { contentDescription = label })
                    }
                }
            }
            if (habit.frequency == FrequencyType.WEEKLY) OutlinedTextField(habit.targetPerPeriod.toString(),
                { onUpdate(habit.copy(targetPerPeriod = it.toIntOrNull() ?: 0)) }, label = { Text(stringResource(R.string.habit_times_week)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(D.Small)) {
                items(HabitIcon.entries) { icon ->
                    FilterChip(habit.icon == icon, { onUpdate(habit.copy(icon = icon)) },
                        label = { Icon(icon.vector(), stringResource(icon.label())) })
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.habit_archive), Modifier.weight(1f))
                val archiveLabel = stringResource(R.string.habit_archive)
                Switch(habit.isArchived, { onUpdate(habit.copy(isArchived = it)) }, Modifier.semantics { contentDescription = archiveLabel })
            }
            if (state.invalid) Text(stringResource(R.string.habit_form_error), color = MaterialTheme.colorScheme.error)
            Button(onClick = onSave, enabled = !state.busy && !state.loading, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.save)) }
            if (habit.id != 0L) {
                OutlinedButton(onClick = { showHistory = true }) { Text(stringResource(R.string.habit_history)) }
                TextButton(onClick = { confirm = true }) { Icon(Icons.Outlined.DeleteOutline, null); Text(stringResource(R.string.paper_delete_habit)) }
            }
        }
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text(stringResource(R.string.paper_delete_habit)) },
        text = { Text(stringResource(R.string.habit_delete_warning)) },
        confirmButton = { TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.cancel)) } })
    if (showHistory) AlertDialog(onDismissRequest = { showHistory = false }, title = { Text(stringResource(R.string.habit_history)) },
        text = { LazyColumn(Modifier.heightIn(max = D.ContentWidth)) {
            if (history.isEmpty()) item { Text(stringResource(R.string.habit_history_empty)) }
            items(history, key = { it.date.toString() }) { check -> Text(check.date.accessibilityLabel(), Modifier.padding(vertical = D.Small)) }
        } }, confirmButton = { TextButton(onClick = { showHistory = false }) { Text(stringResource(R.string.close)) } })
}
