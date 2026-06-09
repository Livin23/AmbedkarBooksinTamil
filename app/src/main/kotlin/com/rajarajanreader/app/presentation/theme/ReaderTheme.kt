package com.rajarajanreader.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.rajarajanreader.app.R
import com.rajarajanreader.app.domain.ReadingTheme

private val NotoSerifTamil = FontFamily(
    Font(R.font.noto_serif_tamil_regular, FontWeight.Normal),
    Font(R.font.noto_serif_tamil_bold,    FontWeight.Bold),
    Font(R.font.noto_serif_tamil_bold,    FontWeight.SemiBold)
)

// ── Royal Chola Palette ──────────────────────────────────────────────────────
data class ReaderColors(
    val bg: Color, val surface: Color, val text: Color,
    val textSec: Color, val accent: Color, val quoteBg: Color,
    val gold: Color, val dropCapColor: Color, val dividerColor: Color,
    val headerGradientStart: Color, val headerGradientEnd: Color
)

// Imperial — deep crimson night with molten gold
private fun imperial() = ReaderColors(
    bg                  = Color(0xFF0D0005),
    surface             = Color(0xFF1A000A),
    text                = Color(0xFFF5ECD5),
    textSec             = Color(0xFFBB9A70),
    accent              = Color(0xFFD4A017),
    quoteBg             = Color(0xFF2A0015),
    gold                = Color(0xFFD4A017),
    dropCapColor        = Color(0xFFD4A017),
    dividerColor        = Color(0xFF7A4A00),
    headerGradientStart = Color(0xFF5A0020),
    headerGradientEnd   = Color(0xFF1A000A)
)

// Parchment — warm ancient manuscript
private fun parchment() = ReaderColors(
    bg                  = Color(0xFFF7EDD0),
    surface             = Color(0xFFFDF5E2),
    text                = Color(0xFF2C1A0A),
    textSec             = Color(0xFF6B4C2A),
    accent              = Color(0xFF8B2500),
    quoteBg             = Color(0xFFEDD9A3),
    gold                = Color(0xFF8B2500),
    dropCapColor        = Color(0xFF8B2500),
    dividerColor        = Color(0xFF8B6540),
    headerGradientStart = Color(0xFF8B2500),
    headerGradientEnd   = Color(0xFF5A1800)
)

// Palace — midnight sapphire with silver-gold
private fun palace() = ReaderColors(
    bg                  = Color(0xFF050A1A),
    surface             = Color(0xFF0A1428),
    text                = Color(0xFFE8DFC0),
    textSec             = Color(0xFF9BA8C0),
    accent              = Color(0xFFC9A84C),
    quoteBg             = Color(0xFF0E1E38),
    gold                = Color(0xFFC9A84C),
    dropCapColor        = Color(0xFFC9A84C),
    dividerColor        = Color(0xFF2A3A5A),
    headerGradientStart = Color(0xFF1A2A5A),
    headerGradientEnd   = Color(0xFF050A1A)
)

fun ReadingTheme.colors() = when (this) {
    ReadingTheme.LIGHT -> parchment()
    ReadingTheme.DARK  -> imperial()
    ReadingTheme.SEPIA -> palace()
}

// ── Typography ────────────────────────────────────────────────────────────────
data class ReaderTypography(
    val title: TextStyle, val heading: TextStyle,
    val body: TextStyle, val quote: TextStyle, val caption: TextStyle,
    val dropCap: TextStyle, val chapterNumber: TextStyle
)

fun buildTypography() = ReaderTypography(
    title         = TextStyle(fontFamily = NotoSerifTamil, fontSize = 28.sp, fontWeight = FontWeight.Bold,     lineHeight = 40.sp, letterSpacing = 0.sp),
    heading       = TextStyle(fontFamily = NotoSerifTamil, fontSize = 20.sp, fontWeight = FontWeight.Bold,     lineHeight = 32.sp, letterSpacing = 0.sp),
    body          = TextStyle(fontFamily = NotoSerifTamil, fontSize = 18.sp, fontWeight = FontWeight.Normal,   lineHeight = 36.sp, letterSpacing = 0.3.sp),
    quote         = TextStyle(fontFamily = NotoSerifTamil, fontSize = 17.sp, fontWeight = FontWeight.Normal,   lineHeight = 34.sp, letterSpacing = 0.sp),
    caption       = TextStyle(fontFamily = NotoSerifTamil, fontSize = 13.sp, fontWeight = FontWeight.Normal,   lineHeight = 20.sp),
    dropCap       = TextStyle(fontFamily = NotoSerifTamil, fontSize = 64.sp, fontWeight = FontWeight.Bold,     lineHeight = 56.sp),
    chapterNumber = TextStyle(fontFamily = NotoSerifTamil, fontSize = 48.sp, fontWeight = FontWeight.Bold,     lineHeight = 56.sp)
)

val LocalReaderColors     = staticCompositionLocalOf { imperial() }
val LocalReaderTypography = staticCompositionLocalOf { buildTypography() }

@Composable
fun TamilReaderTheme(theme: ReadingTheme, content: @Composable () -> Unit) {
    val c = theme.colors()
    val scheme = if (theme == ReadingTheme.LIGHT)
        lightColorScheme(background = c.bg, surface = c.surface, primary = c.accent)
    else
        darkColorScheme(background = c.bg, surface = c.surface, primary = c.accent)

    CompositionLocalProvider(
        LocalReaderColors     provides c,
        LocalReaderTypography provides buildTypography()
    ) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
