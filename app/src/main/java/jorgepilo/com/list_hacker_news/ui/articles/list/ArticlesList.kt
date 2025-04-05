package jorgepilo.com.list_hacker_news.ui.articles.list

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberDismissState
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import jorgepilo.com.list_hacker_news.R
import jorgepilo.com.list_hacker_news.data.model.HackerNewsItem
import jorgepilo.com.list_hacker_news.ui.theme.HackerDarkBackground
import jorgepilo.com.list_hacker_news.ui.theme.HackerGrey
import jorgepilo.com.list_hacker_news.ui.theme.HackerNewsOrange
import jorgepilo.com.list_hacker_news.ui.theme.HackerTextDark
import jorgepilo.com.list_hacker_news.ui.theme.HackerTextLight
import jorgepilo.com.list_hacker_news.ui.theme.ListhackernewsTheme
import kotlinx.coroutines.delay
import java.util.Date

/**
 * Lista principal de artículos con pull-to-refresh y swipe-to-delete
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ArticlesList(
    articles: List<HackerNewsItem>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onArticleClick: (String) -> Unit,
    onArticleDelete: (String) -> Unit,
    isOffline: Boolean = false,
    allowRefresh: Boolean = true
) {
    // Estado para mostrar una notificación de offline
    var showOfflineMessage by remember { mutableStateOf(false) }
    var showNoRefreshMessage by remember { mutableStateOf(false) }
    val context = LocalContext.current
    
    // Función para manejar el intento de refresh
    val handleRefresh = {
        if (!allowRefresh) {
            // Mostrar mensaje de que no se permite refrescar
            showNoRefreshMessage = true
            // Proporcionar feedback háptico
            provideHapticFeedback(context)
        } else if (isOffline) {
            // Mostrar mensaje de offline solo cuando el usuario intenta refrescar explícitamente
            showOfflineMessage = true
            // Proporcionar feedback háptico
            provideHapticFeedback(context)
        }
        
        // Siempre llamar a onRefresh para que el ViewModel maneje la lógica
        // y actualice los estados en la UI
        onRefresh()
    }
    
    // Gestionar el mensaje de offline cuando se intenta refrescar sin conexión
    LaunchedEffect(showOfflineMessage) {
        if (showOfflineMessage) {
            delay(3000) // Mostrar el mensaje por 3 segundos
            showOfflineMessage = false
        }
    }
    
    // Gestionar el mensaje de no-refresh permitido
    LaunchedEffect(showNoRefreshMessage) {
        if (showNoRefreshMessage) {
            delay(3000) // Mostrar el mensaje por 3 segundos
            showNoRefreshMessage = false
        }
    }
    
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = handleRefresh
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Mostrar mensaje cuando no se permite refrescar
            AnimatedVisibility(
                visible = showNoRefreshMessage,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                NoRefreshMessage()
            }
            
            // Mostrar mensaje de offline cuando se detecta un intento de actualizar sin conexión
            AnimatedVisibility(
                visible = showOfflineMessage && isOffline,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                OfflineMessage()
            }
            
            // Lista de artículos
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                if (articles.isEmpty()) {
                    item {
                        EmptyState(isOffline = isOffline)
                    }
                } else {
                    items(
                        items = articles,
                        key = { it.objectID }
                    ) { article ->
                        SwipeableArticleItem(
                            article = article,
                            onArticleClick = onArticleClick,
                            onArticleDelete = onArticleDelete
                        )
                        
                        Divider(
                            color = Color.LightGray.copy(alpha = 0.5f),
                            thickness = 0.5.dp
                        )
                    }
                }
            }
        }
        
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = if (!allowRefresh) Color(0xFFE0E0E0) else if (isOffline) Color(0xFFF0F0F0) else MaterialTheme.colorScheme.surface,
            contentColor = when {
                !allowRefresh -> Color(0xFFAAAAAA)
                isOffline -> Color(0xFFAAAAAA)
                else -> HackerNewsOrange
            }
        )
    }
}

/**
 * Proporciona feedback háptico (vibración) cuando se intenta refrescar sin conexión
 */
