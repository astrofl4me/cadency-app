package br.edu.fsa.planner.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import br.edu.fsa.planner.R
import br.edu.fsa.planner.ui.theme.PlannerDimens as D

@Composable
fun SessionCheckScreen(hasError: Boolean, onRetry: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(D.Page), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineLarge)
        Spacer(Modifier.height(D.Page))
        if (hasError) {
            Text(stringResource(R.string.storage_error))
            TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        } else {
            CircularProgressIndicator()
            Spacer(Modifier.height(D.Large))
            Text(stringResource(R.string.checking_session), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
