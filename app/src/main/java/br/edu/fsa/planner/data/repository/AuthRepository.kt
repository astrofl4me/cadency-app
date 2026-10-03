package br.edu.fsa.planner.data.repository

import br.edu.fsa.planner.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val session: Flow<User?>
    suspend fun signIn(email: String, password: String): Boolean
    suspend fun signInDemo()
    suspend fun signOut()
}
