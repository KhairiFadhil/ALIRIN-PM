package com.example.alirinmobile.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ── Spacing ────────────────────────────────────────────────────────
object Space {
    val s1 = 4.dp;  val s2 = 8.dp;  val s3 = 12.dp; val s4 = 16.dp;  val s5 = 20.dp
    val s6 = 24.dp; val s8 = 32.dp; val s10 = 40.dp; val s12 = 48.dp; val s16 = 64.dp; val s20 = 80.dp
}

// ── Radius ─────────────────────────────────────────────────────────
object Radius {
    val sm   = RoundedCornerShape(10.dp)
    val md   = RoundedCornerShape(14.dp)
    val lg   = RoundedCornerShape(18.dp)
    val xl   = RoundedCornerShape(24.dp)
    val pill = RoundedCornerShape(999.dp)
    val sheet = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
}

val AlirinShapes = Shapes(
    extraSmall = Radius.sm,
    small      = Radius.md,
    medium     = Radius.lg,
    large      = Radius.lg,
    extraLarge = Radius.xl,
)

// ── Motion (ms) ────────────────────────────────────────────────────
object Motion {
    const val fast = 150
    const val base = 250
    const val slow = 400
    const val page = 600
}
