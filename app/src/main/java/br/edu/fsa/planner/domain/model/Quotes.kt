package br.edu.fsa.planner.domain.model

import java.text.Normalizer
import java.time.*

enum class QuoteCategory { MOTIVATIONAL, REFLECTIVE, CALM, DISCIPLINE, RANDOM }
enum class QuoteRefreshMode { EVERY_OPEN, SIX_HOURS, DAILY, THREE_DAYS, WEEKLY, MANUAL }
enum class QuoteSource { API, FALLBACK, USER }
data class CadencyQuote(val id: String, val text: String, val author: String, val category: QuoteCategory,
    val rawCategory: String, val fetchedAt: Long, val isFavorite: Boolean = false, val source: QuoteSource)
data class QuotePreferences(val category: QuoteCategory = QuoteCategory.RANDOM, val refreshMode: QuoteRefreshMode = QuoteRefreshMode.DAILY,
    val currentId: String? = null, val lastRotationAt: Long = 0, val lastRemoteAttemptAt: Long = 0, val lastRemoteSuccessAt: Long = 0)

object QuoteRules {
    fun due(prefs: QuotePreferences, now: Long, onOpen: Boolean, zone: ZoneId): Boolean {
        if (prefs.currentId == null) return true
        if (prefs.refreshMode == QuoteRefreshMode.MANUAL) return false
        if (prefs.refreshMode == QuoteRefreshMode.EVERY_OPEN) return onOpen
        if (prefs.refreshMode == QuoteRefreshMode.DAILY) return Instant.ofEpochMilli(prefs.lastRotationAt).atZone(zone).toLocalDate() <
            Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val duration = when (prefs.refreshMode) { QuoteRefreshMode.SIX_HOURS -> Duration.ofHours(6); QuoteRefreshMode.THREE_DAYS -> Duration.ofDays(3); else -> Duration.ofDays(7) }
        return now - prefs.lastRotationAt >= duration.toMillis()
    }
    fun eligible(quote: CadencyQuote, category: QuoteCategory) = category == QuoteCategory.RANDOM || quote.category == category
    fun choose(quotes: List<CadencyQuote>, category: QuoteCategory, currentId: String?): CadencyQuote? {
        val eligible = quotes.filter { eligible(it, category) }.sortedBy { it.id }
        val primary = eligible.filter { it.source != QuoteSource.FALLBACK }.ifEmpty { eligible }
        if (primary.size > 1) return primary[(primary.indexOfFirst { it.id == currentId } + 1) % primary.size]
        return primary.firstOrNull { it.id != currentId } ?: eligible.firstOrNull { it.id != currentId } ?: primary.firstOrNull()
    }
    fun classify(category: String, text: String): QuoteCategory {
        fun normalized(value: String) = Normalizer.normalize(value.lowercase(java.util.Locale.ROOT), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")
        val raw = normalized(category)
        if (raw in setOf("paz", "saude", "felicidade")) return QuoteCategory.CALM
        val motivational = raw in setOf("motivacao", "motivacional", "inspiracao", "sonhos", "sucesso", "mudanca", "oportunidade", "trabalho")
        val reflective = raw in setOf("reflexao", "proverbios")
        if ((motivational || reflective) && listOf("constancia", "persist", "disciplina", "habito", "pratica", "esforco").any { normalized(text).contains(it) }) return QuoteCategory.DISCIPLINE
        return when { motivational -> QuoteCategory.MOTIVATIONAL; reflective -> QuoteCategory.REFLECTIVE; else -> QuoteCategory.RANDOM }
    }
}
