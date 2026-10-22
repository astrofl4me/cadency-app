package br.edu.fsa.planner.ui.screens.settings
import br.edu.fsa.planner.ui.components.CadencyOutlinedButton as OutlinedButton

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import br.edu.fsa.planner.BuildConfig
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.cadencyColors
import br.edu.fsa.planner.ui.theme.PlannerDimens as D

@Composable
fun SettingsScreen(user: User, busy: Boolean, hasError: Boolean, onLogout: () -> Unit,
                   preferences: PlannerPreferences, onTheme: (ThemeMode) -> Unit,
                   onMood: (PlannerPersonality) -> Unit, onRollover: (Boolean) -> Unit, onAccent: (AccentColor) -> Unit = {}, extraSettings: @Composable () -> Unit = {}) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = D.ContentWidth).fillMaxWidth().verticalScroll(rememberScrollState()).padding(D.Page),
            verticalArrangement = Arrangement.spacedBy(D.Large)) {
            PlannerHeader(stringResource(R.string.profile_eyebrow), stringResource(R.string.profile_title), stringResource(R.string.cadency_tagline))
            SectionTitle(stringResource(R.string.settings_account))
            Text(user.displayName, style = MaterialTheme.typography.titleMedium)
            Text(user.email, style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.demo_account), style = MaterialTheme.typography.bodySmall)
            HorizontalDivider()
            AppearanceSettings(preferences, onTheme, onMood, onAccent)
            HorizontalDivider()
            SectionTitle(stringResource(R.string.settings_planning))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.gentle_rollover), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.rollover_preference_hint), style = MaterialTheme.typography.bodySmall)
                }
                Switch(preferences.gentleRolloverEnabled, onRollover)
            }
            HorizontalDivider()
            extraSettings()
            SectionTitle(stringResource(R.string.about))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.app_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.about_local), style = MaterialTheme.typography.bodySmall)
            if (hasError || preferences.hasError) Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onLogout, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = D.Touch)) {
                Text(stringResource(if (busy) R.string.entering else R.string.sign_out))
            }
        }
    }
}
