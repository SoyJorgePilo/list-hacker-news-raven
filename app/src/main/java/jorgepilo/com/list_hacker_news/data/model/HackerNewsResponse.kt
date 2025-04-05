package jorgepilo.com.list_hacker_news.data.model

import com.google.gson.annotations.SerializedName

data class HackerNewsResponse(
    val hits: List<HackerNewsItem>,
    val page: Int,
    val nbPages: Int,
    val hitsPerPage: Int,
    val nbHits: Int
)

data class HackerNewsItem(
    val objectID: String,
    val author: String?,
    val title: String?,
    @SerializedName("story_title") val storyTitle: String?,
    @SerializedName("story_url") val storyUrl: String?,
    val url: String?,
    @SerializedName("comment_text") val commentText: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("created_at_i") val createdAtTimestamp: Long,
    val points: Int? = 0
) {
    val displayTitle: String
        get() = title ?: storyTitle ?: "Sin título"
    
    val displayUrl: String
        get() = url ?: storyUrl ?: ""
} 