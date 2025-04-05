package jorgepilo.com.list_hacker_news.data.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entidad de Room para almacenar artículos en la base de datos local
 */
@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey
    val objectID: String,
    val title: String?,
    val author: String?,
    val createdAt: String,
    val createdAtTimestamp: Long,
    val url: String?,
    val storyTitle: String?,
    val storyUrl: String?,
    val commentText: String?,
    val points: Int?,
    val isDeleted: Boolean = false,
    val lastUpdated: Date = Date()
) {
    // Propiedad derivada para obtener el título a mostrar (title o storyTitle)
    val displayTitle: String
        get() = title ?: storyTitle ?: "Sin título"
        
    // Propiedad derivada para obtener la URL a mostrar (url o storyUrl)
    val displayUrl: String
        get() = url ?: storyUrl ?: ""
} 