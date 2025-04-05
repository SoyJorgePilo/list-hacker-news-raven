package jorgepilo.com.list_hacker_news.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jorgepilo.com.list_hacker_news.data.source.local.ArticleDatabase
import jorgepilo.com.list_hacker_news.util.ConnectivityObserver
import jorgepilo.com.list_hacker_news.util.NetworkConnectivityObserver
import jorgepilo.com.list_hacker_news.util.StringResourceProvider
import jorgepilo.com.list_hacker_news.util.StringResourceProviderImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideArticleDatabase(@ApplicationContext context: Context) = 
        ArticleDatabase.getDatabase(context)
    
    @Provides
    @Singleton
    fun provideArticleDao(database: ArticleDatabase) = 
        database.articleDao()
    
    @Provides
    @Singleton
    fun provideConnectivityObserver(@ApplicationContext context: Context): ConnectivityObserver = 
        NetworkConnectivityObserver(context)
    
    @Provides
    @Singleton
    fun provideStringResourceProvider(@ApplicationContext context: Context): StringResourceProvider = 
        StringResourceProviderImpl(context)
} 