package com.example.alirinmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.alirinmobile.ui.theme.AlirinText
import com.example.alirinmobile.ui.theme.Bg

@Composable
fun AlirinTopBar(
    title: String? = null,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    transparent: Boolean = false,
    right: @Composable (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .background(if (transparent) Color.Transparent else Bg)
            .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (onBack != null) AlirinIconBubble(icon = AlirinIcons.arrowLeft, onClick = onBack)
        Column(Modifier.weight(1f)) {
            if (title != null) Text(title, style = AlirinText.h3)
            if (subtitle != null) Text(subtitle, style = AlirinText.caption)
        }
        if (right != null) right()
    }
}

/** Phone fake status bar (matches design's punchhole). */
@Composable
fun PhoneStatusBar(time: String = "13:24", modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(start = 32.dp, end = 28.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = time,
            style = AlirinText.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.W600)
        )
        // We omit drawing the punchhole notch since real Android handles its own status bar
        Spacer(Modifier.width(0.dp))
    }
}
