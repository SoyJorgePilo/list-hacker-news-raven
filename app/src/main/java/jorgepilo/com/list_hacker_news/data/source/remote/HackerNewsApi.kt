package jorgepilo.com.list_hacker_news.data.source.remote

import jorgepilo.com.list_hacker_news.data.model.HackerNewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HackerNewsApi {
    @GET("search_by_date")
    suspend fun getArticles(@Query("query") query: String = "android"): HackerNewsResponse
} 