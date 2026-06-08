package com.example.alirinmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.ui.theme.*

enum class BtnVariant { Primary, Soft, Ghost, OnDark, Danger }

@Composable
fun AlirinButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: BtnVariant = BtnVariant.Primary,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    enabled: Boolean = true,
    block: Boolean = false,
    small: Boolean = false,
) {
    val (bg, ink) = when (variant) {
        BtnVariant.Primary -> Primary to Color.White
        BtnVariant.Soft    -> Surface2 to Ink
        BtnVariant.Ghost   -> Color.Transparent to Ink
        BtnVariant.OnDark  -> Color.White.copy(alpha = 0.10f) to Color.White
        BtnVariant.Danger  -> RiskKritisDot to Color.White
    }
    
    // Improved disabled state for better contrast
    val currentBg = if (enabled) bg else Surface3
    val currentInk = if (enabled) ink else Muted

    val height = if (small) 38.dp else 56.dp
    val padH = if (small) 14.dp else 22.dp
    
    // Outer Box ensures minimum 48dp touch target for accessibility
    Box(
        modifier = modifier
            .then(if (block) Modifier.fillMaxWidth() else Modifier)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(Radius.pill)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            Modifier
                .then(if (block) Modifier.fillMaxWidth() else Modifier)
                .height(height)
                .clip(Radius.pill)
                .background(currentBg)
                .padding(horizontal = padH),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        ) {
            if (leadingIcon != null) Icon(leadingIcon, null, tint = currentInk, modifier = Modifier.size(if (small) 14.dp else 18.dp))
            Text(
                text = label,
                color = currentInk,
                fontSize = if (small) 13.sp else 15.5.sp,
                fontWeight = FontWeight.W600,
            )
            if (trailingIcon != null) Icon(trailingIcon, null, tint = currentInk, modifier = Modifier.size(if (small) 14.dp else 18.dp))
        }
    }
}

/** Small icon-only round button used as back arrow on top bars. */
@Composable
fun AlirinIconBubble(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    bg: Color = Surface,
    tint: Color = Ink,
    size: Int = 40,
) {
    // Outer Box ensures minimum 48dp touch target for accessibility
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .clip(RoundedCornerShape(999.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(bg),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = tint, modifier = Modifier.size((size * 0.5f).dp))
        }
    }
}
