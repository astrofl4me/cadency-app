package br.edu.fsa.planner

import android.graphics.Bitmap
import androidx.compose.ui.semantics.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.components.accessibilityLabel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class RefinementFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val container get() = ApplicationProvider.getApplicationContext<PlannerApplication>().container
    private fun waitFor(text: String) { try {
        compose.waitUntil(15000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    } catch (failure: Throwable) { throw AssertionError("Esperando texto: $text", failure) } }
    private fun click(text: String) {
        waitFor(text)
        val dialogButton = compose.onAllNodes(hasText(text) and hasAnyAncestor(isDialog()))
        val node = if (dialogButton.fetchSemanticsNodes().isNotEmpty()) dialogButton.onFirst() else compose.onNodeWithText(text)
        try { node.performScrollTo() } catch (failure: AssertionError) { if (failure.message?.contains("no parent layout") != true) throw failure }
        node.performSemanticsAction(SemanticsActions.OnClick) { it() }
    }
    private fun back() = compose.onNodeWithContentDescription("Voltar").performSemanticsAction(SemanticsActions.OnClick) { it() }
    private fun tab(text: String) = compose.onNode(hasText(text) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).performClick()
    private fun login() {
        compose.waitUntil(15000) { compose.onAllNodesWithText("Experimentar com conta demo").fetchSemanticsNodes().isNotEmpty() || compose.onAllNodesWithText("MEU DIA").fetchSemanticsNodes().isNotEmpty() }
        if (compose.onAllNodesWithText("Experimentar com conta demo").fetchSemanticsNodes().isNotEmpty()) click("Experimentar com conta demo")
        waitFor("MEU DIA")
        if (compose.onAllNodesWithText("Revisar depois").fetchSemanticsNodes().isNotEmpty()) click("Revisar depois")
    }
    private fun screenshot(name: String) {
        compose.waitForIdle()
        val automation = InstrumentationRegistry.getInstrumentation().uiAutomation
        automation.waitForIdle(400, 4000)
        val bitmap = automation.takeScreenshot()
        val folder = File(compose.activity.getExternalFilesDir(null), "refinement").apply { mkdirs() }
        File(folder, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    private fun scrollHabitsTo(text: String) { compose.onNodeWithTag("habits-page").performScrollToNode(hasText(text)) }

    @Test fun personalitiesChangeWithTheSameAccentAndCustomColorPersists() {
        login()
        val preferences = container.preferencesRepository
        val original = runBlocking { preferences.preferences.first() }
        val wine = AccentColor(0x8F315A)
        try {
            runBlocking { preferences.setAccent(wine); preferences.setTheme(ThemeMode.LIGHT) }
            PlannerPersonality.entries.forEach { personality ->
                compose.onNodeWithContentDescription("Configurações").performClick()
                click(personality.name.lowercase().replaceFirstChar { it.uppercaseChar() })
                compose.waitUntil(15000) { runBlocking { preferences.preferences.first().plannerPersonality == personality } }
                assertEquals(wine, runBlocking { preferences.preferences.first().accentColor })
                back(); waitFor("MEU DIA")
                compose.onNodeWithTag("planner-paper-page").performScrollToIndex(0)
                screenshot("personality-${personality.name.lowercase()}")
            }
            compose.onNodeWithContentDescription("Configurações").performClick()
            compose.onNode(hasText("Personalizada ·", substring = true)).performScrollTo().performClick()
            compose.onNode(hasSetTextAction() and hasText("Hexadecimal #RRGGBB")).performTextReplacement("#XYZ")
            compose.onNodeWithText("Use seis dígitos, por exemplo #5B735B.").assertExists()
            compose.onNode(hasSetTextAction() and hasText("Hexadecimal #RRGGBB")).performTextReplacement("#285CAB")
            click("Salvar"); click("Escuro"); click("Cozy")
            compose.activityRule.scenario.recreate()
            waitFor("Do seu jeito")
            val restored = runBlocking { preferences.preferences.first() }
            assertEquals(AccentColor(0x285CAB), restored.accentColor)
            assertEquals(PlannerPersonality.COZY, restored.plannerPersonality)
            assertEquals(ThemeMode.DARK, restored.themeMode)
            back(); screenshot("cozy-blue-dark")
        } finally { runBlocking { preferences.setPersonality(original.plannerPersonality); preferences.setAccent(original.accentColor); preferences.setTheme(original.themeMode) } }
    }

    @Test fun quotesHeatmapPomodoroAndHabitConsentWorkTogether() {
        login()
        val preferences = container.preferencesRepository
        val original = runBlocking { preferences.preferences.first() }
        val today = LocalDate.now()
        var habitId = 0L
        var ownId: String? = null
        try {
            runBlocking { preferences.setPersonality(PlannerPersonality.CLEAN); preferences.setFocusMode(FocusMode.FREE) }
            compose.onNodeWithContentDescription("Configurações").performClick()
            click("Aleatória"); click("Calma")
            click("Diariamente"); click("Somente manual")
            click("Frases favoritas"); click("Adicionar minha frase")
            click("Salvar")
            compose.onNodeWithText("Escreva uma frase para guardar.").assertExists()
            val text = "Um dia de cada vez · ${System.nanoTime() % 10000}"
            compose.onNode(hasSetTextAction() and hasText("Sua frase")).performTextInput(text)
            compose.onNode(hasSetTextAction() and hasText("Autor (opcional)")).performTextInput("Minha rotina")
            click("Salvar"); waitFor(text)
            ownId = runBlocking { container.quoteRepository.current.first()!!.id }
            back(); back(); waitFor("MEU DIA")
            compose.onNodeWithTag("planner-paper-page").performScrollToNode(hasText(text))
            screenshot("personal-quote")
            assertTrue(runBlocking { container.quoteRepository.current.first()!!.isFavorite })
            click("Outra frase")
            compose.waitUntil(15000) { runBlocking { container.quoteRepository.current.first()?.id != ownId } }
            val rotated = runBlocking { preferences.preferences.first().quote.currentId }
            compose.activityRule.scenario.recreate()
            waitFor("MEU DIA")
            assertEquals(rotated, runBlocking { preferences.preferences.first().quote.currentId })
            assertTrue(runBlocking { container.quoteRepository.quotes.first().first { it.id == ownId }.isFavorite })

            val name = "Leitura com calma"
            habitId = runBlocking {
                val id = container.habitRepository.save(Habit(name = name, startsOn = today.minusWeeks(2)))
                container.habitRepository.markDone(id, today.minusDays(3)); container.habitRepository.markDone(id, today.minusDays(6)); id
            }
            tab("Hábitos"); waitFor("Mapa do seu ritmo")
            val date = today.minusDays(3)
            compose.onNode(hasContentDescription(date.accessibilityLabel(), substring = true) and hasAnyAncestor(hasTestTag("habit-overall-map")))
                .performScrollTo().performClick()
            compose.onNodeWithText(date.accessibilityLabel()).assertExists()
            screenshot("heatmap-overall")
            scrollHabitsTo(name); click(name); waitFor("Seu hábito")
            screenshot("habit-details")
            click("Focar neste hábito"); click("Pomodoro")
            click("50 / 10")
            compose.waitUntil(15000) { runBlocking { preferences.preferences.first().pomodoro.focusMinutes == 50 } }
            click("Personalizado")
            listOf("Foco em minutos" to "1", "Pausa curta em minutos" to "1", "Quantidade de ciclos" to "2", "Pausa longa em minutos" to "1").forEach { (label, value) ->
                compose.onNode(hasSetTextAction() and hasText(label)).performTextReplacement(value)
            }
            click("Salvar")
            compose.waitUntil(15000) { runBlocking { preferences.preferences.first().pomodoro == PomodoroConfig(1, 1, 2, 1) } }
            click("Iniciar foco"); click("Pausar")
            compose.activityRule.scenario.recreate(); waitFor("Retomar")
            screenshot("pomodoro-paused")
            click("Retomar")
            compose.waitUntil(75000) { compose.onAllNodesWithText("Começar pausa").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("PAUSA CURTA").assertExists()
            val session = runBlocking { container.focusRepository.active.first()!! }
            assertEquals(FocusPhase.SHORT_BREAK, session.phase)
            assertEquals(60000, session.elapsedMillis)
            assertFalse(runBlocking { container.habitRepository.history(habitId).first().any { it.date == today } })
            screenshot("pomodoro-short-break")
            click("Começar pausa"); click("Pausar"); click("Retomar")
            click("Encerrar"); click("Encerrar")
            waitFor("Quer marcar “$name” como concluído hoje?")
            assertFalse(runBlocking { container.habitRepository.history(habitId).first().any { it.date == today } })
            click("Agora não")
            assertFalse(runBlocking { container.habitRepository.history(habitId).first().any { it.date == today } })
            click("Encerrar")
            runBlocking { preferences.setFocusMode(FocusMode.FREE) }
            click("Focar neste hábito"); click("Iniciar foco")
            compose.waitUntil(5000) { runBlocking { container.focusRepository.active.first()?.let { FocusRules.elapsed(it, System.currentTimeMillis()) >= 1000 } == true } }
            click("Pausar"); click("Encerrar"); click("Encerrar")
            waitFor("Quer marcar “$name” como concluído hoje?")
            click("Marcar como feito")
            compose.waitUntil(15000) { runBlocking { container.habitRepository.history(habitId).first().any { it.date == today } } }
            click("Encerrar"); back(); tab("Progresso"); waitFor("Meu Ritmo")
            screenshot("rhythm-with-pomodoro")
            assertTrue(runBlocking { container.progressRepository.observe(DateInterval(today.minusDays(6), today)).first().focusMinutes >= 1 })
        } finally {
            runBlocking {
                container.focusRepository.active.first()?.let { container.focusRepository.finish(it.id) }
                if (habitId > 0) container.habitRepository.delete(habitId)
                ownId?.let { container.quoteRepository.deleteOwn(it) }
                preferences.setPersonality(original.plannerPersonality); preferences.setFocusMode(original.defaultFocusMode)
                preferences.setPomodoro(original.pomodoro); preferences.setQuoteCategory(original.quote.category); preferences.setQuoteMode(original.quote.refreshMode)
            }
        }
    }
}
