package com.livin.ambedkarindhiavilsathigal.presentation.screen.reader

import android.app.Activity
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.livin.ambedkarindhiavilsathigal.BookReaderApp
import com.livin.ambedkarindhiavilsathigal.domain.SectionType
import com.livin.ambedkarindhiavilsathigal.presentation.component.ChapterProgressBar
import com.livin.ambedkarindhiavilsathigal.presentation.component.OrnamentalSectionDivider
import com.livin.ambedkarindhiavilsathigal.presentation.component.SectionRenderer
import com.livin.ambedkarindhiavilsathigal.presentation.theme.LocalReaderColors
import com.livin.ambedkarindhiavilsathigal.presentation.theme.LocalReaderTypography
import com.livin.ambedkarindhiavilsathigal.presentation.walkthrough.WalkthroughOverlay
import com.livin.ambedkarindhiavilsathigal.presentation.walkthrough.readerWalkthroughSteps

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    chapterIndex: Int,
    onBack      : () -> Unit,
    onOpenIndex : () -> Unit,
    onOpenSearch: () -> Unit,
    onThemeCycle: () -> Unit,
    vm          : ReaderViewModel = viewModel()
) {
    val state     by vm.state.collectAsStateWithLifecycle()
    val c         = LocalReaderColors.current
    val t         = LocalReaderTypography.current
    val listState = rememberLazyListState()
    val context   = LocalContext.current
    var barsVisible    by remember { mutableStateOf(true) }
    var gestureEnabled by remember { mutableStateOf(false) }
    var swipeDragTotal by remember { mutableStateOf(0f) }

    val app = context.applicationContext as BookReaderApp
    val readerWalkthroughDone by app.readerWalkthroughDone.collectAsStateWithLifecycle()
    val wtAnchors = remember { mutableStateMapOf<String, Rect>() }

    val adManager = remember { app.adManager }

    LaunchedEffect(Unit) {
        vm.showAd.collect { targetIndex ->
            val activity = context as? Activity
            if (activity != null) {
                adManager.showIfReady(activity) { vm.loadChapterAfterAd(targetIndex) }
            } else {
                vm.loadChapterAfterAd(targetIndex)
            }
        }
    }

    LaunchedEffect(Unit) {
        vm.init(chapterIndex)
        kotlinx.coroutines.delay(400L)
        gestureEnabled = true
    }
    LaunchedEffect(state.currentIndex) { listState.scrollToItem(0) }

    // Shared tap handler — called from outer box AND from inside each section item
    val onContentTap: () -> Unit = {
        if (state.showFontPanel) {
            vm.dismissFontPanel()
            barsVisible = false
        } else if (!barsVisible) {
            barsVisible = true
        } else {
            barsVisible = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .systemBarsPadding()
            .pointerInput(gestureEnabled) {
                if (!gestureEnabled) return@pointerInput
                detectTapGestures { onContentTap() }
            }
            .pointerInput(gestureEnabled, state.currentIndex) {
                if (!gestureEnabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { swipeDragTotal = 0f },
                    onDragEnd   = {
                        val threshold = 80.dp.toPx()
                        if (swipeDragTotal < -threshold) {
                            vm.navigateTo(state.currentIndex + 1)
                        } else if (swipeDragTotal > threshold) {
                            vm.navigateTo(state.currentIndex - 1)
                        }
                        swipeDragTotal = 0f
                    },
                    onDragCancel = { swipeDragTotal = 0f },
                    onHorizontalDrag = { _, dragAmount -> swipeDragTotal += dragAmount }
                )
            }
    ) {
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CircularProgressIndicator(color = c.accent, strokeWidth = 3.dp)
                    Text("ஏற்றுகிறது…", style = t.caption, color = c.textSec)
                }
            }

            state.error != null -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
            }

            else -> {
                val sections = state.content?.sections ?: emptyList()
                var firstParaFound = false
                var paraCount = 0

                LazyColumn(
                    state          = listState,
                    modifier       = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp)
                        .onGloballyPositioned { coords ->
                            // Register center-of-screen as content_center anchor
                            val b = coords.boundsInWindow()
                            val cx = b.center.x
                            val cy = b.center.y
                            val half = 80f
                            wtAnchors["content_center"] = Rect(cx - half, cy - half, cx + half, cy + half)
                        },
                    contentPadding = PaddingValues(top = 96.dp, bottom = 100.dp)
                ) {
                    item {
                        Column(
                            modifier            = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text      = "அத்தியாயம் ${state.currentIndex + 1}",
                                style     = t.caption.copy(fontWeight = FontWeight.Medium),
                                color     = c.gold.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(6.dp))
                            state.content?.title?.let { title ->
                                Text(
                                    text      = title,
                                    style     = t.heading.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize   = (state.fontSizeSp + 4f).sp,
                                        lineHeight = ((state.fontSizeSp + 4f) * 1.4f).sp
                                    ),
                                    color     = c.gold,
                                    textAlign = TextAlign.Center
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                Box(Modifier.weight(1f).height(0.5.dp).background(c.gold.copy(0.3f)))
                                Text("  ✦  ", color = c.gold.copy(0.6f), fontSize = 14.sp)
                                Box(Modifier.weight(1f).height(0.5.dp).background(c.gold.copy(0.3f)))
                            }
                            Spacer(Modifier.height(24.dp))
                        }
                    }

                    itemsIndexed(
                        items = sections,
                        key   = { i, s -> "${i}_${s.type}_${s.content.take(20)}" }
                    ) { sectionIdx, section ->
                        val isFirst = !firstParaFound && section.type == SectionType.PARAGRAPH
                        if (isFirst) firstParaFound = true
                        val isPara  = section.type == SectionType.PARAGRAPH
                        val shouldDivide = isPara && paraCount > 0 && paraCount % 7 == 0
                        if (isPara) paraCount++

                        if (shouldDivide) OrnamentalSectionDivider()

                        // Long press to share; onTap propagates bars toggle
                        var showShare by remember { mutableStateOf(false) }
                        val isThirdParagraph = isPara && paraCount == 3
                        Box(
                            modifier = Modifier
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { onContentTap() },
                                        onLongPress = {
                                            if (section.type == SectionType.PARAGRAPH || section.type == SectionType.QUOTE) {
                                                showShare = true
                                            }
                                        }
                                    )
                                }
                                .then(
                                    if (isThirdParagraph)
                                        Modifier.onGloballyPositioned { coords ->
                                            wtAnchors["paragraph_area"] = coords.boundsInWindow()
                                        }
                                    else Modifier
                                )
                        ) {
                            SectionRenderer(
                                section          = section,
                                modifier         = Modifier.fillMaxWidth(),
                                isFirstParagraph = isFirst,
                                fontSizeSp       = state.fontSizeSp
                            )
                        }

                        if (showShare) {
                            AlertDialog(
                                onDismissRequest = { showShare = false },
                                containerColor   = c.surface,
                                title = { Text("பகிர்வு", color = c.gold, style = t.heading) },
                                text  = {
                                    Text(
                                        section.content.take(200) + if (section.content.length > 200) "…" else "",
                                        color = c.textSec,
                                        style = t.body.copy(fontSize = 14.sp)
                                    )
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showShare = false
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, section.content + "\n\n— இராசராச சோழன் - முழுமையான வரலாறு")
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, null))
                                    }) {
                                        Text("பகிர்", color = c.gold)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showShare = false }) {
                                        Text("மூடு", color = c.textSec)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // ── Top bar ─────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible  = barsVisible,
            enter    = slideInVertically { -it } + fadeIn(),
            exit     = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(c.surface, c.surface.copy(alpha = 0.96f))))
                ) {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = { onBack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.gold)
                            }
                        },
                        title = {},
                        actions = {
                            // Font size
                            IconButton(
                                onClick  = { vm.toggleFontPanel() },
                                modifier = Modifier.onGloballyPositioned { coords ->
                                    wtAnchors["font_icon"] = coords.boundsInWindow()
                                }
                            ) {
                                Icon(Icons.Filled.FormatSize, "எழுத்து அளவு", tint = c.textSec)
                            }
                            // Bookmark
                            IconButton(onClick = { vm.toggleBookmark() }) {
                                Icon(
                                    imageVector        = if (state.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = "குறிப்பு",
                                    tint               = if (state.isBookmarked) c.gold else c.textSec
                                )
                            }
                            // Search
                            IconButton(onClick = { onOpenSearch() }) {
                                Icon(Icons.Filled.Search, "தேடு", tint = c.textSec)
                            }
                            // Theme
                            IconButton(onClick = onThemeCycle) {
                                Icon(Icons.Filled.Brightness4, "கோலம்", tint = c.textSec)
                            }
                            // Index
                            IconButton(onClick = { onOpenIndex() }) {
                                Icon(Icons.AutoMirrored.Filled.FormatListBulleted, "பொருளடக்கம்", tint = c.textSec)
                            }
                        },
                        windowInsets = WindowInsets(0),
                        colors       = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
                state.book?.let { book ->
                    ChapterProgressBar(current = state.currentIndex, total = book.totalChapters)
                }
            }
        }

        // ── Bottom nav ────────────────────────────────────────────────────────
        AnimatedVisibility(
            visible  = barsVisible,
            enter    = slideInVertically { it } + fadeIn(),
            exit     = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            val hasPrev = state.currentIndex > 0
            val hasNext = state.book?.let { state.currentIndex < it.totalChapters - 1 } ?: false

            Surface(color = c.surface, shadowElevation = 12.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick  = { vm.navigateTo(state.currentIndex - 1) },
                        enabled  = hasPrev,
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = c.gold),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, if (hasPrev) c.gold.copy(0.6f) else c.dividerColor.copy(0.3f)),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Filled.ChevronLeft, null, tint = if (hasPrev) c.gold else c.textSec, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("முந்தையது", style = t.caption.copy(fontSize = 13.sp), color = if (hasPrev) c.gold else c.textSec)
                    }
                    Box(Modifier.padding(horizontal = 12.dp).size(6.dp).background(c.gold.copy(0.4f), CircleShape))
                    Button(
                        onClick  = { vm.navigateTo(state.currentIndex + 1) },
                        enabled  = hasNext,
                        shape    = RoundedCornerShape(10.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = if (hasNext) c.gold else c.surface,
                            contentColor   = if (hasNext) Color(0xFF1A000A) else c.textSec
                        ),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Text("அடுத்தது", style = t.caption.copy(fontSize = 13.sp), color = if (hasNext) Color(0xFF1A000A) else c.textSec)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Filled.ChevronRight, null, tint = if (hasNext) Color(0xFF1A000A) else c.textSec, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // ── Font size overlay ────────────────────────────────────────────────
        AnimatedVisibility(
            visible  = state.showFontPanel,
            enter    = slideInVertically { it } + fadeIn(),
            exit     = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth()
        ) {
            FontSizePanel(
                currentSize  = state.fontSizeSp,
                onSizeChange = { vm.setFontSize(it) },
                onDismiss    = { vm.dismissFontPanel() },
                colors       = c
            )
        }

        // ── Reader walkthrough (first chapter open only) ─────────────────────
        if (!readerWalkthroughDone && !state.isLoading && state.content != null) {
            WalkthroughOverlay(
                steps     = readerWalkthroughSteps,
                anchors   = wtAnchors,
                onComplete = { app.markReaderWalkthroughDone() }
            )
        }
    }
}

@Composable
private fun FontSizePanel(
    currentSize : Float,
    onSizeChange: (Float) -> Unit,
    onDismiss   : () -> Unit,
    colors      : com.livin.ambedkarindhiavilsathigal.presentation.theme.ReaderColors
) {
    Surface(
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        color = colors.surface,
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        run {
            Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp)) {
                Text(
                    "எழுத்து அளவு",
                    color = colors.gold,
                    style = androidx.compose.ui.text.TextStyle(
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("அ", color = colors.textSec, fontSize = 14.sp)
                    Slider(
                        value         = currentSize,
                        onValueChange = onSizeChange,
                        valueRange    = 14f..26f,
                        steps         = 5,
                        colors        = SliderDefaults.colors(
                            thumbColor       = colors.gold,
                            activeTrackColor = colors.gold
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
                    )
                    Text("அ", color = colors.textSec, fontSize = 26.sp)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${currentSize.toInt()} sp",
                    color    = colors.textSec,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick  = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("முடிந்தது", color = colors.gold)
                }
            }
        }
    }
}

