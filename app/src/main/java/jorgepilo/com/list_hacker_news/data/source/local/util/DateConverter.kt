package jorgepilo.com.list_hacker_news.data.source.local.util

import androidx.room.TypeConverter
import java.util.Date

/**
 * Clase para convertir tipos de Date a Long y viceversa para Room
 */
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
} 