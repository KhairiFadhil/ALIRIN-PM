package com.example.alirinmobile.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.feature.AuthUiState
import com.example.alirinmobile.ui.components.*
import com.example.alirinmobile.ui.theme.*

/**
 * Stateless UI for the staff-login page. All inputs come in via parameters; all
 * interactions flow out via lambdas. Easy to preview and to test.
 */
@Composable
fun LoginContent(
    ui: AuthUiState,
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onBack: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val canSubmit = username.isNotBlank() && password.length >= 4 && ui !is AuthUiState.Submitting

    Column(modifier.fillMaxSize().background(Bg)) {
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
                LabeledField(
                    label = "ID STAFF",
                    value = username,
                    onValueChange = onUsernameChange,
                    placeholder = "contoh: emilys",
                    leadingIcon = AlirinIcons.users,
                    keyboardType = KeyboardType.Text,
                )
                LabeledField(
                    label = "PIN",
                    value = password,
                    onValueChange = onPasswordChange,
                    placeholder = "6 digit angka",
                    leadingIcon = AlirinIcons.shield,
                    keyboardType = KeyboardType.Password,
                    password = true,
                    suffix = if (password.isNotEmpty()) "${password.length}/6" else null,
                )
                if (ui is AuthUiState.Failed) ErrorBanner(message = ui.message)
            }

            Spacer(Modifier.height(14.dp))
            Text(
                "Lupa PIN?",
                color = Primary,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.W600,
                style = TextStyle(textDecoration = TextDecoration.Underline),
            )

            Spacer(Modifier.height(24.dp))
            InfoBox(
                "ID staff diberikan oleh admin kelurahan. Belum punya? Hubungi pengawas wilayah kamu."
            )

            Spacer(Modifier.height(16.dp))
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
            AlirinButton(
                label = if (ui is AuthUiState.Submitting) "Memverifikasi..." else "Masuk",
                onClick = onSubmit,
                block = true,
                enabled = canSubmit,
                trailingIcon = if (ui is AuthUiState.Submitting) null else AlirinIcons.arrowRight,
            )
        }
    }
}

// ── Small UI atoms used only by LoginContent ──────────────────────
@Composable
private fun LabeledField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType,
    password: Boolean = false,
    suffix: String? = null,
) {
    var passwordVisible by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    Column {
        Text(label, style = AlirinText.eyebrow.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 6.dp))
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
                visualTransformation = if (password && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
                textStyle = AlirinText.body.copy(color = Ink),
                cursorBrush = SolidColor(Primary),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) Text(
                            placeholder, color = Faint, fontSize = 15.sp, fontWeight = FontWeight.W500,
                        )
                        inner()
                    }
                }
            )
            if (password) {
                val icon = if (passwordVisible) AlirinIcons.eyeOff else AlirinIcons.eye
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                        .clip(Radius.pill)
                        .clickable { passwordVisible = !passwordVisible },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = "Toggle Password", tint = Muted, modifier = Modifier.size(20.dp))
                }
            } else if (suffix != null) {
                Text(suffix, style = AlirinText.mono.copy(fontSize = 12.sp, color = Muted, fontWeight = FontWeight.W600))
            }
        }
    }
}

@Composable
private fun ErrorBanner(message: String) {
    Row(
        Modifier
            .clip(Radius.md)
            .background(RiskKritisBg)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(AlirinIcons.info, null, tint = RiskKritisInk, modifier = Modifier.size(16.dp))
        Text(message, color = RiskKritisInk, fontSize = 12.5.sp, fontWeight = FontWeight.W500)
    }
}

@Composable
private fun InfoBox(text: String) {
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
        Text(text, style = AlirinText.caption, lineHeight = 18.sp)
    }
}
