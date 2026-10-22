package br.edu.fsa.planner.ui.components

import br.edu.fsa.planner.R
import br.edu.fsa.planner.domain.model.*

fun QuoteCategory.label() = when (this) {
    QuoteCategory.MOTIVATIONAL -> R.string.quote_motivational; QuoteCategory.REFLECTIVE -> R.string.quote_reflective
    QuoteCategory.CALM -> R.string.quote_calm; QuoteCategory.DISCIPLINE -> R.string.quote_discipline; QuoteCategory.RANDOM -> R.string.quote_random
}
fun QuoteRefreshMode.label() = when (this) {
    QuoteRefreshMode.EVERY_OPEN -> R.string.quote_every_open; QuoteRefreshMode.SIX_HOURS -> R.string.quote_six_hours
    QuoteRefreshMode.DAILY -> R.string.quote_daily; QuoteRefreshMode.THREE_DAYS -> R.string.quote_three_days
    QuoteRefreshMode.WEEKLY -> R.string.quote_weekly; QuoteRefreshMode.MANUAL -> R.string.quote_manual
}
