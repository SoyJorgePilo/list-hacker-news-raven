package jorgepilo.com.list_hacker_news.data.repository

import android.util.Log
import jorgepilo.com.list_hacker_news.data.model.HackerNewsItem
import jorgepilo.com.list_hacker_news.data.model.toEntityList
import jorgepilo.com.list_hacker_news.data.model.toModelList
import jorgepilo.com.list_hacker_news.data.source.local.dao.ArticleDao
import jorgepilo.com.list_hacker_news.data.source.remote.RetrofitClient
import jorgepilo.com.list_hacker_news.util.ConnectivityObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Repositorio que maneja la lógica de datos para artículos con soporte offline
 */
class ArticlesRepository @Inject constructor(
    private val articleDao: ArticleDao,
    private val connectivityObserver: ConnectivityObserver
) : IArticlesRepository {
    
    private val hackerNewsApi = RetrofitClient.hackerNewsApi
    private val TAG = "ArticlesRepository"
    
    /**
     * Obtiene artículos con estrategia offline-first:
     * 1. Emite datos locales primero (si existen)
     * 2. Si hay conexión, intenta obtener datos frescos y actualizarlos localmente
     * 3. Si no hay conexión, usa solo datos locales
     */
    override fun getArticles(): Flow<Result<List<HackerNewsItem>>> = flow {
        // 1. Emitir datos locales primero si existen
        val localArticles = articleDao.getArticles().first()
        if (localArticles.isNotEmpty()) {
            emit(Result.success(localArticles.toModelList()))
        }
        
        // 2. Si hay conexión, intentar actualizar desde la API
        if (connectivityObserver.isNetworkAvailable()) {
            try {
                val response = hackerNewsApi.getArticles()
                val remoteArticles = response.hits
                
                // Guardar los artículos en la base de datos local
                articleDao.insertArticles(remoteArticles.toEntityList())
                
                // Emitir los artículos actualizados (sin los eliminados)
                val freshLocalArticles = articleDao.getArticles().first()
                emit(Result.success(freshLocalArticles.toModelList()))
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener artículos de la API", e)
                // Si falló la obtención remota pero ya emitimos datos locales, 
                // no emitimos error para no interrumpir la experiencia del usuario
                if (localArticles.isEmpty()) {
                    emit(Result.failure(e))
                }
            }
        } else {
            // Si no hay conexión y no tenemos datos locales, emitir error
            if (localArticles.isEmpty()) {
                emit(Result.failure(Exception("No hay conexión a internet ni datos en caché")))
            }
            // De lo contrario ya emitimos los datos locales, así que no hacemos nada más
        }
    }.flowOn(Dispatchers.IO)
        .catch { e ->
            // Capturar cualquier excepción
            emit(Result.failure(Exception("Error al obtener artículos: ${e.message}")))
        }
    
    /**
     * Observa los cambios en los artículos almacenados localmente
     */
    override fun observeArticles(): Flow<List<HackerNewsItem>> {
        return articleDao.getArticles()
            .map { it.toModelList() }
            .catch { e ->
                Log.e(TAG, "Error observando artículos", e)
                emit(emptyList())
            }
    }
    
    /**
     * Elimina un artículo (soft delete en la base de datos)
     */
    override suspend fun deleteArticle(articleId: String) {
        articleDao.softDeleteArticle(articleId)
    }
    
    /**
     * Refresca los artículos desde la API
     */
    override suspend fun refreshArticles(): Result<List<HackerNewsItem>> {
        return try {
            if (!connectivityObserver.isNetworkAvailable()) {
                return Result.failure(Exception("No hay conexión a internet"))
            }
            
            val response = hackerNewsApi.getArticles()
            val remoteArticles = response.hits
            
            // Guardar los artículos en la base de datos local
            articleDao.insertArticles(remoteArticles.toEntityList())
            
            // Limpiar artículos eliminados (físicamente) para evitar acumulación
            articleDao.deleteMarkedArticles()
            
            // Obtener artículos actualizados (sin los eliminados)
            val freshLocalArticles = articleDao.getArticles().first()
            Result.success(freshLocalArticles.toModelList())
        } catch (e: Exception) {
            Log.e(TAG, "Error al refrescar artículos", e)
            Result.failure(e)
        }
    }
} 