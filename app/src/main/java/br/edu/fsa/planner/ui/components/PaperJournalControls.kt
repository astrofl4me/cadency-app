package br.edu.fsa.planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.ui.theme.PlannerPaper as Paper
import java.time.DayOfWeek
import java.time.LocalDate

private fun Mood.face(): ImageVector = when (this) {
    Mood.DIFFICULT -> Icons.Outlined.SentimentDissatisfied
    Mood.QUIET -> Icons.Outlined.SentimentNeutral
    Mood.GOOD -> Icons.Outlined.SentimentSatisfied
    Mood.GREAT -> Icons.Outlined.SentimentVerySatisfied
}

@Composable
fun PaperWeekStrip(date: LocalDate) {
    val labels = stringArrayResource(R.array.weekdays_monday_short)
    Row(Modifier.fillMaxWidth().padding(top = D.Large), horizontalArrangement = Arrangement.SpaceBetween) {
        PlannerDates.week(date).days.forEachIndexed { index, day ->
            Column(Modifier.weight(1f).semantics(mergeDescendants = true) {
                contentDescription = day.accessibilityLabel(); selected = day == date
            }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(D.Tiny)) {
                Text(labels[index], style = Paper.Caption, color = Paper.Muted)
                Box(Modifier.size(D.PaperDot).background(if (day == date) Paper.Ink else Paper.Rule, CircleShape))
            }
        }
    }
}

@Composable
fun PaperMoodSelector(selected: Mood?, onSelect: (Mood) -> Unit) {
    Column(Modifier.fillMaxWidth().background(Paper.Panel, Paper.Shape).padding(D.Medium)) {
        Text(stringResource(R.string.paper_feeling), style = Paper.Label, color = Paper.Muted)
        Row(Modifier.fillMaxWidth().padding(top = D.Small), horizontalArrangement = Arrangement.SpaceBetween) {
            Mood.entries.forEach { mood ->
                val description = stringResource(mood.label())
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(Modifier.size(D.Touch).background(if (selected == mood) Paper.Selected else Paper.Panel, CircleShape)
                        .selectable(selected == mood, role = Role.RadioButton, onClick = { onSelect(mood) })
                        .semantics { contentDescription = description }, contentAlignment = Alignment.Center) {
                        Icon(mood.face(), null, modifier = Modifier.size(D.PaperMood), tint = Paper.Ink)
                    }
                    Text(stringResource(mood.label()), style = Paper.Caption, color = Paper.Muted)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PaperWaterTracker(glasses: Int, onChange: (Int) -> Unit) {
    Column(Modifier.fillMaxWidth().background(Paper.Panel, Paper.Shape).padding(D.Medium)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.paper_water), style = Paper.Label, color = Paper.Muted, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.paper_water_count, glasses, PlannerJournal.WaterGlasses), style = Paper.Caption, color = Paper.Muted)
        }
        BoxWithConstraints(Modifier.fillMaxWidth().padding(top = D.Small)) {
            FlowRow(maxItemsInEachRow = if (maxWidth >= D.Touch * PlannerJournal.WaterGlasses) PlannerJournal.WaterGlasses else 4) {
                repeat(PlannerJournal.WaterGlasses) { index ->
                    val filled = index < glasses
                    IconToggleButton(checked = filled, onCheckedChange = { onChange(if (glasses == index + 1) index else index + 1) },
                        modifier = Modifier.weight(1f).height(D.Touch)) {
                        Icon(Icons.Outlined.WaterDrop,
                            stringResource(R.string.paper_water_set, index + 1), tint = if (filled) Paper.Ink else Paper.Rule,
                            modifier = Modifier.size(D.Icon))
                    }
                }
            }
        }
        Text(stringResource(R.string.paper_water_hint), style = Paper.Caption, color = Paper.Muted)
    }
}

