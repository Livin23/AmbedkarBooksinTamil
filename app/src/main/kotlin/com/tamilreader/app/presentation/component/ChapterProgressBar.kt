package com.tamilbookreader.app.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tamilbookreader.app.presentation.theme.LocalReaderColors
import com.tamilbookreader.app.presentation.theme.LocalReaderTypography

@Composable
fun ChapterProgressBar(current: Int, total: Int) {
    val c = LocalReaderColors.current
    val t = LocalReaderTypography.current
    val progress by animateFloatAsState(
        targetValue = if (total > 0) (current + 1f) / total else 0f,
        label = "progress"
    )
    Column(Modifier.fillMaxWidth()) {
        LinearProgressIndicator(
            progress   = { progress },
            modifier   = Modifier.fillMaxWidth(),
            color      = c.accent,
            trackColor = c.surface
        )
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically
        ) {
            Text("அத்தியாயம் ${current + 1} / $total", style = t.caption, color = c.textSec)
            Text("${((current + 1f) / total * 100).toInt()}%", style = t.caption, color = c.textSec)
        }
    }
}
