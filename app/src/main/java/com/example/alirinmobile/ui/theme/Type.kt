package com.example.alirinmobile.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.alirinmobile.R

private val GoogleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

private val JakartaName = GoogleFont("Plus Jakarta Sans")
private val MonoName = GoogleFont("JetBrains Mono")

val PlusJakartaSans = FontFamily(
    Font(googleFont = JakartaName, fontProvider = GoogleFontsProvider, weight = FontWeight.W400),
    Font(googleFont = JakartaName, fontProvider = GoogleFontsProvider, weight = FontWeight.W500),
    Font(googleFont = JakartaName, fontProvider = GoogleFontsProvider, weight = FontWeight.W600),
    Font(googleFont = JakartaName, fontProvider = GoogleFontsProvider, weight = FontWeight.W700),
    Font(googleFont = JakartaName, fontProvider = GoogleFontsProvider, weight = FontWeight.W800),
)

val JetBrainsMono = FontFamily(
    Font(googleFont = MonoName, fontProvider = GoogleFontsProvider, weight = FontWeight.W400),
    Font(googleFont = MonoName, fontProvider = GoogleFontsProvider, weight = FontWeight.W500),
)

/**
 * Custom text styles. Maps theme.css .t-* classes.
 */
object AlirinText {
    val display   = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.W700, fontSize = 30.sp, lineHeight = 32.sp, letterSpacing = (-0.75).sp, color = Ink)
    val h1        = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.W700, fontSize = 24.sp, lineHeight = 28.sp, letterSpacing = (-0.48).sp, color = Ink)
    val h2        = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.W700, fontSize = 19.sp, lineHeight = 24.sp, letterSpacing = (-0.28).sp, color = Ink)
    val h3        = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.W700, fontSize = 16.sp, lineHeight = 21.sp, letterSpacing = (-0.16).sp, color = Ink)
    val body      = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.W500, fontSize = 15.sp, lineHeight = 21.sp, color = Ink)
    val bodyR     = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.W400, fontSize = 14.sp, lineHeight = 21.sp, color = Ink3)
    val label     = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.W600, fontSize = 13.sp, color = Ink2, letterSpacing = 0.13.sp)
    val caption   = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.W500, fontSize = 12.sp, color = Muted, letterSpacing = 0.12.sp)
    val eyebrow   = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.W700, fontSize = 11.sp, color = Muted, letterSpacing = 1.21.sp)
    val mono      = TextStyle(fontFamily = JetBrainsMono,   fontWeight = FontWeight.W500, fontSize = 11.sp, letterSpacing = (-0.11).sp, color = Ink)
    val monoCode  = TextStyle(fontFamily = JetBrainsMono,   fontWeight = FontWeight.W600, fontSize = 13.sp, color = Ink3, letterSpacing = (-0.13).sp)
    val statValue = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.W700, fontSize = 22.sp, lineHeight = 22.sp, letterSpacing = (-0.44).sp, color = Ink)
    val btnLabel  = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.W600, fontSize = 16.sp, letterSpacing = (-0.08).sp, color = Ink)
}

val AlirinTypography = Typography(
    displayLarge = AlirinText.display,
    headlineLarge = AlirinText.h1,
    headlineMedium = AlirinText.h2,
    titleLarge = AlirinText.h3,
    bodyLarge = AlirinText.body,
    bodyMedium = AlirinText.bodyR,
    labelLarge = AlirinText.btnLabel,
    labelMedium = AlirinText.label,
    labelSmall = AlirinText.caption,
)
