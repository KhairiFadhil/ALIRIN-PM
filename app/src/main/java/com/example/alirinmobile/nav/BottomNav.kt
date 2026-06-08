package com.example.alirinmobile.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.alirinmobile.ui.components.AlirinIcons
import com.example.alirinmobile.ui.theme.*

/**
 * NavItem.center is preserved for back-compat with role-aware sets but the pill-style
 * renderer treats all cells identically (Lapor is just another cell that opens the flow).
 */
data class NavItem(val id: String, val icon: ImageVector, val label: String, val center: Boolean = false)

val DefaultNavItems = listOf(
    NavItem("beranda", AlirinIcons.home,     "Beranda"),
    NavItem("peta",    AlirinIcons.map,      "Peta"),
    NavItem("lapor",   AlirinIcons.plus,     "Lapor", center = true),
    NavItem("status",  AlirinIcons.document, "Status"),
    NavItem("tentang", AlirinIcons.info,     "Tentang"),
)

/**
 * Pill-style bottom nav (matches the reference's `'pill'` variant in components.jsx):
 *   – 5 equal-weight cells laid out in a row (grid-like spacing).
 *   – Active cell = primary-filled rounded pill with icon + label.
 *   – Inactive = transparent, icon-only in muted ink, no label.
 */
@Composable
fun BottomNav(
    current: String,
    onSelect: (String) -> Unit,
    items: List<NavItem> = DefaultNavItems,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth().background(Surface)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(Hairline))
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 12.dp, bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items.forEach { item ->
                PillCell(
                    item = item,
                    active = current == item.id,
                    onTap = { onSelect(item.id) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PillCell(
    item: NavItem,
    active: Boolean,
    onTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(Radius.pill)
            .background(if (active) Primary else Color.Transparent)
            .clickable(onClick = onTap),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            item.icon,
            contentDescription = item.label,
            tint = if (active) Color.White else Muted,
            modifier = Modifier.size(24.dp),
        )
    }
}
