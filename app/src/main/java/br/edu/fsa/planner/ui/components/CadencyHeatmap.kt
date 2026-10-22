package br.edu.fsa.planner.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.theme.LocalCadencyPersonality
import br.edu.fsa.planner.ui.theme.PlannerDimens as D

@Composable
fun CadencyHeatmap(days: List<HeatmapDay>, modifier: Modifier = Modifier, mini: Boolean = false) {
    if (days.isEmpty()) return
    val visual = LocalCadencyPersonality.current
    var selected by rememberSaveable(days.first().date.toString(), days.size) { mutableIntStateOf(days.indexOfLast { it.status != HeatmapStatus.FUTURE }.coerceAtLeast(0)) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(D.Small)) {
        if (mini) {
            Column(verticalArrangement = Arrangement.spacedBy(visual.heatmapGap)) {
                days.chunked(7).forEach { week -> Row(horizontalArrangement = Arrangement.spacedBy(visual.heatmapGap)) {
                    week.forEach { day -> HeatCell(day, false, true) {} }
                } }
            }
        } else {
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(visual.heatmapGap)) {
                Column(verticalArrangement = Arrangement.spacedBy(visual.heatmapGap)) {
                    java.time.DayOfWeek.entries.forEach { day ->
                        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                            Text(day.getDisplayName(java.time.format.TextStyle.NARROW, PlannerLocale), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                days.chunked(7).forEachIndexed { weekIndex, week ->
                    Column(verticalArrangement = Arrangement.spacedBy(visual.heatmapGap)) {
                        week.forEachIndexed { dayIndex, day -> HeatCell(day, selected == weekIndex * 7 + dayIndex, false) { selected = weekIndex * 7 + dayIndex } }
                    }
                }
            }
            val day = days[selected.coerceIn(days.indices)]
            Text(day.date.accessibilityLabel(), style = MaterialTheme.typography.titleSmall)
            Text(heatmapDescription(day), style = MaterialTheme.typography.bodySmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = { selected = (selected - 1).coerceAtLeast(0) }, enabled = selected > 0) { Text(stringResource(R.string.heatmap_previous)) }
                TextButton(onClick = { selected = (selected + 1).coerceAtMost(days.lastIndex) }, enabled = selected < days.lastIndex) { Text(stringResource(R.string.heatmap_next)) }
            }
            Text(stringResource(R.string.heatmap_legend), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun HeatCell(day: HeatmapDay, selected: Boolean, mini: Boolean, onClick: () -> Unit) {
    val visual = LocalCadencyPersonality.current
    val surface = MaterialTheme.colorScheme.surfaceContainer
    val fill = if (day.intensity > 0) lerp(surface, MaterialTheme.colorScheme.primary, day.intensity / 5f) else surface
    val description = day.date.accessibilityLabel() + ". " + heatmapDescription(day)
    val shape = RoundedCornerShape(visual.heatmapRadius)
    val cellSize = if (mini) 12.dp else 24.dp
    Box(Modifier.size(cellSize).background(fill, shape)
        .border(if (selected) 2.dp else 1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, shape)
        .then(if (mini) Modifier else Modifier.clickable(role = Role.Button, onClick = onClick))
        .semantics { contentDescription = description; this.selected = selected }, contentAlignment = Alignment.Center) {
        val mark = when (day.status) { HeatmapStatus.FUTURE -> ""; HeatmapStatus.NOT_SCHEDULED -> "–"; HeatmapStatus.FLEXIBLE -> "·"; HeatmapStatus.OPEN -> "○"; HeatmapStatus.RECORDED -> if (day.percent == 100) "✓" else "·" }
        Text(mark, style = MaterialTheme.typography.labelSmall,
            color = if (day.intensity >= 4) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun heatmapDescription(day: HeatmapDay): String = when (day.status) {
    HeatmapStatus.FUTURE -> stringResource(R.string.heatmap_future)
    HeatmapStatus.NOT_SCHEDULED -> stringResource(R.string.heatmap_not_planned)
    HeatmapStatus.FLEXIBLE -> stringResource(R.string.heatmap_flexible)
    else -> stringResource(R.string.heatmap_result, day.done, day.planned, day.percent)
}
