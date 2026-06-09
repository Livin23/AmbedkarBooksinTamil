package com.rajarajanreader.app.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.rajarajanreader.app.presentation.theme.LocalReaderColors
import com.rajarajanreader.app.presentation.theme.LocalReaderTypography

@Composable
fun ChapterProgressBar(current: Int, total: Int) {
    val c        = LocalReaderColors.current
    val t        = LocalReaderTypography.current
    val progress by animateFloatAsState(
        targetValue    = if (total > 0) (current + 1f) / total else 0f,
        animationSpec  = tween(600),
        label          = "progress"
    )

    Column(Modifier.fillMaxWidth()) {
        // Golden gradient progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(c.surface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(c.gold.copy(0.6f), c.gold, c.gold.copy(0.8f))
                        )
                    )
            )
        }
        Row(
            modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Text(
                "அத்தியாயம் ${current + 1} / $total",
                style = t.caption,
                color = c.textSec
            )
            Text(
                "${((current + 1f) / total * 100).toInt()}%",
                style = t.caption,
                color = c.gold.copy(alpha = 0.8f)
            )
        }
    }
}
