package com.livin.ambedkarindhiavilsathigal.presentation.screen.index

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.livin.ambedkarindhiavilsathigal.BookReaderApp
import com.livin.ambedkarindhiavilsathigal.domain.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class IndexState(
    val book        : Book?       = null,
    val bookmarks   : Set<String> = emptySet(),
    val lastChapter : Int         = 0,
    val isLoading   : Boolean     = true
)

class IndexViewModel(app: Application) : AndroidViewModel(app) {
    private val appContext = app as BookReaderApp
    private val repo = appContext.bookRepo

    private val _book = MutableStateFlow<Book?>(null)

    val state = combine(_book, appContext.bookmarks, appContext.lastChapter) { book, marks, last ->
        IndexState(book = book, bookmarks = marks, lastChapter = last, isLoading = book == null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IndexState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repo.getBook() }.onSuccess { _book.value = it }
        }
    }
}
