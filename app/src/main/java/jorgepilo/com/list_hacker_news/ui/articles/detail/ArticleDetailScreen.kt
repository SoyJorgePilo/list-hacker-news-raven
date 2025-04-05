package jorgepilo.com.list_hacker_news.ui.articles.detail

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import jorgepilo.com.list_hacker_news.ui.theme.HackerDarkBackground
import jorgepilo.com.list_hacker_news.ui.theme.HackerGrey
import jorgepilo.com.list_hacker_news.ui.theme.HackerNewsOrange
import jorgepilo.com.list_hacker_news.ui.theme.ListhackernewsTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalClipboardManager

/**
 * Pantalla de detalle que muestra el contenido de un artículo en un WebView
 */
@Composable
fun ArticleDetailScreen(
    url: String,
    title: String,
    onNavigateBack: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    val context = LocalContext.current
    var isOffline by remember { mutableStateOf(false) }
    
    // Verificar conectividad inicial
    LaunchedEffect(Unit) {
        isOffline = !isNetworkAvailable(context)
    }
    
    ArticleDetailContent(
        url = url,
        title = title,
        isLoading = isLoading,
        loadError = loadError,
        isOffline = isOffline,
        onNavigateBack = onNavigateBack,
        onRefresh = { 
            // Comprobar conectividad antes de refrescar
            isOffline = !isNetworkAvailable(context)
            if (!isOffline) {
                webView?.reload() 
            }
        },
        onWebViewCreated = { webView = it },
        onLoadingStateChanged = { loading -> isLoading = loading },
        onErrorStateChanged = { error -> 
            loadError = error
            // Si recibimos un error, verificar si es debido a la conexión
            if (error != null) {
                isOffline = !isNetworkAvailable(context)
            }
        }
    )
}

/**
 * Función para verificar si hay conexión a internet disponible
 */
private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}

/**
 * Contenido principal de la pantalla de detalle, separado para facilitar pruebas y previsualización
 */
