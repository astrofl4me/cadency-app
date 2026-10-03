package br.edu.fsa.planner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.fsa.planner.data.repository.AuthRepository
import br.edu.fsa.planner.domain.model.User
import br.edu.fsa.planner.domain.model.validateLogin
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionUiState(val ready: Boolean = false, val user: User? = null, val hasError: Boolean = false)
enum class AuthError { CREDENTIALS, STORAGE }
data class LoginUiState(
    val email: String = "", val password: String = "", val passwordVisible: Boolean = false,
    val emailInvalid: Boolean = false, val passwordInvalid: Boolean = false,
    val busy: Boolean = false, val error: AuthError? = null,
)

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _session = MutableStateFlow(SessionUiState())
    val session = _session.asStateFlow()
    private val _login = MutableStateFlow(LoginUiState())
    val login = _login.asStateFlow()
    private var sessionJob: Job? = null

    init { retrySession() }

    fun retrySession() {
        sessionJob?.cancel()
        _session.value = SessionUiState()
        sessionJob = viewModelScope.launch {
            repository.session.catch { _session.value = SessionUiState(hasError = true) }
                .collect { _session.value = SessionUiState(ready = true, user = it) }
        }
    }

    fun emailChanged(value: String) { _login.update { it.copy(email = value, emailInvalid = false, error = null) } }
    fun passwordChanged(value: String) { _login.update { it.copy(password = value, passwordInvalid = false, error = null) } }
    fun togglePassword() { _login.update { it.copy(passwordVisible = !it.passwordVisible) } }

    fun signIn(demo: Boolean = false) {
        if (_login.value.busy) return
        val form = _login.value
        val validation = validateLogin(form.email, form.password)
        if (!demo && !validation.isValid) {
            _login.update { it.copy(emailInvalid = validation.emailInvalid, passwordInvalid = validation.passwordInvalid) }
            return
        }
        _login.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                val success = if (demo) { repository.signInDemo(); true } else repository.signIn(form.email, form.password)
                _login.update { if (success) LoginUiState() else it.copy(busy = false, error = AuthError.CREDENTIALS) }
            } catch (error: CancellationException) { throw error
            } catch (_: Exception) { _login.update { it.copy(busy = false, error = AuthError.STORAGE) } }
        }
    }

    fun signOut() {
        if (_login.value.busy) return
        _login.update { it.copy(busy = true, error = null) }
        viewModelScope.launch {
            try {
                repository.signOut()
                _login.value = LoginUiState()
            } catch (error: CancellationException) { throw error
            } catch (_: Exception) { _login.update { it.copy(busy = false, error = AuthError.STORAGE) } }
        }
    }
}