private fun provideHapticFeedback(context: Context) {
    try {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    } catch (e: Exception) {
        // Ignorar errores de vibración
    }
}

/**
 * Mensaje que se muestra cuando se está en modo sin conexión
 */
@Composable
fun OfflineMessage() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFA000))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = stringResource(R.string.offline_mode),
            tint = Color.White
        )
        Text(
            text = stringResource(R.string.showing_local_data),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/**
 * Estado vacío cuando no hay artículos disponibles
 */
@Composable
fun EmptyState(isOffline: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isOffline) stringResource(R.string.no_local_articles) else stringResource(R.string.no_articles_available),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isOffline) 
                stringResource(R.string.connect_to_download)
            else 
                stringResource(R.string.try_refresh),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = HackerGrey
        )
    }
}

/**
 * Artículo con funcionalidad de swipe para eliminar
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SwipeableArticleItem(
    article: HackerNewsItem,
    onArticleClick: (String) -> Unit,
    onArticleDelete: (String) -> Unit
) {
    var isRemoved by remember { mutableStateOf(false) }
    
    val dismissState = rememberDismissState {
        if (it == DismissValue.DismissedToStart) {
            isRemoved = true
            onArticleDelete(article.objectID)
            true
        } else {
            false
        }
    }
    
    AnimatedVisibility(
        visible = !isRemoved,
        exit = fadeOut(
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
    ) {
        SwipeToDismiss(
            state = dismissState,
            background = {
                SwipeDismissBackground(dismissState.dismissDirection)
            },
            dismissContent = {
                ArticleItem(
                    article = article,
                    onClick = { onArticleClick(article.objectID) }
                )
            },
            directions = setOf(DismissDirection.EndToStart)
        )
    }
}

/**
 * Fondo que se muestra al deslizar para eliminar
 */
@Composable
fun SwipeDismissBackground(dismissDirection: DismissDirection?) {
    val color = if (dismissDirection == DismissDirection.EndToStart) 
        Color.Red.copy(alpha = 0.8f) else Color.Transparent
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(end = 16.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = stringResource(R.string.action_delete),
            tint = Color.White
        )
    }
}

/**
 * Elemento de artículo individual
 */
@Composable
fun ArticleItem(
    article: HackerNewsItem,
    onClick: () -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) HackerDarkBackground else Color.White
    val contentColor = if (isDarkTheme) HackerTextLight else HackerTextDark
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        ArticleTitle(title = article.displayTitle)
        
        Spacer(modifier = Modifier.height(4.dp))
        
        ArticleMetadata(author = article.author)
    }
}

/**
 * Título del artículo
 */
@Composable
fun ArticleTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        color = if (isSystemInDarkTheme()) HackerTextLight else HackerTextDark
    )
}

/**
 * Metadata del artículo (solo muestra el autor)
 */
@Composable
fun ArticleMetadata(author: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val authorText = author ?: stringResource(R.string.author_unknown)
        val metadataColor = HackerGrey
        
        Text(
            text = authorText,
            style = MaterialTheme.typography.bodyMedium,
            color = metadataColor
        )
    }
}

/**
 * Mensaje que se muestra cuando no se permite refrescar
 */
@Composable
fun NoRefreshMessage() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF607D8B))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = stringResource(R.string.no_refresh_allowed),
            tint = Color.White
        )
        Text(
            text = stringResource(R.string.no_refresh_offline_start),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
@Composable
private fun ArticleItemPreview() {
    ListhackernewsTheme {
        ArticleItem(
            article = HackerNewsItem(
                objectID = "1",
                title = "Ejemplo de artículo con un título largo para probar el truncado de texto cuando se excede la cantidad máxima de líneas",
                author = "Autor de ejemplo",
                createdAt = "2023-04-04T12:00:00.000Z",
                createdAtTimestamp = Date().time,
                url = "https://example.com",
                storyTitle = null,
                storyUrl = null,
                commentText = null,
                points = 42
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ArticleMetadataPreview() {
    ListhackernewsTheme {
        ArticleMetadata(
            author = "Autor de prueba"
        )
    }
} 