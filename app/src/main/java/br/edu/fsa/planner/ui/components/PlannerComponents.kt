package br.edu.fsa.planner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.res.stringResource
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D

@Composable
fun PlannerHeader(eyebrow: String, title: String, subtitle: String) {
    Text(eyebrow, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
    Spacer(Modifier.height(D.Small))
    Text(title, style = MaterialTheme.typography.headlineLarge)
    Spacer(Modifier.height(D.Tiny))
    Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun DateNavigator(previousDescription: String, nextDescription: String, currentLabel: String,
                  onPrevious: () -> Unit, onNext: () -> Unit, onCurrent: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        IconButton(onClick = onPrevious, modifier = Modifier.size(D.Touch)) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, previousDescription)
        }
        TextButton(onClick = onCurrent, modifier = Modifier.heightIn(min = D.Touch)) { Text(currentLabel) }
        IconButton(onClick = onNext, modifier = Modifier.size(D.Touch)) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, nextDescription)
        }
    }
}

@Composable
fun SectionTitle(title: String, count: Int? = null, onAdd: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().heightIn(min = D.Touch), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        if (count != null && count > 0) Text(count.toString(), style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (onAdd != null) IconButton(onClick = onAdd) { Icon(Icons.Outlined.Add, stringResource(R.string.add_to_section, title)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlannerItemRow(item: PlannerItem, onToggle: () -> Unit, onEdit: () -> Unit, compact: Boolean = false) {
    val checkboxDescription = stringResource(if (item.isCompleted) R.string.reopen_item else R.string.complete_item, item.title)
    Row(Modifier.fillMaxWidth().clickable(onClickLabel = stringResource(R.string.edit_item), onClick = onEdit)
        .padding(vertical = D.Small).heightIn(min = D.Touch), verticalAlignment = Alignment.Top) {
        Checkbox(item.isCompleted, { onToggle() }, modifier = Modifier.semantics { contentDescription = checkboxDescription })
        Column(Modifier.weight(1f).padding(top = D.Small), verticalArrangement = Arrangement.spacedBy(D.Tiny)) {
            Text(item.title, style = MaterialTheme.typography.bodyLarge,
                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = if (item.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(D.Small)) {
                item.startTime?.let { Text(if (item.endTime != null) "$it – ${item.endTime}" else it.toString(),
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Text(stringResource(item.category.label()), style = MaterialTheme.typography.labelMedium, color = item.category.color())
                if (item.type == ItemType.EVENT) Text(stringResource(R.string.type_event), style = MaterialTheme.typography.labelMedium)
                if (item.priority == Priority.HIGH || item.type == ItemType.PRIORITY) Text(stringResource(R.string.high_priority_label),
                    style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
            }
            if (!compact && item.description.isNotBlank()) Text(item.description, maxLines = 2,
                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
fun EmptyState(message: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(vertical = D.Medium), verticalArrangement = Arrangement.spacedBy(D.Small)) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        if (action != null && onAction != null) OutlinedButton(onClick = onAction) { Text(action) }
    }
}

@Composable
fun DataError(onRetry: () -> Unit) {
    EmptyState(stringResource(R.string.storage_error), stringResource(R.string.retry), onRetry)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoodSelector(selected: Mood?, onSelect: (Mood) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(D.Small)) {
        Mood.entries.forEach { mood -> FilterChip(mood == selected, { onSelect(mood) },
            label = { Text(stringResource(mood.label())) }, modifier = Modifier.heightIn(min = D.Touch)) }
    }
}

@Composable
fun NoteDialog(title: String, initialText: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf(initialText) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = {
        OutlinedTextField(text, { if (it.length <= 2000) text = it }, modifier = Modifier.fillMaxWidth(),
            minLines = 3, maxLines = 6, label = { Text(stringResource(R.string.note_text)) })
    }, confirmButton = { TextButton(onClick = { onSave(text); onDismiss() }) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}
