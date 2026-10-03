package br.edu.fsa.planner.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import br.edu.fsa.planner.R
import br.edu.fsa.planner.ui.theme.PlannerDimens as D
import br.edu.fsa.planner.viewmodel.AuthError
import br.edu.fsa.planner.viewmodel.LoginUiState

@Composable
fun LoginScreen(state: LoginUiState, onEmail: (String) -> Unit, onPassword: (String) -> Unit,
                onVisibility: () -> Unit, onSignIn: () -> Unit, onDemo: () -> Unit) {
    val keyboard = LocalSoftwareKeyboardController.current
    Box(Modifier.fillMaxSize().safeDrawingPadding().imePadding(), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = D.ContentWidth).fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(D.Page), verticalArrangement = Arrangement.spacedBy(D.Large)) {
            Surface(shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(Icons.Outlined.MenuBook, contentDescription = null, modifier = Modifier.padding(D.Large).size(D.Section))
            }
            Spacer(Modifier.height(D.Small))
            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.login_tagline), color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(D.Large))
            OutlinedTextField(state.email, onEmail, Modifier.fillMaxWidth(), enabled = !state.busy,
                label = { Text(stringResource(R.string.email)) }, singleLine = true, isError = state.emailInvalid,
                supportingText = { if (state.emailInvalid) Text(stringResource(R.string.email_error)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next))
            OutlinedTextField(state.password, onPassword, Modifier.fillMaxWidth(), enabled = !state.busy,
                label = { Text(stringResource(R.string.password)) }, singleLine = true, isError = state.passwordInvalid,
                supportingText = { if (state.passwordInvalid) Text(stringResource(R.string.password_error)) },
                visualTransformation = if (state.passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { IconButton(onClick = onVisibility) {
                    Icon(if (state.passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        stringResource(if (state.passwordVisible) R.string.hide_password else R.string.show_password))
                } }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboard?.hide(); onSignIn() }))
            state.error?.let { Text(stringResource(if (it == AuthError.CREDENTIALS) R.string.credentials_error else R.string.storage_error),
                color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) }
            Button(onClick = { keyboard?.hide(); onSignIn() }, enabled = !state.busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = D.Touch)) {
                Text(stringResource(if (state.busy) R.string.entering else R.string.sign_in))
            }
            OutlinedButton(onClick = { keyboard?.hide(); onDemo() }, enabled = !state.busy,
                modifier = Modifier.fillMaxWidth().heightIn(min = D.Touch)) {
                Text(stringResource(R.string.try_demo))
            }
            Text(stringResource(R.string.local_auth_hint), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
