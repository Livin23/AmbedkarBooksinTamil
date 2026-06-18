package com.livin.ambedkarindhiavilsathigal.presentation.screen.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livin.ambedkarindhiavilsathigal.BookReaderApp
import com.livin.ambedkarindhiavilsathigal.presentation.theme.LocalReaderColors
import com.livin.ambedkarindhiavilsathigal.presentation.theme.LocalReaderTypography
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val body: String
)

private val pages = listOf(
    OnboardingPage(
        icon     = Icons.AutoMirrored.Filled.MenuBook,
        title    = "வரலாற்று படைப்பு",
        subtitle = "இந்தியாவில் சாதிகள்",
        body     = "டாக்டர் பி.ஆர். அம்பேத்கர் எழுதிய இந்த அரிய நூலை தமிழில் படிக்கும் வாய்ப்பு இப்போது உங்கள் கைகளில்."
    ),
    OnboardingPage(
        icon     = Icons.Default.Palette,
        title    = "படிப்பு அனுபவம்",
        subtitle = "உங்களுக்கு ஏற்ற தோற்றம்",
        body     = "மூன்று அழகான தீம்களில் படிக்கலாம் — இருண்ட பயன்முறை, பழந்தாள் நிறம், அல்லது இரவு நீலம். எழுத்து அளவையும் மாற்றலாம்."
    ),
    OnboardingPage(
        icon     = Icons.Default.Explore,
        title    = "எளிதான வழிசெலுத்தல்",
        subtitle = "தேடல் • புக்மார்க் • அத்தியாயம்",
        body     = "அட்டவணையில் நேரடியாக அத்தியாயம் தேர்வு செய்யலாம். புக்மார்க் வைத்துத் தொடர்ந்து படிக்கலாம். தேடல் மூலம் உள்ளடக்கம் கண்டுபிடிக்கலாம்."
    )
)

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val c          = LocalReaderColors.current
    val t          = LocalReaderTypography.current
    val context    = LocalContext.current
    val pagerState = rememberPagerState { pages.size }
    val scope      = rememberCoroutineScope()

    val isLastPage = pagerState.currentPage == pages.lastIndex

    fun complete() {
        (context.applicationContext as BookReaderApp).markOnboardingShown()
        onDone()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(c.headerGradientStart, c.bg, c.bg)))
    ) {
        TextButton(
            onClick  = ::complete,
            modifier = Modifier.align(Alignment.TopEnd).padding(top = 48.dp, end = 16.dp)
        ) {
            Text("தவிர்", color = c.gold.copy(alpha = 0.65f), style = t.caption.copy(fontSize = 14.sp))
        }

        HorizontalPager(
            state    = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 56.dp, bottom = 160.dp)
        ) { page ->
            OnboardingPage(page = pages[page])
        }

        Column(
            modifier            = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 32.dp, end = 32.dp, bottom = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(pages.size) { idx ->
                    val active   = pagerState.currentPage == idx
                    val dotWidth by animateDpAsState(if (active) 28.dp else 8.dp, tween(300), label = "dotW")
                    val dotColor by animateColorAsState(
                        targetValue   = if (active) c.gold else c.gold.copy(alpha = 0.3f),
                        animationSpec = tween(300),
                        label         = "dotC"
                    )
                    Box(
                        modifier = Modifier
                            .size(width = dotWidth, height = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(dotColor)
                    )
                }
            }

            Button(
                onClick = {
                    if (isLastPage) complete()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                },
                modifier  = Modifier.fillMaxWidth().height(56.dp),
                shape     = RoundedCornerShape(16.dp),
                colors    = ButtonDefaults.buttonColors(containerColor = c.gold, contentColor = Color(0xFF1A000A)),
                elevation = ButtonDefaults.buttonElevation(8.dp)
            ) {
                Text(
                    text  = if (isLastPage) "படிக்கத் தொடங்கு" else "அடுத்து  →",
                    style = t.body.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    color = Color(0xFF1A000A)
                )
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage) {
    val c = LocalReaderColors.current
    val t = LocalReaderTypography.current

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier        = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(c.gold.copy(alpha = 0.12f))
                .border(1.dp, c.gold.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = page.icon,
                contentDescription = null,
                tint               = c.gold,
                modifier           = Modifier.size(60.dp)
            )
        }

        Spacer(Modifier.height(32.dp))

        Text(
            text      = page.title,
            style     = t.heading.copy(fontSize = 22.sp),
            color     = c.gold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text      = page.subtitle,
            style     = t.caption.copy(fontSize = 13.sp, letterSpacing = 0.8.sp),
            color     = c.gold.copy(alpha = 0.65f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        HorizontalDivider(
            modifier  = Modifier.fillMaxWidth(0.55f),
            color     = c.gold.copy(alpha = 0.3f),
            thickness = 0.5.dp
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text      = page.body,
            style     = t.body.copy(fontSize = 17.sp, lineHeight = 32.sp),
            color     = c.text,
            textAlign = TextAlign.Center
        )
    }
}