@Composable
fun PaperHabitTracker(weekStart: LocalDate, habits: List<WeeklyHabit>, onSave: (WeeklyHabit) -> Unit,
                      onToggle: (WeeklyHabit, DayOfWeek) -> Unit, onDelete: (WeeklyHabit) -> Unit) {
    var editing by rememberSaveable { mutableStateOf(false) }
    var habitId by rememberSaveable { mutableStateOf<Long?>(null) }
    val days = stringArrayResource(R.array.weekdays_monday_short)
    Column(Modifier.fillMaxWidth().padding(horizontal = D.Medium)) {
        PaperSectionHeading(stringResource(R.string.paper_habit_tracker)) { habitId = null; editing = true }
        BoxWithConstraints(Modifier.fillMaxWidth()) {
            val tableWidth = D.PaperHabitLabelWidth + D.Touch * DayOfWeek.entries.size
            val needsScroll = maxWidth < tableWidth
            Column {
                Column(Modifier.horizontalScroll(rememberScrollState())) {
                    Column(Modifier.width(tableWidth)) {
                        Row(Modifier.fillMaxWidth()) {
                            Spacer(Modifier.width(D.PaperHabitLabelWidth))
                            DayOfWeek.entries.forEach { day ->
                                Box(Modifier.weight(1f).height(D.Touch), contentAlignment = Alignment.Center) {
                                    Text(days[day.value - 1], style = Paper.Caption, color = Paper.Muted)
                                }
                            }
                        }
                        habits.forEach { habit ->
                            PaperHabitRow(habit, weekStart, { habitId = habit.id; editing = true }, onToggle)
                        }
                        repeat((4 - habits.size).coerceAtLeast(0)) { PaperRules(1) }
                    }
                }
                if (needsScroll) {
                    Text(stringResource(R.string.paper_habits_scroll), style = Paper.Caption, color = Paper.Muted,
                        modifier = Modifier.padding(top = D.Small))
                }
            }
        }
        if (habits.isEmpty()) Text(stringResource(R.string.paper_habits_hint), style = Paper.Caption, color = Paper.Muted)
        TextButton(onClick = { habitId = null; editing = true }) { Text(stringResource(R.string.paper_add_habit), color = Paper.Ink) }
    }
    if (editing) {
        val habit = habits.firstOrNull { it.id == habitId }
        HabitDialog(habit, weekStart, { editing = false }, onSave, onDelete)
    }
}

@Composable
private fun PaperHabitRow(habit: WeeklyHabit, weekStart: LocalDate, onEdit: () -> Unit,
                          onToggle: (WeeklyHabit, DayOfWeek) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = D.Touch), verticalAlignment = Alignment.CenterVertically) {
        Text(habit.title, style = Paper.Caption, color = Paper.Ink,
            modifier = Modifier.width(D.PaperHabitLabelWidth).heightIn(min = D.Touch)
                .clickable(onClickLabel = stringResource(R.string.paper_edit_habit), onClick = onEdit)
                .padding(end = D.Small, top = D.Small))
        DayOfWeek.entries.forEach { day ->
            val selected = day in habit.completedDays
            Box(Modifier.weight(1f).height(D.Touch)
                .selectable(selected, role = Role.Checkbox, onClick = { onToggle(habit, day) })
                .semantics { contentDescription = "${habit.title}, ${weekStart.plusDays((day.value - 1).toLong()).weekdayLabel()}" },
                contentAlignment = Alignment.Center) {
                Box(Modifier.size(D.Medium).border(D.Hairline, Paper.Marker, CircleShape)
                    .background(if (selected) Paper.Marker else Paper.Background, CircleShape))
            }
        }
    }
}

@Composable
private fun HabitDialog(habit: WeeklyHabit?, weekStart: LocalDate, onDismiss: () -> Unit,
                        onSave: (WeeklyHabit) -> Unit, onDelete: (WeeklyHabit) -> Unit) {
    var title by rememberSaveable(habit?.id) { mutableStateOf(habit?.title.orEmpty()) }
    var invalid by rememberSaveable { mutableStateOf(false) }
    AlertDialog(onDismissRequest = onDismiss,
        title = { Text(stringResource(if (habit == null) R.string.paper_add_habit else R.string.paper_edit_habit)) },
        text = { Column {
            OutlinedTextField(title, { if (it.length <= PlannerJournal.HabitTitleLimit) { title = it; invalid = false } },
                modifier = Modifier.testTag("habit-name"),
                label = { Text(stringResource(R.string.paper_habit_name)) }, singleLine = true, isError = invalid,
                supportingText = { if (invalid) Text(stringResource(R.string.paper_habit_required)) })
            if (habit != null) TextButton(onClick = { onDelete(habit); onDismiss() }) { Text(stringResource(R.string.paper_delete_habit)) }
        } },
        confirmButton = { TextButton(onClick = {
            if (title.isBlank()) invalid = true
            else { onSave((habit ?: WeeklyHabit(weekStart = weekStart, title = title)).copy(title = title.trim())); onDismiss() }
        }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}
