package br.edu.fsa.planner.ui.screens.editor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.viewmodel.EditorUiState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(state: EditorUiState, onUpdate: ((PlannerForm) -> PlannerForm) -> Unit,
                 onSave: () -> Unit, onDelete: () -> Unit, onBack: () -> Unit, onRetry: () -> Unit) {
    var showDate by rememberSaveable { mutableStateOf(false) }
    var showDetails by rememberSaveable { mutableStateOf(state.isEditing) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.errors) {
        if (state.errors.description || state.errors.startTime || state.errors.endTime || state.errors.timeOrder) showDetails = true
    }
    val form = state.form
    val date = runCatching { LocalDate.parse(form.date) }.getOrDefault(LocalDate.now())
    Scaffold(topBar = {
        TopAppBar(title = { Text(stringResource(if (state.isEditing) R.string.edit_item else R.string.new_item)) },
            navigationIcon = { IconButton(onClick = onBack, enabled = !state.busy) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back))
            } }, actions = {
                if (state.isEditing && !state.missingItem) IconButton(onClick = { confirmDelete = true }, enabled = !state.busy && !state.loading) {
                    Icon(Icons.Outlined.DeleteOutline, stringResource(R.string.delete_item))
                }
            })
    }, bottomBar = {
        Surface {
            Button(onClick = onSave, enabled = !state.busy && !state.loading && !state.missingItem,
                modifier = Modifier.navigationBarsPadding().imePadding().padding(D.Large).fillMaxWidth().heightIn(min = D.Touch)) {
                Text(stringResource(if (state.busy) R.string.saving else R.string.save_item))
            }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.TopCenter) {
            if (state.loading) CircularProgressIndicator(Modifier.padding(D.Page))
            else if (state.missingItem) Text(stringResource(R.string.item_missing), Modifier.padding(D.Page))
            else Column(Modifier.widthIn(max = D.ContentWidth).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(D.Page), verticalArrangement = Arrangement.spacedBy(D.Large)) {
                Text(stringResource(R.string.editor_intro), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(form.title, { value -> onUpdate { it.copy(title = value) } }, Modifier.fillMaxWidth(),
                    enabled = !state.busy, label = { Text(stringResource(R.string.item_title)) }, singleLine = true,
                    isError = state.errors.title != null, keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    supportingText = { state.errors.title?.let { Text(stringResource(if (it == TitleError.REQUIRED) R.string.title_required else R.string.title_long)) } })
                OutlinedButton(onClick = { showDate = true }, enabled = !state.busy,
                    modifier = Modifier.fillMaxWidth().heightIn(min = D.Touch)) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.width(D.Small))
                    Text(stringResource(R.string.item_date_value, date.longLabel(), date.year.toString()))
                }
                if (state.errors.date) Text(stringResource(R.string.date_error), color = MaterialTheme.colorScheme.error)
                Text(stringResource(R.string.item_type), style = MaterialTheme.typography.titleSmall)
                EnumSelector(ItemType.entries, form.type, { it.label() }, { value -> onUpdate { it.copy(type = value) } }, !state.busy)
                TextButton(onClick = { showDetails = !showDetails }) {
                    Text(stringResource(if (showDetails) R.string.less_details else R.string.more_details))
                    Icon(Icons.Outlined.ExpandMore, contentDescription = null)
                }
                AnimatedVisibility(showDetails) {
                    Column(verticalArrangement = Arrangement.spacedBy(D.Large)) {
                        OutlinedTextField(form.description, { value -> onUpdate { it.copy(description = value) } }, Modifier.fillMaxWidth(),
                            enabled = !state.busy, label = { Text(stringResource(R.string.description)) }, minLines = 2, maxLines = 5,
                            isError = state.errors.description,
                            supportingText = { if (state.errors.description) Text(stringResource(R.string.description_long)) })
                        Text(stringResource(R.string.optional_time), style = MaterialTheme.typography.titleSmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(D.Medium)) {
                            OutlinedTextField(form.startTime, { value -> onUpdate { it.copy(startTime = value) } }, Modifier.weight(1f),
                                enabled = !state.busy, label = { Text(stringResource(R.string.start_time)) },
                                placeholder = { Text(stringResource(R.string.time_hint)) }, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), isError = state.errors.startTime,
                                supportingText = { if (state.errors.startTime) Text(stringResource(R.string.start_time_error)) })
                            OutlinedTextField(form.endTime, { value -> onUpdate { it.copy(endTime = value) } }, Modifier.weight(1f),
                                enabled = !state.busy, label = { Text(stringResource(R.string.end_time)) },
                                placeholder = { Text(stringResource(R.string.time_hint)) }, singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii), isError = state.errors.endTime || state.errors.timeOrder,
                                supportingText = { if (state.errors.endTime || state.errors.timeOrder) Text(stringResource(if (state.errors.timeOrder) R.string.time_order_error else R.string.time_error)) })
                        }
                        Text(stringResource(R.string.item_priority), style = MaterialTheme.typography.titleSmall)
                        EnumSelector(Priority.entries, form.priority, { it.label() }, { value -> onUpdate { it.copy(priority = value) } }, !state.busy)
                        Text(stringResource(R.string.item_category), style = MaterialTheme.typography.titleSmall)
                        EnumSelector(Category.entries, form.category, { it.label() }, { value -> onUpdate { it.copy(category = value) } }, !state.busy)
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.completed), Modifier.weight(1f))
                            Switch(form.isCompleted, { value -> onUpdate { it.copy(isCompleted = value) } }, enabled = !state.busy)
                        }
                    }
                }
                if (state.storageError) {
                    Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error)
                    if (state.isEditing) TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
                }
            }
        }
    }
    if (showDate) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(onDismissRequest = { showDate = false }, confirmButton = {
            TextButton(onClick = {
                picker.selectedDateMillis?.let { timestamp -> onUpdate { it.copy(date = Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.UTC).toLocalDate().toString()) } }
                showDate = false
            }, enabled = picker.selectedDateMillis != null) { Text(stringResource(R.string.choose)) }
        }, dismissButton = { TextButton(onClick = { showDate = false }) { Text(stringResource(R.string.cancel)) } }) {
            DatePicker(picker)
        }
    }
    if (confirmDelete) AlertDialog(onDismissRequest = { confirmDelete = false },
        title = { Text(stringResource(R.string.delete_title)) }, text = { Text(stringResource(R.string.delete_message, form.title)) },
        confirmButton = { TextButton(onClick = { confirmDelete = false; onDelete() }) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(stringResource(R.string.cancel)) } })
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> EnumSelector(values: List<T>, selected: T, label: (T) -> Int, onSelect: (T) -> Unit, enabled: Boolean) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(D.Small)) {
        values.forEach { value -> FilterChip(selected = value == selected, onClick = { onSelect(value) }, enabled = enabled,
            label = { Text(stringResource(label(value))) }, modifier = Modifier.heightIn(min = D.Touch)) }
    }
}
