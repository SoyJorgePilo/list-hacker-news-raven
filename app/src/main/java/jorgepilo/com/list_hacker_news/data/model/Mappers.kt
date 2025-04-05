package jorgepilo.com.list_hacker_news.data.model

import jorgepilo.com.list_hacker_news.data.source.local.entity.ArticleEntity
import java.util.Date

/**
 * Convierte un modelo HackerNewsItem a una entidad ArticleEntity
 */
fun HackerNewsItem.toEntity(): ArticleEntity {
    return ArticleEntity(
        objectID = objectID,
        title = title,
        author = author,
        createdAt = createdAt,
        createdAtTimestamp = createdAtTimestamp,
        url = url,
        storyTitle = storyTitle,
        storyUrl = storyUrl,
        commentText = commentText,
        points = points,
        isDeleted = false,
        lastUpdated = Date()
    )
}

/**
 * Convierte una entidad ArticleEntity a un modelo HackerNewsItem
 */
fun ArticleEntity.toModel(): HackerNewsItem {
    return HackerNewsItem(
        objectID = objectID,
        title = title,
        author = author,
        createdAt = createdAt,
        createdAtTimestamp = createdAtTimestamp,
        url = url,
        storyTitle = storyTitle,
        storyUrl = storyUrl,
        commentText = commentText,
        points = points
    )
}

/**
 * Convierte una lista de entidades ArticleEntity a una lista de modelos HackerNewsItem
 */
fun List<ArticleEntity>.toModelList(): List<HackerNewsItem> {
    return map { it.toModel() }
}

/**
 * Convierte una lista de modelos HackerNewsItem a una lista de entidades ArticleEntity
 */
fun List<HackerNewsItem>.toEntityList(): List<ArticleEntity> {
    return map { it.toEntity() }
} 