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
import java.time.LocalDate
import java.time.LocalTime
import java.time.DayOfWeek
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.screens.daily.DailyScreen
import br.edu.fsa.planner.ui.screens.weekly.WeeklyScreen
import br.edu.fsa.planner.viewmodel.DailyUiState
import br.edu.fsa.planner.viewmodel.WeeklyUiState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.content.res.Configuration
import br.edu.fsa.planner.ui.theme.cadencyColors

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35], qualifiers = "w360dp-h800dp-xxhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PlannerUiHostTest {
    @get:Rule val compose = createAndroidComposeRule<ComponentActivity>()
    private val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @After fun closeStore() = runBlocking { storeScope.coroutineContext.job.cancelAndJoin() }

    @Test fun systemThemeRespondsToDeviceConfigurationWhileForcedModesOverrideIt() {
        val night = mutableStateOf(false)
        val mode = mutableStateOf(ThemeMode.SYSTEM)
        var background = androidx.compose.ui.graphics.Color.Unspecified
        compose.setContent {
            val configuration = Configuration(LocalConfiguration.current).apply {
                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    if (night.value) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO
            }
            CompositionLocalProvider(LocalConfiguration provides configuration) {
                PlannerTheme(mode.value, PlannerMood.COZY) {
                    val color = MaterialTheme.colorScheme.background
                    SideEffect { background = color }
                    Text("Cadency")
                }
            }
        }
        compose.runOnIdle { org.junit.Assert.assertEquals(cadencyColors(PlannerMood.COZY, false).background, background); night.value = true }
        compose.runOnIdle { org.junit.Assert.assertEquals(cadencyColors(PlannerMood.COZY, true).background, background); mode.value = ThemeMode.LIGHT }
        compose.runOnIdle { org.junit.Assert.assertEquals(cadencyColors(PlannerMood.COZY, false).background, background); mode.value = ThemeMode.DARK; night.value = false }
        compose.runOnIdle { org.junit.Assert.assertEquals(cadencyColors(PlannerMood.COZY, true).background, background) }
    }

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
        waitFor("Etapas")
        compose.onNodeWithContentDescription("Editar plano").performClick()
        compose.onNode(hasSetTextAction() and hasText("O que você quer planejar?")).performTextReplacement("Revisar circuitos")
        compose.onNodeWithText("Salvar plano").performSemanticsAction(SemanticsActions.OnClick) { it() }
        waitFor("Etapas")
        compose.onNodeWithContentDescription("Voltar").performClick()
        waitForCheckbox("Concluir Revisar circuitos")
        compose.onNodeWithContentDescription("Concluir Revisar circuitos").performScrollTo().performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription("Marcar Revisar circuitos como pendente").fetchSemanticsNodes().isNotEmpty() }
        screenshot("daily-task")
        compose.onNodeWithContentDescription("Registrar 3 copos de água").performScrollTo().performClick()
        waitFor("3 de 8")
        screenshot("daily-journal")
        compose.onNode(hasScrollAction()).performScrollToIndex(0)
        compose.onNodeWithContentDescription("Próximo dia").performClick()
        compose.onNodeWithText("Voltar para hoje").performClick()
        compose.onNodeWithText("Semana").performClick()
        waitFor("Planner semanal")
        screenshot("weekly")
        compose.onNodeWithText("Mês").performClick()
        waitFor("MEU MÊS")
        compose.onNodeWithTag("planner-month-page").performScrollToNode(hasText("Revisar circuitos"))
        screenshot("monthly")
        compose.onNodeWithText("Hoje").performClick()
        waitFor("Revisar circuitos")
        compose.onNodeWithContentDescription("Registrar 3 copos de água").performScrollTo()
        compose.onNodeWithText("3 de 8").assertExists()
        compose.onNodeWithTag("planner-paper-page").performScrollToNode(hasText("Revisar circuitos"))
        compose.onNodeWithText("Revisar circuitos").performClick()
        waitFor("Etapas")
        compose.onNodeWithContentDescription("Editar plano").performClick()
        compose.onNodeWithContentDescription("Excluir plano").performClick()
        compose.onNodeWithText("Excluir", useUnmergedTree = true).performClick()
        compose.waitUntil(10_000) { compose.onAllNodesWithText("Revisar circuitos").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithContentDescription("Configurações").performClick()
        waitFor("Do seu jeito")
        screenshot("profile")
        compose.onNodeWithText("Sair da conta").performScrollTo().performSemanticsAction(SemanticsActions.OnClick) { it() }
        waitFor("Experimentar com conta demo")
    }

    @Test
    @Config(qualifiers = "w840dp-h1180dp-xhdpi")
    fun paperPagesKeepTwoReadableColumnsOnTablet() {
        val date = LocalDate.of(2026, 10, 21)
        val items = listOf(
            PlannerItem(id = 1, title = "Apresentar o projeto", date = date, type = ItemType.PRIORITY, priority = Priority.HIGH),
            PlannerItem(id = 2, title = "Revisar anotações", date = date, startTime = LocalTime.of(9, 0)),
        )
        val weekly = mutableStateOf(false)
        compose.setContent { PlannerTheme {
            if (!weekly.value) DailyScreen(
                state = DailyUiState(date, date, items, note = PlannerNote("day:$date", text = "Ideias para a próxima etapa.",
                    mood = Mood.GOOD, waterGlasses = 3, gratitude = "Uma manhã tranquila.", selfCare = "Caminhar ao ar livre."), loading = false),
                onPrevious = {}, onNext = {}, onToday = {}, onAdd = { _, _ -> }, onEdit = {}, onToggle = {},
                onMood = {}, onNote = { _, _ -> }, onWater = {}, onRetry = {},
            ) else WeeklyScreen(
                state = WeeklyUiState(PlannerDates.week(date), date, items,
                    note = PlannerNote("week", text = "Separar tempo para descansar.", highlight = "Fazer uma coisa de cada vez."),
                    loading = false, habits = listOf(WeeklyHabit(1, PlannerDates.week(date).start, "Ler",
                        setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)))),
                onPrevious = {}, onNext = {}, onCurrent = {}, onAdd = { _, _ -> }, onEdit = {}, onToggle = {},
                onOpenDay = {}, onNote = { _, _ -> }, onSaveHabit = {}, onToggleHabit = { _, _ -> }, onDeleteHabit = {}, onRetry = {},
            )
        } }
        compose.onNodeWithText("Como estou me sentindo?").assertIsDisplayed()
        compose.onNodeWithText("Hoje eu agradeço por").assertIsDisplayed()
        val priorityBounds = compose.onNodeWithText("Prioridades").getUnclippedBoundsInRoot()
        val moodBounds = compose.onNodeWithText("Como estou me sentindo?").getUnclippedBoundsInRoot()
        org.junit.Assert.assertTrue(moodBounds.left > priorityBounds.right)
        screenshot("daily-tablet")
        compose.runOnIdle { weekly.value = true }
        compose.onNodeWithText("Planner semanal").assertIsDisplayed()
        compose.onNodeWithText("Acompanhamento de hábitos").assertIsDisplayed()
        compose.onNodeWithText("domingo").assertIsDisplayed()
        screenshot("weekly-tablet")
    }
}
