package com.rajarajanreader.app

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.ads.MobileAds
import com.rajarajanreader.app.ads.AdManager
import com.rajarajanreader.app.data.BookRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private val Application.dataStore: DataStore<Preferences> by preferencesDataStore("reader_prefs")

class BookReaderApp : Application() {
    val bookRepo  by lazy { BookRepository(this) }
    val adManager by lazy { AdManager(this) }
    val scope     = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private object Keys {
        val BOOKMARKS              = stringPreferencesKey("bookmarks")
        val LAST_CHAPTER           = intPreferencesKey("last_chapter")
        val FONT_SIZE              = floatPreferencesKey("font_size")
        val INDEX_WALKTHROUGH_DONE = booleanPreferencesKey("index_walkthrough_done")
        val READER_WALKTHROUGH_DONE= booleanPreferencesKey("reader_walkthrough_done")
    }

    // ── Bookmarks ─────────────────────────────────────────────────────────────
    val bookmarks: StateFlow<Set<String>> by lazy {
        dataStore.data
            .map { prefs -> prefs[Keys.BOOKMARKS]?.split(",")?.filter { it.isNotEmpty() }?.toSet() ?: emptySet() }
            .stateIn(scope, SharingStarted.Eagerly, emptySet())
    }

    fun toggleBookmark(chapterId: String) {
        scope.launch {
            dataStore.edit { prefs ->
                val current = prefs[Keys.BOOKMARKS]?.split(",")?.filter { it.isNotEmpty() }?.toMutableSet() ?: mutableSetOf()
                if (chapterId in current) current.remove(chapterId) else current.add(chapterId)
                prefs[Keys.BOOKMARKS] = current.joinToString(",")
            }
        }
    }

    // ── Last chapter ──────────────────────────────────────────────────────────
    val lastChapter: StateFlow<Int> by lazy {
        dataStore.data
            .map { prefs -> prefs[Keys.LAST_CHAPTER] ?: 0 }
            .stateIn(scope, SharingStarted.Eagerly, 0)
    }

    fun saveLastChapter(index: Int) {
        scope.launch { dataStore.edit { it[Keys.LAST_CHAPTER] = index } }
    }

    // ── Font size ─────────────────────────────────────────────────────────────
    val fontSize: StateFlow<Float> by lazy {
        dataStore.data
            .map { prefs -> prefs[Keys.FONT_SIZE] ?: 18f }
            .stateIn(scope, SharingStarted.Eagerly, 18f)
    }

    fun saveFontSize(size: Float) {
        scope.launch { dataStore.edit { it[Keys.FONT_SIZE] = size } }
    }

    // ── Walkthrough seen flags ────────────────────────────────────────────────
    val indexWalkthroughDone: StateFlow<Boolean> by lazy {
        dataStore.data
            .map { it[Keys.INDEX_WALKTHROUGH_DONE] ?: false }
            .stateIn(scope, SharingStarted.Eagerly, false)
    }

    val readerWalkthroughDone: StateFlow<Boolean> by lazy {
        dataStore.data
            .map { it[Keys.READER_WALKTHROUGH_DONE] ?: false }
            .stateIn(scope, SharingStarted.Eagerly, false)
    }

    fun markIndexWalkthroughDone() {
        scope.launch { dataStore.edit { it[Keys.INDEX_WALKTHROUGH_DONE] = true } }
    }

    fun markReaderWalkthroughDone() {
        scope.launch { dataStore.edit { it[Keys.READER_WALKTHROUGH_DONE] = true } }
    }

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) { adManager.preload() }
    }

    override fun onTerminate() {
        scope.cancel()
        super.onTerminate()
    }
}
