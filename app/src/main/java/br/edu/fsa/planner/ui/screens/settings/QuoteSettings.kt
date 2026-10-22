package br.edu.fsa.planner.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.*

@Composable
fun QuoteSettings(prefs: QuotePreferences, onCategory: (QuoteCategory) -> Unit, onMode: (QuoteRefreshMode) -> Unit, onFavorites: () -> Unit) {
    SectionTitle(stringResource(R.string.quote_settings))
    PreferenceMenu(stringResource(R.string.quote_category), stringResource(prefs.category.label()), QuoteCategory.entries.map { stringResource(it.label()) },
        { onCategory(QuoteCategory.entries[it]) })
    PreferenceMenu(stringResource(R.string.quote_frequency), stringResource(prefs.refreshMode.label()), QuoteRefreshMode.entries.map { stringResource(it.label()) },
        { onMode(QuoteRefreshMode.entries[it]) })
    Text(stringResource(R.string.quote_frequency_hint), style = MaterialTheme.typography.bodySmall)
    CadencyOutlinedButton(onFavorites) { Text(stringResource(R.string.quote_favorites)) }
}

@Composable
private fun PreferenceMenu(title: String, value: String, choices: List<String>, onSelect: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Text(title, style = MaterialTheme.typography.titleSmall)
    Box {
        CadencyOutlinedButton(onClick = { open = true }) { Text(value) }
        DropdownMenu(open, { open = false }) { choices.forEachIndexed { index, choice ->
            DropdownMenuItem(text = { Text(choice) }, onClick = { onSelect(index); open = false })
        } }
    }
}
