package com.example.alirinmobile.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alirinmobile.feature.AuthUiState
import com.example.alirinmobile.feature.AuthViewModel

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    fun clearTransientError() {
        if (ui is AuthUiState.Failed) viewModel.ui.value = AuthUiState.Initial
    }

    LoginContent(
        ui = ui,
        username = username,
        password = password,
        onUsernameChange = { username = it; clearTransientError() },
        onPasswordChange = { password = it; clearTransientError() },
        onBack = onBack,
        onSubmit = { viewModel.login(username.trim(), password) },
    )
}
