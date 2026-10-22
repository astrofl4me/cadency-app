package br.edu.fsa.planner.ui.screens.focus
import br.edu.fsa.planner.ui.components.CadencyButton as Button
import br.edu.fsa.planner.ui.components.CadencyOutlinedButton as OutlinedButton

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.CadencyTopBar
import br.edu.fsa.planner.ui.components.CadencySurface
import br.edu.fsa.planner.ui.components.FocusModeSelector
import br.edu.fsa.planner.ui.components.PomodoroConfiguration
import br.edu.fsa.planner.ui.theme.PlannerPaper
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.viewmodel.FocusState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FocusScreen(state: FocusState, onChoose: (Int) -> Unit, onCustom: () -> Unit, onCustomText: (String) -> Unit,
                onStart: () -> Unit, onPause: () -> Unit, onResume: () -> Unit, onFinish: () -> Unit,
                onRestart: () -> Unit, onCompleteTask: () -> Unit, onMode: (FocusMode) -> Unit = {},
                onPomodoro: (PomodoroConfig) -> Unit = {}, onMarkHabit: () -> Unit = {}, onDismissHabit: () -> Unit = {}, onBack: () -> Unit) {
    var confirm by rememberSaveable { mutableStateOf(false) }
    Scaffold(topBar = { CadencyTopBar(stringResource(R.string.focus_flow), onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).imePadding().padding(D.Page),
            verticalArrangement = Arrangement.spacedBy(D.Page), horizontalAlignment = Alignment.CenterHorizontally) {
            if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (state.error) Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error)
            val session = state.session
            Text((session?.title ?: state.title).ifBlank { stringResource(R.string.focus_free) }, style = MaterialTheme.typography.headlineMedium)
            if (session == null) {
                FocusModeSelector(state.mode, onMode)
                Text(stringResource(R.string.focus_hint), style = MaterialTheme.typography.bodyMedium)
                if (state.mode == FocusMode.FREE) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(D.Small)) {
                    FocusRules.presets.forEach { minutes -> FilterChip(!state.custom && state.minutes == minutes,
                        { onChoose(minutes) }, label = { Text(stringResource(R.string.duration_minutes, minutes)) }) }
                    FilterChip(state.custom, onCustom, label = { Text(stringResource(R.string.focus_custom)) })
                }
                if (state.custom) OutlinedTextField(state.customText, onCustomText, singleLine = true,
                    label = { Text(stringResource(R.string.focus_duration)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = state.invalid, supportingText = { if (state.invalid) Text(stringResource(R.string.focus_duration_error)) })
                } else PomodoroConfiguration(state.pomodoro, onPomodoro)
                Button(onClick = onStart, enabled = !state.busy && !state.loading, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.start_focus)) }
            } else {
                val active = session.status == FocusStatus.RUNNING || session.status == FocusStatus.PAUSED
                val remaining = FocusRules.remaining(session, state.now)
                if (session.mode == FocusMode.POMODORO) {
                    Text(stringResource(when (session.phase) { FocusPhase.FOCUS -> R.string.pomodoro_phase_focus; FocusPhase.SHORT_BREAK -> R.string.pomodoro_phase_short; FocusPhase.LONG_BREAK -> R.string.pomodoro_phase_long }),
                        style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.pomodoro_cycle_count, session.cycleIndex, session.pomodoro.cycles))
                    if (session.phase != FocusPhase.FOCUS) Text(stringResource(R.string.pomodoro_rest_hint))
                }
                CadencySurface(emphasized = true) {
                Box(Modifier.size(D.FocusTimer), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(progress = { 1f - remaining.toFloat() / FocusRules.durationMillis(session) },
                        modifier = Modifier.fillMaxSize(), trackColor = MaterialTheme.colorScheme.surfaceContainerHighest)
                    Text(FocusRules.timer(remaining), style = PlannerPaper.TimerText)
                }
                }
                Text(stringResource(when (session.status) {
                    FocusStatus.RUNNING -> R.string.focus_running; FocusStatus.PAUSED -> R.string.focus_paused
                    FocusStatus.FINISHED -> R.string.focus_finished; FocusStatus.STOPPED -> R.string.focus_recorded
                }), style = MaterialTheme.typography.titleMedium)
                if (active) {
                    Button(onClick = if (session.status == FocusStatus.RUNNING) onPause else onResume, enabled = !state.busy,
                        modifier = Modifier.fillMaxWidth()) { Text(stringResource(when {
                            session.status == FocusStatus.RUNNING -> R.string.focus_pause
                            session.awaitingPhaseStart && session.phase == FocusPhase.FOCUS -> R.string.pomodoro_start_cycle
                            session.awaitingPhaseStart -> R.string.pomodoro_start_break
                            else -> R.string.focus_resume
                        })) }
                    OutlinedButton(onClick = { confirm = true }, enabled = !state.busy) { Text(stringResource(R.string.focus_end)) }
                } else {
                    Text(if (session.elapsedMillis >= 60000) stringResource(R.string.focus_minutes_done, (session.elapsedMillis / 60000).toInt())
                        else stringResource(R.string.focus_seconds_done, (session.elapsedMillis / 1000).toInt()))
                    Button(onClick = onRestart, enabled = !state.busy) { Text(stringResource(R.string.focus_continue)) }
                    TextButton(onClick = onBack) { Text(stringResource(R.string.focus_end)) }
                }
                if (session.plannerItemId != null) TextButton(onClick = onCompleteTask, enabled = !state.busy) { Text(stringResource(R.string.focus_complete_task)) }
            }
        }
    }
    state.offerHabit?.let { name -> AlertDialog(onDismissRequest = onDismissHabit, title = { Text(stringResource(R.string.habit_focus_finished)) },
        text = { Text(stringResource(R.string.habit_focus_question, name)) },
        confirmButton = { TextButton(onClick = onMarkHabit) { Text(stringResource(R.string.habit_focus_mark)) } },
        dismissButton = { TextButton(onClick = onDismissHabit) { Text(stringResource(R.string.not_now)) } }) }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text(stringResource(R.string.focus_end_question)) },
        text = { Text(stringResource(R.string.focus_end_hint)) },
        confirmButton = { TextButton(onClick = { confirm = false; onFinish() }) { Text(stringResource(R.string.focus_end)) } },
        dismissButton = { TextButton(onClick = { confirm = false }) { Text(stringResource(R.string.focus_continue)) } })
}
