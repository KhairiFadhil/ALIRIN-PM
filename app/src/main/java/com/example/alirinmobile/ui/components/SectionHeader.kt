package com.example.alirinmobile.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.ui.theme.AlirinText
import com.example.alirinmobile.ui.theme.Primary

@Composable
fun SectionHeader(
    title: String,
    eyebrow: String? = null,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier.fillMaxWidth().padding(bottom = 12.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(Modifier.weight(1f, fill = true)) {
            if (eyebrow != null) Text(eyebrow, style = AlirinText.eyebrow, modifier = Modifier.padding(bottom = 4.dp))
            Text(title, style = AlirinText.h3)
        }
        if (action != null) {
            Row(
                Modifier.clickable(onClick = onAction ?: {}),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = action,
                    color = Primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W600,
                )
                Icon(
                    AlirinIcons.chevronRight, null,
                    tint = Primary, modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
