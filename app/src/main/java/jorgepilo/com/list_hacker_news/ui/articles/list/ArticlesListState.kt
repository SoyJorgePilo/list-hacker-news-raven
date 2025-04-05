package jorgepilo.com.list_hacker_news.ui.articles.list

import jorgepilo.com.list_hacker_news.data.model.HackerNewsItem

/**
 * Clase que representa el estado de la pantalla de listado de artículos.
 * Esta clase de datos inmutable encapsula toda la información necesaria para
 * representar los diferentes estados posibles de la UI.
 *
 * @property articles Lista de artículos a mostrar
 * @property isLoading Indica si se está cargando inicialmente
 * @property error Mensaje de error si existe, null si no hay error
 * @property isRefreshing Indica si se está refrescando la lista (pull-to-refresh)
 * @property hasInitialConnectivity Indica si al inicio de la app había conexión
 */
data class ArticlesListState(
    val articles: List<HackerNewsItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isRefreshing: Boolean = false,
    val hasInitialConnectivity: Boolean = true
) {
    /**
     * Propiedades derivadas para facilitar decisiones en la UI
     */
    val hasArticles: Boolean
        get() = articles.isNotEmpty()
    
    val hasError: Boolean
        get() = error != null
    
    /**
     * Indica si se permite hacer refresh
     * Solo permitimos refresh si había conexión inicial
     */
    val allowRefresh: Boolean
        get() = hasInitialConnectivity
}