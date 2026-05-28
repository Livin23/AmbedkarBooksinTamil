package com.tamilbookreader.app.presentation.screen.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tamilbookreader.app.R
import com.tamilbookreader.app.presentation.theme.LocalReaderColors
import com.tamilbookreader.app.presentation.theme.LocalReaderTypography
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onStart: () -> Unit, vm: SplashViewModel = viewModel()) {
    val state  by vm.state.collectAsStateWithLifecycle()
    val c      = LocalReaderColors.current
    val t      = LocalReaderTypography.current
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is SplashState.Ready) { delay(300); visible = true }
    }

    Box(Modifier.fillMaxSize().background(c.bg), Alignment.Center) {
        when (val s = state) {
            is SplashState.Loading -> CircularProgressIndicator(color = c.accent)
            is SplashState.Error   -> Text(s.msg, color = MaterialTheme.colorScheme.error)
            is SplashState.Ready   -> AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }
            ) {
                Column(
                    Modifier.padding(36.dp),
                    horizontalAlignment   = Alignment.CenterHorizontally,
                    verticalArrangement   = Arrangement.spacedBy(28.dp)
                ) {
                    // Book cover — Ambedkar portrait with gradient overlay
                    Box(
                        Modifier
                            .width(180.dp)
                            .height(260.dp)
                            .clip(RoundedCornerShape(16.dp))
                    ) {
                        androidx.compose.foundation.Image(
                            painter          = painterResource(R.drawable.ambedkar_portrait),
                            contentDescription = "Dr. B.R. Ambedkar",
                            contentScale     = ContentScale.Crop,
                            modifier         = Modifier.fillMaxSize()
                        )
                        // gradient scrim so title text is readable
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .align(Alignment.BottomCenter)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                                    )
                                )
                        )
                        Text(
                            text      = s.book.title,
                            style     = t.caption.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp, lineHeight = 20.sp),
                            color     = Color.White,
                            textAlign = TextAlign.Center,
                            modifier  = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(horizontal = 10.dp, vertical = 10.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(s.book.title,  style = t.title,   color = c.text, textAlign = TextAlign.Center)
                        Text(
                            s.book.author,
                            style = t.caption.copy(fontWeight = FontWeight.Medium, fontSize = 14.sp),
                            color = c.text.copy(alpha = 0.75f)
                        )
                        Text(
                            "${s.book.totalChapters} அத்தியாயங்கள்",
                            style = t.caption.copy(fontWeight = FontWeight.Medium, fontSize = 13.sp),
                            color = c.accent
                        )
                    }

                    Button(
                        onClick  = onStart,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = c.accent)
                    ) {
                        Text("படிக்கத் தொடங்கு",
                            style  = t.body.copy(fontWeight = FontWeight.SemiBold),
                            color  = Color.White
                        )
                    }
                }
            }
        }
    }
}
