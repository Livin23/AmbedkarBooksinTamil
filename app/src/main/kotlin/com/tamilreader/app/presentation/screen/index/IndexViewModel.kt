package com.tamilbookreader.app.presentation.screen.index

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tamilbookreader.app.BookReaderApp
import com.tamilbookreader.app.domain.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class IndexState(
    val book      : Book?       = null,
    val bookmarks : Set<String> = emptySet(),
    val isLoading : Boolean     = true
)

class IndexViewModel(app: Application) : AndroidViewModel(app) {
    private val appContext = app as BookReaderApp
    private val repo = appContext.bookRepo

    private val _book = MutableStateFlow<Book?>(null)

    val state = combine(_book, appContext.bookmarks) { book, marks ->
        IndexState(book = book, bookmarks = marks, isLoading = book == null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), IndexState())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repo.getBook() }.onSuccess { _book.value = it }
        }
    }
}
