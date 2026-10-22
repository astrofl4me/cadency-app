package br.edu.fsa.planner

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import br.edu.fsa.planner.data.local.database.PlannerDatabase
import br.edu.fsa.planner.data.local.database.Migration2To3
import br.edu.fsa.planner.data.local.database.Migration3To4
import br.edu.fsa.planner.data.repository.PlannerRepository
import br.edu.fsa.planner.data.repository.AuthRepository
import br.edu.fsa.planner.data.repository.LocalAuthRepository
import br.edu.fsa.planner.data.repository.PreferencesRepository
import br.edu.fsa.planner.data.repository.HabitRepository
import br.edu.fsa.planner.data.repository.FocusRepository
import br.edu.fsa.planner.data.repository.ProgressRepository

private val Context.sessionDataStore by preferencesDataStore(name = "planner_preferences")

class PlannerApplication : Application() {
    val container by lazy { AppContainer(this) }
}

class AppContainer(context: Context, val authRepository: AuthRepository = LocalAuthRepository(context.sessionDataStore)) {
    val preferencesRepository = PreferencesRepository(context.sessionDataStore)
    private val database by lazy {
        Room.databaseBuilder(context.applicationContext, PlannerDatabase::class.java, "planner.db")
            .addMigrations(PlannerDatabase.Migration1To2, Migration2To3, Migration3To4).build()
    }
    val habitRepository by lazy { HabitRepository(database.habitDao()) }
    val focusRepository by lazy { FocusRepository(database.focusDao()) }
    val progressRepository by lazy { ProgressRepository(database.plannerDao(), habitRepository, focusRepository) }
    val plannerRepository by lazy { PlannerRepository(database.plannerDao(), habits = habitRepository) }
}
