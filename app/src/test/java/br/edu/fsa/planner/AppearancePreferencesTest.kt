package br.edu.fsa.planner

import android.content.Context
import androidx.datastore.core.okio.OkioStorage
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.PreferencesSerializer
import androidx.test.core.app.ApplicationProvider
import br.edu.fsa.planner.data.repository.*
import br.edu.fsa.planner.domain.model.*
import br.edu.fsa.planner.ui.theme.cadencyColors
import androidx.compose.ui.graphics.luminance
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import okio.FileSystem
import okio.Path.Companion.toPath
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class AppearancePreferencesTest {
    @Test fun appearanceAndReviewDateSurviveReopenAndLogout() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val file = File(context.filesDir, "appearance-${System.nanoTime()}.preferences_pb")
        fun store(scope: CoroutineScope) = PreferenceDataStoreFactory.create(scope = scope,
            storage = OkioStorage(FileSystem.SYSTEM, PreferencesSerializer) { file.absolutePath.toPath() })
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val initialStore = store(scope)
        val preferences = PreferencesRepository(initialStore)
        val auth = LocalAuthRepository(initialStore)
        assertEquals(ThemeMode.SYSTEM, preferences.preferences.first().themeMode)
        preferences.setTheme(ThemeMode.DARK)
        preferences.setMood(PlannerPersonality.COZY)
        preferences.setAccent(AccentColor(0x8F315A))
        preferences.setFocusMode(FocusMode.POMODORO)
        preferences.setPomodoro(PomodoroConfig(50, 10, 3, 20))
        preferences.setRollover(false)
        preferences.markRolloverReviewed(LocalDate.of(2026, 10, 21))
        auth.signInDemo(); auth.signOut()
        scope.coroutineContext.job.cancelAndJoin()
        val reopenedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val saved = PreferencesRepository(store(reopenedScope)).preferences.first()
            assertEquals(ThemeMode.DARK, saved.themeMode)
            assertEquals(PlannerPersonality.COZY, saved.plannerPersonality)
            assertEquals(AccentColor(0x8F315A), saved.accentColor)
            assertEquals(FocusMode.POMODORO, saved.defaultFocusMode)
            assertEquals(PomodoroConfig(50, 10, 3, 20), saved.pomodoro)
            assertFalse(saved.gentleRolloverEnabled)
            assertEquals(LocalDate.of(2026, 10, 21), saved.lastRolloverReviewDate)
        } finally { reopenedScope.coroutineContext.job.cancelAndJoin(); file.delete() }
    }

    @Test fun everyMoodHasReadableLightAndDarkPalettes() {
        for (mood in PlannerPersonality.entries) for (dark in listOf(false, true)) {
            val colors = cadencyColors(mood, dark)
            val contrast = (maxOf(colors.onBackground.luminance(), colors.background.luminance()) + 0.05f) /
                (minOf(colors.onBackground.luminance(), colors.background.luminance()) + 0.05f)
            assertTrue("$mood dark=$dark contrast=$contrast", contrast >= 4.5f)
            assertEquals(dark, colors.background.luminance() < 0.5f)
        }
    }

    @Test fun customAccentsStayReadableIndependentlyOfPersonality() {
        val accents = AccentPreset.entries.map { it.color } + listOf(AccentColor(0xFFFFFF), AccentColor(0), AccentColor(0xFFFF00))
        for (personality in PlannerPersonality.entries) for (dark in listOf(false, true)) for (accent in accents) {
            val colors = cadencyColors(personality, dark, accent)
            assertTrue(br.edu.fsa.planner.ui.theme.colorContrast(colors.primary, colors.background) >= 4.5f)
            assertTrue(br.edu.fsa.planner.ui.theme.colorContrast(colors.primary, colors.onPrimary) >= 4.5f)
        }
        assertEquals(AccentColor(0x123ABC), AccentColor.parse("#123abc"))
        assertNull(AccentColor.parse("#XYZ123"))
        val cozy = br.edu.fsa.planner.ui.theme.personalityTokens(PlannerPersonality.COZY)
        val clean = br.edu.fsa.planner.ui.theme.personalityTokens(PlannerPersonality.CLEAN)
        val focus = br.edu.fsa.planner.ui.theme.personalityTokens(PlannerPersonality.FOCUS)
        assertNotEquals(cozy.typography, clean.typography)
        assertNotEquals(cozy.cardRadius, clean.cardRadius)
        assertNotEquals(clean.spacing, focus.spacing)
        assertTrue(focus.prioritizeNow)
    }
}
