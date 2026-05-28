package com.tamilbookreader.app.domain

data class Book(
    val id: String,
    val title: String,
    val author: String,
    val coverImagePath: String,
    val totalChapters: Int,
    val chapters: List<Chapter>
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

data class Section(val type: SectionType, val content: String)

enum class SectionType {
    PARAGRAPH, HEADING, QUOTE;
    companion object {
        fun from(v: String) = when (v.lowercase()) {
            "heading" -> HEADING
            "quote"   -> QUOTE
            else      -> PARAGRAPH
        }
    }
}

enum class ReadingTheme { LIGHT, DARK, SEPIA }
