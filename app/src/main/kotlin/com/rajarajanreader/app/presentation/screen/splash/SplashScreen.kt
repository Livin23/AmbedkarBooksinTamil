package com.rajarajanreader.app.presentation.screen.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rajarajanreader.app.R
import com.rajarajanreader.app.presentation.theme.LocalReaderColors
import com.rajarajanreader.app.presentation.theme.LocalReaderTypography
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onStart: () -> Unit, vm: SplashViewModel = viewModel()) {
    val state   by vm.state.collectAsStateWithLifecycle()
    val c       = LocalReaderColors.current
    val t       = LocalReaderTypography.current
    var visible by remember { mutableStateOf(false) }

    // Subtle pulsing glow on the cover image
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.0f, targetValue = 0.18f,
        animationSpec = infiniteRepeatable(tween(2400, easing = EaseInOutSine), RepeatMode.Reverse),
        label = "glowAlpha"
    )

    LaunchedEffect(state) {
        if (state is SplashState.Ready) { delay(200); visible = true }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(c.headerGradientStart, c.bg, c.bg))),
        contentAlignment = Alignment.Center
    ) {
        when (val s = state) {
            is SplashState.Loading -> CircularProgressIndicator(color = c.accent)
            is SplashState.Error   -> Text(s.msg, color = MaterialTheme.colorScheme.error)
            is SplashState.Ready   -> AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(900)) + slideInVertically(tween(800)) { it / 5 }
            ) {
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // ── Mural hero image ────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .shadow(24.dp, RoundedCornerShape(16.dp))
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        Image(
                            painter            = painterResource(R.drawable.mural_hero),
                            contentDescription = "royal mural",
                            contentScale       = ContentScale.Crop,
                            modifier           = Modifier.fillMaxSize()
                        )
                        // Deep vignette at bottom so title sits on it
                        Box(
                            Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, Color.Transparent, c.bg.copy(alpha = 0.55f))
                                )
                            )
                        )
                        // Subtle gold shimmer overlay
                        Box(
                            Modifier.fillMaxSize().background(c.gold.copy(alpha = glowAlpha * 0.5f))
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // ── Ornamental divider ──────────────────────────────────
                    OrnamentalDivider(color = c.gold, modifier = Modifier.padding(bottom = 20.dp))

                    // ── Title ───────────────────────────────────────────────
                    Text(
                        text      = s.book.title,
                        style     = t.title.copy(fontSize = 26.sp, lineHeight = 38.sp),
                        color     = c.gold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(6.dp))

                    // ── Author ──────────────────────────────────────────────
                    Text(
                        text      = s.book.author,
                        style     = t.caption.copy(fontSize = 14.sp, letterSpacing = 1.sp),
                        color     = c.gold.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )

                    // ── Era badge ───────────────────────────────────────────
                    Surface(
                        shape    = RoundedCornerShape(20.dp),
                        color    = c.gold.copy(alpha = 0.15f),
                        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                    ) {
                        Text(
                            text     = "RAJA RAJA-I  •  ${s.book.totalChapters} அத்தியாயங்கள்",
                            style    = t.caption.copy(fontSize = 12.sp),
                            color    = c.gold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }

                    Spacer(Modifier.height(28.dp))

                    OrnamentalDivider(color = c.gold.copy(alpha = 0.5f), modifier = Modifier.padding(bottom = 28.dp))

                    // ── Start button ────────────────────────────────────────
                    Button(
                        onClick  = onStart,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape    = RoundedCornerShape(16.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = c.gold,
                            contentColor   = Color(0xFF1A000A)
                        ),
                        elevation = ButtonDefaults.buttonElevation(8.dp)
                    ) {
                        Text(
                            "படிக்கத் தொடங்கு",
                            style = t.body.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                            color = Color(0xFF1A000A)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrnamentalDivider(color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = color.copy(alpha = 0.4f), thickness = 0.5.dp)
        Text("  ✦  ❧  ✦  ", color = color, fontSize = 14.sp)
        HorizontalDivider(modifier = Modifier.weight(1f), color = color.copy(alpha = 0.4f), thickness = 0.5.dp)
    }
}
