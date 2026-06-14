package com.rajarajanreader.app.presentation.screen.splash

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rajarajanreader.app.BookReaderApp
import com.rajarajanreader.app.domain.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SplashState {
    object Loading                    : SplashState
    data class Ready(val book: Book)  : SplashState
    data class Error(val msg: String) : SplashState
}

class SplashViewModel(app: Application) : AndroidViewModel(app) {
    private val appInstance = app as BookReaderApp
    private val repo        = appInstance.bookRepo

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state = _state.asStateFlow()

    val onboardingShown = appInstance.onboardingShown
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { repo.getBook() }
                .onSuccess { _state.value = SplashState.Ready(it) }
                .onFailure { _state.value = SplashState.Error(it.message ?: "பிழை") }
        }
    }
}
