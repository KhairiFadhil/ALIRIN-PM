package com.example.alirinmobile.feature.lapor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.ui.components.AlirinButton
import com.example.alirinmobile.ui.components.AlirinIconBubble
import com.example.alirinmobile.ui.components.AlirinIcons
import com.example.alirinmobile.ui.components.BtnVariant
import com.example.alirinmobile.ui.theme.*

@Composable
fun StepHeader(
    step: Int,
    total: Int,
    onBack: () -> Unit,
    title: String,
    subtitle: String? = null,
) {
    Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AlirinIconBubble(icon = AlirinIcons.arrowLeft, onClick = onBack)

            Row(
                Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                repeat(total) { i ->
                    Box(
                        Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (i < step) Ink else Hairline2)
                    )
                }
            }
            Text("Step $step/$total", style = AlirinText.caption)
        }
        Column(Modifier.padding(horizontal = 4.dp)) {
            Text(title, style = AlirinText.h1, modifier = Modifier.padding(bottom = 4.dp))
            if (subtitle != null) Text(subtitle, style = AlirinText.bodyR)
        }
    }
}

@Composable
fun FlowFooter(
    primary: String,
    onPrimary: () -> Unit,
    enabled: Boolean = true,
    info: (@Composable () -> Unit)? = null,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to Bg.copy(alpha = 0f),
                    0.3f to Bg,
                    1f to Bg,
                )
            )
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 16.dp),
    ) {
        if (info != null) {
            Row(
                Modifier.padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) { info() }
        }
        AlirinButton(
            label = primary,
            onClick = onPrimary,
            block = true,
            enabled = enabled,
            trailingIcon = AlirinIcons.arrowRight,
        )
    }
}

@Composable
fun SubLabel(text: String, modifier: Modifier = Modifier) {
    Text(text, style = AlirinText.eyebrow, modifier = modifier.padding(bottom = 10.dp))
}

@Composable
fun SubLabelWithOptional(text: String, optional: Boolean = false, modifier: Modifier = Modifier) {
    Row(modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text, style = AlirinText.eyebrow)
        if (optional) {
            Text(
                " · opsional",
                color = Muted,
                fontWeight = FontWeight.W600,
                fontSize = 11.sp,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Composable
fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector? = null,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minHeight: Int = 52,
) {
    Row(
        modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = minHeight.dp)
            .clip(Radius.md)
            .background(Surface2)
            .padding(horizontal = 14.dp),
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        if (leadingIcon != null) Icon(leadingIcon, null, tint = Muted, modifier = Modifier.size(18.dp).padding(top = if (singleLine) 0.dp else 16.dp))
        androidx.compose.foundation.text.BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = AlirinText.body.copy(color = Ink),
            cursorBrush = androidx.compose.ui.graphics.SolidColor(Primary),
            modifier = Modifier
                .weight(1f)
                .padding(vertical = if (singleLine) 0.dp else 14.dp),
            decorationBox = { inner ->
                Box(contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = Faint, fontSize = 15.sp, fontWeight = FontWeight.W500)
                    }
                    inner()
                }
            }
        )
    }
}

@Composable
fun ReadOnlyField(
    label: String,
    value: String,
    mono: Boolean = false,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .clip(Radius.md)
            .background(Surface)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(label, style = AlirinText.caption, modifier = Modifier.padding(bottom = 2.dp))
        Text(
            value,
            fontWeight = FontWeight.W600,
            fontSize = 14.sp,
            color = Ink,
            style = if (mono) AlirinText.mono.copy(fontSize = 14.sp, fontWeight = FontWeight.W600) else AlirinText.body,
            letterSpacing = (-0.14).sp,
        )
    }
}
