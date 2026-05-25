package com.example.alirinmobile.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.alirinmobile.ui.components.AlirinButton
import com.example.alirinmobile.ui.components.AlirinIcons
import com.example.alirinmobile.ui.components.BtnVariant
import com.example.alirinmobile.ui.components.LogoBadge
import com.example.alirinmobile.ui.theme.*

@Composable
fun AdminWallScreen(onLogout: () -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Bg)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        LogoBadge(size = 72, radius = 18)
        Spacer(Modifier.height(24.dp))
        Text("Admin pakai website.", style = AlirinText.h1)
        Spacer(Modifier.height(8.dp))
        Text(
            "Dashboard kota, statistik, dan konfigurasi scoring ada di admin.alirin.id. Aplikasi mobile fokus ke warga dan staff lapangan.",
            style = AlirinText.bodyR,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
        Spacer(Modifier.height(24.dp))
        Icon(AlirinIcons.shield, null, tint = Primary, modifier = Modifier.size(32.dp))
        Spacer(Modifier.height(32.dp))
        AlirinButton(
            label = "Keluar",
            onClick = onLogout,
            variant = BtnVariant.Soft,
            block = true,
        )
    }
}
