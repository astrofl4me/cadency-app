package br.edu.fsa.planner.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircleOutline
import br.edu.fsa.planner.ui.components.CadencyTopBar
import br.edu.fsa.planner.ui.screens.habits.*
import br.edu.fsa.planner.viewmodel.HabitsViewModel
import br.edu.fsa.planner.viewmodel.HabitEditorViewModel
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
import br.edu.fsa.planner.viewmodel.SettingsViewModel
import br.edu.fsa.planner.ui.screens.auth.LoginScreen
import br.edu.fsa.planner.ui.screens.editor.EditorScreen
import br.edu.fsa.planner.viewmodel.EditorViewModel
import br.edu.fsa.planner.viewmodel.EditorResult
import br.edu.fsa.planner.viewmodel.PlannerViewModel
import br.edu.fsa.planner.viewmodel.PlannerNotice
import br.edu.fsa.planner.ui.screens.daily.DailyScreen
import br.edu.fsa.planner.ui.screens.weekly.WeeklyScreen
import br.edu.fsa.planner.ui.screens.monthly.MonthlyScreen
import br.edu.fsa.planner.ui.screens.settings.SettingsScreen
import br.edu.fsa.planner.ui.screens.auth.SessionCheckScreen
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import br.edu.fsa.planner.domain.model.ItemType
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.ui.platform.LocalResources
import androidx.compose.runtime.remember
import java.time.LocalDate
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.ui.theme.PlannerPaper as Paper

