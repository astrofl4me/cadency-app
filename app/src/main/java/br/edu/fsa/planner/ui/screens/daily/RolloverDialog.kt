package br.edu.fsa.planner.ui.screens.daily

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.PlannerItem
import br.edu.fsa.planner.ui.components.PlannerDateDialog
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.viewmodel.RolloverState
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RolloverDialog(state: RolloverState, onSchedule: (PlannerItem, LocalDate) -> Unit,
                   onInbox: (PlannerItem) -> Unit, onDiscard: (PlannerItem) -> Unit, onAll: () -> Unit, onDismiss: () -> Unit) {
    var selected by remember { mutableStateOf<PlannerItem?>(null) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.rollover_title)) },
        text = { LazyColumn(Modifier.heightIn(max = D.ContentWidth), verticalArrangement = Arrangement.spacedBy(D.Large)) {
            item { Text(stringResource(R.string.rollover_hint)) }
            if (state.error) item { Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error) }
            if (state.items.isEmpty()) item { Text(stringResource(R.string.rollover_done)) }
            items(state.items, key = { it.id }) { item ->
                Text(item.title, style = MaterialTheme.typography.titleSmall)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(D.Small)) {
                    TextButton(onClick = { onSchedule(item, state.today) }) { Text(stringResource(R.string.today_label)) }
                    TextButton(onClick = { selected = item }) { Text(stringResource(R.string.rollover_choose_date)) }
                    TextButton(onClick = { onInbox(item) }) { Text(stringResource(R.string.rollover_inbox)) }
                    TextButton(onClick = { onDiscard(item) }) { Text(stringResource(R.string.rollover_discard)) }
                }
                HorizontalDivider()
            }
        } }, confirmButton = { TextButton(onClick = onAll, enabled = state.items.isNotEmpty()) { Text(stringResource(R.string.rollover_all)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.rollover_later)) } })
    selected?.let { item -> PlannerDateDialog(state.today, { selected = null }, { onSchedule(item, it) }) }
}
