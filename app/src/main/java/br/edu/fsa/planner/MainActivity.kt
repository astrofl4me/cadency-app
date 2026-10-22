package br.edu.fsa.planner

import android.os.Bundle
import android.graphics.drawable.ColorDrawable
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.viewmodel.initializer
import br.edu.fsa.planner.viewmodel.SettingsViewModel
import br.edu.fsa.planner.ui.navigation.PlannerNavigation
import br.edu.fsa.planner.ui.theme.PlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as PlannerApplication).container
        setContent {
            val settings: SettingsViewModel = viewModel(factory = viewModelFactory { initializer { SettingsViewModel(container.preferencesRepository) } })
            val preferences by settings.preferences.collectAsStateWithLifecycle()
            if (preferences.ready) PlannerTheme(preferences.themeMode, preferences.plannerPersonality, preferences.accentColor) {
                val light = MaterialTheme.colorScheme.background.luminance() > 0.5f
                val background = MaterialTheme.colorScheme.background.toArgb()
                SideEffect {
                    window.setBackgroundDrawable(ColorDrawable(background))
                    val style = if (light) SystemBarStyle.light(background, background) else SystemBarStyle.dark(background)
                    enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                }
                PlannerNavigation(container)
            }
        }
    }
}
