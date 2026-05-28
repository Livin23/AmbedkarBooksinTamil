package com.tamilbookreader.app.presentation.screen.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tamilbookreader.app.BookReaderApp
import com.tamilbookreader.app.domain.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SplashState {
    object Loading                    : SplashState
    data class Ready(val book: Book)  : SplashState
    data class Error(val msg: String) : SplashState
}

class SplashViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = (app as BookReaderApp).bookRepo

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repo.getBook() }
                .onSuccess { _state.value = SplashState.Ready(it) }
                .onFailure { _state.value = SplashState.Error(it.message ?: "பிழை") }
        }
    }
}
