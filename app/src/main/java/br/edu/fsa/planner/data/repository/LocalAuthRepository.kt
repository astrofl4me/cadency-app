package br.edu.fsa.planner.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import br.edu.fsa.planner.domain.model.User
import kotlinx.coroutines.flow.map

class LocalAuthRepository(private val preferences: DataStore<Preferences>) : AuthRepository {
    private val sessionEmail = stringPreferencesKey("session_email")

    override val session = preferences.data.map { values ->
        values[sessionEmail]?.takeIf { it == DemoAccount.email }?.let { User(it, "Demo") }
    }

    override suspend fun signIn(email: String, password: String): Boolean {
        if (!email.trim().equals(DemoAccount.email, ignoreCase = true) || password != DemoAccount.password) {
            return false
        }
        preferences.edit { it[sessionEmail] = DemoAccount.email }
        return true
    }

    override suspend fun signInDemo() {
        signIn(DemoAccount.email, DemoAccount.password)
    }

    override suspend fun signOut() {
        preferences.edit { it.remove(sessionEmail) }
    }

    // This public demo account is deliberately local and offers no security boundary.
    private object DemoAccount {
        const val email = "demo@planner.app"
        const val password = "123456"
    }
}
