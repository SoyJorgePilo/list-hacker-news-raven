package jorgepilo.com.list_hacker_news.util

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {
    fun observe(): Flow<ConnectionState>
    suspend fun isNetworkAvailable(): Boolean
} 