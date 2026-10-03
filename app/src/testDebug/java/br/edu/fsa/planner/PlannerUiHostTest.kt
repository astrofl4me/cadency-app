package br.edu.fsa.planner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.activity.ComponentActivity
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.compose.ui.test.*
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import br.edu.fsa.planner.data.repository.LocalAuthRepository
import br.edu.fsa.planner.ui.navigation.PlannerNavigation
import br.edu.fsa.planner.ui.theme.PlannerTheme
import kotlinx.coroutines.*
import okio.FileSystem
import okio.Path.Companion.toPath
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PlannerUiHostTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @After fun closeStore() = runBlocking { storeScope.coroutineContext.job.cancelAndJoin() }

    private fun waitFor(text: String) {
        compose.waitUntil(10_000) { compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty() }
    }
    private fun screenshot(name: String) {
        val dismissable = SemanticsMatcher.keyIsDefined(SemanticsActions.Dismiss)
        repeat(4) {
            if (compose.onAllNodes(dismissable).fetchSemanticsNodes().isNotEmpty()) {
                compose.onAllNodes(dismissable)[0].performSemanticsAction(SemanticsActions.Dismiss) { it() }
            }
        }
        val directory = File(requireNotNull(System.getProperty("planner.verificationDir")))
        directory.mkdirs()
        lateinit var bitmap: Bitmap
        // Window PixelCopy needs a real display; draw the native-rendered view on the JVM host.
        compose.runOnIdle {
            val view = compose.activity.window.decorView
            bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
            view.draw(Canvas(bitmap))
        }
        File(directory, "$name.png").outputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }
        bitmap.recycle()
    }
    private fun waitForCheckbox(description: String) {
        try {
            compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription(description).fetchSemanticsNodes().isNotEmpty() }
        } catch (failure: Throwable) {
            val directory = File(requireNotNull(System.getProperty("planner.verificationDir")))
            File(directory, "ui-failure.txt").writeText(compose.onRoot(useUnmergedTree = true).printToString())
            screenshot("ui-failure")
            throw failure
        }
    }

    @Test fun loginCrudNavigationAndLogoutWorkWithRealRepositories() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val store = PreferenceDataStoreFactory.create(scope = storeScope,
            storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer) {
                File(context.filesDir, "ui-test.preferences_pb").absolutePath.toPath()
            })
        val container = AppContainer(context, LocalAuthRepository(store))
        compose.setContent { PlannerTheme { PlannerNavigation(container) } }
        waitFor("Experimentar com conta demo")
        screenshot("login")
        compose.onNodeWithText("Entrar").performClick()
        compose.onNodeWithText("Informe um e-mail válido.").assertExists()
        compose.onNodeWithText("Experimentar com conta demo").performClick()
        waitFor("Planos do dia")
        screenshot("daily-empty")
        compose.onNodeWithContentDescription("Adicionar plano").performClick()
        compose.onNode(hasSetTextAction() and hasText("O que você quer planejar?")).performTextInput("Estudar circuitos")
        screenshot("editor")
        compose.onNodeWithText("Salvar plano").performSemanticsAction(SemanticsActions.OnClick) { it() }
        waitForCheckbox("Concluir Estudar circuitos")
        compose.onNodeWithText("Estudar circuitos").performScrollTo().performClick()
        compose.onNode(hasSetTextAction() and hasText("O que você quer planejar?")).performTextReplacement("Revisar circuitos")
        compose.onNodeWithText("Salvar plano").performSemanticsAction(SemanticsActions.OnClick) { it() }
        waitForCheckbox("Concluir Revisar circuitos")
        compose.onNodeWithContentDescription("Concluir Revisar circuitos").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription("Marcar Revisar circuitos como pendente").fetchSemanticsNodes().isNotEmpty() }
        screenshot("daily-task")
        compose.onNode(hasScrollAction()).performScrollToIndex(0)
        compose.onNodeWithContentDescription("Próximo dia").performClick()
        compose.onNodeWithText("Voltar para hoje").performClick()
        compose.onNodeWithText("Semana").performClick()
        waitFor("Intenção da semana")
        screenshot("weekly")
        compose.onNodeWithText("Mês").performClick()
        waitFor("Revisar circuitos")
        screenshot("monthly")
        compose.onNodeWithText("Hoje").performClick()
        waitFor("Revisar circuitos")
        compose.onNodeWithText("Revisar circuitos").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Excluir plano").performClick()
        compose.onNodeWithText("Excluir", useUnmergedTree = true).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Revisar circuitos").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("Perfil").performClick()
        waitFor("Do seu jeito")
        screenshot("profile")
        compose.onNodeWithText("Sair da conta").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }
        waitFor("Experimentar com conta demo")
    }
}
