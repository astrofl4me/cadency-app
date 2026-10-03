package br.edu.fsa.planner.ui.screens.monthly

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.viewmodel.MonthlyUiState
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun MonthlyScreen(state: MonthlyUiState, onPrevious: () -> Unit, onNext: () -> Unit, onCurrent: () -> Unit,
                  onSelect: (LocalDate) -> Unit, onOpenDay: (LocalDate) -> Unit, onAdd: (LocalDate, ItemType) -> Unit,
                  onEdit: (PlannerItem) -> Unit, onToggle: (PlannerItem) -> Unit, onRetry: () -> Unit) {
    val selectedItems = PlannerDates.itemsIn(state.items, DateInterval(state.selectedDate, state.selectedDate))
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(Modifier.widthIn(max = D.ContentWidth).fillMaxSize(), contentPadding = PaddingValues(top = D.Page, bottom = D.Hero + D.Page)) {
            item {
                Column(Modifier.padding(horizontal = D.Page)) {
                    PlannerHeader(stringResource(R.string.monthly_eyebrow), state.month.titleLabel(), stringResource(R.string.monthly_subtitle))
                    DateNavigator(stringResource(R.string.previous_month), stringResource(R.string.next_month), stringResource(R.string.current_month),
                        onPrevious, onNext, onCurrent)
                    HorizontalDivider()
                }
                Spacer(Modifier.height(D.Large))
                CalendarGrid(state, onSelect)
                Column(Modifier.padding(D.Page)) {
                    Text(stringResource(R.string.calendar_hint), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(D.Medium))
                    if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                    if (state.hasError) DataError(onRetry)
                    HorizontalDivider()
                    Spacer(Modifier.height(D.Large))
                    SectionTitle(state.selectedDate.longLabel(), selectedItems.size) { onAdd(state.selectedDate, ItemType.TASK) }
                    TextButton(onClick = { onOpenDay(state.selectedDate) }) { Text(stringResource(R.string.open_daily)) }
                }
            }
            if (!state.loading && !state.hasError) {
                if (selectedItems.isEmpty()) item {
                    Column(Modifier.padding(horizontal = D.Page)) {
                        EmptyState(stringResource(R.string.month_day_empty), stringResource(R.string.add_first), { onAdd(state.selectedDate, ItemType.TASK) })
                    }
                }
                items(selectedItems, key = { it.id }) { item ->
                    Box(Modifier.padding(horizontal = D.Page)) { PlannerItemRow(item, { onToggle(item) }, { onEdit(item) }) }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(state: MonthlyUiState, onSelect: (LocalDate) -> Unit) {
    val shortDays = stringArrayResource(R.array.weekdays_short)
    val fullDays = stringArrayResource(R.array.weekdays_full)
    val grouped = state.items.groupBy { it.date }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val calendarWidth = maxOf(maxWidth, D.Touch * 7)
        Box(Modifier.horizontalScroll(rememberScrollState())) {
            Column(Modifier.width(calendarWidth)) {
                Row {
                    shortDays.forEachIndexed { index, label -> Box(Modifier.weight(1f).height(D.Touch), contentAlignment = Alignment.Center) {
                        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.semantics { contentDescription = fullDays[index] })
                    } }
                }
                PlannerDates.monthGrid(state.month).chunked(7).forEach { week ->
                    Row {
                        week.forEach { date ->
                            val dayItems = grouped[date].orEmpty()
                            val isSelected = date == state.selectedDate
                            val isToday = date == state.today
                            val description = stringResource(R.string.calendar_day_description,
                                "${date.weekdayLabel()}, ${date.longLabel()} de ${date.year}", dayItems.size,
                                if (isToday) stringResource(R.string.calendar_today_suffix) else "")
                            Surface(onClick = { onSelect(date) }, modifier = Modifier.weight(1f).height(D.CalendarCell).padding(D.Tiny)
                                .semantics { contentDescription = description; selected = isSelected },
                                color = when { isSelected -> MaterialTheme.colorScheme.primary; isToday -> MaterialTheme.colorScheme.primaryContainer; else -> MaterialTheme.colorScheme.surface },
                                shape = MaterialTheme.shapes.medium,
                                border = if (isToday) BorderStroke(D.Hairline, MaterialTheme.colorScheme.primary) else null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyMedium,
                                        color = when { isSelected -> MaterialTheme.colorScheme.onPrimary
                                            YearMonth.from(date) != state.month -> MaterialTheme.colorScheme.onSurfaceVariant
                                            else -> MaterialTheme.colorScheme.onSurface })
                                    Row(horizontalArrangement = Arrangement.spacedBy(D.Tiny)) {
                                        dayItems.map { it.category }.distinct().take(3).forEach { category ->
                                            Surface(color = if (isSelected) MaterialTheme.colorScheme.onPrimary else category.color(),
                                                shape = MaterialTheme.shapes.extraLarge, modifier = Modifier.size(D.Indicator)) {}
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
