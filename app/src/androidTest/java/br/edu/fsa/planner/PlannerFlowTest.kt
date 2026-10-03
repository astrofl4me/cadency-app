package br.edu.fsa.planner

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
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
        compose.onNodeWithText("Salvar plano").performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription("Concluir Teste do planner").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("Teste do planner").performScrollTo().performClick()
        compose.onNode(hasSetTextAction() and hasText("O que você quer planejar?")).performTextReplacement("Plano revisado")
        compose.onNodeWithText("Salvar plano").performClick()
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
        compose.onNodeWithText("Hoje").performClick()
        waitFor("Plano revisado")
        compose.onNodeWithText("Plano revisado").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Excluir plano").performClick()
        compose.onNodeWithText("Excluir", useUnmergedTree = true).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Plano revisado").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("Perfil").performClick()
        compose.onNodeWithText("Sair da conta").performScrollTo().performClick()
        waitFor("Experimentar com conta demo")
        compose.onNodeWithText("Experimentar com conta demo").performClick()
        waitFor("MEU DIA")
    }
}
