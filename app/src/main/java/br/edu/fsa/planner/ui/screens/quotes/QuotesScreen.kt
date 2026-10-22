package br.edu.fsa.planner.ui.screens.quotes

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
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.*
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.viewmodel.QuoteState
import kotlinx.coroutines.launch

@Composable
fun QuotesScreen(state: QuoteState, onFavorite: (String) -> Unit, onDelete: (String) -> Unit,
                  onAdd: suspend (String, String) -> Boolean, onBack: () -> Unit) {
    var add by rememberSaveable { mutableStateOf(false) }
    var delete by rememberSaveable { mutableStateOf<String?>(null) }
    val rows = state.quotes.filter { it.isFavorite || it.source == QuoteSource.USER }
    Scaffold(topBar = { CadencyTopBar(stringResource(R.string.quote_favorites), onBack) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(D.Page), verticalArrangement = Arrangement.spacedBy(D.Large)) {
            item { CadencyButton(onClick = { add = true }) { Text(stringResource(R.string.quote_add_own)) } }
            if (state.error) item { Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error) }
            if (rows.isEmpty()) item { EmptyState(stringResource(R.string.quote_favorites_empty)) }
            items(rows, key = { it.id }) { quote -> CadencySurface(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(D.Medium)) {
                    Text(quote.text, style = MaterialTheme.typography.bodyLarge)
                    Text(quote.author.ifBlank { stringResource(R.string.quote_no_author) }, style = MaterialTheme.typography.bodySmall)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        if (quote.source == QuoteSource.USER) IconButton(onClick = { delete = quote.id }) { Icon(Icons.Outlined.DeleteOutline, stringResource(R.string.quote_delete_own)) }
                        IconButton(onClick = { onFavorite(quote.id) }) { Icon(if (quote.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                            stringResource(if (quote.isFavorite) R.string.quote_unfavorite else R.string.quote_favorite)) }
                    }
                }
            } }
        }
    }
    if (add) OwnQuoteDialog(onDismiss = { add = false }, onAdd = onAdd)
    delete?.let { id -> AlertDialog(onDismissRequest = { delete = null }, title = { Text(stringResource(R.string.quote_delete_own)) },
        text = { Text(stringResource(R.string.quote_delete_question)) },
        confirmButton = { TextButton(onClick = { onDelete(id); delete = null }) { Text(stringResource(R.string.delete)) } },
        dismissButton = { TextButton(onClick = { delete = null }) { Text(stringResource(R.string.cancel)) } }) }
}

@Composable
private fun OwnQuoteDialog(onDismiss: () -> Unit, onAdd: suspend (String, String) -> Boolean) {
    var text by rememberSaveable { mutableStateOf("") }
    var author by rememberSaveable { mutableStateOf("") }
    var attempted by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text(stringResource(R.string.quote_add_own)) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(D.Small)) {
            OutlinedTextField(text, { text = it.take(1000) }, label = { Text(stringResource(R.string.quote_text)) }, minLines = 3, maxLines = 5,
                isError = attempted && text.isBlank(), supportingText = { if (attempted && text.isBlank()) Text(stringResource(R.string.quote_required)) })
            OutlinedTextField(author, { author = it.take(120) }, label = { Text(stringResource(R.string.quote_author)) }, singleLine = true)
            if (failed) Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { TextButton(onClick = {
        attempted = true
        if (text.isNotBlank()) { busy = true; scope.launch { if (onAdd(text, author)) onDismiss() else failed = true; busy = false } }
    }, enabled = !busy) { Text(stringResource(R.string.save)) } },
        dismissButton = { TextButton(onClick = onDismiss, enabled = !busy) { Text(stringResource(R.string.cancel)) } })
}
