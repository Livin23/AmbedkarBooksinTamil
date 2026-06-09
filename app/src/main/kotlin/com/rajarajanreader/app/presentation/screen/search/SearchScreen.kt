package com.rajarajanreader.app.presentation.screen.search

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rajarajanreader.app.BookReaderApp
import com.rajarajanreader.app.domain.SectionType
import com.rajarajanreader.app.presentation.theme.LocalReaderColors
import com.rajarajanreader.app.presentation.theme.LocalReaderTypography
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SearchResult(
    val chapterIndex: Int,
    val chapterTitle: String,
    val snippet     : String,
    val matchStart  : Int,
    val matchEnd    : Int
)

data class SearchState(
    val query     : String         = "",
    val results   : List<SearchResult> = emptyList(),
    val isSearching: Boolean       = false
)

@OptIn(FlowPreview::class)
class SearchViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as BookReaderApp).bookRepo

    private val _query = MutableStateFlow("")
    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .collect { q ->
                    if (q.length < 2) {
                        _state.update { it.copy(results = emptyList(), isSearching = false, query = q) }
                        return@collect
                    }
                    _state.update { it.copy(isSearching = true, query = q) }
                    val results = searchBook(q)
                    _state.update { it.copy(results = results, isSearching = false) }
                }
        }
    }

    fun onQuery(q: String) {
        _query.value = q
        _state.update { it.copy(query = q) }
    }

    private suspend fun searchBook(query: String): List<SearchResult> =
        kotlinx.coroutines.withContext(Dispatchers.IO) {
            val book    = runCatching { repo.getBook() }.getOrNull() ?: return@withContext emptyList()
            val lowerQ  = query.lowercase()
            val results = mutableListOf<SearchResult>()
            for (ch in book.chapters) {
                val content = runCatching { repo.getChapterContent(ch.contentPath) }.getOrNull() ?: continue
                for (section in content.sections) {
                    if (section.type == SectionType.LIST_ITEM || section.type == SectionType.IMAGE) continue
                    val lower = section.content.lowercase()
                    val idx   = lower.indexOf(lowerQ)
                    if (idx != -1) {
                        val start   = maxOf(0, idx - 40)
                        val end     = minOf(section.content.length, idx + query.length + 80)
                        val snippet = (if (start > 0) "…" else "") +
                                      section.content.substring(start, end) +
                                      (if (end < section.content.length) "…" else "")
                        results.add(
                            SearchResult(
                                chapterIndex = ch.index,
                                chapterTitle = ch.title,
                                snippet      = snippet,
                                matchStart   = if (start > 0) idx - start + 1 else idx,
                                matchEnd     = if (start > 0) idx - start + 1 + query.length else idx + query.length
                            )
                        )
                        if (results.size >= 100) return@withContext results
                    }
                }
            }
            results
        }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack        : () -> Unit,
    onChapterClick: (Int) -> Unit,
    vm            : SearchViewModel = viewModel()
) {
    val state        by vm.state.collectAsStateWithLifecycle()
    val c            = LocalReaderColors.current
    val t            = LocalReaderTypography.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100)
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(c.bg)
            .systemBarsPadding()
    ) {
        // ── Search bar ─────────────────────────────────────────────────────
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .background(c.surface)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = c.gold)
            }
            OutlinedTextField(
                value          = state.query,
                onValueChange  = { vm.onQuery(it) },
                placeholder    = { Text("நூலில் தேடுக…", color = c.textSec, fontSize = 16.sp) },
                singleLine     = true,
                modifier       = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                colors         = OutlinedTextFieldDefaults.colors(
                    focusedTextColor    = c.text,
                    unfocusedTextColor  = c.text,
                    focusedBorderColor  = c.gold,
                    unfocusedBorderColor= c.dividerColor.copy(0.4f),
                    cursorColor         = c.gold
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* triggered by debounce */ }),
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { vm.onQuery("") }) {
                            Icon(Icons.Filled.Clear, "Clear", tint = c.textSec)
                        }
                    }
                },
                leadingIcon = { Icon(Icons.Filled.Search, null, tint = c.textSec) },
                shape = RoundedCornerShape(12.dp)
            )
        }

        // ── Results ─────────────────────────────────────────────────────────
        when {
            state.isSearching -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = c.accent, modifier = Modifier.size(36.dp))
            }

            state.query.length >= 2 && state.results.isEmpty() -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text(
                        "«${state.query}» தேடுதலில் எதுவும் கிடைக்கவில்லை",
                        color = c.textSec,
                        style = t.body.copy(fontSize = 16.sp),
                        modifier = Modifier.padding(32.dp)
                    )
                }
            }

            state.query.isEmpty() -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.padding(horizontal = 40.dp)
                    ) {
                        Text("🔍", fontSize = 48.sp)
                        Text(
                            "நூலில் ஒரு வார்த்தை அல்லது தொடரைத் தேடுங்கள்",
                            color     = c.textSec,
                            style     = t.body.copy(fontSize = 16.sp, lineHeight = 28.sp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            "எ.கா: இராசராசன், கங்கைகொண்டான், திருவரங்கம்",
                            color     = c.textSec.copy(0.55f),
                            style     = t.caption.copy(fontSize = 13.sp, lineHeight = 22.sp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            else -> {
                if (state.results.isNotEmpty()) {
                    Text(
                        "${state.results.size} முடிவுகள்",
                        color    = c.textSec,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.results) { result ->
                        SearchResultCard(
                            result  = result,
                            query   = state.query,
                            onClick = { onChapterClick(result.chapterIndex) },
                            colors  = c,
                            t       = t
                        )
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    result : SearchResult,
    query  : String,
    onClick: () -> Unit,
    colors : com.rajarajanreader.app.presentation.theme.ReaderColors,
    t      : com.rajarajanreader.app.presentation.theme.ReaderTypography
) {
    Surface(
        modifier       = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape          = RoundedCornerShape(12.dp),
        color          = colors.surface,
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text  = result.chapterTitle,
                style = t.caption.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
                color = colors.gold
            )
            Spacer(Modifier.height(6.dp))
            val snippet = result.snippet
            val lowerQ  = query.lowercase()
            val lowerS  = snippet.lowercase()
            val idx     = lowerS.indexOf(lowerQ)
            val annotated = buildAnnotatedString {
                if (idx >= 0) {
                    append(snippet.substring(0, idx))
                    withStyle(SpanStyle(background = colors.gold.copy(0.3f), color = colors.text, fontWeight = FontWeight.SemiBold)) {
                        append(snippet.substring(idx, minOf(idx + query.length, snippet.length)))
                    }
                    append(snippet.substring(minOf(idx + query.length, snippet.length)))
                } else {
                    append(snippet)
                }
            }
            Text(text = annotated, style = t.body.copy(fontSize = 14.sp, lineHeight = 22.sp), color = colors.textSec)
        }
    }
}
