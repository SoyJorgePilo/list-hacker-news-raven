package jorgepilo.com.list_hacker_news.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Esquema de colores para tema oscuro inspirado en Hacker News
 */
private val DarkColorScheme = darkColorScheme(
    primary = HackerNewsOrange,
    onPrimary = HackerTextLight,
    primaryContainer = HackerNewsOrange,
    onPrimaryContainer = HackerTextLight,
    secondary = HackerGrey,
    onSecondary = HackerTextLight,
    background = HackerDarkBackground,
    onBackground = HackerTextLight,
    surface = HackerDarkSurface,
    onSurface = HackerTextLight
)

/**
 * Esquema de colores para tema claro inspirado en Hacker News
 */
private val LightColorScheme = lightColorScheme(
    primary = HackerNewsOrange,
    onPrimary = HackerTextLight,
    primaryContainer = HackerNewsOrange,
    onPrimaryContainer = HackerTextLight,
    secondary = HackerGrey,
    onSecondary = HackerTextDark,
    background = Color.White,
    onBackground = HackerTextDark,
    surface = Color.White,
    onSurface = HackerTextDark
)

/**
 * Tema principal de la aplicación Hacker News
 *
 * @param darkTheme Indica si se debe usar el tema oscuro
 * @param dynamicColor Indica si se debe usar colores dinámicos (Android 12+)
 * @param content Contenido a envolver con el tema
 */
@Composable
fun ListhackernewsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Lo desactivamos por defecto para mantener nuestra identidad de marca
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    // Ajustar el color de la barra de estado
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = HackerNewsOrange.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}