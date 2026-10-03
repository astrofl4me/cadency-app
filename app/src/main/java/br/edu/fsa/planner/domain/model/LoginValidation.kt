package br.edu.fsa.planner.domain.model

data class LoginValidation(val emailInvalid: Boolean, val passwordInvalid: Boolean) {
    val isValid get() = !emailInvalid && !passwordInvalid
}

fun validateLogin(email: String, password: String): LoginValidation = LoginValidation(
    emailInvalid = !Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$").matches(email.trim()),
    passwordInvalid = password.length < 6,
)
