package com.livin.ambedkarindhiavilsathigal.presentation.screen.index

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.livin.ambedkarindhiavilsathigal.BookReaderApp
import com.livin.ambedkarindhiavilsathigal.domain.BookPart
import com.livin.ambedkarindhiavilsathigal.domain.Chapter
import com.livin.ambedkarindhiavilsathigal.presentation.theme.LocalReaderColors
import com.livin.ambedkarindhiavilsathigal.presentation.theme.LocalReaderTypography
import com.livin.ambedkarindhiavilsathigal.presentation.walkthrough.WalkthroughOverlay
import com.livin.ambedkarindhiavilsathigal.presentation.walkthrough.indexWalkthroughSteps

sealed class IndexItem {
    data class Part(val part: BookPart) : IndexItem()
    data class Ch(val chapter: Chapter, val globalIdx: Int) : IndexItem()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexScreen(
    onChapterClick: (Int) -> Unit,
    onThemeCycle  : () -> Unit,
    onOpenSearch  : () -> Unit,
    vm            : IndexViewModel = viewModel()
) {
    val state     by vm.state.collectAsStateWithLifecycle()
    val c         = LocalReaderColors.current
    val t         = LocalReaderTypography.current
    val listState = rememberLazyListState()
    val context   = LocalContext.current
    val app       = context.applicationContext as BookReaderApp

    val walkthroughDone by app.indexWalkthroughDone.collectAsStateWithLifecycle()
    val anchors = remember { mutableStateMapOf<String, Rect>() }

    val firstVisibleIndex by remember { derivedStateOf { listState.firstVisibleItemIndex } }
    val headerExpanded     = firstVisibleIndex == 0

    // Build flat list: parts interleaved with chapters
    val items: List<IndexItem> = remember(state.book) {
        val book = state.book ?: return@remember emptyList()
        val list = mutableListOf<IndexItem>()
        val parts = book.parts

        if (parts.isEmpty()) {
            book.chapters.forEachIndexed { i, ch -> list.add(IndexItem.Ch(ch, i)) }
        } else {
            var usedChapters = mutableSetOf<Int>()
            parts.forEach { part ->
                list.add(IndexItem.Part(part))
                (part.startChapterIdx..part.endChapterIdx).forEach { idx ->
                    book.chapters.getOrNull(idx)?.let { ch ->
                        list.add(IndexItem.Ch(ch, idx))
                        usedChapters.add(idx)
                    }
                }
            }
            // Append any chapters not covered by a part
            book.chapters.forEachIndexed { i, ch ->
                if (i !in usedChapters) list.add(IndexItem.Ch(ch, i))
            }
        }
        list
    }

    Box(modifier = Modifier.fillMaxSize().background(c.bg)) {
        Column {
            // ── Gradient hero header ───────────────────────────────────────
            AnimatedContent(
                targetState = headerExpanded,
                transitionSpec = { fadeIn(tween(300)) togetherWith fadeOut(tween(300)) },
                label = "header"
            ) { expanded ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (expanded) Modifier.height(210.dp) else Modifier.height(80.dp))
                        .background(Brush.verticalGradient(listOf(c.headerGradientStart, c.headerGradientEnd)))
                ) {
                    if (expanded) {
                        Column(
                            modifier            = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 20.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text("♛", fontSize = 28.sp, color = c.gold)
                                Row {
                                    IconButton(
                                        onClick  = onOpenSearch,
                                        modifier = Modifier.onGloballyPositioned { coords ->
                                            anchors["search_icon"] = coords.boundsInWindow()
                                        }
                                    ) {
                                        Icon(Icons.Filled.Search, "தேடு", tint = c.gold)
                                    }
                                    IconButton(
                                        onClick  = onThemeCycle,
                                        modifier = Modifier.onGloballyPositioned { coords ->
                                            anchors["theme_icon"] = coords.boundsInWindow()
                                        }
                                    ) {
                                        Icon(Icons.Filled.Brightness4, "கோலம்", tint = c.gold)
                                    }
                                }
                            }
                            Column {
                                Text(
                                    text      = state.book?.title ?: "",
                                    style     = t.title.copy(fontSize = 20.sp, lineHeight = 30.sp),
                                    color     = Color.White,
                                    textAlign = TextAlign.Start
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text  = "${state.book?.totalChapters ?: 0} அத்தியாயங்கள்",
                                    style = t.caption.copy(fontStyle = FontStyle.Italic, fontSize = 12.sp),
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier              = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("♛  ${state.book?.title ?: ""}", style = t.heading, color = Color.White, maxLines = 1)
                            Row {
                                IconButton(onClick = onOpenSearch) {
                                    Icon(Icons.Filled.Search, null, tint = c.gold)
                                }
                                IconButton(onClick = onThemeCycle) {
                                    Icon(Icons.Filled.Brightness4, null, tint = c.gold)
                                }
                            }
                        }
                    }
                }
            }

            // ── Chapter list ───────────────────────────────────────────────
            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator(color = c.accent)
                }
            } else {
                LazyColumn(
                    state               = listState,
                    modifier            = Modifier.fillMaxSize(),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    itemsIndexed(items, key = { i, item ->
                        when (item) {
                            is IndexItem.Part -> "part_${item.part.title}"
                            is IndexItem.Ch   -> "ch_${item.chapter.id}"
                        }
                    }) { listIdx, item ->
                        when (item) {
                            is IndexItem.Part -> PartHeader(item.part, modifier = Modifier.padding(top = if (listIdx == 0) 0.dp else 20.dp, bottom = 8.dp))
                            is IndexItem.Ch   -> {
                                val bookmarked = item.chapter.id in state.bookmarks
                                val resumeHere = state.lastChapter == item.globalIdx
                                val isFirstChapter = item.globalIdx == 0
                                ChapterCard(
                                    index      = item.globalIdx + 1,
                                    title      = item.chapter.title,
                                    bookmarked = bookmarked,
                                    resumeHere = resumeHere,
                                    onClick    = { onChapterClick(item.globalIdx) },
                                    delay      = (listIdx * 15).coerceAtMost(250),
                                    onPositioned = if (isFirstChapter) { rect ->
                                        anchors["first_chapter"] = rect
                                    } else null
                                )
                                Spacer(Modifier.height(10.dp))
                            }
                        }
                    }
                    item { Spacer(Modifier.height(24.dp)) }
                }
            }
        }

        // ── Walkthrough overlay (first-run only) ─────────────────────────────
        if (!walkthroughDone) {
            WalkthroughOverlay(
                steps     = indexWalkthroughSteps,
                anchors   = anchors,
                onComplete = { app.markIndexWalkthroughDone() }
            )
        }
    }
}

