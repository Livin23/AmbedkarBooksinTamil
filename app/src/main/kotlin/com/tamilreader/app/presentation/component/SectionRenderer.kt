package com.tamilbookreader.app.presentation.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.tamilbookreader.app.domain.Section
import com.tamilbookreader.app.domain.SectionType
import com.tamilbookreader.app.presentation.theme.LocalReaderColors
import com.tamilbookreader.app.presentation.theme.LocalReaderTypography

@Composable
fun SectionRenderer(section: Section, modifier: Modifier = Modifier) {
    val c = LocalReaderColors.current
    val t = LocalReaderTypography.current

    when (section.type) {
        SectionType.HEADING -> Text(
            text     = section.content,
            style    = t.heading,
            color    = c.text,
            modifier = modifier.padding(top = 24.dp, bottom = 8.dp)
        )

        SectionType.PARAGRAPH -> Text(
            text     = section.content,
            style    = t.body,
            color    = c.text,
            modifier = modifier.padding(vertical = 8.dp)
        )

        SectionType.QUOTE -> Card(
            shape    = RoundedCornerShape(8.dp),
            colors   = CardDefaults.cardColors(containerColor = c.quoteBg),
            modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)
        ) {
            Text(
                text     = "“${section.content}”",
                style    = t.quote.copy(fontStyle = FontStyle.Italic),
                color    = c.text,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}
