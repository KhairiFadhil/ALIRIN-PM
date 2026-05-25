package com.example.alirinmobile.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.alirinmobile.feature.AuthUiState
import com.example.alirinmobile.feature.AuthViewModel
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*

/**
 * Staff login (was the general LoginScreen). Citizens get past this via the Welcome
 * screen's "Mulai sebagai Warga" button which sets anonymous mode directly.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(Modifier.fillMaxSize().background(Bg)) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AlirinIconBubble(icon = AlirinIcons.arrowLeft, onClick = onBack)
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            // Hero
            Icon(
                AlirinIcons.shield, null,
                tint = Primary,
                modifier = Modifier.size(32.dp).padding(bottom = 14.dp),
            )
            Text("KHUSUS PETUGAS", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 6.dp))
            Text(
                "Masuk sebagai\nStaff Validator",
                style = AlirinText.display,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Text(
                "Masukkan ID staff & PIN untuk validasi laporan warga.",
                style = AlirinText.bodyR,
                modifier = Modifier.padding(bottom = 24.dp),
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Column {
                    Text("ID STAFF", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 6.dp))
                    StaffInput(
                        value = username,
                        onValueChange = { username = it; if (ui is AuthUiState.Failed) viewModel.ui.value = AuthUiState.Initial },
                        placeholder = "contoh: emilys",
                        leadingIcon = AlirinIcons.users,
                        keyboardType = KeyboardType.Text,
                    )
                }
                Column {
                    Text("PIN", style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 6.dp))
                    StaffInput(
                        value = password,
                        onValueChange = {
                            password = it
                            if (ui is AuthUiState.Failed) viewModel.ui.value = AuthUiState.Initial
                        },
                        placeholder = "6 digit angka",
                        leadingIcon = AlirinIcons.shield,
                        keyboardType = KeyboardType.Password,
                        password = true,
                        suffix = if (password.isNotEmpty()) "${password.length}/6" else null,
                    )
                }
                if (ui is AuthUiState.Failed) {
                    Row(
                        Modifier
                            .clip(Radius.md)
                            .background(RiskKritisBg)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(AlirinIcons.info, null, tint = RiskKritisInk, modifier = Modifier.size(16.dp))
                        Text(
                            (ui as AuthUiState.Failed).message,
                            color = RiskKritisInk,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.W500,
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "Lupa PIN?",
                color = Primary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.W600,
                style = androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline),
            )

            Spacer(Modifier.height(24.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(Radius.md)
                    .background(Surface2)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(AlirinIcons.info, null, tint = Muted, modifier = Modifier.size(18.dp).padding(top = 1.dp))
                Text(
                    "ID staff diberikan oleh admin kelurahan. Belum punya? Hubungi pengawas wilayah kamu.",
                    style = AlirinText.caption,
                    lineHeight = 18.sp,
                )
            }

            Spacer(Modifier.height(16.dp))
            // Dev helper: dummyjson test creds
            Text(
                "Uji: emilys / emilyspass (staff) — michaelw / michaelwpass (admin)",
                style = AlirinText.caption.copy(fontSize = 11.sp),
            )
            Spacer(Modifier.height(40.dp))
        }

        Column(
            Modifier
                .fillMaxWidth()
                .background(Bg)
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 28.dp)
        ) {
            val canSubmit = username.isNotBlank() && password.length >= 4 && ui !is AuthUiState.Submitting
            AlirinButton(
                label = if (ui is AuthUiState.Submitting) "Memverifikasi..." else "Masuk",
                onClick = { viewModel.login(username.trim(), password) },
                block = true,
                enabled = canSubmit,
                trailingIcon = if (ui is AuthUiState.Submitting) null else AlirinIcons.arrowRight,
            )
        }
    }
}

@Composable
private fun StaffInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    password: Boolean = false,
    suffix: String? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(Radius.md)
            .background(Surface2)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(leadingIcon, null, tint = Muted, modifier = Modifier.size(18.dp))
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (password) PasswordVisualTransformation()
                else androidx.compose.ui.text.input.VisualTransformation.None,
            textStyle = AlirinText.body.copy(color = Ink),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Primary),
            modifier = Modifier.weight(1f),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) Text(placeholder, color = Faint, fontSize = 15.sp, fontWeight = FontWeight.W500)
                    inner()
                }
            }
        )
        if (suffix != null) {
            Text(suffix, style = AlirinText.mono.copy(fontSize = 12.sp, color = Muted, fontWeight = FontWeight.W600))
        }
    }
}
