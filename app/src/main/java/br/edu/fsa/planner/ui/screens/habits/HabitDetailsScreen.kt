package br.edu.fsa.planner.ui.screens.habits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.viewmodel.HabitDetailsState

@Composable
fun HabitDetailsScreen(state: HabitDetailsState, onWeeks: (Int) -> Unit, onEdit: () -> Unit, onBack: () -> Unit, onFocus: (() -> Unit)? = null) {
    Scaffold(topBar = { CadencyTopBar(stringResource(R.string.habit_details), onBack) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(D.Page), verticalArrangement = Arrangement.spacedBy(D.Large)) {
            if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            if (state.error) item { Text(stringResource(R.string.storage_error)) }
            state.habit?.let { habit ->
                item {
                    Text(habit.name, style = MaterialTheme.typography.headlineLarge)
                    Text(if (habit.frequency == FrequencyType.WEEKLY) countedString(R.plurals.habit_weekly_goal, habit.targetPerPeriod, habit.targetPerPeriod)
                        else stringResource(R.string.habit_daily_goal))
                    if (habit.description.isNotBlank()) Text(habit.description)
                    Row(horizontalArrangement = Arrangement.spacedBy(D.Small)) {
                        CadencyOutlinedButton(onEdit) { Text(stringResource(R.string.paper_edit_habit)) }
                    }
                    if (onFocus != null) CadencyButton(onFocus) { Text(stringResource(R.string.habit_focus)) }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(D.Small)) { state.availableWeeks.forEach { count ->
                        FilterChip(state.weeks == count, { onWeeks(count) }, label = { Text(stringResource(when (count) { 26 -> R.string.period_six_months; 52 -> R.string.period_year; else -> R.string.period_three_months })) })
                    } }
                    CadencyHeatmap(state.map)
                }
                item {
                    SectionTitle(stringResource(R.string.habit_recent_stats))
                    Text(stringResource(R.string.habit_consistency_value, state.consistency))
                    if (habit.frequency == FrequencyType.DAILY) Text(countedString(R.plurals.habit_streak_days, state.streak, state.streak))
                    else Text(stringResource(R.string.heatmap_flexible_hint))
                    Text(countedString(R.plurals.habit_history_count, state.checks.size, state.checks.size))
                    Text(stringResource(R.string.habit_first_day, habit.startsOn.accessibilityLabel()), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
