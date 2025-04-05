package jorgepilo.com.list_hacker_news.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jorgepilo.com.list_hacker_news.data.repository.ArticlesRepository
import jorgepilo.com.list_hacker_news.data.repository.IArticlesRepository
import jorgepilo.com.list_hacker_news.data.source.local.dao.ArticleDao
import jorgepilo.com.list_hacker_news.util.ConnectivityObserver
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideArticlesRepository(
        articleDao: ArticleDao,
        connectivityObserver: ConnectivityObserver
    ): IArticlesRepository {
        return ArticlesRepository(articleDao, connectivityObserver)
    }
} 