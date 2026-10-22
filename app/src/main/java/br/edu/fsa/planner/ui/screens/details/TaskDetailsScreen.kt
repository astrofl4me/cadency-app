package br.edu.fsa.planner.ui.screens.details
import br.edu.fsa.planner.ui.components.CadencyButton as Button
import br.edu.fsa.planner.ui.components.CadencyOutlinedButton as OutlinedButton
import br.edu.fsa.planner.ui.components.CadencyCheckbox as Checkbox

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.viewmodel.TaskDetailsState

@Composable
fun TaskDetailsScreen(state: TaskDetailsState, draft: String, onDraft: (String) -> Unit, onAdd: () -> Unit,
                      onToggle: (Subtask) -> Unit, onRemove: (Subtask) -> Unit, onMove: (Subtask, Int) -> Unit,
                      onToggleParent: () -> Unit, onDismiss: () -> Unit, onEdit: () -> Unit, onBack: () -> Unit,
                      onFocus: (() -> Unit)? = null) {
    Scaffold(topBar = { CadencyTopBar(stringResource(R.string.task_details), onBack) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(D.Page), verticalArrangement = Arrangement.spacedBy(D.Large)) {
            if (state.loading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            if (state.error) item { Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error) }
            state.item?.let { item ->
                item {
                    Text(item.title, style = MaterialTheme.typography.headlineLarge)
                    Text(item.date.accessibilityLabel(), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.priority_status, stringResource(item.priority.label())), style = MaterialTheme.typography.labelLarge)
                    if (item.description.isNotBlank()) Text(item.description)
                    Row(horizontalArrangement = Arrangement.spacedBy(D.Medium)) {
                        OutlinedButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, stringResource(R.string.edit_item)); Text(stringResource(R.string.edit_item)) }
                        Button(onClick = onToggleParent) { Text(stringResource(if (item.isCompleted) R.string.task_reopen else R.string.completed)) }
                    }
                    if (onFocus != null) Button(onClick = onFocus) { Text(stringResource(R.string.start_focus)) }
                }
                item {
                    SectionTitle(stringResource(R.string.subtasks_title))
                    Text(countedString(R.plurals.subtask_progress, state.steps.size, state.steps.count { it.isCompleted }, state.steps.size), style = MaterialTheme.typography.bodySmall)
                    if (state.steps.isEmpty()) Text(stringResource(R.string.subtasks_hint), style = MaterialTheme.typography.bodyMedium)
                }
                items(state.steps, key = { it.id }) { step ->
                    var menu by rememberSaveable { mutableStateOf(false) }
                    val stepLabel = stringResource(R.string.subtask_check, step.title)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(step.isCompleted, { onToggle(step) }, Modifier.semantics { contentDescription = stepLabel })
                        Text(step.title, Modifier.weight(1f))
                        Box {
                            IconButton(onClick = { menu = true }) { Icon(Icons.Outlined.MoreVert, stringResource(R.string.subtask_options, step.title)) }
                            DropdownMenu(menu, { menu = false }) {
                                DropdownMenuItem(text = { Text(stringResource(R.string.move_up)) }, onClick = { onMove(step, -1); menu = false })
                                DropdownMenuItem(text = { Text(stringResource(R.string.move_down)) }, onClick = { onMove(step, 1); menu = false })
                                DropdownMenuItem(text = { Text(stringResource(R.string.delete)) }, onClick = { onRemove(step); menu = false })
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(draft, onDraft, label = { Text(stringResource(R.string.subtask_next)) },
                        modifier = Modifier.fillMaxWidth(), isError = state.invalidStep,
                        supportingText = { if (state.invalidStep) Text(stringResource(R.string.title_required)) },
                        trailingIcon = { IconButton(onClick = onAdd) { Icon(Icons.Outlined.Add, stringResource(R.string.subtask_add)) } })
                    Spacer(Modifier.height(D.Section))
                }
            }
        }
    }
    if (state.offerCompletion) AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.subtasks_finished)) },
        text = { Text(stringResource(R.string.subtasks_complete_parent)) },
        confirmButton = { TextButton(onClick = onToggleParent) { Text(stringResource(R.string.completed)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.not_now)) } })
}
