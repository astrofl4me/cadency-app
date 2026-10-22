package br.edu.fsa.planner.ui.screens.now

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.PlannerItem
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.viewmodel.NowState
import java.time.format.DateTimeFormatter

@Composable
fun NowModeScreen(state: NowState, onComplete: () -> Unit, onSkip: () -> Unit, onChoose: () -> Unit,
                  onReset: () -> Unit, onBack: () -> Unit, onFocus: ((PlannerItem) -> Unit)? = null) {
    Scaffold(topBar = { CadencyTopBar(stringResource(R.string.now_mode), onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(D.Page),
            verticalArrangement = Arrangement.spacedBy(D.Section)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.now_eyebrow), style = MaterialTheme.typography.labelLarge)
                Text(state.now.format(DateTimeFormatter.ofPattern("HH:mm")), style = MaterialTheme.typography.headlineMedium)
            }
            Text(stringResource(when (state.now.hour) { in 5..11 -> R.string.good_morning; in 12..17 -> R.string.good_afternoon; else -> R.string.good_evening }),
                style = MaterialTheme.typography.titleMedium)
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (state.error) Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error)
            val current = state.candidates.firstOrNull()
            if (current == null && !state.loading) {
                Text(stringResource(if (state.hasSkipped) R.string.now_breath else R.string.now_clear), style = MaterialTheme.typography.headlineLarge)
                Button(onClick = onChoose) { Text(stringResource(R.string.now_choose)) }
                if (state.hasSkipped) TextButton(onClick = onReset) { Text(stringResource(R.string.now_reset)) }
            } else if (current != null) {
                Text(current.title, style = MaterialTheme.typography.headlineLarge)
                current.startTime?.let { Text(if (current.endTime != null) "$it – ${current.endTime}" else it.toString(), style = MaterialTheme.typography.titleLarge) }
                Text(stringResource(R.string.priority_status, stringResource(current.priority.label())))
                if (onFocus != null) Button(onClick = { onFocus(current) }, modifier = Modifier.fillMaxWidth().heightIn(min = D.Touch)) { Text(stringResource(R.string.start_focus)) }
                OutlinedButton(onClick = onComplete, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.completed)) }
                TextButton(onClick = onSkip, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text(stringResource(R.string.now_skip)) }
                state.candidates.getOrNull(1)?.let { next ->
                    HorizontalDivider()
                    Text(stringResource(R.string.now_next), style = MaterialTheme.typography.labelLarge)
                    Text(next.title, style = MaterialTheme.typography.titleMedium)
                    next.startTime?.let { Text(it.toString(), style = MaterialTheme.typography.bodyMedium) }
                }
            }
        }
    }
}
