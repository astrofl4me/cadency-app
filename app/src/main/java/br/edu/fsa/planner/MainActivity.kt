package br.edu.fsa.planner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.compose.ui.graphics.toArgb
import br.edu.fsa.planner.ui.theme.PlannerColors
import br.edu.fsa.planner.ui.navigation.PlannerNavigation
import br.edu.fsa.planner.ui.theme.PlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val systemBarColor = PlannerColors.Paper.toArgb()
        val legacyBarColor = PlannerColors.Ink.toArgb()
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(systemBarColor, legacyBarColor),
            navigationBarStyle = SystemBarStyle.light(systemBarColor, legacyBarColor))
        val container = (application as PlannerApplication).container
        setContent { PlannerTheme { PlannerNavigation(container) } }
    }
}
