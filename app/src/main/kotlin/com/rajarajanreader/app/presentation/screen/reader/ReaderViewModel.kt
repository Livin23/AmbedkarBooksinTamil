package com.rajarajanreader.app.presentation.screen.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rajarajanreader.app.BookReaderApp
import com.rajarajanreader.app.domain.Book
import com.rajarajanreader.app.domain.ChapterContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class ReaderState(
    val book            : Book?           = null,
    val content         : ChapterContent? = null,
    val currentIndex    : Int             = 0,
    val isBookmarked    : Boolean         = false,
    val isLoading       : Boolean         = true,
    val error           : String?         = null,
    val fontSizeSp      : Float           = 18f,
    val showFontPanel   : Boolean         = false
)

class ReaderViewModel(app: Application) : AndroidViewModel(app) {
    private val appContext = app as BookReaderApp
    private val repo       = appContext.bookRepo

    private val _state = MutableStateFlow(ReaderState())
    val state = _state.asStateFlow()

    private val _showAd = MutableSharedFlow<Int>()
    val showAd = _showAd.asSharedFlow()

    private var navCount = 0

    init {
        viewModelScope.launch {
            appContext.bookmarks.collect { marks ->
                val id = currentChapterId() ?: return@collect
                _state.update { it.copy(isBookmarked = id in marks) }
            }
        }
        viewModelScope.launch {
            appContext.fontSize.collect { size ->
                _state.update { it.copy(fontSizeSp = size) }
            }
        }
    }

    fun init(chapterIndex: Int) {
        if (_state.value.book != null) {
            if (_state.value.currentIndex != chapterIndex) navigateTo(chapterIndex)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repo.getBook() }.fold(
                onSuccess = { book ->
                    _state.update { it.copy(book = book) }
                    loadChapter(chapterIndex)
                },
                onFailure = { _state.update { s -> s.copy(error = it.message, isLoading = false) } }
            )
        }
    }

    fun navigateTo(index: Int) {
        val total = _state.value.book?.totalChapters ?: return
        if (index !in 0 until total) return
        navCount++
        if (navCount % 3 == 0) {
            viewModelScope.launch { _showAd.emit(index) }
        } else {
            loadChapter(index)
        }
    }

    fun loadChapterAfterAd(index: Int) = loadChapter(index)

    fun toggleBookmark() {
        val id = currentChapterId() ?: return
        appContext.toggleBookmark(id)
    }

    fun setFontSize(size: Float) {
        val clamped = size.coerceIn(14f, 26f)
        _state.update { it.copy(fontSizeSp = clamped) }
        appContext.saveFontSize(clamped)
    }

    fun toggleFontPanel() {
        _state.update { it.copy(showFontPanel = !it.showFontPanel) }
    }

    fun dismissFontPanel() {
        _state.update { it.copy(showFontPanel = false) }
    }

    private fun loadChapter(index: Int) {
        val path = _state.value.book?.chapters?.getOrNull(index)?.contentPath ?: return
        _state.update { it.copy(isLoading = true, content = null) }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repo.getChapterContent(path) }.fold(
                onSuccess = { content ->
                    val id = _state.value.book?.chapters?.getOrNull(index)?.id ?: ""
                    _state.update { s ->
                        s.copy(
                            content      = content,
                            currentIndex = index,
                            isLoading    = false,
                            isBookmarked = id in appContext.bookmarks.value
                        )
                    }
                    appContext.saveLastChapter(index)
                },
                onFailure = { _state.update { s -> s.copy(error = it.message, isLoading = false) } }
            )
        }
    }

    private fun currentChapterId() = _state.value.book?.chapters?.getOrNull(_state.value.currentIndex)?.id
}
