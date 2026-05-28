package com.tamilbookreader.app.presentation.screen.reader

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tamilbookreader.app.presentation.component.ChapterProgressBar
import com.tamilbookreader.app.presentation.component.SectionRenderer
import com.tamilbookreader.app.presentation.theme.LocalReaderColors
import com.tamilbookreader.app.presentation.theme.LocalReaderTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    chapterIndex: Int,
    onBack      : () -> Unit,
    onOpenIndex : () -> Unit,
    onThemeCycle: () -> Unit,
    vm          : ReaderViewModel = viewModel()
) {
    val state     by vm.state.collectAsStateWithLifecycle()
    val c         = LocalReaderColors.current
    val t         = LocalReaderTypography.current
    val listState = rememberLazyListState()

    LaunchedEffect(Unit)         { vm.init(chapterIndex) }
    LaunchedEffect(state.currentIndex) { listState.scrollToItem(0) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.text)
                        }
                    },
                    title = {
                        Text(
                            state.content?.title ?: state.book?.title ?: "",
                            style   = t.heading,
                            color   = c.text,
                            maxLines= 1
                        )
                    },
                    actions = {
                        IconButton(onClick = { vm.toggleBookmark() }) {
                            Icon(
                                imageVector = if (state.isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (state.isBookmarked) c.accent else c.textSec
                            )
                        }
                        IconButton(onClick = onThemeCycle) {
                            Icon(Icons.Filled.Brightness4, "Theme", tint = c.textSec)
                        }
                        IconButton(onClick = onOpenIndex) {
                            Icon(Icons.Filled.FormatListBulleted, "Index", tint = c.textSec)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = c.surface)
                )
                state.book?.let { book ->
                    ChapterProgressBar(current = state.currentIndex, total = book.totalChapters)
                }
            }
        },
        bottomBar = {
            val hasPrev = state.currentIndex > 0
            val hasNext = state.book?.let { state.currentIndex < it.totalChapters - 1 } ?: false

            NavigationBar(containerColor = c.surface, tonalElevation = 0.dp) {
                NavigationBarItem(
                    selected = false, enabled = hasPrev,
                    onClick  = { vm.navigateTo(state.currentIndex - 1) },
                    icon     = { Icon(Icons.Filled.ChevronLeft, "முந்தைய") },
                    label    = { Text("முந்தைய", style = t.caption) },
                    colors   = NavigationBarItemDefaults.colors(indicatorColor = c.quoteBg)
                )
                NavigationBarItem(
                    selected = false, enabled = hasNext,
                    onClick  = { vm.navigateTo(state.currentIndex + 1) },
                    icon     = { Icon(Icons.Filled.ChevronRight, "அடுத்த") },
                    label    = { Text("அடுத்த", style = t.caption) },
                    colors   = NavigationBarItemDefaults.colors(indicatorColor = c.quoteBg)
                )
            }
        },
        containerColor = c.bg
    ) { padding ->
        when {
            state.isLoading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator(color = c.accent)
            }
            state.error != null -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                Text(state.error ?: "", color = MaterialTheme.colorScheme.error)
            }
            else -> LazyColumn(
                state          = listState,
                modifier       = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                contentPadding = PaddingValues(vertical = 20.dp)
            ) {
                state.content?.sections?.let { sections ->
                    items(sections, key = { "${it.type}_${it.content.take(25)}" }) { section ->
                        SectionRenderer(section = section, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}
