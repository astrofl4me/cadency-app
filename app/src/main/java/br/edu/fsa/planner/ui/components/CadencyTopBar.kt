package br.edu.fsa.planner.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import br.edu.fsa.planner.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadencyTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(title = { Text(title, style = MaterialTheme.typography.headlineSmall) }, navigationIcon = {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back)) }
    })
}
