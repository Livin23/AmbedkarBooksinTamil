package com.livin.ambedkarindhiavilsathigal

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.ads.MobileAds
import com.livin.ambedkarindhiavilsathigal.ads.AdManager
import com.livin.ambedkarindhiavilsathigal.data.BookRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

private val Application.dataStore: DataStore<Preferences> by preferencesDataStore("reader_prefs")

class BookReaderApp : Application() {
    val bookRepo  by lazy { BookRepository(this) }
    val adManager by lazy { AdManager(this) }
    val scope     = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private object Keys {
        val BOOKMARKS        = stringPreferencesKey("bookmarks")
        val LAST_CHAPTER     = intPreferencesKey("last_chapter")
        val FONT_SIZE        = floatPreferencesKey("font_size")
        val ONBOARDING_SHOWN = booleanPreferencesKey("onboarding_shown")
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

    // ── Onboarding ────────────────────────────────────────────────────────────
    val onboardingShown: StateFlow<Boolean> by lazy {
        dataStore.data
            .map { prefs -> prefs[Keys.ONBOARDING_SHOWN] ?: false }
            .stateIn(scope, SharingStarted.Eagerly, false)
    }

    fun markOnboardingShown() {
        scope.launch { dataStore.edit { it[Keys.ONBOARDING_SHOWN] = true } }
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

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this) { adManager.preload() }
    }

    override fun onTerminate() {
        scope.cancel()
        super.onTerminate()
    }
}