@Composable
private fun PartHeader(part: BookPart, modifier: Modifier = Modifier) {
    val c = LocalReaderColors.current
    val t = LocalReaderTypography.current

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).height(0.5.dp).background(c.gold.copy(0.3f)))
            Text("  ✦  ", color = c.gold.copy(0.5f), fontSize = 11.sp)
            Box(Modifier.weight(1f).height(0.5.dp).background(c.gold.copy(0.3f)))
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(c.gold.copy(0.12f))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text  = part.title,
                style = t.caption.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                color = c.gold
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ChapterCard(
    index       : Int,
    title       : String,
    bookmarked  : Boolean,
    resumeHere  : Boolean,
    onClick     : () -> Unit,
    delay       : Int,
    onPositioned: ((Rect) -> Unit)? = null
) {
    val c = LocalReaderColors.current
    val t = LocalReaderTypography.current

    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        appeared = true
    }

    AnimatedVisibility(
        visible = appeared,
        enter   = fadeIn(tween(400)) + slideInHorizontally(tween(400)) { -it / 5 }
    ) {
        Surface(
            modifier        = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .then(
                    if (onPositioned != null)
                        Modifier.onGloballyPositioned { coords -> onPositioned(coords.boundsInWindow()) }
                    else Modifier
                ),
            shape           = RoundedCornerShape(14.dp),
            color           = c.surface,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chapter number badge
                Box(
                    modifier         = Modifier
                        .width(56.dp)
                        .fillMaxHeight()
                        .background(
                            Brush.verticalGradient(listOf(c.gold.copy(0.25f), c.gold.copy(0.10f))),
                            RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp)
                        )
                        .padding(vertical = 18.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text      = "$index",
                        style     = t.body.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                        color     = c.gold,
                        textAlign = TextAlign.Center
                    )
                }

                Box(Modifier.width(2.dp).height(48.dp).background(c.gold.copy(alpha = 0.4f)))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 16.dp)
                ) {
                    Text(
                        text  = title,
                        style = t.body.copy(lineHeight = 26.sp, fontSize = 16.sp),
                        color = c.text
                    )
                    if (resumeHere) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "தொடர்ந்து படிக்க",
                            style = t.caption.copy(fontSize = 11.sp, fontStyle = FontStyle.Italic),
                            color = c.gold.copy(0.8f)
                        )
                    }
                }

                if (bookmarked) {
                    Icon(
                        imageVector        = Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint               = c.gold,
                        modifier           = Modifier.padding(end = 14.dp).size(20.dp)
                    )
                }
            }
        }
    }
}
