package br.edu.fsa.planner.ui.screens.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.viewmodel.ProgressState

@Composable
fun ProgressScreen(state: ProgressState, onPeriod: (ProgressPeriod) -> Unit) {
    val report = state.report
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(D.Page), verticalArrangement = Arrangement.spacedBy(D.Page)) {
        item {
            PlannerHeader(stringResource(R.string.rhythm_eyebrow), stringResource(R.string.rhythm_title), stringResource(R.string.rhythm_subtitle))
            Row(horizontalArrangement = Arrangement.spacedBy(D.Small)) {
                ProgressPeriod.entries.forEach { period -> FilterChip(state.period == period, { onPeriod(period) },
                    label = { Text(stringResource(R.string.period_days, period.days.toInt())) }) }
            }
        }
        if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
        if (state.error) item { Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error) }
        if (!state.loading && !report.hasActivity) item { EmptyState(stringResource(R.string.rhythm_empty)) }
        item {
            val metrics = listOf(R.string.metric_tasks to report.completedTasks.toInt(), R.string.metric_habits to report.habitCheckIns,
                R.string.metric_focus to report.focusMinutes.toInt(), R.string.metric_days to report.plannedDays)
            metrics.chunked(2).forEach { row -> Row(Modifier.fillMaxWidth()) { row.forEach { (label, value) ->
                Column(Modifier.weight(1f).padding(vertical = D.Small)) {
                    Text(value.toString(), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(label), style = MaterialTheme.typography.bodySmall)
                }
            } } }
        }
        item {
            RhythmChart(stringResource(R.string.chart_tasks), report.tasks)
            Text(stringResource(R.string.chart_tasks_hint), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item { RhythmChart(stringResource(R.string.chart_focus), report.focus) }
        item { SectionTitle(stringResource(R.string.habit_consistency)) }
        if (report.habits.isEmpty()) item { Text(stringResource(R.string.habit_consistency_empty), style = MaterialTheme.typography.bodyMedium) }
        items(report.habits, key = { it.habit.id }) { habit ->
            Column(verticalArrangement = Arrangement.spacedBy(D.Small)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(habit.habit.name, Modifier.weight(1f)); Text(stringResource(R.string.percent_value, habit.percent))
                }
                LinearProgressIndicator(progress = { habit.percent / 100f }, modifier = Modifier.fillMaxWidth())
                Text(stringResource(R.string.habit_period_summary, habit.done, habit.planned), style = MaterialTheme.typography.bodySmall)
            }
        }
        if (report.hasActivity) item {
            HorizontalDivider()
            val best = report.tasks.maxByOrNull { it.value }
            if (best != null && best.value > 0) Text(stringResource(R.string.insight_tasks, best.date.weekdayLabel(), best.value.toInt()), Modifier.padding(vertical = D.Small))
            if (report.habitCheckIns > 0 && report.opportunities > 0) Text(stringResource(R.string.insight_habits, report.habitPercent), Modifier.padding(vertical = D.Small))
            if (report.focusMinutes > 0) Text(stringResource(R.string.insight_focus, report.focusMinutes.toInt(), state.period.days.toInt()), Modifier.padding(vertical = D.Small))
            Text(stringResource(R.string.rhythm_encouragement), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
