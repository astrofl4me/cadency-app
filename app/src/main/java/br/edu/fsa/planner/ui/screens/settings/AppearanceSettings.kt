package br.edu.fsa.planner.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AppearanceSettings(preferences: PlannerPreferences, onTheme: (ThemeMode) -> Unit,
                       onPersonality: (PlannerPersonality) -> Unit, onAccent: (AccentColor) -> Unit) {
    var custom by rememberSaveable { mutableStateOf(false) }
    SectionTitle(stringResource(R.string.settings_appearance))
    Text(stringResource(R.string.personality_title), style = MaterialTheme.typography.titleSmall)
    PlannerPersonality.entries.forEach { personality ->
        Column(Modifier.fillMaxWidth().selectable(preferences.plannerPersonality == personality,
            role = Role.RadioButton, onClick = { onPersonality(personality) })) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(preferences.plannerPersonality == personality, null)
                Column {
                    Text(stringResource(personality.label()), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(personality.description()), style = MaterialTheme.typography.bodySmall)
                }
            }
            PlannerTheme(preferences.themeMode, personality, preferences.accentColor) {
                CadencySurface(Modifier.fillMaxWidth().padding(start = D.Touch, bottom = D.Small).clearAndSetSemantics { }, emphasized = true) {
                    Row(Modifier.padding(D.Medium), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(D.Small)) {
                        Column(Modifier.weight(1f)) {
                            Text(stringResource(R.string.preview_today), style = MaterialTheme.typography.titleLarge)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CadencyCheckbox(true, null)
                                Text(stringResource(R.string.preview_task), style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        CadencyButton({}, enabled = false) { Text(stringResource(R.string.preview_button)) }
                    }
                }
            }
        }
    }
    Text(stringResource(R.string.accent_title), style = MaterialTheme.typography.titleSmall)
    FlowRow(horizontalArrangement = Arrangement.spacedBy(D.Small)) {
        AccentPreset.entries.forEach { preset -> FilterChip(preferences.accentColor == preset.color, { onAccent(preset.color) },
            label = { Text(stringResource(preset.label())) }, leadingIcon = {
                Box(Modifier.size(D.Large).background(Color(0xFF000000L or preset.color.rgb.toLong()), CircleShape))
            }) }
    }
    OutlinedButton(onClick = { custom = true }) { Text(stringResource(R.string.accent_custom, preferences.accentColor.hex)) }
    Text(stringResource(R.string.device_theme), style = MaterialTheme.typography.titleSmall)
    ThemeMode.entries.forEach { mode ->
        Row(Modifier.fillMaxWidth().heightIn(min = D.Touch).selectable(preferences.themeMode == mode, role = Role.RadioButton,
            onClick = { onTheme(mode) }), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(preferences.themeMode == mode, null)
            Text(stringResource(mode.label()), Modifier.padding(start = D.Medium))
        }
    }
    if (custom) AccentPicker(preferences.accentColor, { custom = false }, { onAccent(it); custom = false })
}

@Composable
private fun AccentPicker(initial: AccentColor, onDismiss: () -> Unit, onSave: (AccentColor) -> Unit) {
    var hex by rememberSaveable { mutableStateOf(initial.hex) }
    val selected = AccentColor.parse(hex)
    val hsv = remember(hex) { FloatArray(3).also { android.graphics.Color.colorToHSV(0xFF000000.toInt() or (selected ?: initial).rgb, it) } }
    fun adjust(index: Int, value: Float) { val copy = hsv.copyOf(); copy[index] = value; hex = AccentColor(android.graphics.Color.HSVToColor(copy) and 0xFFFFFF).hex }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(stringResource(R.string.accent_picker)) }, text = {
        Column {
            Box(Modifier.fillMaxWidth().height(D.Hero).background(Color(0xFF000000L or (selected ?: initial).rgb.toLong()), MaterialTheme.shapes.medium))
            Text(stringResource(R.string.accent_hue)); Slider(hsv[0], { adjust(0, it) }, valueRange = 0f..360f)
            Text(stringResource(R.string.accent_saturation)); Slider(hsv[1], { adjust(1, it) })
            Text(stringResource(R.string.accent_brightness)); Slider(hsv[2], { adjust(2, it) })
            OutlinedTextField(hex, { hex = it.take(7) }, label = { Text(stringResource(R.string.accent_hex)) }, singleLine = true,
                isError = selected == null, supportingText = { if (selected == null) Text(stringResource(R.string.accent_invalid)) })
        }
    }, confirmButton = { TextButton(onClick = { selected?.let(onSave) }, enabled = selected != null) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}

private fun AccentPreset.label() = when (this) {
    AccentPreset.SAGE -> R.string.accent_sage; AccentPreset.TERRACOTTA -> R.string.accent_terracotta
    AccentPreset.LAVENDER -> R.string.accent_lavender; AccentPreset.BLUE -> R.string.accent_blue
    AccentPreset.ROSE -> R.string.accent_rose; AccentPreset.AMBER -> R.string.accent_amber; AccentPreset.GRAPHITE -> R.string.accent_graphite
}
