package jorgepilo.com.list_hacker_news.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import jorgepilo.com.list_hacker_news.data.source.local.dao.ArticleDao
import jorgepilo.com.list_hacker_news.data.source.local.entity.ArticleEntity
import jorgepilo.com.list_hacker_news.data.source.local.util.DateConverter

/**
 * Base de datos local para guardar artículos y permitir consulta offline
 */
@Database(entities = [ArticleEntity::class], version = 1, exportSchema = false)
@TypeConverters(DateConverter::class)
abstract class ArticleDatabase : RoomDatabase() {
    
    abstract fun articleDao(): ArticleDao
    
    companion object {
        @Volatile
        private var INSTANCE: ArticleDatabase? = null
        
        fun getDatabase(context: Context): ArticleDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArticleDatabase::class.java,
                    "articles_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                instance
            }
        }
    }
} 