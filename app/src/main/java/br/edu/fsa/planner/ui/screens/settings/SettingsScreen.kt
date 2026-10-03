package br.edu.fsa.planner.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.edu.fsa.planner.BuildConfig
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.User
import br.edu.fsa.planner.ui.components.PlannerHeader
import br.edu.fsa.planner.ui.components.SectionTitle
import br.edu.fsa.planner.ui.theme.PlannerDimens as D

@Composable
fun SettingsScreen(user: User, busy: Boolean, hasError: Boolean, onLogout: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(Modifier.widthIn(max = D.ContentWidth).fillMaxWidth().verticalScroll(rememberScrollState()).padding(D.Page),
            verticalArrangement = Arrangement.spacedBy(D.Large)) {
            PlannerHeader(stringResource(R.string.profile_eyebrow), stringResource(R.string.profile_title), stringResource(R.string.profile_subtitle))
            Spacer(Modifier.height(D.Small))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(D.Large)) {
                Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.extraLarge) {
                    Box(Modifier.size(D.Brand), contentAlignment = Alignment.Center) {
                        Text(user.displayName.take(1), style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(D.Tiny)) {
                    Text(user.displayName, style = MaterialTheme.typography.titleMedium)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.demo_account), style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider()
            SectionTitle(stringResource(R.string.your_planner))
            Row(horizontalArrangement = Arrangement.spacedBy(D.Large)) {
                Icon(Icons.Outlined.Palette, contentDescription = null)
                Column(verticalArrangement = Arrangement.spacedBy(D.Tiny)) {
                    Text(stringResource(R.string.theme_title), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.theme_current), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.theme_future), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(D.Large)) {
                Icon(Icons.Outlined.Tune, contentDescription = null)
                Column(verticalArrangement = Arrangement.spacedBy(D.Tiny)) {
                    Text(stringResource(R.string.personalization_title), style = MaterialTheme.typography.titleSmall)
                    Text(stringResource(R.string.personalization_future), style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider()
            SectionTitle(stringResource(R.string.about))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.app_version, BuildConfig.VERSION_NAME), style = MaterialTheme.typography.bodyMedium)
            Text(stringResource(R.string.about_local), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(D.Large))
            if (hasError) Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error)
            OutlinedButton(onClick = onLogout, enabled = !busy, modifier = Modifier.fillMaxWidth().heightIn(min = D.Touch)) {
                Text(stringResource(if (busy) R.string.entering else R.string.sign_out))
            }
        }
    }
}
