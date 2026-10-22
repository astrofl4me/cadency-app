package br.edu.fsa.planner.ui.screens.daily

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.PlannerItem
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import java.time.LocalDate

@Composable
fun InboxScreen(items: List<PlannerItem>, onOpen: (PlannerItem) -> Unit, onSchedule: (PlannerItem, LocalDate) -> Unit, onBack: () -> Unit) {
    var selected by remember { mutableStateOf<PlannerItem?>(null) }
    Scaffold(topBar = { CadencyTopBar(stringResource(R.string.inbox_title), onBack) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(D.Page), verticalArrangement = Arrangement.spacedBy(D.Large)) {
            item { Text(stringResource(R.string.inbox_hint), style = MaterialTheme.typography.bodyMedium) }
            if (items.isEmpty()) item { EmptyState(stringResource(R.string.inbox_empty)) }
            items(items, key = { it.id }) { item ->
                Text(item.title, Modifier.fillMaxWidth().clickable { onOpen(item) }.padding(vertical = D.Medium), style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(D.Small)) {
                    OutlinedButton(onClick = { onSchedule(item, LocalDate.now()) }) { Text(stringResource(R.string.inbox_today)) }
                    TextButton(onClick = { selected = item }) { Text(stringResource(R.string.rollover_choose_date)) }
                }
                HorizontalDivider()
            }
        }
    }
    selected?.let { item -> PlannerDateDialog(LocalDate.now(), { selected = null }, { onSchedule(item, it) }) }
}
