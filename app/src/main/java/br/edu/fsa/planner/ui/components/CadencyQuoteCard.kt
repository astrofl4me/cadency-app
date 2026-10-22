package br.edu.fsa.planner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.theme.LocalCadencyPersonality
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.viewmodel.QuoteState
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
fun CadencyQuoteCard(state: QuoteState, onNext: () -> Unit, onFavorite: (String) -> Unit) {
    val visual = LocalCadencyPersonality.current
    var expanded by rememberSaveable(visual.quietNavigation) { mutableStateOf(!visual.quietNavigation) }
    val quote = state.current
    var fullQuote by rememberSaveable(quote?.id) { mutableStateOf(false) }
    var overflows by remember(quote?.id) { mutableStateOf(false) }
    CadencySurface(Modifier.fillMaxWidth(), emphasized = true) {
        Column(Modifier.padding(D.Large).animateContentSize(tween(visual.motionMillis)), verticalArrangement = Arrangement.spacedBy(D.Small)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.quote_title), Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                if (visual.quietNavigation) TextButton(onClick = { expanded = !expanded }) { Text(stringResource(if (expanded) R.string.quote_hide else R.string.quote_read)) }
            }
            AnimatedVisibility(expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(D.Small)) {
                    if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
                    if (quote != null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(D.Medium)) {
                            if (visual.sectionMarker > D.None) Box(Modifier.width(visual.sectionMarker).height(D.Hero).background(MaterialTheme.colorScheme.primary))
                            Text(quote.text, style = visual.quoteStyle, maxLines = if (fullQuote) Int.MAX_VALUE else 3, overflow = TextOverflow.Ellipsis,
                                onTextLayout = { if (!fullQuote) overflows = it.hasVisualOverflow })
                        }
                        if (overflows || fullQuote) TextButton(onClick = { fullQuote = !fullQuote }) { Text(stringResource(if (fullQuote) R.string.quote_less else R.string.quote_more)) }
                        Text(quote.author.ifBlank { stringResource(R.string.quote_no_author) }, style = MaterialTheme.typography.bodySmall)
                        Text(quote.rawCategory.ifBlank { stringResource(quote.category.label()) }, style = MaterialTheme.typography.labelSmall)
                        val update = maxOf(quote.fetchedAt, state.preferences.lastRotationAt)
                        if (update > 0) Text(stringResource(R.string.quote_updated, Instant.ofEpochMilli(update).atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", PlannerLocale))), style = MaterialTheme.typography.labelSmall)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onNext) { Icon(Icons.Outlined.Refresh, null); Text(stringResource(R.string.quote_another)) }
                            IconButton(onClick = { onFavorite(quote.id) }) { Icon(if (quote.isFavorite) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                stringResource(if (quote.isFavorite) R.string.quote_unfavorite else R.string.quote_favorite)) }
                        }
                        Text(stringResource(when (quote.source) { QuoteSource.API -> R.string.quote_source_api; QuoteSource.USER -> R.string.quote_source_user; else -> R.string.quote_source_local }),
                            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (state.error) Text(stringResource(R.string.storage_error), color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