@Composable
fun ArticleDetailContent(
    url: String,
    title: String,
    isLoading: Boolean,
    loadError: String?,
    isOffline: Boolean = false,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    onLoadingStateChanged: (Boolean) -> Unit,
    onErrorStateChanged: (String?) -> Unit
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) HackerDarkBackground else Color.White
    val snackbarHostState = remember { SnackbarHostState() }
    var showOfflineMessage by remember { mutableStateOf(false) }
    
    // Mostrar Snackbar cuando intentamos refrescar sin conexión
    LaunchedEffect(isOffline) {
        if (isOffline) {
            snackbarHostState.showSnackbar(
                message = "No hay conexión a internet",
                duration = SnackbarDuration.Short
            )
        }
    }
    
    // Mostrar mensaje temporal cuando intentamos cargar sin conexión
    LaunchedEffect(isOffline, isLoading) {
        if (isOffline && isLoading) {
            showOfflineMessage = true
            delay(3000)
            showOfflineMessage = false
        }
    }
    
    Scaffold(
        topBar = {
            ArticleDetailTopBar(
                title = title,
                isOffline = isOffline,
                onNavigateBack = onNavigateBack,
                onRefresh = onRefresh
            )
        },
        containerColor = backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Banner de sin conexión
            AnimatedVisibility(
                visible = showOfflineMessage,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                OfflineMessage()
            }
            
            // WebView para mostrar el contenido del artículo
            ArticleWebView(
                url = url,
                onWebViewCreated = onWebViewCreated,
                onLoadingStateChanged = onLoadingStateChanged,
                onErrorStateChanged = onErrorStateChanged,
                isOffline = isOffline,
                modifier = Modifier.fillMaxSize()
            )
            
            // Indicador de carga mientras se carga la página
            if (isLoading) {
                LoadingIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = if (isOffline) Color.Gray else HackerNewsOrange
                )
            }
            
            // Mostrar mensaje de error si hay problemas cargando la URL
            loadError?.let { error ->
                ErrorMessage(
                    error = error,
                    isOffline = isOffline,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

/**
 * Mensaje que se muestra cuando se está en modo sin conexión
 */
@Composable
fun OfflineMessage(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Red.copy(alpha = 0.8f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color.White
        )
        Spacer(modifier = Modifier.padding(8.dp))
        Text(
            text = "No hay conexión a internet. Mostrando contenido en caché.",
            color = Color.White
        )
    }
}

/**
 * Barra superior para la pantalla de detalle
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailTopBar(
    title: String,
    isOffline: Boolean = false,
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit
) {
    TopAppBar(
        title = { 
            Column {
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                if (isOffline) {
                    Text(
                        text = "Modo sin conexión",
                        color = Color.White.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(
                onClick = onRefresh,
                enabled = !isOffline
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh, 
                    contentDescription = "Refrescar",
                    tint = if (isOffline) Color.White.copy(alpha = 0.5f) else Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = HackerNewsOrange,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        )
    )
}

/**
 * WebView para mostrar el contenido de un artículo
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ArticleWebView(
    url: String,
    onWebViewCreated: (WebView) -> Unit,
    onLoadingStateChanged: (Boolean) -> Unit,
    onErrorStateChanged: (String?) -> Unit,
    isOffline: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Si no hay conexión y es la primera carga, mostrar un error directamente
    DisposableEffect(isOffline) {
        if (isOffline) {
            onLoadingStateChanged(false)
            onErrorStateChanged("No hay conexión a internet")
        }
        onDispose { }
    }
    
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        onLoadingStateChanged(true)
                        onErrorStateChanged(null)
                    }
                    
                    override fun onPageFinished(view: WebView, url: String) {
                        super.onPageFinished(view, url)
                        onLoadingStateChanged(false)
                    }
                    
                    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                        super.onReceivedError(view, request, error)
                        onLoadingStateChanged(false)
                        val errorMessage = if (!isNetworkAvailable(context)) {
                            "No hay conexión a internet"
                        } else {
                            "Error al cargar la página"
                        }
                        onErrorStateChanged(errorMessage)
                    }
                    
                    // Mantener la navegación dentro del WebView
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        // Si no hay conexión, evitar cargar nuevas URLs
                        if (!isNetworkAvailable(context)) {
                            onErrorStateChanged("No hay conexión a internet")
                            return true
                        }
                        return false // Permite que el WebView maneje la carga de URLs
                    }
                }
                
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadsImagesAutomatically = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                    setSupportZoom(true)
                    builtInZoomControls = true
                    displayZoomControls = false
                    
                    // Configurar caché para modo offline
                    cacheMode = if (isOffline) {
                        android.webkit.WebSettings.LOAD_CACHE_ONLY // Usar solo caché si no hay conexión
                    } else {
                        android.webkit.WebSettings.LOAD_DEFAULT // Usar caché si está disponible, sino hacer petición de red
                    }
                }
                
                // Solo cargar URL si hay conexión o es la primera vez
                if (!isOffline || url.isNotEmpty()) {
                    loadUrl(url)
                }
                
                onWebViewCreated(this)
            }
        },
        modifier = modifier
    )
}

/**
 * Indicador de carga
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = HackerNewsOrange
) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color
    )
}

/**
 * Mensaje de error
 */
@Composable
fun ErrorMessage(
    error: String,
    isOffline: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isOffline) "Sin conexión a internet" else "Error al cargar el contenido",
            style = MaterialTheme.typography.titleMedium,
            color = Color.Red
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = if (isOffline) 
                "El contenido web no se puede cargar en modo sin conexión." 
            else 
                error,
            style = MaterialTheme.typography.bodyMedium,
            color = HackerGrey
        )
    }
}

// PREVISUALIZACIÓN

@Preview(showBackground = true)
@Composable
fun ArticleDetailContentPreview() {
    ListhackernewsTheme {
        ArticleDetailContent(
            url = "https://example.com",
            title = "Ejemplo de artículo con un título muy largo que debería truncarse",
            isLoading = false,
            loadError = null,
            onNavigateBack = {},
            onRefresh = {},
            onWebViewCreated = {},
            onLoadingStateChanged = {},
            onErrorStateChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ArticleDetailLoadingPreview() {
    ListhackernewsTheme {
        ArticleDetailContent(
            url = "https://example.com",
            title = "Artículo cargando",
            isLoading = true,
            loadError = null,
            onNavigateBack = {},
            onRefresh = {},
            onWebViewCreated = {},
            onLoadingStateChanged = {},
            onErrorStateChanged = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ArticleDetailErrorPreview() {
    ListhackernewsTheme {
        ArticleDetailContent(
            url = "https://example.com",
            title = "Artículo con error",
            isLoading = false,
            loadError = "Error al cargar la página",
            isOffline = true,
            onNavigateBack = {},
            onRefresh = {},
            onWebViewCreated = {},
            onLoadingStateChanged = {},
            onErrorStateChanged = {}
        )
    }
} 