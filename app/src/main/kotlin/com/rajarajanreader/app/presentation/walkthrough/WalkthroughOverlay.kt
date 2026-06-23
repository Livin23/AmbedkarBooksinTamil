package com.livin.ambedkarindhiavilsathigal.presentation.walkthrough

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.livin.ambedkarindhiavilsathigal.presentation.theme.LocalReaderColors
import com.livin.ambedkarindhiavilsathigal.presentation.theme.LocalReaderTypography
import kotlin.math.roundToInt

private val SPOTLIGHT_PAD_PX = 14f
private val TOOLTIP_MARGIN_PX = 20f

@Composable
fun WalkthroughOverlay(
    steps    : List<WalkthroughStep>,
    anchors  : Map<String, Rect>,
    onComplete: () -> Unit
) {
    var stepIndex by remember { mutableIntStateOf(0) }
    val step = steps.getOrNull(stepIndex) ?: return

    val c       = LocalReaderColors.current
    val t       = LocalReaderTypography.current
    val density = LocalDensity.current

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "wt_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.9f, targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOut), RepeatMode.Reverse),
        label = "pulse_alpha"
    )
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.18f,
        animationSpec = infiniteRepeatable(tween(900, easing = EaseInOut), RepeatMode.Reverse),
        label = "pulse_scale"
    )

    val spotlightRect: Rect? = step.anchorKey?.let { anchors[it] }

    // Expanded spotlight rect with padding
    val expandedRect: Rect? = spotlightRect?.let {
        Rect(
            it.left   - SPOTLIGHT_PAD_PX,
            it.top    - SPOTLIGHT_PAD_PX,
            it.right  + SPOTLIGHT_PAD_PX,
            it.bottom + SPOTLIGHT_PAD_PX
        )
    }

    fun advance() {
        if (stepIndex < steps.lastIndex) stepIndex++ else onComplete()
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                indication     = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { advance() }
    ) {
        val screenH = constraints.maxHeight.toFloat()
        val screenW = constraints.maxWidth.toFloat()

        // ── Semi-transparent overlay with spotlight cutout ──────────────────
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            drawRect(color = Color.Black.copy(alpha = 0.78f))
            if (expandedRect != null) {
                when (step.highlightShape) {
                    HighlightShape.CIRCLE -> {
                        val cx = expandedRect.center.x
                        val cy = expandedRect.center.y
                        val r  = maxOf(expandedRect.width, expandedRect.height) / 2f
                        drawCircle(Color.Transparent, radius = r * pulseScale, center = Offset(cx, cy), blendMode = BlendMode.Clear)
                    }
                    HighlightShape.ROUND_RECT -> {
                        drawRoundRect(
                            color       = Color.Transparent,
                            topLeft     = Offset(expandedRect.left, expandedRect.top),
                            size        = Size(expandedRect.width, expandedRect.height),
                            cornerRadius = CornerRadius(24f),
                            blendMode   = BlendMode.Clear
                        )
                    }
                }
            }
        }

        // ── Pulsing gold ring around spotlight ──────────────────────────────
        if (expandedRect != null) {
            val ringAlpha = pulseAlpha
            val ringScale = pulseScale
            val cx = with(density) { expandedRect.center.x.toDp() }
            val cy = with(density) { expandedRect.center.y.toDp() }

            when (step.highlightShape) {
                HighlightShape.CIRCLE -> {
                    val baseR = with(density) { (maxOf(expandedRect.width, expandedRect.height) / 2f).toDp() }
                    val ringR = baseR * ringScale + 6.dp
                    Box(
                        modifier = Modifier
                            .size(ringR * 2)
                            .offset(cx - ringR, cy - ringR)
                            .graphicsLayer { alpha = ringAlpha }
                            .drawBehind {
                                drawCircle(
                                    color  = Color(0xFFD4A017),
                                    radius = size.minDimension / 2f,
                                    style  = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                                )
                            }
                    )
                }
                HighlightShape.ROUND_RECT -> {
                    val rW = with(density) { expandedRect.width.toDp() } * ringScale
                    val rH = with(density) { expandedRect.height.toDp() }
                    val rX = with(density) { expandedRect.left.toDp() } - (rW - with(density) { expandedRect.width.toDp() }) / 2
                    val rY = with(density) { expandedRect.top.toDp() } - 4.dp
                    Box(
                        modifier = Modifier
                            .width(rW)
                            .height(rH + 8.dp)
                            .offset(rX, rY)
                            .graphicsLayer { alpha = ringAlpha }
                            .drawBehind {
                                drawRoundRect(
                                    color  = Color(0xFFD4A017),
                                    cornerRadius = CornerRadius(26f),
                                    style  = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                                )
                            }
                    )
                }
            }
        }

        // ── Tooltip card placement ───────────────────────────────────────────
        val cardHorizPad = 24.dp
        val cardWidth    = maxWidth - cardHorizPad * 2

        val tooltipTopPx: Float = if (expandedRect == null) {
            // Welcome step — center vertically
            screenH / 2f - with(density) { 110.dp.toPx() }
        } else {
            val belowY = expandedRect.bottom + TOOLTIP_MARGIN_PX
            val aboveY = expandedRect.top - TOOLTIP_MARGIN_PX - with(density) { 160.dp.toPx() }
            if (step.tooltipBelow && belowY + with(density) { 160.dp.toPx() } < screenH) belowY
            else if (aboveY > 0f) aboveY
            else belowY.coerceAtMost(screenH - with(density) { 160.dp.toPx() } - 80f)
        }

        Box(
            modifier = Modifier
                .width(cardWidth)
                .offset {
                    IntOffset(
                        x = with(density) { cardHorizPad.toPx() }.roundToInt(),
                        y = tooltipTopPx.roundToInt()
                    )
                }
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1A0008), Color(0xFF0D0005)))
                )
                .clickable(
                    indication        = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { }  // consume click so it doesn't advance prematurely
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text      = step.title,
                    style     = t.heading.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color     = Color(0xFFD4A017),
                    textAlign = TextAlign.Start
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = step.body,
                    style = t.body.copy(fontSize = 15.sp, lineHeight = 26.sp),
                    color = Color.White.copy(alpha = 0.88f)
                )
                Spacer(Modifier.height(14.dp))

                // Step dots
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    steps.forEachIndexed { i, _ ->
                        val active = i == stepIndex
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (active) 8.dp else 5.dp)
                                .background(
                                    if (active) Color(0xFFD4A017) else Color.White.copy(0.35f),
                                    CircleShape
                                )
                        )
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Buttons row
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onComplete) {
                        Text("தவிர்", color = Color.White.copy(0.5f), fontSize = 13.sp)
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFD4A017))
                            .clickable(
                                indication        = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { advance() }
                            .padding(horizontal = 22.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text  = if (stepIndex == steps.lastIndex) "முடிந்தது" else "அடுத்தது →",
                            color = Color(0xFF0D0005),
                            style = t.body.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        )
                    }
                }
            }
        }
    }
}
