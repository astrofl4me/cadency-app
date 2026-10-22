package br.edu.fsa.planner.ui.screens.habits
import br.edu.fsa.planner.ui.components.CadencyButton as Button
import br.edu.fsa.planner.ui.components.CadencyOutlinedButton as OutlinedButton
import br.edu.fsa.planner.ui.components.CadencyCheckbox as Checkbox

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.ui.theme.LocalCadencyPersonality
import androidx.compose.animation.core.tween
import br.edu.fsa.planner.viewmodel.HabitsState
import java.time.LocalDate

fun HabitIcon.vector() = when (this) {
    HabitIcon.WATER -> Icons.Outlined.WaterDrop; HabitIcon.BOOK -> Icons.AutoMirrored.Outlined.MenuBook
    HabitIcon.HEALTH -> Icons.Outlined.FavoriteBorder; HabitIcon.STUDY -> Icons.Outlined.School
    HabitIcon.CALM -> Icons.Outlined.Spa; HabitIcon.CHECK -> Icons.Outlined.CheckCircleOutline
}

@Composable
fun HabitsScreen(state: HabitsState, onAdd: () -> Unit, onEdit: (Habit) -> Unit,
                 onToggle: (Habit, LocalDate) -> Unit, onPrevious: () -> Unit, onNext: () -> Unit,
                 onCurrent: () -> Unit, onArchived: (Boolean) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(D.Page), verticalArrangement = Arrangement.spacedBy(D.Large)) {
        item {
            PlannerHeader(stringResource(R.string.habits_eyebrow), stringResource(R.string.tab_habits), stringResource(R.string.habits_subtitle))
            DateNavigator(stringResource(R.string.previous_week), stringResource(R.string.next_week), stringResource(R.string.current_week), onPrevious, onNext, onCurrent)
            Text(stringResource(R.string.week_interval, state.week.start.shortLabel(), state.week.endInclusive.shortLabel(), state.week.endInclusive.year.toString()),
                style = MaterialTheme.typography.bodySmall)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.habits_archived), Modifier.weight(1f))
                val archivedLabel = stringResource(R.string.habits_archived)
                Switch(state.archived, onArchived, Modifier.semantics { contentDescription = archivedLabel })
            }
            Button(onClick = onAdd) { Icon(Icons.Outlined.Add, null); Text(stringResource(R.string.paper_add_habit)) }
        }
        if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (state.error) item { Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error) }
        if (!state.loading && state.habits.isEmpty()) item { EmptyState(stringResource(R.string.habits_empty)) }
        items(state.habits, key = { it.id }) { habit ->
            CadencySurface(Modifier.fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().animateContentSize(tween(LocalCadencyPersonality.current.motionMillis)).padding(D.Medium), verticalArrangement = Arrangement.spacedBy(D.Small)) {
                Row(Modifier.fillMaxWidth().clickable { onEdit(habit) }.heightIn(min = D.Touch), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(D.Medium)) {
                    Icon(habit.icon.vector(), null, tint = MaterialTheme.colorScheme.primary)
                    Column(Modifier.weight(1f)) {
                        Text(habit.name, style = MaterialTheme.typography.titleMedium)
                        Text(stringResource(if (habit.frequency == FrequencyType.WEEKLY) R.string.habit_weekly_goal else R.string.habit_daily_goal,
                            habit.targetPerPeriod), style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Outlined.Edit, stringResource(R.string.paper_edit_habit))
                }
                LazyRow {
                    items(state.week.days, key = { it.toString() }) { date ->
                        val checked = state.checks.any { it.habitId == habit.id && it.date == date }
                        val checkLabel = stringResource(R.string.habit_check_date, habit.name, date.accessibilityLabel())
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(date.weekdayLabel().take(1).uppercase(PlannerLocale), style = MaterialTheme.typography.labelSmall)
                            Checkbox(checked, { onToggle(habit, date) }, enabled = date <= state.today && HabitRules.isPlanned(habit, date),
                                modifier = Modifier.size(D.Touch).semantics { contentDescription = checkLabel })
                            Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                val weekDone = state.checks.count { it.habitId == habit.id && it.date in state.week }
                val weekGoal = HabitRules.opportunities(habit, state.week)
                Text(stringResource(R.string.habit_week_summary, weekDone, weekGoal), style = MaterialTheme.typography.bodySmall)
                val checkedToday = state.checks.any { it.habitId == habit.id && it.date == state.today }
                if (HabitRules.isPlanned(habit, state.today) && !state.archived) {
                    OutlinedButton(onClick = { onToggle(habit, state.today) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(if (checkedToday) R.string.habit_undo_today else R.string.habit_complete_today))
                    }
                }
                HorizontalDivider()
            }
            }
        }
    }
}