private data class PlannerTab(val route: String, val label: Int, val icon: ImageVector)
private val tabs = listOf(
    PlannerTab("daily", R.string.tab_today, Icons.Outlined.Today),
    PlannerTab("weekly", R.string.tab_week, Icons.Outlined.DateRange),
    PlannerTab("monthly", R.string.tab_month, Icons.Outlined.CalendarMonth),
    PlannerTab("habits", R.string.tab_habits, Icons.Outlined.CheckCircleOutline),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerNavigation(container: AppContainer) {
    val habits: HabitsViewModel = viewModel(factory = viewModelFactory { initializer { HabitsViewModel(container.habitRepository, createSavedStateHandle()) } })
    val settings: SettingsViewModel = viewModel(factory = viewModelFactory { initializer { SettingsViewModel(container.preferencesRepository) } })
    val preferences by settings.preferences.collectAsStateWithLifecycle()
    val settingsError by settings.writeError.collectAsStateWithLifecycle()
    val auth: AuthViewModel = viewModel(factory = viewModelFactory { initializer { AuthViewModel(container.authRepository) } })
    val planner: PlannerViewModel = viewModel(factory = viewModelFactory {
        initializer { PlannerViewModel(container.plannerRepository, createSavedStateHandle()) }
    })
    val session by auth.session.collectAsStateWithLifecycle()
    val login by auth.login.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val snackbar = remember { SnackbarHostState() }
    val resources = LocalResources.current
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner, planner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (isActive) { planner.refreshToday(); habits.refreshToday(); delay(60_000) }
        }
    }
    fun addItem(date: LocalDate, type: ItemType) { navController.navigate("editor?date=$date&type=${type.name}") }
    LaunchedEffect(planner, resources) {
        planner.notices.collect { notice ->
            val message = when (notice) {
                PlannerNotice.SAVED -> R.string.item_saved; PlannerNotice.DELETED -> R.string.item_deleted
                PlannerNotice.COMPLETED -> R.string.item_completed; PlannerNotice.REOPENED -> R.string.item_reopened
                PlannerNotice.NOTE_SAVED -> R.string.note_saved; PlannerNotice.ERROR -> R.string.storage_error
            }
            snackbar.showSnackbar(resources.getString(message))
        }
    }
    LaunchedEffect(session.ready, session.user, entry?.destination?.route) {
        val route = entry?.destination?.route
        if (session.ready && route != null) {
            val target = if (session.user == null) "login" else "daily"
            val needsRedirect = if (session.user == null) route != "login" else route in listOf("splash", "login")
            if (needsRedirect) {
                navController.navigate(target) { popUpTo(navController.graph.id) { inclusive = true }; launchSingleTop = true }
            }
        }
    }
    Scaffold(topBar = {
        if (session.user != null && tabs.any { it.route == entry?.destination?.route }) {
            TopAppBar(title = { Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium) }, actions = {
                IconButton(onClick = { navController.navigate("settings") }) { Icon(Icons.Outlined.Settings, stringResource(R.string.open_settings)) }
            })
        } else if (entry?.destination?.route == "settings") CadencyTopBar(stringResource(R.string.open_settings)) { navController.popBackStack() }
    }, snackbarHost = { SnackbarHost(snackbar) }, floatingActionButton = {
        if (session.user != null && entry?.destination?.route in listOf("daily", "weekly", "monthly")) {
            FloatingActionButton(onClick = { addItem(when (entry?.destination?.route) {
                "weekly" -> planner.weekAddDate(); "monthly" -> planner.monthAddDate(); else -> planner.currentDay()
            }, ItemType.TASK) }, containerColor = Paper.Ink, contentColor = Paper.Background) {
                Icon(Icons.Outlined.Add, stringResource(R.string.add_item))
            }
        }
    }, bottomBar = {
        if (session.user != null && tabs.any { it.route == entry?.destination?.route }) NavigationBar(
            containerColor = Paper.Background, tonalElevation = D.None) {
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
                    colors = NavigationBarItemDefaults.colors(selectedIconColor = Paper.Ink, selectedTextColor = Paper.Ink,
                        indicatorColor = Paper.Panel, unselectedIconColor = Paper.Muted, unselectedTextColor = Paper.Muted),
                )
            }
        }
    }) { padding ->
        NavHost(navController, startDestination = "splash", modifier = Modifier.padding(padding).consumeWindowInsets(padding)) {
            composable("splash") {
                SessionCheckScreen(session.hasError, auth::retrySession)
            }
            composable("login") {
                LoginScreen(login, auth::emailChanged, auth::passwordChanged, auth::togglePassword,
                    { auth.signIn() }, { auth.signIn(demo = true) })
            }
            composable("settings") {
                session.user?.let { SettingsScreen(it, login.busy, login.error != null || settingsError,
                    auth::signOut, preferences, settings::setTheme, settings::setMood, settings::setRollover) }
            }
            composable("habitEditor?habitId={habitId}", arguments = listOf(navArgument("habitId") { type = NavType.LongType; defaultValue = 0L })) {
                val editor: HabitEditorViewModel = viewModel(factory = viewModelFactory { initializer { HabitEditorViewModel(container.habitRepository, createSavedStateHandle()) } })
                val state by editor.state.collectAsStateWithLifecycle()
                val history by editor.history.collectAsStateWithLifecycle()
                LaunchedEffect(state.finished) { if (state.finished) navController.popBackStack() }
                HabitEditorScreen(state, history, editor::update, editor::save, editor::delete) { navController.popBackStack() }
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
                            planner::setMood, planner::saveDailyReflection, planner::setWaterGlasses, planner::retryData)
                    } else if (tab.route == "weekly") {
                        val state by planner.weekly.collectAsStateWithLifecycle()
                        WeeklyScreen(state, { planner.moveWeek(-1) }, { planner.moveWeek(1) }, planner::goCurrentWeek,
                            ::addItem, { navController.navigate("editor?itemId=${it.id}") }, planner::toggle,
                            { planner.selectDay(it); navController.navigate("daily") { popUpTo("daily"); launchSingleTop = true } },
                            planner::saveWeeklyNote, planner::saveHabit, planner::toggleHabit, planner::deleteHabit, planner::retryData)
                    } else if (tab.route == "monthly") {
                        val state by planner.monthly.collectAsStateWithLifecycle()
                        MonthlyScreen(state, { planner.moveMonth(-1) }, { planner.moveMonth(1) }, planner::goCurrentMonth,
                            planner::selectCalendarDay,
                            { planner.selectDay(it); navController.navigate("daily") { popUpTo("daily"); launchSingleTop = true } },
                            ::addItem, { navController.navigate("editor?itemId=${it.id}") }, planner::toggle, planner::retryData)
                    } else {
                        val state by habits.state.collectAsStateWithLifecycle()
                        HabitsScreen(state, { navController.navigate("habitEditor") },
                            { navController.navigate("habitEditor?habitId=${it.id}") }, habits::toggle,
                            { habits.moveWeek(-1) }, { habits.moveWeek(1) }, habits::currentWeek, habits::showArchived)
                    }
                }
            }
        }
    }
}
