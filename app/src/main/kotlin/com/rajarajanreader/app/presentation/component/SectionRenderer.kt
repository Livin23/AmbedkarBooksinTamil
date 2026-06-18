package com.livin.ambedkarindhiavilsathigal.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.offset
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.livin.ambedkarindhiavilsathigal.domain.Section
import com.livin.ambedkarindhiavilsathigal.domain.SectionType
import com.livin.ambedkarindhiavilsathigal.presentation.theme.LocalReaderColors
import com.livin.ambedkarindhiavilsathigal.presentation.theme.LocalReaderTypography

@Composable
fun SectionRenderer(
    section: Section,
    modifier: Modifier = Modifier,
    isFirstParagraph: Boolean = false,
    fontSizeSp: Float = 18f
) {
    val c = LocalReaderColors.current
    val t = LocalReaderTypography.current

    val bodyStyle  = t.body.copy(fontSize = fontSizeSp.sp, lineHeight = (fontSizeSp * 1.9f).sp)
    val quoteStyle = t.quote.copy(fontSize = (fontSizeSp - 1f).sp, lineHeight = (fontSizeSp * 1.85f).sp)

    when (section.type) {

        SectionType.HEADING -> {
            Column(modifier = modifier.padding(top = 28.dp, bottom = 10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(28.dp).height(1.dp).background(c.gold.copy(0.6f)))
                    Spacer(Modifier.width(8.dp))
                    Text("✦", color = c.gold, fontSize = 11.sp)
                    Spacer(Modifier.width(8.dp))
                    Box(Modifier.weight(1f).height(1.dp).background(c.gold.copy(0.25f)))
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text  = section.content,
                    style = t.heading.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize   = (fontSizeSp + 2f).sp,
                        lineHeight = ((fontSizeSp + 2f) * 1.5f).sp
                    ),
                    color = c.gold
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier.width(44.dp).height(2.dp).background(
                        Brush.horizontalGradient(listOf(c.gold, c.gold.copy(0f)))
                    )
                )
            }
        }

        SectionType.PARAGRAPH -> {
            if (isFirstParagraph && section.content.length > 2) {
                val firstChar  = section.content.first().toString()
                val restOfText = section.content.drop(1)
                Row(
                    modifier          = modifier.padding(vertical = 10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text     = firstChar,
                        style    = t.dropCap.copy(fontSize = (fontSizeSp * 3.4f).sp, lineHeight = (fontSizeSp * 2.8f).sp),
                        color    = c.dropCapColor,
                        modifier = Modifier.padding(end = 4.dp, top = 2.dp)
                    )
                    Text(text = restOfText, style = bodyStyle, color = c.text)
                }
            } else {
                Text(
                    text     = section.content,
                    style    = bodyStyle,
                    color    = c.text,
                    modifier = modifier.padding(vertical = 9.dp)
                )
            }
        }

        SectionType.LIST_ITEM -> {
            Row(
                modifier          = modifier.padding(vertical = 5.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = (fontSizeSp * 0.55f).dp, end = 10.dp)
                        .size(5.dp)
                        .background(c.gold.copy(0.7f), CircleShape)
                )
                Text(
                    text  = section.content,
                    style = bodyStyle.copy(lineHeight = (fontSizeSp * 1.7f).sp),
                    color = c.text
                )
            }
        }

        SectionType.QUOTE -> {
            Surface(
                shape    = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp, bottomStart = 4.dp),
                color    = c.quoteBg,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
                    .drawBehind {
                        drawLine(
                            color       = c.gold,
                            start       = Offset(0f, 0f),
                            end         = Offset(0f, size.height),
                            strokeWidth = 3.dp.toPx()
                        )
                    }
            ) {
                Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp)) {
                    Text(
                        text     = "“",
                        style    = t.dropCap.copy(fontSize = (fontSizeSp * 2.8f).sp, lineHeight = (fontSizeSp * 2.4f).sp),
                        color    = c.gold.copy(alpha = 0.4f),
                        modifier = Modifier.padding(end = 8.dp).offset(y = (-4).dp)
                    )
                    Text(
                        text  = buildAnnotatedString {
                            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                            append(section.content)
                            pop()
                        },
                        style = quoteStyle,
                        color = c.text
                    )
                }
            }
        }

        SectionType.VERSE -> {
            Surface(
                shape  = RoundedCornerShape(4.dp),
                color  = c.gold.copy(alpha = 0.06f),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .drawBehind {
                        drawLine(
                            color       = c.gold.copy(alpha = 0.7f),
                            start       = Offset(0f, 0f),
                            end         = Offset(0f, size.height),
                            strokeWidth = 3.5.dp.toPx()
                        )
                    }
            ) {
                Column(modifier = Modifier.padding(start = 18.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)) {
                    Text(
                        text  = "“",
                        color = c.gold.copy(0.35f),
                        fontSize = (fontSizeSp * 2.2f).sp,
                        lineHeight = (fontSizeSp * 1.6f).sp,
                        modifier = Modifier.offset(y = (-4).dp)
                    )
                    Text(
                        text  = buildAnnotatedString {
                            pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                            append(section.content)
                            pop()
                        },
                        style = bodyStyle.copy(
                            fontSize   = (fontSizeSp - 0.5f).sp,
                            lineHeight = (fontSizeSp * 2.0f).sp
                        ),
                        color = c.text
                    )
                }
            }
        }

        SectionType.VERSE_ATTRIBUTION -> {
            Text(
                text      = "— ${section.content}",
                style     = bodyStyle.copy(
                    fontSize   = (fontSizeSp - 3f).sp,
                    fontStyle  = FontStyle.Italic,
                    lineHeight = (fontSizeSp * 1.4f).sp
                ),
                color     = c.gold.copy(0.75f),
                textAlign = TextAlign.End,
                modifier  = modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 10.dp, end = 4.dp)
            )
        }

        SectionType.VERSE_MEANING -> {
            Surface(
                shape  = RoundedCornerShape(6.dp),
                color  = c.gold.copy(alpha = 0.04f),
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Text(
                        text  = "பொருள்",
                        style = bodyStyle.copy(
                            fontSize   = (fontSizeSp - 4f).sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = c.gold.copy(0.6f)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = section.content,
                        style = bodyStyle.copy(
                            fontSize   = (fontSizeSp - 2f).sp,
                            lineHeight = (fontSizeSp * 1.7f).sp
                        ),
                        color = c.text.copy(alpha = 0.82f)
                    )
                }
            }
        }

        SectionType.IMAGE -> {
            val ctx = LocalContext.current
            Column(
                modifier            = modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(ctx)
                        .data("file:///android_asset/images/${section.content}")
                        .crossfade(true)
                        .build(),
                    contentDescription = section.caption.ifEmpty { null },
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                )
                if (section.caption.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text      = section.caption,
                        style     = bodyStyle.copy(
                            fontSize   = (fontSizeSp - 4f).sp,
                            fontStyle  = FontStyle.Italic,
                            lineHeight = (fontSizeSp * 1.4f).sp
                        ),
                        color     = c.textSec,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OrnamentalSectionDivider(modifier: Modifier = Modifier) {
    val c = LocalReaderColors.current
    Row(
        modifier              = modifier.fillMaxWidth().padding(vertical = 20.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(Modifier.weight(1f).height(0.5.dp).background(c.dividerColor.copy(0.4f)))
        Text("  ❖ ❖ ❖  ", color = c.gold.copy(0.6f), fontSize = 12.sp)
        Box(Modifier.weight(1f).height(0.5.dp).background(c.dividerColor.copy(0.4f)))
    }
}
