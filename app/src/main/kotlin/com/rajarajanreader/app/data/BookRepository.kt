package com.rajarajanreader.app.data

import android.content.Context
import com.google.gson.Gson
import com.rajarajanreader.app.domain.*

class BookRepository(private val context: Context) {
    private val gson = Gson()
    private var cachedBook: Book? = null
    private val contentCache = mutableMapOf<String, ChapterContent>()

    fun getBook(): Book = cachedBook ?: loadBook().also { cachedBook = it }

    fun getChapterContent(path: String): ChapterContent =
        contentCache[path] ?: loadContent(path).also { contentCache[path] = it }

    fun getAllSections(): List<Pair<Int, Section>> {
        val book = getBook()
        return book.chapters.flatMap { ch ->
            val content = runCatching { getChapterContent(ch.contentPath) }.getOrNull()
            content?.sections?.map { ch.index to it } ?: emptyList()
        }
    }

    private fun loadBook(): Book {
        val dto = context.assets.open("book_config.json").bufferedReader()
            .use { gson.fromJson(it, BookDto::class.java) }
        return Book(
            id            = dto.id,
            title         = dto.title,
            author        = dto.author,
            coverImagePath= dto.coverImagePath,
            totalChapters = dto.totalChapters,
            chapters      = dto.chapters.map { Chapter(it.id, it.index, it.title, it.contentPath) },
            parts         = dto.parts?.map { BookPart(it.title, it.startChapterIdx, it.endChapterIdx) } ?: emptyList()
        )
    }

    private fun loadContent(path: String): ChapterContent {
        val dto = context.assets.open(path).bufferedReader()
            .use { gson.fromJson(it, ChapterContentDto::class.java) }
        return ChapterContent(
            chapterId = dto.chapterId,
            title     = dto.title,
            sections  = dto.sections.map {
                Section(SectionType.from(it.type), it.content, it.caption ?: "")
            }
        )
    }
}
