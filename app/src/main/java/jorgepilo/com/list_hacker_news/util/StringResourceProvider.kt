package jorgepilo.com.list_hacker_news.util

import android.content.Context
import androidx.annotation.StringRes
import javax.inject.Inject

/**
 * Interfaz para proveer recursos de strings de forma que facilite pruebas unitarias
 */
interface StringResourceProvider {
    fun getString(@StringRes resId: Int): String
    fun getString(@StringRes resId: Int, vararg formatArgs: Any): String
}

/**
 * Implementación de StringResourceProvider que utiliza el contexto de la aplicación
 */
class StringResourceProviderImpl @Inject constructor(
    private val context: Context
) : StringResourceProvider {
    
    override fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }
    
    override fun getString(@StringRes resId: Int, vararg formatArgs: Any): String {
        return context.getString(resId, *formatArgs)
    }
} 