package jorgepilo.com.list_hacker_news

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import jorgepilo.com.list_hacker_news.navigation.AppNavigation
import jorgepilo.com.list_hacker_news.ui.theme.ListhackernewsTheme

/**
 * Actividad principal que sirve como punto de entrada a la aplicación.
 * Configura la navegación y el contenido de la aplicación utilizando Jetpack Compose.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Habilitar edge-to-edge para usar todo el espacio disponible en pantalla
        enableEdgeToEdge()
        
        setContent {
            ListhackernewsTheme {
                // Surface principal que envuelve toda la UI
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Componente de navegación que maneja todas las pantallas
                    AppNavigation(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}