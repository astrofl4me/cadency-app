package br.edu.fsa.planner

import br.edu.fsa.planner.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class QuoteRulesTest {
    private val zone = ZoneId.of("America/Sao_Paulo")
    private val at = ZonedDateTime.of(2026, 10, 22, 12, 0, 0, 0, zone).toInstant().toEpochMilli()
    @Test fun refreshModesRespectBoundariesAndManualChoice() {
        fun prefs(mode: QuoteRefreshMode) = QuotePreferences(currentId = "saved", refreshMode = mode, lastRotationAt = at)
        assertFalse(QuoteRules.due(prefs(QuoteRefreshMode.EVERY_OPEN), at, false, zone))
        assertTrue(QuoteRules.due(prefs(QuoteRefreshMode.EVERY_OPEN), at, true, zone))
        assertFalse(QuoteRules.due(prefs(QuoteRefreshMode.SIX_HOURS), at + 6 * 3600000L - 1, false, zone))
        assertTrue(QuoteRules.due(prefs(QuoteRefreshMode.SIX_HOURS), at + 6 * 3600000L, false, zone))
        assertFalse(QuoteRules.due(prefs(QuoteRefreshMode.THREE_DAYS), at + 2 * 86400000L, false, zone))
        assertTrue(QuoteRules.due(prefs(QuoteRefreshMode.THREE_DAYS), at + 3 * 86400000L, false, zone))
        assertTrue(QuoteRules.due(prefs(QuoteRefreshMode.WEEKLY), at + 7 * 86400000L, false, zone))
        assertFalse(QuoteRules.due(prefs(QuoteRefreshMode.MANUAL), at + 30 * 86400000L, true, zone))
        val midnight = LocalDate.of(2026, 10, 23).atStartOfDay(zone).toInstant().toEpochMilli()
        assertTrue(QuoteRules.due(prefs(QuoteRefreshMode.DAILY).copy(lastRotationAt = midnight - 1000), midnight, false, zone))
    }
    @Test fun rotationVisitsAllCachedQuotesAndKeepsCategory() {
        val quotes = (1..3).map { CadencyQuote("$it", "Frase $it", "Teste", QuoteCategory.CALM, "Paz", at, source = QuoteSource.API) }
        val first = QuoteRules.choose(quotes, QuoteCategory.CALM, null)!!
        val second = QuoteRules.choose(quotes, QuoteCategory.CALM, first.id)!!
        val third = QuoteRules.choose(quotes, QuoteCategory.CALM, second.id)!!
        assertEquals(3, setOf(first.id, second.id, third.id).size)
        assertNull(QuoteRules.choose(quotes, QuoteCategory.DISCIPLINE, null))
    }
    @Test fun localMappingUsesRealCategoriesAndDoesNotInventDisciplineEndpoint() {
        assertEquals(QuoteCategory.CALM, QuoteRules.classify("Paz", "Com calma."))
        assertEquals(QuoteCategory.REFLECTIVE, QuoteRules.classify("Reflexão", "Escolher um caminho."))
        assertEquals(QuoteCategory.DISCIPLINE, QuoteRules.classify("Reflexão", "A constância ajuda."))
        assertEquals(QuoteCategory.MOTIVATIONAL, QuoteRules.classify("Motivação", "Um novo começo."))
        assertEquals(QuoteCategory.RANDOM, QuoteRules.classify("Bíblia", "Um texto da coleção."))
    }
}
