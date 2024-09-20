package com.astract.saludapp.network

import com.astract.saludapp.models.NewsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface NewsApiService {
    @GET
    suspend fun getHealthNews(@Url url: String): Response<NewsResponse>
}

