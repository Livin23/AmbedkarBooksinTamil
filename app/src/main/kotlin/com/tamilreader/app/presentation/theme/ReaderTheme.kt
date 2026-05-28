package com.tamilbookreader.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.tamilbookreader.app.domain.ReadingTheme

// ── Palette ──────────────────────────────────────────────────────────
data class ReaderColors(
    val bg: Color, val surface: Color, val text: Color,
    val textSec: Color, val accent: Color, val quoteBg: Color
)

private fun light()  = ReaderColors(Color(0xFFFAFAFA), Color(0xFFFFFFFF), Color(0xFF1C1B1F), Color(0xFF49454F), Color(0xFF6750A4), Color(0xFFF3EDF7))
private fun dark()   = ReaderColors(Color(0xFF121212), Color(0xFF1E1E1E), Color(0xFFE6E1E5), Color(0xFFCAC4D0), Color(0xFFD0BCFF), Color(0xFF2A2A2A))
private fun sepia()  = ReaderColors(Color(0xFFF5E6C8), Color(0xFFFBF0DA), Color(0xFF3E2723), Color(0xFF5D4037), Color(0xFF8D6E63), Color(0xFFEDD9A3))

fun ReadingTheme.colors() = when (this) {
    ReadingTheme.LIGHT -> light()
    ReadingTheme.DARK  -> dark()
    ReadingTheme.SEPIA -> sepia()
}

// ── Typography ───────────────────────────────────────────────────────
data class ReaderTypography(
    val title: TextStyle, val heading: TextStyle,
    val body: TextStyle, val quote: TextStyle, val caption: TextStyle
)

fun buildTypography() = ReaderTypography(
    title   = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold,   lineHeight = 36.sp, letterSpacing = 0.sp),
    heading = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium, lineHeight = 30.sp, letterSpacing = 0.sp),
    body    = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Normal, lineHeight = 32.sp, letterSpacing = 0.2.sp),
    quote   = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Normal, lineHeight = 30.sp, letterSpacing = 0.sp),
    caption = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, lineHeight = 20.sp)
)

// ── Composition locals ────────────────────────────────────────────────
val LocalReaderColors     = staticCompositionLocalOf { light() }
val LocalReaderTypography = staticCompositionLocalOf { buildTypography() }

// ── Root theme composable ─────────────────────────────────────────────
@Composable
fun TamilReaderTheme(theme: ReadingTheme, content: @Composable () -> Unit) {
    val c = theme.colors()
    val scheme = if (theme == ReadingTheme.DARK)
        darkColorScheme(background = c.bg, surface = c.surface, primary = c.accent)
    else
        lightColorScheme(background = c.bg, surface = c.surface, primary = c.accent)

    CompositionLocalProvider(
        LocalReaderColors     provides c,
        LocalReaderTypography provides buildTypography()
    ) {
        MaterialTheme(colorScheme = scheme, content = content)
    }
}
