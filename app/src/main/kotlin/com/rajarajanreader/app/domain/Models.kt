package com.rajarajanreader.app.domain

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverImagePath: String,
    val totalChapters: Int,
    val chapters: List<Chapter>,
    val parts: List<BookPart> = emptyList()
)

data class BookPart(
    val title: String,
    val startChapterIdx: Int,
    val endChapterIdx: Int
)

data class Chapter(
    val id: String,
    val index: Int,
    val title: String,
    val contentPath: String
)

data class ChapterContent(
    val chapterId: String,
    val title: String,
    val sections: List<Section>
)

data class Section(
    val type   : SectionType,
    val content: String,
    val caption: String = ""   // used by IMAGE sections
)

enum class SectionType {
    PARAGRAPH, HEADING, QUOTE, LIST_ITEM,
    VERSE, VERSE_ATTRIBUTION, VERSE_MEANING, IMAGE;

    companion object {
        fun from(v: String) = when (v.lowercase()) {
            "heading"          -> HEADING
            "quote"            -> QUOTE
            "list_item"        -> LIST_ITEM
            "verse"            -> VERSE
            "verse_attribution"-> VERSE_ATTRIBUTION
            "verse_meaning"    -> VERSE_MEANING
            "image"            -> IMAGE
            else               -> PARAGRAPH
        }
    }
}

enum class ReadingTheme { LIGHT, DARK, SEPIA }
