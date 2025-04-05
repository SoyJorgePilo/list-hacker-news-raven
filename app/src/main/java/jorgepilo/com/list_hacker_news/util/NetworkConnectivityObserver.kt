package jorgepilo.com.list_hacker_news.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Estados de conectividad
 */
enum class ConnectionState {
    Available, Unavailable, Lost
}

/**
 * Implementación del observador de conectividad utilizando el ConnectivityManager de Android
 */
class NetworkConnectivityObserver(
    private val context: Context
) : ConnectivityObserver {
    
    private val connectivityManager = context.getSystemService<ConnectivityManager>()
    
    /**
     * Devuelve un Flow que emite el estado de conectividad actual y los cambios
     */
    override fun observe(): Flow<ConnectionState> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                trySend(ConnectionState.Available)
            }
            
            override fun onLost(network: Network) {
                super.onLost(network)
                trySend(ConnectionState.Lost)
            }
            
            override fun onUnavailable() {
                super.onUnavailable()
                trySend(ConnectionState.Unavailable)
            }
        }
        
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        
        connectivityManager?.registerNetworkCallback(networkRequest, callback)
        
        // Emitir el estado inicial
        val currentState = if (isNetworkAvailable()) {
            ConnectionState.Available
        } else {
            ConnectionState.Unavailable
        }
        trySend(currentState)
        
        awaitClose {
            connectivityManager?.unregisterNetworkCallback(callback)
        }
    }.distinctUntilChanged()
    
    /**
     * Comprueba si la red está disponible actualmente
     */
    override suspend fun isNetworkAvailable(): Boolean {
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
} 