package jorgepilo.com.list_hacker_news.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import jorgepilo.com.list_hacker_news.ui.articles.detail.ArticleDetailScreen
import jorgepilo.com.list_hacker_news.ui.articles.list.ArticlesListScreen
import jorgepilo.com.list_hacker_news.ui.articles.list.ArticlesListViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Contiene las rutas y argumentos de navegación de la aplicación
 */
object AppDestinations {
    const val ARTICLES_LIST_ROUTE = "articles_list"
    const val ARTICLE_DETAIL_ROUTE = "article_detail"
    const val ARTICLE_ID_ARG = "articleId"
    const val ARTICLE_TITLE_ARG = "articleTitle"
    const val ARTICLE_URL_ARG = "articleUrl"
    
    /**
     * Construye la ruta hacia la pantalla de detalle con los parámetros codificados
     *
     * @param articleId Identificador único del artículo
     * @param articleTitle Título del artículo (será codificado)
     * @param articleUrl URL del artículo (será codificado)
     * @return Ruta completa para la navegación
     */
    fun articleDetailRoute(articleId: String, articleTitle: String, articleUrl: String): String {
        val encodedTitle = URLEncoder.encode(articleTitle, StandardCharsets.UTF_8.toString())
        val encodedUrl = URLEncoder.encode(articleUrl, StandardCharsets.UTF_8.toString())
        return "$ARTICLE_DETAIL_ROUTE/$articleId/$encodedTitle/$encodedUrl"
    }
}

/**
 * Componente principal de navegación de la aplicación
 */
@Composable
fun AppNavigation(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = AppDestinations.ARTICLES_LIST_ROUTE,
        modifier = modifier
    ) {
        articlesListScreen(
            navController = navController
        )
        
        articleDetailScreen(
            navController = navController
        )
    }
}

/**
 * Define la pantalla de listado de artículos en el grafo de navegación
 */
private fun NavGraphBuilder.articlesListScreen(
    navController: NavHostController
) {
    composable(AppDestinations.ARTICLES_LIST_ROUTE) {
        val viewModel: ArticlesListViewModel = hiltViewModel()
        
        ArticlesListScreen(
            viewModel = viewModel,
            onArticleClick = { articleId ->
                navigateToArticleDetail(navController, viewModel, articleId)
            }
        )
    }
}

/**
 * Define la pantalla de detalle de artículo en el grafo de navegación
 */
private fun NavGraphBuilder.articleDetailScreen(
    navController: NavHostController
) {
    composable(
        route = "${AppDestinations.ARTICLE_DETAIL_ROUTE}/{${AppDestinations.ARTICLE_ID_ARG}}/{${AppDestinations.ARTICLE_TITLE_ARG}}/{${AppDestinations.ARTICLE_URL_ARG}}",
        arguments = listOf(
            navArgument(AppDestinations.ARTICLE_ID_ARG) { type = NavType.StringType },
            navArgument(AppDestinations.ARTICLE_TITLE_ARG) { type = NavType.StringType },
            navArgument(AppDestinations.ARTICLE_URL_ARG) { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val articleTitle = backStackEntry.arguments?.getString(AppDestinations.ARTICLE_TITLE_ARG) ?: ""
        val articleUrl = backStackEntry.arguments?.getString(AppDestinations.ARTICLE_URL_ARG) ?: ""
        
        val decodedTitle = URLDecoder.decode(articleTitle, StandardCharsets.UTF_8.toString())
        val decodedUrl = URLDecoder.decode(articleUrl, StandardCharsets.UTF_8.toString())
        
        ArticleDetailScreen(
            url = decodedUrl,
            title = decodedTitle,
            onNavigateBack = { navController.popBackStack() }
        )
    }
}

/**
 * Función auxiliar para navegar al detalle de un artículo
 */
private fun navigateToArticleDetail(
    navController: NavHostController, 
    viewModel: ArticlesListViewModel, 
    articleId: String
) {
    // Buscar el artículo por ID
    val article = viewModel.uiState.value.articles.find { it.objectID == articleId }
    
    article?.let {
        if (it.displayUrl.isNotEmpty()) {
            // Navegar al detalle del artículo
            val route = AppDestinations.articleDetailRoute(
                articleId = it.objectID,
                articleTitle = it.displayTitle,
                articleUrl = it.displayUrl
            )
            navController.navigate(route)
        }
    }
} 