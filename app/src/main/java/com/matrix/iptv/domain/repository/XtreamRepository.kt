package com.matrix.iptv.domain.repository

import com.matrix.iptv.data.remote.model.*

interface XtreamRepository {
    suspend fun getLiveCategories(baseUrl: String, user: String, pass: String): Result<List<LiveCategory>>
    suspend fun getLiveStreams(baseUrl: String, user: String, pass: String, categoryId: String): Result<List<LiveStream>>
    
    suspend fun getVodCategories(baseUrl: String, user: String, pass: String): Result<List<VodCategory>>
    suspend fun getVodStreams(baseUrl: String, user: String, pass: String, categoryId: String): Result<List<VodStream>>
    
    suspend fun getSeriesCategories(baseUrl: String, user: String, pass: String): Result<List<SeriesCategory>>
    suspend fun getSeriesStreams(baseUrl: String, user: String, pass: String, categoryId: String): Result<List<SeriesStream>>
    suspend fun getSeriesInfo(baseUrl: String, user: String, pass: String, seriesId: Int): Result<SeriesInfo>
}
