package com.matrix.iptv.domain.repository

import com.matrix.iptv.data.remote.model.*

interface XtreamRepository {
    suspend fun getLiveCategories(baseUrl: String, user: String, pass: String): Result<List<LiveCategory>>
    suspend fun getLiveStreams(baseUrl: String, user: String, pass: String, categoryId: String): Result<List<LiveStream>>
    
    suspend fun getVodCategories(baseUrl: String, user: String, pass: String): Result<List<VodCategory>>
    suspend fun getVodStreams(baseUrl: String, user: String, pass: String, categoryId: String): Result<List<VodStream>>
    suspend fun getVodInfo(baseUrl: String, user: String, pass: String, vodId: Int): Result<VodInfoResponse>
    
    suspend fun getSeriesCategories(baseUrl: String, user: String, pass: String): Result<List<SeriesCategory>>
    suspend fun getSeriesStreams(baseUrl: String, user: String, pass: String, categoryId: String): Result<List<SeriesStream>>
    suspend fun getSeriesInfo(baseUrl: String, user: String, pass: String, seriesId: Int): Result<SeriesInfo>
    suspend fun getShortEpg(baseUrl: String, user: String, pass: String, streamId: Int): Result<EpgResponse>
    
    // Global search across cached data (RAM)
    fun searchLive(query: String): List<LiveStream>
    fun searchMovies(query: String): List<VodStream>
    fun searchSeries(query: String): List<SeriesStream>

    // Server-side search: fetches ALL streams (category=-1) for searching when cache empty
    suspend fun searchLiveOnServer(baseUrl: String, user: String, pass: String, query: String): List<LiveStream>
    suspend fun searchMoviesOnServer(baseUrl: String, user: String, pass: String, query: String): List<VodStream>
    suspend fun searchSeriesOnServer(baseUrl: String, user: String, pass: String, query: String): List<SeriesStream>
    
    fun clearCache()
}
