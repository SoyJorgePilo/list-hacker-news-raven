package jorgepilo.com.list_hacker_news.ui.articles.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jorgepilo.com.list_hacker_news.data.model.HackerNewsItem
import jorgepilo.com.list_hacker_news.data.repository.IArticlesRepository
import jorgepilo.com.list_hacker_news.util.ConnectionState
import jorgepilo.com.list_hacker_news.util.ConnectivityObserver
import jorgepilo.com.list_hacker_news.util.StringResourceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel que gestiona la lista de artículos de Hacker News con soporte offline
 */
@HiltViewModel
class ArticlesListViewModel @Inject constructor(
    private val repository: IArticlesRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val stringProvider: StringResourceProvider
) : ViewModel() {
    
    // Constantes para mensajes de error
    companion object {
        private const val ERROR_NO_CONNECTION = "Sin conexión a internet. Mostrando datos locales."
        private const val ERROR_NO_REFRESH_OFFLINE_START = "No se permite refrescar cuando la app inició sin conexión."
        private const val ERROR_NO_CONNECTION_REFRESH = "No hay conexión a internet para actualizar"
        private const val ERROR_UNKNOWN = "Error desconocido"
    }
    
    // Estado UI interno mutable
    private val _uiState = MutableStateFlow(ArticlesListState())
    
    // Estado UI expuesto como inmutable
    val uiState: StateFlow<ArticlesListState> = _uiState.asStateFlow()
    
    // Estado de conectividad
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()
    
    // Bandera para indicar si la aplicación se inició sin conexión
    private val _startedOffline = MutableStateFlow(false)
    val startedOffline: StateFlow<Boolean> = _startedOffline.asStateFlow()
    
    init {
        // Verificar conectividad inicial inmediatamente
        viewModelScope.launch {
            checkInitialConnectivity()
            
            // Luego observar cambios continuos de conectividad
            observeNetworkConnectivity()
            
            // Observar cambios en la base de datos
            observeLocalArticles()
            
            // Cargar artículos (online u offline según disponibilidad)
            loadArticles()
        }
    }
    
    /**
     * Verifica el estado inicial de conectividad
     */
    private suspend fun checkInitialConnectivity() {
        val isConnected = connectivityObserver.isNetworkAvailable()
        _isOffline.value = !isConnected
        
        // Establecer la bandera si iniciamos sin conexión
        if (!isConnected) {
            _startedOffline.value = true
            _uiState.update { it.copy(
                error = ERROR_NO_CONNECTION,
                hasInitialConnectivity = false
            )}
        } else {
            _uiState.update { it.copy(
                hasInitialConnectivity = true
            )}
        }
    }
    
    /**
     * Observa el estado de la conectividad de red
     */
    private fun observeNetworkConnectivity() {
        connectivityObserver.observe().onEach { state ->
            val wasOffline = _isOffline.value
            val isOffline = state != ConnectionState.Available
            _isOffline.value = isOffline
            
            // Si recuperamos la conexión después de estar offline
            if (wasOffline && !isOffline) {
                // Limpiar mensajes de error relacionados con la conexión
                if (_uiState.value.error?.contains("conexión") == true) {
                    _uiState.update { it.copy(error = null) }
                }
                
                // Intentar actualizar datos solo si no iniciamos sin conexión
                if (!_startedOffline.value && _uiState.value.articles.isNotEmpty()) {
                    refreshArticles()
                }
            }
            
            // Si perdimos la conexión, actualizamos el estado pero no mostramos mensaje
            if (!wasOffline && isOffline) {
                // Sólo actualizamos el estado interno de offline sin mostrar mensaje de error
                // El mensaje solo se mostrará si el usuario intenta refrescar explícitamente
            }
        }.launchIn(viewModelScope)
    }
    
    /**
     * Observa los cambios en los artículos almacenados localmente
     */
    private fun observeLocalArticles() {
        viewModelScope.launch {
            repository.observeArticles().collectLatest { articles ->
                if (articles.isNotEmpty()) {
                    _uiState.update { it.copy(
                        articles = articles,
                        isLoading = false
                    )}
                }
            }
        }
    }
    
    /**
     * Carga la lista de artículos desde el repositorio
     */
    fun loadArticles() {
        repository.getArticles()
            .onStart { handleLoadingState() }
            .onEach { result -> handleResult(result) }
            .catch { error -> handleError(error) }
            .launchIn(viewModelScope)
    }
    
    /**
     * Refresca la lista de artículos (pull-to-refresh)
     */
    fun refreshArticles() {
        // No permitir refrescar si la app se inició sin conexión
        if (_startedOffline.value) {
            _uiState.update { it.copy(
                isRefreshing = false,
                error = ERROR_NO_REFRESH_OFFLINE_START
            )}
            return
        }
        
        // Comprobar si hay conexión actualmente
        if (_isOffline.value) {
            _uiState.update { it.copy(
                isRefreshing = false,
                error = ERROR_NO_CONNECTION_REFRESH
            )}
            return
        }
        
        _uiState.update { it.copy(
            isRefreshing = true,
            error = null
        )}
        
        viewModelScope.launch {
            val result = repository.refreshArticles()
            result.fold(
                onSuccess = { articles ->
                    _uiState.update { it.copy(
                        isRefreshing = false,
                        error = null
                    )}
                },
                onFailure = { error ->
                    val errorMessage = if (error.message?.contains("conexión") == true) {
                        ERROR_NO_CONNECTION_REFRESH
                    } else {
                        error.message ?: ERROR_UNKNOWN
                    }
                    
                    _uiState.update { it.copy(
                        isRefreshing = false,
                        error = errorMessage
                    )}
                }
            )
        }
    }
    
    /**
     * Elimina un artículo de la lista por su ID
     */
    fun deleteArticle(articleId: String) {
        viewModelScope.launch {
            repository.deleteArticle(articleId)
        }
    }
    
    /**
     * Maneja el estado de carga
     */
    private fun handleLoadingState() {
        // Solo mostrar carga si no tenemos artículos aún y tenemos conexión
        if (_uiState.value.articles.isEmpty() && !_isOffline.value) {
            _uiState.update { it.copy(isLoading = true, error = null) }
        }
    }
    
    /**
     * Procesa el resultado de la carga de artículos
     */
    private fun handleResult(result: Result<List<HackerNewsItem>>) {
        result.fold(
            onSuccess = { articles -> handleSuccess(articles) },
            onFailure = { error -> handleError(error) }
        )
    }
    
    /**
     * Maneja un resultado exitoso
     */
    private fun handleSuccess(articles: List<HackerNewsItem>) {
        _uiState.update { 
            it.copy(
                articles = articles,
                isLoading = false,
                isRefreshing = false,
                error = null
            )
        }
    }
    
    /**
     * Maneja un error durante la carga
     */
    private fun handleError(error: Throwable) {
        // Si ya tenemos artículos, no mostrar el error de forma intrusiva a menos que sea un error de conexión
        val shouldShowError = _uiState.value.articles.isEmpty() || 
                             (error.message?.contains("conexión") == true && _isOffline.value)
        
        _uiState.update { currentState ->
            currentState.copy(
                isLoading = false,
                isRefreshing = false,
                error = if (shouldShowError) error.message ?: ERROR_UNKNOWN else null
            )
        }
    }
} 