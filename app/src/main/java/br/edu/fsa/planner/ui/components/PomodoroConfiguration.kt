package br.edu.fsa.planner.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D

@Composable
fun FocusModeSelector(mode: FocusMode, onMode: (FocusMode) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(D.Small)) {
        FocusMode.entries.forEach { option -> FilterChip(mode == option, { onMode(option) },
            label = { Text(stringResource(if (option == FocusMode.FREE) R.string.focus_free_mode else R.string.pomodoro)) }) }
    }
}

@Composable
fun PomodoroConfiguration(config: PomodoroConfig, onSave: (PomodoroConfig) -> Unit) {
    var showCustom by rememberSaveable { mutableStateOf(false) }
    Row(horizontalArrangement = Arrangement.spacedBy(D.Small)) {
        listOf(25 to 5, 50 to 10).forEach { (work, rest) -> FilterChip(config.focusMinutes == work && config.shortBreakMinutes == rest,
            { onSave(PomodoroConfig(work, rest)) }, label = { Text(stringResource(R.string.pomodoro_preset, work, rest)) }) }
    }
    Text(stringResource(R.string.pomodoro_summary, config.focusMinutes, config.shortBreakMinutes, config.cycles, config.longBreakMinutes),
        style = MaterialTheme.typography.bodySmall)
    TextButton(onClick = { showCustom = true }) { Text(stringResource(R.string.focus_custom)) }
    if (showCustom) PomodoroEditor(config, { showCustom = false }, { onSave(it); showCustom = false })
}

@Composable
private fun PomodoroEditor(initial: PomodoroConfig, onDismiss: () -> Unit, onSave: (PomodoroConfig) -> Unit) {
    var work by rememberSaveable { mutableStateOf(initial.focusMinutes.toString()) }
    var rest by rememberSaveable { mutableStateOf(initial.shortBreakMinutes.toString()) }
    var cycles by rememberSaveable { mutableStateOf(initial.cycles.toString()) }
    var long by rememberSaveable { mutableStateOf(initial.longBreakMinutes.toString()) }
    val value = PomodoroConfig(work.toIntOrNull() ?: 0, rest.toIntOrNull() ?: 0, cycles.toIntOrNull() ?: 0, long.toIntOrNull() ?: 0)
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.pomodoro_custom)) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(D.Small)) {
            listOf(Triple(R.string.pomodoro_work, work) { text: String -> work = text.take(3) },
                Triple(R.string.pomodoro_short, rest) { text: String -> rest = text.take(3) },
                Triple(R.string.pomodoro_cycles, cycles) { text: String -> cycles = text.take(2) },
                Triple(R.string.pomodoro_long, long) { text: String -> long = text.take(3) }).forEach { (label, text, update) ->
                OutlinedTextField(text, update, label = { Text(stringResource(label)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
            }
            if (!value.valid) Text(stringResource(R.string.pomodoro_invalid), color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { TextButton(onClick = { onSave(value) }, enabled = value.valid) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}
