package br.edu.fsa.planner.ui.screens.progress

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.DailyCount
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D

@Composable
fun RhythmChart(title: String, values: List<DailyCount>) {
    if (values.isEmpty()) return
    var selected by rememberSaveable(values.first().date.toString(), values.size) { mutableIntStateOf(values.lastIndex) }
    val current = values[selected.coerceIn(values.indices)]
    val color = MaterialTheme.colorScheme.primary
    val baseline = MaterialTheme.colorScheme.outlineVariant
    val description = stringResource(R.string.chart_description, title,
        values.joinToString("; ") { "${it.date.shortLabel()}: ${it.value}" })
    Column(verticalArrangement = Arrangement.spacedBy(D.Medium)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        Text(stringResource(R.string.chart_value, current.date.accessibilityLabel(), current.value.toInt()), style = MaterialTheme.typography.bodySmall)
        Canvas(Modifier.fillMaxWidth().height(D.ChartHeight).semantics { contentDescription = description }
            .pointerInput(values) { detectTapGestures { position -> selected = (position.x / size.width * values.size).toInt().coerceIn(values.indices) } }) {
            val maximum = values.maxOf { it.value }.coerceAtLeast(1)
            val slot = size.width / values.size
            drawLine(baseline, Offset(0f, size.height), Offset(size.width, size.height))
            values.forEachIndexed { index, entry ->
                val height = size.height * (entry.value.toFloat() / maximum)
                drawRoundRect(color.copy(alpha = if (index == selected) 1f else 0.5f),
                    Offset(slot * index + slot * 0.18f, size.height - height), Size(slot * 0.64f, height), CornerRadius(D.Tiny.toPx()))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (values.size == 7) values.forEach { entry -> Text(entry.date.weekdayLabel().take(3), style = MaterialTheme.typography.labelSmall) }
            else { Text(values.first().date.shortLabel(), style = MaterialTheme.typography.labelSmall); Text(values.last().date.shortLabel(), style = MaterialTheme.typography.labelSmall) }
        }
    }
}
