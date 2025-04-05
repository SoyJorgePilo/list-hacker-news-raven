package jorgepilo.com.list_hacker_news.data.repository

import jorgepilo.com.list_hacker_news.data.model.HackerNewsItem
import kotlinx.coroutines.flow.Flow

interface IArticlesRepository {
    fun getArticles(): Flow<Result<List<HackerNewsItem>>>
    suspend fun refreshArticles(): Result<List<HackerNewsItem>>
    suspend fun deleteArticle(articleId: String)
    fun observeArticles(): Flow<List<HackerNewsItem>>
} 