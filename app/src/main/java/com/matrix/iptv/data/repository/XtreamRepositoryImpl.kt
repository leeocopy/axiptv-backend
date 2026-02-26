package com.matrix.iptv.data.repository

import com.matrix.iptv.data.remote.model.*
import com.matrix.iptv.domain.repository.XtreamRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XtreamRepositoryImpl @Inject constructor() : XtreamRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
        encodeDefaults = true
    }

    private suspend fun <T> fetch(url: String, serializer: kotlinx.serialization.KSerializer<T>): Result<T> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "Mozilla/5.0") // Standard UA for Xtream
                .build()
            
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Server returned ${response.code}"))
                }
                val body = response.body?.string() ?: return@withContext Result.failure(Exception("Empty response body"))
                try {
                    val data = json.decodeFromString(serializer, body)
                    Result.success(data)
                } catch (e: Exception) {
                    Result.failure(Exception("Parse error: ${e.message}"))
                }
            }
        } catch (e: java.net.UnknownHostException) {
            Result.failure(Exception("Server unreachable"))
        } catch (e: java.net.SocketTimeoutException) {
            Result.failure(Exception("Timeout"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLiveCategories(baseUrl: String, user: String, pass: String): Result<List<LiveCategory>> {
        val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_live_categories"
        return fetch(url, ListSerializer(LiveCategory.serializer()))
    }

    private val liveAllCache = mutableMapOf<String, List<LiveStream>>()
    private val vodAllCache = mutableMapOf<String, List<VodStream>>()
    private val seriesAllCache = mutableMapOf<String, List<SeriesStream>>()

    override suspend fun getLiveStreams(baseUrl: String, user: String, pass: String, categoryId: String): Result<List<LiveStream>> {
        if (categoryId == "-1") {
            val cacheKey = "$baseUrl|$user"
            liveAllCache[cacheKey]?.let { return Result.success(it) }

            // 1. Try fetching all at once
            val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_live_streams"
            val initial = fetch(url, ListSerializer(LiveStream.serializer()))
            
            if (initial.isSuccess && initial.getOrNull()?.isNotEmpty() == true) {
                val data = initial.getOrThrow()
                liveAllCache[cacheKey] = data
                return Result.success(data)
            }

            // 2. Fallback: Merge all categories
            val cats = getLiveCategories(baseUrl, user, pass).getOrNull() ?: return initial
            val merged = mutableListOf<LiveStream>()
            cats.forEach { cat ->
                getLiveStreams(baseUrl, user, pass, cat.id).onSuccess { merged.addAll(it) }
            }
            val result = merged.distinctBy { it.streamId }
            liveAllCache[cacheKey] = result
            return Result.success(result)
        }
        val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_live_streams&category_id=$categoryId"
        return fetch(url, ListSerializer(LiveStream.serializer()))
    }

    override suspend fun getVodCategories(baseUrl: String, user: String, pass: String): Result<List<VodCategory>> {
        val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_vod_categories"
        return fetch(url, ListSerializer(VodCategory.serializer()))
    }

    override suspend fun getVodStreams(baseUrl: String, user: String, pass: String, categoryId: String): Result<List<VodStream>> {
        if (categoryId == "-1") {
            val cacheKey = "$baseUrl|$user"
            vodAllCache[cacheKey]?.let { return Result.success(it) }

            // 1. Try fetching all at once
            val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_vod_streams"
            val initial = fetch(url, ListSerializer(VodStream.serializer()))
            
            if (initial.isSuccess && initial.getOrNull()?.isNotEmpty() == true) {
                val data = initial.getOrThrow()
                vodAllCache[cacheKey] = data
                return Result.success(data)
            }

            // 2. Fallback: Merge all
            val cats = getVodCategories(baseUrl, user, pass).getOrNull() ?: return initial
            val merged = mutableListOf<VodStream>()
            cats.forEach { cat ->
                getVodStreams(baseUrl, user, pass, cat.id).onSuccess { merged.addAll(it) }
            }
            val result = merged.distinctBy { it.streamId }
            vodAllCache[cacheKey] = result
            return Result.success(result)
        }
        val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_vod_streams&category_id=$categoryId"
        return fetch(url, ListSerializer(VodStream.serializer()))
    }

    override suspend fun getSeriesCategories(baseUrl: String, user: String, pass: String): Result<List<SeriesCategory>> {
        val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_series_categories"
        return fetch(url, ListSerializer(SeriesCategory.serializer()))
    }

    override suspend fun getSeriesStreams(baseUrl: String, user: String, pass: String, categoryId: String): Result<List<SeriesStream>> {
        if (categoryId == "-1") {
            val cacheKey = "$baseUrl|$user"
            seriesAllCache[cacheKey]?.let { return Result.success(it) }

            // 1. Try fetching all at once
            val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_series"
            val initial = fetch(url, ListSerializer(SeriesStream.serializer()))
            
            if (initial.isSuccess && initial.getOrNull()?.isNotEmpty() == true) {
                val data = initial.getOrThrow()
                seriesAllCache[cacheKey] = data
                return Result.success(data)
            }

            // 2. Fallback: Merge all
            val cats = getSeriesCategories(baseUrl, user, pass).getOrNull() ?: return initial
            val merged = mutableListOf<SeriesStream>()
            cats.forEach { cat ->
                getSeriesStreams(baseUrl, user, pass, cat.id).onSuccess { merged.addAll(it) }
            }
            val result = merged.distinctBy { it.seriesId }
            seriesAllCache[cacheKey] = result
            return Result.success(result)
        }
        val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_series&category_id=$categoryId"
        return fetch(url, ListSerializer(SeriesStream.serializer()))
    }

    override suspend fun getSeriesInfo(baseUrl: String, user: String, pass: String, seriesId: Int): Result<SeriesInfo> {
        val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_series_info&series_id=$seriesId"
        return fetch(url, SeriesInfo.serializer())
    }
}
