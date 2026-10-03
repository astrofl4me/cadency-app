package br.edu.fsa.planner.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.compose.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.fsa.planner.R
import br.edu.fsa.planner.AppContainer
import br.edu.fsa.planner.viewmodel.AuthViewModel
import br.edu.fsa.planner.ui.screens.auth.LoginScreen

private data class PlannerTab(val route: String, val label: Int, val icon: ImageVector)
private val tabs = listOf(
    PlannerTab("daily", R.string.tab_today, Icons.Outlined.Today),
    PlannerTab("weekly", R.string.tab_week, Icons.Outlined.DateRange),
    PlannerTab("monthly", R.string.tab_month, Icons.Outlined.CalendarMonth),
    PlannerTab("settings", R.string.tab_settings, Icons.Outlined.Settings),
)

@Composable
fun PlannerNavigation(container: AppContainer) {
    val auth: AuthViewModel = viewModel(factory = viewModelFactory { initializer { AuthViewModel(container.authRepository) } })
    val session by auth.session.collectAsStateWithLifecycle()
    val login by auth.login.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    LaunchedEffect(session.ready, session.user) {
        if (session.ready) {
            val target = if (session.user == null) "login" else "daily"
            if (session.user == null || navController.currentDestination?.route in listOf(null, "splash", "login")) {
                navController.navigate(target) { popUpTo(navController.graph.id) { inclusive = true }; launchSingleTop = true }
            }
        }
    }
    Scaffold(bottomBar = {
        if (session.user != null && tabs.any { it.route == entry?.destination?.route }) NavigationBar {
            tabs.forEach { tab ->
                NavigationBarItem(
                    selected = entry?.destination?.route == tab.route,
                    onClick = { navController.navigate(tab.route) {
                        popUpTo("daily") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    } },
                    icon = { Icon(tab.icon, contentDescription = null) },
                    label = { Text(stringResource(tab.label)) },
                )
            }
        }
    }) { padding ->
        NavHost(navController, startDestination = "splash", modifier = Modifier.padding(padding)) {
            composable("splash") {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (session.hasError) Button(onClick = auth::retrySession) { Text(stringResource(R.string.retry)) }
                    else CircularProgressIndicator()
                }
            }
            composable("login") {
                LoginScreen(login, auth::emailChanged, auth::passwordChanged, auth::togglePassword,
                    { auth.signIn() }, { auth.signIn(demo = true) })
            }
            tabs.forEach { tab ->
                composable(tab.route) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (tab.route == "settings") Button(onClick = auth::signOut) { Text(stringResource(R.string.sign_out)) }
                        else Text(stringResource(tab.label), style = MaterialTheme.typography.headlineLarge)
                    }
                }
            }
        }
    }
}
