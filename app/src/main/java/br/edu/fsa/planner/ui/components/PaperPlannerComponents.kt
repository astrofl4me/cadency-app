package br.edu.fsa.planner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.PlannerItem
import br.edu.fsa.planner.domain.model.SubtaskProgress
import br.edu.fsa.planner.ui.theme.PlannerPaper as Paper
import br.edu.fsa.planner.ui.theme.PlannerDimens as D

@Composable
fun PaperPageHeader(eyebrow: String, title: String, date: String) {
    Column(verticalArrangement = Arrangement.spacedBy(D.Small)) {
        Text(eyebrow, style = Paper.Label, color = Paper.Muted)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(D.Medium)) {
            Text(title, style = Paper.Title, color = Paper.Ink)
            Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text(date, style = Paper.Caption, color = Paper.Muted)
                Spacer(Modifier.height(D.Small))
                HorizontalDivider(color = Paper.Rule)
            }
        }
    }
}

@Composable
fun PaperColumns(primary: @Composable () -> Unit, secondary: @Composable () -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if (maxWidth >= D.PaperColumnsBreakpoint) {
            Row(horizontalArrangement = Arrangement.spacedBy(D.Page)) {
                Column(Modifier.weight(1.2f)) { primary() }
                Column(Modifier.weight(1f)) { secondary() }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(D.Page)) {
                Column { primary() }
                Column { secondary() }
            }
        }
    }
}

@Composable
fun PaperSectionHeading(title: String, onAdd: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().heightIn(min = D.Touch), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = Paper.Label, color = Paper.Muted, modifier = Modifier.weight(1f))
        if (onAdd != null) IconButton(onClick = onAdd) {
            Icon(Icons.Outlined.Add, stringResource(R.string.add_to_section, title), tint = Paper.Muted,
                modifier = Modifier.size(D.Icon))
        }
    }
}

@Composable
fun PaperRules(count: Int, modifier: Modifier = Modifier) {
    Column(modifier) {
        repeat(count) { Box(Modifier.fillMaxWidth().height(D.PaperRuleHeight), contentAlignment = Alignment.BottomCenter) {
            HorizontalDivider(color = Paper.Rule)
        } }
    }
}

@Composable
fun PaperItemRow(item: PlannerItem, onToggle: () -> Unit, onEdit: () -> Unit, progress: SubtaskProgress? = null) {
    val description = stringResource(if (item.isCompleted) R.string.reopen_item else R.string.complete_item, item.title)
    Row(Modifier.fillMaxWidth().heightIn(min = D.Touch)
        .clickable(onClickLabel = stringResource(R.string.edit_item), onClick = onEdit),
        verticalAlignment = Alignment.CenterVertically) {
        Checkbox(item.isCompleted, { onToggle() }, modifier = Modifier.semantics { contentDescription = description },
            colors = CheckboxDefaults.colors(checkedColor = Paper.Marker, uncheckedColor = Paper.Marker))
        Column(Modifier.weight(1f).padding(vertical = D.Small)) {
            Text(item.title, style = Paper.Body, color = if (item.isCompleted) Paper.Muted else Paper.Ink,
                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None)
            if (progress != null && progress.total > 0) Text(stringResource(R.string.subtask_progress, progress.done, progress.total),
                style = Paper.Caption, color = Paper.Muted)
            item.startTime?.let { Text(if (item.endTime != null) "$it – ${item.endTime}" else it.toString(),
                style = Paper.Caption, color = Paper.Muted) }
        }
    }
    HorizontalDivider(color = Paper.Rule)
}

@Composable
fun PaperItemSection(title: String, items: List<PlannerItem>, shaded: Boolean = false, minimumRows: Int = 3,
                     emptyMessage: String? = null, onAdd: () -> Unit, onToggle: (PlannerItem) -> Unit,
                     onEdit: (PlannerItem) -> Unit, progress: Map<Long, SubtaskProgress> = emptyMap()) {
    Column(Modifier.fillMaxWidth().clip(Paper.Shape)
        .background(if (shaded) Paper.Panel else Paper.Background).padding(horizontal = D.Medium, vertical = D.Tiny)) {
        PaperSectionHeading(title, onAdd)
        items.forEach { item -> PaperItemRow(item, { onToggle(item) }, { onEdit(item) }, progress[item.id]) }
        if (items.isEmpty()) {
            Row(Modifier.fillMaxWidth().heightIn(min = D.Touch).clickable(onClick = onAdd),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(D.Medium)) {
                Box(Modifier.size(D.PaperDot).background(Paper.Marker, CircleShape))
                Text(emptyMessage ?: stringResource(R.string.paper_add_here), style = Paper.Caption, color = Paper.Muted)
            }
        }
        PaperRules((minimumRows - items.size.coerceAtLeast(1)).coerceAtLeast(0))
        Spacer(Modifier.height(D.Medium))
    }
}

@Composable
fun PaperNoteArea(title: String, text: String, hint: String, onEdit: () -> Unit,
                  modifier: Modifier = Modifier, shaded: Boolean = false, ruled: Boolean = true) {
    val editDescription = stringResource(R.string.paper_edit_note, title)
    Column(modifier.fillMaxWidth().clip(Paper.Shape)
        .background(if (shaded) Paper.Panel else Paper.Background)
        .clickable(onClickLabel = editDescription, onClick = onEdit)
        .padding(horizontal = D.Medium, vertical = D.Small)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = Paper.Label, color = Paper.Muted, modifier = Modifier.weight(1f))
            IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, editDescription,
                tint = Paper.Muted, modifier = Modifier.size(D.Icon)) }
        }
        Text(text.ifBlank { hint }, style = Paper.Body, color = if (text.isBlank()) Paper.Muted else Paper.Ink)
        if (ruled) PaperRules(2)
        Spacer(Modifier.height(D.Small))
    }
}
