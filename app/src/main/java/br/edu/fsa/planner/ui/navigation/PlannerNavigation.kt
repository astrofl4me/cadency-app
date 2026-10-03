package br.edu.fsa.planner.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Add
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
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.viewModelFactory
import br.edu.fsa.planner.R
import br.edu.fsa.planner.AppContainer
import br.edu.fsa.planner.viewmodel.AuthViewModel
import br.edu.fsa.planner.ui.screens.auth.LoginScreen
import br.edu.fsa.planner.ui.screens.editor.EditorScreen
import br.edu.fsa.planner.viewmodel.EditorViewModel
import br.edu.fsa.planner.viewmodel.EditorResult
import br.edu.fsa.planner.viewmodel.PlannerViewModel
import br.edu.fsa.planner.viewmodel.PlannerNotice
import br.edu.fsa.planner.ui.screens.daily.DailyScreen
import br.edu.fsa.planner.ui.screens.weekly.WeeklyScreen
import br.edu.fsa.planner.domain.model.ItemType
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember
import java.time.LocalDate

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
    val planner: PlannerViewModel = viewModel(factory = viewModelFactory {
        initializer { PlannerViewModel(container.plannerRepository, createSavedStateHandle()) }
    })
    val session by auth.session.collectAsStateWithLifecycle()
    val login by auth.login.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    fun addItem(date: LocalDate, type: ItemType) { navController.navigate("editor?date=$date&type=${type.name}") }
    LaunchedEffect(planner) {
        planner.notices.collect { notice ->
            val message = when (notice) {
                PlannerNotice.SAVED -> R.string.item_saved; PlannerNotice.DELETED -> R.string.item_deleted
                PlannerNotice.COMPLETED -> R.string.item_completed; PlannerNotice.REOPENED -> R.string.item_reopened
                PlannerNotice.NOTE_SAVED -> R.string.note_saved; PlannerNotice.ERROR -> R.string.storage_error
            }
            snackbar.showSnackbar(context.getString(message))
        }
    }
    LaunchedEffect(session.ready, session.user) {
        if (session.ready) {
            val target = if (session.user == null) "login" else "daily"
            if (session.user == null || navController.currentDestination?.route in listOf(null, "splash", "login")) {
                navController.navigate(target) { popUpTo(navController.graph.id) { inclusive = true }; launchSingleTop = true }
            }
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, floatingActionButton = {
        if (session.user != null && entry?.destination?.route in listOf("daily", "weekly", "monthly")) {
            FloatingActionButton(onClick = { addItem(if (entry?.destination?.route == "weekly") planner.weekAddDate() else planner.currentDay(), ItemType.TASK) }) {
                Icon(Icons.Outlined.Add, stringResource(R.string.add_item))
            }
        }
    }, bottomBar = {
        if (session.user != null && tabs.any { it.route == entry?.destination?.route }) NavigationBar {
            tabs.forEach { tab ->
                NavigationBarItem(
                    selected = entry?.destination?.route == tab.route,
                    onClick = { if (tab.route == "daily") planner.goToday(); navController.navigate(tab.route) {
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
            composable("editor?itemId={itemId}&date={date}&type={type}", arguments = listOf(
                navArgument("itemId") { type = NavType.LongType; defaultValue = 0L },
                navArgument("date") { type = NavType.StringType; defaultValue = LocalDate.now().toString() },
                navArgument("type") { type = NavType.StringType; defaultValue = "TASK" },
            )) {
                val editor: EditorViewModel = viewModel(factory = viewModelFactory {
                    initializer { EditorViewModel(container.plannerRepository, createSavedStateHandle()) }
                })
                val state by editor.state.collectAsStateWithLifecycle()
                LaunchedEffect(state.result) {
                    state.result?.let { result ->
                        navController.popBackStack()
                        planner.editorFinished(result)
                    }
                }
                EditorScreen(state, editor::update, editor::save, editor::delete, { navController.popBackStack() }, editor::load)
            }
            tabs.forEach { tab ->
                composable(tab.route) {
                    if (tab.route == "daily") {
                        val state by planner.daily.collectAsStateWithLifecycle()
                        DailyScreen(state, { planner.moveDay(-1) }, { planner.moveDay(1) }, planner::goToday,
                            ::addItem, { navController.navigate("editor?itemId=${it.id}") }, planner::toggle,
                            planner::setMood, planner::saveDailyNote, planner::retryData)
                    } else if (tab.route == "weekly") {
                        val state by planner.weekly.collectAsStateWithLifecycle()
                        WeeklyScreen(state, { planner.moveWeek(-1) }, { planner.moveWeek(1) }, planner::goCurrentWeek,
                            ::addItem, { navController.navigate("editor?itemId=${it.id}") }, planner::toggle,
                            { planner.selectDay(it); navController.navigate("daily") { popUpTo("daily"); launchSingleTop = true } },
                            planner::saveWeeklyNote, planner::retryData)
                    } else Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (tab.route == "settings") Button(onClick = auth::signOut) { Text(stringResource(R.string.sign_out)) }
                        else Text(stringResource(tab.label), style = MaterialTheme.typography.headlineLarge)
                    }
                }
            }
        }
    }
}
