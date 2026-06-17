package com.example.alirinmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.alirinmobile.data.ReportStatus
import com.example.alirinmobile.data.RiskLevel
import com.example.alirinmobile.ui.theme.PrimaryOnDark
import com.example.alirinmobile.ui.theme.Radius

@Composable
fun Dot(color: Color, size: Int = 6, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
fun Pill(
    label: String,
    bg: Color,
    ink: Color,
    dotColor: Color? = null,
    leadingIcon: ImageVector? = null,
    fontSize: Int = 11,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .height(24.dp)
            .clip(Radius.pill)
            .background(bg)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (dotColor != null) Dot(color = dotColor)
        if (leadingIcon != null) Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = ink,
            modifier = Modifier.size(11.dp),
        )
        Text(
            text = label,
            color = ink,
            fontSize = fontSize.sp,
            fontWeight = FontWeight.W600,
        )
    }
}

@Composable
fun RiskPill(level: RiskLevel, score: Int? = null, modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(24.dp)
            .clip(Radius.pill)
            .background(level.bg)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Dot(color = level.dot)
        Text(
            text = level.label,
            color = level.ink,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.W600,
        )
        if (score != null) {
            Text(
                text = "· $score",
                color = level.ink.copy(alpha = 0.7f),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.W500,
            )
        }
    }
}

@Composable
fun StatusPill(status: ReportStatus, modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(24.dp)
            .clip(Radius.pill)
            .background(status.bg)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Dot(color = status.dot)
        Text(
            text = status.label,
            color = status.ink,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.W600,
        )
    }
}

@Composable
fun PillOnDark(label: String, fontSize: Int = 11, modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(24.dp)
            .clip(Radius.pill)
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.95f),
            fontSize = fontSize.sp,
            fontWeight = FontWeight.W600,
        )
    }
}

@Composable
fun PillWithIconOnDark(label: String, leadingIcon: ImageVector, modifier: Modifier = Modifier) {
    Row(
        modifier
            .height(24.dp)
            .clip(Radius.pill)
            .background(Color.White.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(leadingIcon, null, tint = PrimaryOnDark, modifier = Modifier.size(11.dp))
        Text(label, color = Color.White.copy(alpha = 0.95f), fontSize = 11.sp, fontWeight = FontWeight.W600)
    }
}
