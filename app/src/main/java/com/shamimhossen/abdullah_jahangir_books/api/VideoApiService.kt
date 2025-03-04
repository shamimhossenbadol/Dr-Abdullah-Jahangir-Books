package com.shamimhossen.abdullah_jahangir_books.api

import com.shamimhossen.abdullah_jahangir_books.models.Videos
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface VideoApiService {
    @GET("/youtube/v3/playlistItems")
    fun getVideos(
        @Query("part") part: String,
        @Query("maxResults") maxResults: Int?,
        @Query("playlistId") playlistId: String,
        @Query("key") key: String,
        @Query("pageToken") pageToken: String?
    ): Call<Videos>
}