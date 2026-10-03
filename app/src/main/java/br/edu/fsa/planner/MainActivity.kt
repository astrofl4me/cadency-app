package br.edu.fsa.planner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import br.edu.fsa.planner.ui.navigation.PlannerNavigation
import br.edu.fsa.planner.ui.theme.PlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as PlannerApplication).container
        setContent { PlannerTheme { PlannerNavigation(container) } }
    }
}
