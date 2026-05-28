package com.tamilbookreader.app.data

import android.content.Context
import com.google.gson.Gson
import com.tamilbookreader.app.domain.Book
import com.tamilbookreader.app.domain.Chapter
import com.tamilbookreader.app.domain.ChapterContent
import com.tamilbookreader.app.domain.Section
import com.tamilbookreader.app.domain.SectionType

class BookRepository(private val context: Context) {
    private val gson = Gson()
    private var cachedBook: Book? = null
    private val contentCache = mutableMapOf<String, ChapterContent>()

    fun getBook(): Book = cachedBook ?: loadBook().also { cachedBook = it }

    fun getChapterContent(path: String): ChapterContent =
        contentCache[path] ?: loadContent(path).also { contentCache[path] = it }

    private fun loadBook(): Book {
        val dto = context.assets.open("book_config.json").bufferedReader()
            .use { gson.fromJson(it, BookDto::class.java) }
        return Book(
            id            = dto.id,
            title         = dto.title,
            author        = dto.author,
            coverImagePath= dto.coverImagePath,
            totalChapters = dto.totalChapters,
            chapters      = dto.chapters.map { Chapter(it.id, it.index, it.title, it.contentPath) }
        )
    }

    private fun loadContent(path: String): ChapterContent {
        val dto = context.assets.open(path).bufferedReader()
            .use { gson.fromJson(it, ChapterContentDto::class.java) }
        return ChapterContent(
            chapterId = dto.chapterId,
            title     = dto.title,
            sections  = dto.sections.map { Section(SectionType.from(it.type), it.content) }
        )
    }
}
