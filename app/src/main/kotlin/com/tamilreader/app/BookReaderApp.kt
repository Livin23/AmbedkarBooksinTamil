package com.tamilbookreader.app

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.tamilbookreader.app.ads.AdManager
import com.tamilbookreader.app.data.BookRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class BookReaderApp : Application() {
    val bookRepo  by lazy { BookRepository(this) }
    val adManager by lazy { AdManager(this) }

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) { adManager.preload() }
    }

    private val _bookmarks = MutableStateFlow<Set<String>>(emptySet())
    val bookmarks = _bookmarks.asStateFlow()

    fun toggleBookmark(chapterId: String) {
        _bookmarks.update { current ->
            if (chapterId in current) current - chapterId else current + chapterId
        }
    }
}
