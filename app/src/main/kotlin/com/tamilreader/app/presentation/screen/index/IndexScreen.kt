package com.tamilbookreader.app.presentation.screen.index

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tamilbookreader.app.presentation.theme.LocalReaderColors
import com.tamilbookreader.app.presentation.theme.LocalReaderTypography

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexScreen(
    onChapterClick: (Int) -> Unit,
    onThemeCycle: () -> Unit,
    vm: IndexViewModel = viewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val c = LocalReaderColors.current
    val t = LocalReaderTypography.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            state.book?.title ?: "நூல் பட்டியல்",
                            style = t.heading,
                            color = c.text,
                            textAlign = TextAlign.Center
                        )
                        state.book?.let {
                            Text(
                                "${it.totalChapters} அத்தியாயங்கள்",
                                style = t.caption.copy(fontWeight = FontWeight.Medium),
                                color = c.accent
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onThemeCycle) {
                        Icon(Icons.Filled.Brightness4, "கோலம் மாற்று", tint = c.textSec)
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Outlined.Settings, "அமைப்புகள்", tint = c.textSec)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = c.surface)
            )
        },
        containerColor = c.bg
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator(color = c.accent)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding      = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(state.book!!.chapters) { _, ch ->
                    val bookmarked = ch.id in state.bookmarks
                    Card(
                        modifier  = Modifier.fillMaxWidth().clickable { onChapterClick(ch.index) },
                        colors    = CardDefaults.cardColors(containerColor = c.surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            // Fixed-width number column — never wraps or collides
                            Text(
                                text     = "${ch.index + 1}.",
                                style    = t.body.copy(fontWeight = FontWeight.SemiBold),
                                color    = c.accent,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(28.dp).paddingFromBaseline(top = 0.dp)
                            )
                            Spacer(Modifier.width(14.dp))
                            // Title takes all remaining space, wraps cleanly
                            Text(
                                text     = ch.title,
                                style    = t.body,
                                color    = c.text,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(12.dp))
                            Icon(
                                imageVector        = if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                contentDescription = null,
                                tint               = if (bookmarked) c.accent else c.textSec,
                                modifier           = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
