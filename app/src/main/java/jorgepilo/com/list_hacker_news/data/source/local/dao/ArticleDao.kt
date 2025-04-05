package jorgepilo.com.list_hacker_news.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import jorgepilo.com.list_hacker_news.data.source.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO para operaciones de base de datos sobre artículos
 */
@Dao
interface ArticleDao {
    
    /**
     * Obtiene todos los artículos no eliminados ordenados por fecha de creación
     */
    @Query("SELECT * FROM articles WHERE isDeleted = 0 ORDER BY createdAtTimestamp DESC")
    fun getArticles(): Flow<List<ArticleEntity>>
    
    /**
     * Inserta uno o más artículos, reemplazando los existentes si hay conflicto
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<ArticleEntity>)
    
    /**
     * Inserta un artículo, reemplazando el existente si hay conflicto
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)
    
    /**
     * Actualiza un artículo existente
     */
    @Update
    suspend fun updateArticle(article: ArticleEntity)
    
    /**
     * Marca un artículo como eliminado
     */
    @Query("UPDATE articles SET isDeleted = 1 WHERE objectID = :articleId")
    suspend fun softDeleteArticle(articleId: String)
    
    /**
     * Elimina físicamente los artículos marcados como eliminados
     */
    @Query("DELETE FROM articles WHERE isDeleted = 1")
    suspend fun deleteMarkedArticles()
    
    /**
     * Obtiene un artículo por su ID
     */
    @Query("SELECT * FROM articles WHERE objectID = :articleId LIMIT 1")
    suspend fun getArticleById(articleId: String): ArticleEntity?
} 