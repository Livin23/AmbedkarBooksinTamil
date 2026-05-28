package com.tamilbookreader.app.presentation.screen.reader

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tamilbookreader.app.BookReaderApp
import com.tamilbookreader.app.domain.Book
import com.tamilbookreader.app.domain.ChapterContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReaderState(
    val book        : Book?          = null,
    val content     : ChapterContent?= null,
    val currentIndex: Int            = 0,
    val isBookmarked: Boolean        = false,
    val isLoading   : Boolean        = true,
    val error       : String?        = null
)

class ReaderViewModel(app: Application) : AndroidViewModel(app) {
    private val appContext = app as BookReaderApp
    private val repo = appContext.bookRepo

    private val _state = MutableStateFlow(ReaderState())
    val state = _state.asStateFlow()

    init {
        // Sync bookmark icon whenever the shared bookmark set changes
        viewModelScope.launch {
            appContext.bookmarks.collect { marks ->
                val id = currentChapterId() ?: return@collect
                _state.update { it.copy(isBookmarked = id in marks) }
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
        loadChapter(index)
    }

    fun toggleBookmark() {
        val id = currentChapterId() ?: return
        appContext.toggleBookmark(id)
    }

    private fun loadChapter(index: Int) {
        val path = _state.value.book?.chapters?.getOrNull(index)?.contentPath ?: return
        _state.update { it.copy(isLoading = true, content = null) }
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repo.getChapterContent(path) }.fold(
                onSuccess = { content ->
                    val id = _state.value.book?.chapters?.getOrNull(index)?.id ?: ""
                    _state.update { s -> s.copy(content = content, currentIndex = index, isLoading = false, isBookmarked = id in appContext.bookmarks.value) }
                },
                onFailure = { _state.update { s -> s.copy(error = it.message, isLoading = false) } }
            )
        }
    }

    private fun currentChapterId() = _state.value.book?.chapters?.getOrNull(_state.value.currentIndex)?.id
}
