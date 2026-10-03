package br.edu.fsa.planner

import android.app.Application
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import br.edu.fsa.planner.data.repository.AuthRepository
import br.edu.fsa.planner.data.repository.LocalAuthRepository

private val Context.sessionDataStore by preferencesDataStore(name = "planner_preferences")

class PlannerApplication : Application() {
    val container by lazy { AppContainer(this) }
}

class AppContainer(context: Context) {
    val authRepository: AuthRepository = LocalAuthRepository(context.sessionDataStore)
}
