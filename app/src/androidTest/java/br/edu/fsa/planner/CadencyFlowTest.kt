package br.edu.fsa.planner

import android.graphics.Bitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.theme.cadencyColors
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CadencyFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    private val container get() = ApplicationProvider.getApplicationContext<PlannerApplication>().container
    @After fun closeTestFocus() {
        runBlocking {
            container.focusRepository.active.first()?.takeIf { it.title.startsWith("Etapas UI") }?.let { container.focusRepository.finish(it.id) }
        }
    }
    private fun waitFor(text: String) = compose.waitUntil(15000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    private fun click(text: String) {
        waitFor(text)
        val node = compose.onNodeWithText(text)
        try { node.performScrollTo() } catch (failure: AssertionError) {
            if (failure.message?.contains("no parent layout") != true) throw failure
        }
        node.performSemanticsAction(SemanticsActions.OnClick) { it() }
    }
    private fun tab(text: String) { compose.onNode(hasText(text) and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).performClick() }
    private fun back() { compose.onNodeWithContentDescription("Voltar").performSemanticsAction(SemanticsActions.OnClick) { it() } }
    private fun showHabit(title: String) { compose.waitUntil(15000) {
        try { compose.onNodeWithTag("habits-page").performScrollToNode(hasText(title)); true } catch (_: AssertionError) { false }
    } }
    private fun habitAction(id: Long, label: String) {
        compose.onNode(hasText(label) and hasAnyAncestor(hasTestTag("habit-$id"))).performScrollTo().performClick()
    }
    private fun login() {
        compose.waitUntil(15000) { compose.onAllNodesWithText("Experimentar com conta demo").fetchSemanticsNodes().isNotEmpty() ||
            compose.onAllNodesWithText("MEU DIA").fetchSemanticsNodes().isNotEmpty() }
        if (compose.onAllNodesWithText("Experimentar com conta demo").fetchSemanticsNodes().isNotEmpty()) click("Experimentar com conta demo")
        waitFor("MEU DIA")
        if (compose.onAllNodesWithText("Revisar depois").fetchSemanticsNodes().isNotEmpty()) click("Revisar depois")
    }
    private fun screenshot(name: String): Bitmap {
        compose.waitForIdle()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        val directory = File(compose.activity.getExternalFilesDir(null), "cadency-verification").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        return bitmap
    }

    @Test fun habitsStepsRolloverNowFocusAndProgressRoundTrip() {
        login()
        val today = LocalDate.now()
        val planner = container.plannerRepository
        val title = "Etapas UI ${System.nanoTime()}"
        val task = runBlocking { planner.save(PlannerItem(title = title, date = today, priority = Priority.HIGH)) }
        compose.onNodeWithTag("planner-paper-page").performScrollToNode(hasText(title))
        click(title)
        waitFor("Etapas")
        compose.onNode(hasSetTextAction() and hasText("Próxima etapa")).performTextInput("Pesquisar")
        compose.onNodeWithContentDescription("Adicionar etapa").performScrollTo().performClick()
        waitFor("Pesquisar")
        compose.onNodeWithContentDescription("Etapa: Pesquisar").performScrollTo().performClick()
        waitFor("Todas as etapas foram concluídas. Concluir a tarefa também?")
        click("Agora não")
        assertFalse(runBlocking { planner.get(task)!!.isCompleted })
        screenshot("steps").recycle()
        click("Iniciar foco")
        click("Personalizado")
        compose.onNode(hasSetTextAction() and hasText("Duração em minutos")).performTextReplacement("1")
        click("Iniciar foco")
        waitFor("Pausar")
        click("Pausar")
        waitFor("Retomar")
        compose.activityRule.scenario.recreate()
        waitFor("Retomar")
        click("Retomar")
        screenshot("focus").recycle()
        back(); waitFor("Etapas"); back()
        compose.onNodeWithTag("planner-paper-page").performScrollToIndex(0)
        waitFor("MEU DIA")

        tab("Hábitos")
        compose.onNodeWithTag("habits-page").performScrollToNode(hasText("Adicionar hábito"))
        click("Adicionar hábito")
        val habitName = "Leitura UI ${System.nanoTime()}"
        compose.onNode(hasSetTextAction() and hasText("Nome do hábito")).performTextInput(habitName)
        click("Salvar")
        showHabit(habitName)
        val habitId = runBlocking { container.habitRepository.habits.first().first { it.name == habitName }.id }
        habitAction(habitId, "Concluir hoje")
        waitFor("Desfazer hoje")
        habitAction(habitId, "Desfazer hoje"); waitFor("Concluir hoje"); habitAction(habitId, "Concluir hoje")
        screenshot("habits").recycle()
        click(habitName); click("Editar hábito"); click("Histórico")
        compose.onNodeWithText(today.accessibilityLabelForTest()).assertExists()
        click("Fechar"); back(); back()
        compose.activityRule.scenario.recreate()
        showHabit(habitName)
        waitFor(habitName)
        click(habitName)
        click("Editar hábito")
        compose.onNode(hasSetTextAction() and hasText("Nome do hábito")).performTextReplacement("$habitName revisado")
        compose.onNodeWithContentDescription("Arquivar hábito").performScrollTo().performClick()
        click("Salvar")
        waitFor("$habitName revisado"); back()
        compose.onNodeWithTag("habits-page").performScrollToNode(hasContentDescription("Mostrar arquivados"))
        compose.onNodeWithContentDescription("Mostrar arquivados").performScrollTo().performClick()
        showHabit("$habitName revisado")
        waitFor("$habitName revisado")
        click("$habitName revisado"); click("Editar hábito"); click("Excluir hábito"); click("Excluir")

        val pending = runBlocking { planner.save(PlannerItem(title = "Reorganizar UI", date = today.minusDays(1))) }
        tab("Hoje")
        compose.waitUntil(15000) { compose.onAllNodesWithText("Algumas coisas ficaram de ontem.").fetchSemanticsNodes().isNotEmpty() ||
            compose.onAllNodes(hasText("Vamos reorganizar?", substring = true)).fetchSemanticsNodes().isNotEmpty() }
        if (compose.onAllNodesWithText("Algumas coisas ficaram de ontem.").fetchSemanticsNodes().isEmpty()) {
            compose.onNodeWithTag("planner-paper-page").performScrollToIndex(0)
            compose.onNode(hasText("Vamos reorganizar?", substring = true)).performClick()
        }
        waitFor("Algumas coisas ficaram de ontem.")
        val row = compose.onNodeWithTag("rollover-$pending")
        row.performScrollTo()
        compose.onNode(hasText("Deixar na Inbox") and hasAnyAncestor(hasTestTag("rollover-$pending"))).performClick()
        click("Revisar depois")
        compose.onNode(hasText("Caixa de entrada ·", substring = true)).performClick()
        waitFor("Reorganizar UI")
        compose.onNode(hasText("Trazer para hoje")).performClick()
        compose.waitUntil(15000) { runBlocking { planner.get(pending)!!.date == today && !planner.get(pending)!!.isInbox } }
        back()
        compose.onNodeWithTag("planner-paper-page").performScrollToIndex(0)
        click("Entrar no Modo Agora")
        waitFor("Pular por agora")
        screenshot("now").recycle()
        click("Pular por agora")
        click("Concluído")
        back()
        compose.onNodeWithTag("planner-paper-page").performScrollToIndex(0)
        click("Focus Flow")
        // Real deadline continues while other screens are used; allow the one-minute session to finish.
        compose.waitUntil(75000) { compose.onAllNodesWithText("Tempo concluído ✨").fetchSemanticsNodes().isNotEmpty() }
        click("Concluir tarefa")
        tab("Progresso"); waitFor("Meu Ritmo")
        click("30 dias"); click("7 dias")
        screenshot("progress").recycle()
        val report = runBlocking { container.progressRepository.observe(DateInterval(today.minusDays(6), today)).first() }
        assertTrue(report.focusMinutes >= 1)
        assertTrue(report.completedTasks >= 1)
        runBlocking { planner.delete(task); planner.delete(pending) }
    }

    @Test fun allAppearancePresetsApplyAndPersist() {
        login()
        val preferences = container.preferencesRepository
        val original = runBlocking { preferences.preferences.first() }
        compose.onNodeWithContentDescription("Configurações").performClick()
        try {
            listOf(ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
                click(if (mode == ThemeMode.LIGHT) "Claro" else "Escuro")
                PlannerPersonality.entries.forEach { mood ->
                    click(mood.name.lowercase().replaceFirstChar { it.uppercaseChar() })
                    compose.waitUntil(10000) { runBlocking { preferences.preferences.first().plannerPersonality == mood } }
                    val bitmap = screenshot("${mood.name.lowercase()}-${mode.name.lowercase()}")
                    val background = cadencyColors(mood, mode == ThemeMode.DARK).background.toArgb()
                    assertEquals(background, bitmap.getPixel(4, bitmap.height / 2))
                    assertEquals("A barra de status acompanha o tema do app", background, bitmap.getPixel(4, 4))
                    assertEquals("A barra de navegação acompanha o tema do app", background, bitmap.getPixel(4, bitmap.height - 4))
                    bitmap.recycle()
                }
            }
            compose.activityRule.scenario.recreate()
            waitFor("Do seu jeito")
            compose.onNodeWithText("Escuro").performScrollTo().assertIsSelected()
            compose.onNodeWithText("Focus").performScrollTo().assertIsSelected()
        } finally { runBlocking { preferences.setTheme(original.themeMode); preferences.setMood(original.plannerPersonality) } }
    }
}

private fun LocalDate.accessibilityLabelForTest(): String = format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", java.util.Locale.forLanguageTag("pt-BR")))
