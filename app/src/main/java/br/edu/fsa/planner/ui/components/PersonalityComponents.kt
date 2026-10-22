package br.edu.fsa.planner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import br.edu.fsa.planner.ui.theme.LocalCadencyPersonality
import br.edu.fsa.planner.ui.theme.PlannerDimens as D

@Composable
fun CadencySurface(modifier: Modifier = Modifier, emphasized: Boolean = false, content: @Composable () -> Unit) {
    val tokens = LocalCadencyPersonality.current
    Surface(modifier, shape = tokens.cardShape, shadowElevation = tokens.elevation,
        color = if (emphasized) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.surface,
        border = if (tokens.border > 0.dp) BorderStroke(tokens.border, MaterialTheme.colorScheme.outlineVariant) else null,
        content = content)
}

@Composable
fun CadencyButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
                  colors: ButtonColors = ButtonDefaults.buttonColors(), content: @Composable RowScope.() -> Unit) =
    Button(onClick, modifier, enabled, shape = LocalCadencyPersonality.current.controlShape, colors = colors, content = content)

@Composable
fun CadencyOutlinedButton(onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true,
                          colors: ButtonColors = ButtonDefaults.outlinedButtonColors(), content: @Composable RowScope.() -> Unit) =
    OutlinedButton(onClick, modifier, enabled, shape = LocalCadencyPersonality.current.controlShape, colors = colors, content = content)

@Composable
fun CadencyCheckbox(checked: Boolean, onCheckedChange: ((Boolean) -> Unit)?, modifier: Modifier = Modifier,
                    enabled: Boolean = true, colors: CheckboxColors = CheckboxDefaults.colors()) {
    if (!LocalCadencyPersonality.current.roundCheckbox) {
        Checkbox(checked, onCheckedChange, modifier, enabled, colors = colors)
    } else {
        Box(modifier.size(D.Touch).alpha(if (enabled) 1f else 0.38f).then(if (onCheckedChange != null) Modifier.toggleable(checked, enabled = enabled, role = Role.Checkbox,
            onValueChange = onCheckedChange) else Modifier), contentAlignment = Alignment.Center) {
            Surface(Modifier.size(D.Page), shape = CircleShape,
                color = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                border = BorderStroke(D.Hairline, MaterialTheme.colorScheme.primary)) {
                if (checked) Icon(Icons.Outlined.Check, null, Modifier.padding(D.Tiny), tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
