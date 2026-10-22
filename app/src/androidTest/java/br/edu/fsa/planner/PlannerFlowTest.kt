package br.edu.fsa.planner

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.Role
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlannerFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun waitFor(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun openTodayTab() {
        compose.onNode(hasText("Hoje") and SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)).performClick()
        compose.onNodeWithTag("planner-paper-page").performScrollToIndex(0)
    }

    private fun waitForHabitSelection() {
        compose.waitUntil(10_000) {
            compose.onAllNodes(hasContentDescription("Leitura de teste, segunda-feira") and isSelected())
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForTag(tag: String) {
        try {
            compose.waitUntil(10_000) { compose.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
        } catch (failure: Throwable) {
            compose.onAllNodes(isRoot(), useUnmergedTree = true).printToString(maxDepth = 30)
                .lineSequence().forEach { android.util.Log.e("PlannerUiTest", it) }
            throw failure
        }
    }

    @Test fun demoCanCreateEditCompleteDeleteNavigateAndLogout() {
        // Also tolerates an existing demo session on a development device.
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText("Experimentar com conta demo").fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodesWithText("MEU DIA").fetchSemanticsNodes().isNotEmpty()
        }
        if (compose.onAllNodesWithText("Experimentar com conta demo").fetchSemanticsNodes().isNotEmpty()) {
            compose.onNodeWithText("Experimentar com conta demo").performClick()
        }
        waitFor("MEU DIA")
        compose.onNodeWithContentDescription("Adicionar plano").performClick()
        compose.onNode(hasSetTextAction() and hasText("O que você quer planejar?")).performTextInput("Teste do planner")
        compose.onNodeWithText("Salvar plano").performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription("Concluir Teste do planner").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Teste do planner").performScrollTo().performClick()
        waitFor("Etapas")
        compose.onNodeWithContentDescription("Editar plano").performClick()
        compose.onNode(hasSetTextAction() and hasText("O que você quer planejar?")).performTextReplacement("Plano revisado")
        compose.onNodeWithText("Salvar plano").performSemanticsAction(SemanticsActions.OnClick) { it() }
        waitFor("Etapas")
        compose.onNodeWithContentDescription("Voltar").performSemanticsAction(SemanticsActions.OnClick) { it() }
        compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription("Concluir Plano revisado").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithContentDescription("Concluir Plano revisado").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription("Marcar Plano revisado como pendente").fetchSemanticsNodes().isNotEmpty() }
        compose.activityRule.scenario.recreate()
        waitFor("MEU DIA")
        compose.onNodeWithText("Plano revisado").performScrollTo().assertExists()
        compose.onNodeWithText("Semana").performClick()
        waitFor("MINHA SEMANA")
        compose.onNodeWithContentDescription("Próxima semana").performClick()
        compose.onNodeWithText("Semana atual").performClick()
        compose.onNodeWithText("Mês").performClick()
        waitFor("MEU MÊS")
        compose.onNodeWithContentDescription("Próximo mês").performClick()
        compose.onNodeWithText("Mês atual").performClick()
        openTodayTab()
        waitFor("Plano revisado")
        compose.onNodeWithText("Plano revisado").performScrollTo().performClick()
        waitFor("Etapas")
        compose.onNodeWithContentDescription("Editar plano").performClick()
        compose.onNodeWithContentDescription("Excluir plano").performClick()
        compose.onNodeWithText("Excluir", useUnmergedTree = true).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Plano revisado").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithContentDescription("Registrar 4 copos de água").performScrollTo().performClick()
        waitFor("4 de 8")
        compose.onNodeWithTag("planner-paper-page").performScrollToNode(hasContentDescription("Editar Hoje eu agradeço por"))
        compose.onNodeWithContentDescription("Editar Hoje eu agradeço por")
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        waitForTag("note-editor")
        compose.onNodeWithTag("note-editor").performTextReplacement("Aprender algo novo")
        compose.onNodeWithText("Salvar", useUnmergedTree = true).performClick()
        waitFor("Aprender algo novo")
        compose.onNodeWithText("Semana").performClick()
        waitFor("Planner semanal")
        compose.onNodeWithTag("planner-paper-page").performScrollToNode(hasText("Adicionar hábito"))
        compose.onNodeWithText("Adicionar hábito").performClick()
        waitForTag("habit-name")
        compose.onNodeWithTag("habit-name").performTextInput("Leitura de teste")
        compose.onNodeWithText("Salvar", useUnmergedTree = true).performClick()
        waitFor("Leitura de teste")
        compose.onNodeWithContentDescription("Leitura de teste, segunda-feira").performScrollTo().performClick()
        waitForHabitSelection()
        compose.onNodeWithContentDescription("Leitura de teste, segunda-feira").assertIsSelected()
        compose.activityRule.scenario.recreate()
        waitFor("Leitura de teste")
        compose.onNodeWithTag("planner-paper-page").performScrollToNode(hasText("Leitura de teste"))
        waitForHabitSelection()
        compose.onNodeWithContentDescription("Leitura de teste, segunda-feira").assertIsSelected()
        compose.onNodeWithText("Leitura de teste").performScrollTo().performClick()
        compose.onNodeWithTag("habit-name").performTextReplacement("Hábito revisado")
        compose.onNodeWithText("Salvar", useUnmergedTree = true).performClick()
        waitFor("Hábito revisado")
        compose.onNodeWithText("Hábito revisado").performScrollTo().performClick()
        compose.onNodeWithText("Excluir hábito").performClick()
        openTodayTab()
        waitFor("MEU DIA")
        compose.onNodeWithContentDescription("Registrar 4 copos de água").performScrollTo()
        compose.onNodeWithText("4 de 8").assertExists()
        compose.onNodeWithTag("planner-paper-page").performScrollToNode(hasText("Aprender algo novo"))
        compose.onNodeWithText("Aprender algo novo").assertExists()
        compose.onNodeWithContentDescription("Configurações").performClick()
        compose.onNodeWithText("Sair da conta").performScrollTo()
            .performSemanticsAction(SemanticsActions.OnClick) { it() }
        waitFor("Experimentar com conta demo")
        compose.onNodeWithText("Experimentar com conta demo").performClick()
        waitFor("MEU DIA")
    }
}
