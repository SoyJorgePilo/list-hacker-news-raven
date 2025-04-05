package jorgepilo.com.list_hacker_news.ui.articles.list

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import jorgepilo.com.list_hacker_news.R
import jorgepilo.com.list_hacker_news.data.model.HackerNewsItem
import jorgepilo.com.list_hacker_news.ui.theme.HackerDarkBackground
import jorgepilo.com.list_hacker_news.ui.theme.HackerGrey
import jorgepilo.com.list_hacker_news.ui.theme.HackerNewsOrange
import jorgepilo.com.list_hacker_news.ui.theme.ListhackernewsTheme
import java.util.Date

/**
 * Pantalla principal que muestra el listado de artículos de Hacker News
 */
@Composable
fun ArticlesListScreen(
    viewModel: ArticlesListViewModel = hiltViewModel(),
    onArticleClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isOffline by viewModel.isOffline.collectAsState()
    val startedOffline by viewModel.startedOffline.collectAsState()
    
    ArticlesListContent(
        uiState = uiState,
        isOffline = isOffline,
        startedOffline = startedOffline,
        onRefresh = viewModel::refreshArticles,
        onArticleClick = onArticleClick,
        onArticleDelete = viewModel::deleteArticle
    )
}

/**
 * Contenido principal de la pantalla, separado para facilitar la previsualización
 */
@Composable
fun ArticlesListContent(
    uiState: ArticlesListState,
    isOffline: Boolean = false,
    startedOffline: Boolean = false,
    onRefresh: () -> Unit,
    onArticleClick: (String) -> Unit,
    onArticleDelete: (String) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) HackerDarkBackground else Color.White
    val snackbarHostState = remember { SnackbarHostState() }
    
    // String que necesitamos para el Snackbar
    val connectionRestoredMessage = stringResource(R.string.connection_restored_no_refresh)
    
    // Mostrar un Snackbar solo cuando estamos en un caso específico
    LaunchedEffect(isOffline, uiState.error, startedOffline) {
        if (startedOffline && !isOffline) {
            // Si iniciamos sin conexión y ahora tenemos, mostrar un mensaje
            snackbarHostState.showSnackbar(
                message = connectionRestoredMessage,
                duration = SnackbarDuration.Long
            )
        }
        // Ya no mostramos el mensaje cuando se pierde la conexión
        // El mensaje se mostrará solo cuando el usuario intente refrescar sin conexión
    }
    
    Scaffold(
        topBar = {
            HackerNewsTopBar(isOffline = isOffline, startedOffline = startedOffline)
        },
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                // Caso 1: Cargando inicialmente
                uiState.isLoading && !uiState.hasArticles && !uiState.isRefreshing -> {
                    LoadingIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Caso 2: Error sin artículos
                uiState.hasError && !uiState.hasArticles -> {
                    ErrorMessage(
                        error = uiState.error,
                        isOffline = isOffline,
                        startedOffline = startedOffline,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Caso 3: Mostrar lista (con o sin artículos)
                else -> {
                    ArticlesList(
                        articles = uiState.articles,
                        isRefreshing = uiState.isRefreshing,
                        onRefresh = onRefresh,
                        onArticleClick = onArticleClick,
                        onArticleDelete = onArticleDelete,
                        isOffline = isOffline,
                        allowRefresh = uiState.allowRefresh // No permitir refresh si iniciamos sin conexión
                    )
                }
            }
        }
    }
}

/**
 * Barra superior de la aplicación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HackerNewsTopBar(
    isOffline: Boolean = false,
    startedOffline: Boolean = false
) {
    TopAppBar(
        title = { 
            Column {
                Text(
                    text = stringResource(R.string.app_title),
                    color = Color.White
                )
                if (isOffline) {
                    // Solo mostrar "Modo sin conexión" cuando realmente estamos sin conexión
                    Text(
                        text = stringResource(R.string.offline_mode),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                } else if (startedOffline) {
                    // Si se recuperó la conexión pero inició sin conexión
                    Text(
                        text = stringResource(R.string.offline_start),
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HackerNewsOrange,
            titleContentColor = Color.White
        )
    )
}

/**
 * Indicador de carga
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = HackerNewsOrange
    )
}

/**
 * Mensaje de error
 */
@Composable
fun ErrorMessage(
    error: String?,
    isOffline: Boolean = false,
    startedOffline: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when {
                isOffline -> stringResource(R.string.no_internet_connection)
                startedOffline && !isOffline -> stringResource(R.string.offline_start)
                else -> stringResource(R.string.error_loading_articles)
            },
            style = MaterialTheme.typography.titleMedium,
            color = Color.Red
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when {
                isOffline -> stringResource(R.string.showing_local_data)
                startedOffline && !isOffline -> stringResource(R.string.started_offline_now_online)
                else -> error ?: stringResource(R.string.unknown_error)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = HackerGrey
        )
        
        if (startedOffline) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.no_refresh_allowed),
                style = MaterialTheme.typography.bodyMedium,
                color = HackerGrey
            )
        }
    }
}

// PREVISUALIZACIÓN

@Preview(showBackground = true)
@Composable
private fun ArticlesListContentPreview() {
    ListhackernewsTheme {
        ArticlesListContent(
            uiState = ArticlesListState(
                articles = previewArticles,
                isLoading = false,
                isRefreshing = false,
                error = null
            ),
            onRefresh = {},
            onArticleClick = {},
            onArticleDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticlesListLoadingPreview() {
    ListhackernewsTheme {
        ArticlesListContent(
            uiState = ArticlesListState(
                articles = emptyList(),
                isLoading = true,
                isRefreshing = false,
                error = null
            ),
            onRefresh = {},
            onArticleClick = {},
            onArticleDelete = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticlesListErrorPreview() {
    ListhackernewsTheme {
        ArticlesListContent(
            uiState = ArticlesListState(
                articles = emptyList(),
                isLoading = false,
                isRefreshing = false,
                error = "No se pudo conectar al servidor"
            ),
            isOffline = true,
            startedOffline = true,
            onRefresh = {},
            onArticleClick = {},
            onArticleDelete = {}
        )
    }
}

// Datos de ejemplo para previsualización
private val previewArticles = listOf(
    HackerNewsItem(
        objectID = "1",
        title = "Título de ejemplo 1",
        author = "autor1",
        createdAt = "2023-04-01T12:00:00.000Z",
        createdAtTimestamp = Date().time,
        url = "https://example.com/1",
        storyTitle = null,
        storyUrl = null,
        commentText = null,
        points = 10
    ),
    HackerNewsItem(
        objectID = "2",
        title = "Título de ejemplo 2 muy largo que debería truncarse si es demasiado extenso para mostrar en una sola línea",
        author = "autor2",
        createdAt = "2023-04-02T12:00:00.000Z",
        createdAtTimestamp = Date().time,
        url = "https://example.com/2",
        storyTitle = null,
        storyUrl = null,
        commentText = null,
        points = 20
    )
) 