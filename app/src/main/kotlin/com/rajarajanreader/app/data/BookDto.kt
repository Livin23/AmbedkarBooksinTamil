package com.livin.ambedkarindhiavilsathigal.data

import com.google.gson.annotations.SerializedName

data class BookDto(
    @SerializedName("id")            val id: String,
    @SerializedName("title")         val title: String,
    @SerializedName("author")        val author: String,
    @SerializedName("coverImagePath")val coverImagePath: String,
    @SerializedName("totalChapters") val totalChapters: Int,
    @SerializedName("chapters")      val chapters: List<ChapterDto>,
    @SerializedName("parts")         val parts: List<BookPartDto>? = null
)

data class BookPartDto(
    @SerializedName("title")           val title: String,
    @SerializedName("startChapterIdx") val startChapterIdx: Int,
    @SerializedName("endChapterIdx")   val endChapterIdx: Int
)

data class ChapterDto(
    @SerializedName("id")          val id: String,
    @SerializedName("index")       val index: Int,
    @SerializedName("title")       val title: String,
    @SerializedName("contentPath") val contentPath: String
)

data class ChapterContentDto(
    @SerializedName("chapterId") val chapterId: String,
    @SerializedName("title")     val title: String,
    @SerializedName("sections")  val sections: List<SectionDto>
)

data class SectionDto(
    @SerializedName("type")    val type: String,
    @SerializedName("content") val content: String,
    @SerializedName("caption") val caption: String? = null
)
