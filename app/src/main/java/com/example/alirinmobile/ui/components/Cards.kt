package com.example.alirinmobile.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.alirinmobile.ui.theme.Hairline
import com.example.alirinmobile.ui.theme.Primary
import com.example.alirinmobile.ui.theme.Radius
import com.example.alirinmobile.ui.theme.Surface

/** Standard white card with soft shadow + 18 dp radius. */
@Composable
fun AlirinCard(
    modifier: Modifier = Modifier,
    bg: Color = Surface,
    padding: PaddingValues = PaddingValues(0.dp),
    elevation: Boolean = true,
    border: Boolean = false,
    shape: RoundedCornerShape = Radius.lg,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .then(if (elevation) Modifier.shadow(2.dp, shape, ambientColor = Color(0x14141C1C), spotColor = Color(0x14141C1C)) else Modifier)
            .clip(shape)
            .background(bg)
            .then(if (border) Modifier.border(BorderStroke(1.dp, Hairline), shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding)
    ) {
        content()
    }
}

/** Flat card variant (no shadow, hairline border). */
@Composable
fun AlirinFlatCard(
    modifier: Modifier = Modifier,
    bg: Color = Surface,
    padding: PaddingValues = PaddingValues(0.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) = AlirinCard(modifier, bg, padding, elevation = false, border = true, onClick = onClick, content = content)

/** Green primary card (the bike-rods hero style). */
@Composable
fun AlirinGreenCard(
    modifier: Modifier = Modifier,
    bg: Color = Primary,
    padding: PaddingValues = PaddingValues(0.dp),
    shape: RoundedCornerShape = Radius.lg,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    // Shadow tint mirrors the brand teal so the soft drop matches the card colour.
    val shadowTint = Color(0x730E8770)
    Box(
        modifier
            .shadow(8.dp, shape, ambientColor = shadowTint, spotColor = shadowTint)
            .clip(shape)
            .background(bg)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(padding)
    ) {
        content()
    }
}
