package com.matrix.iptv.data.repository

import com.matrix.iptv.data.remote.model.*
import com.matrix.iptv.domain.repository.XtreamRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.builtins.ListSerializer
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XtreamRepositoryImpl @Inject constructor(
    private val streamCacheDao: com.matrix.iptv.data.local.db.StreamCacheDao,
    private val dataStoreManager: com.matrix.iptv.data.local.prefs.DataStoreManager
) : XtreamRepository {

    private val client = createUnsafeOkHttpClient()

    private val json = Json { 
        ignoreUnknownKeys = true 
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // TTL-bounded cache: entries expire after 10 minutes, max 500 entries
    private data class CacheEntry<T>(val data: T, val timestamp: Long = System.currentTimeMillis())
    private val CACHE_TTL_MS = 10 * 60 * 1000L // 10 min
    private val MAX_CACHE_ENTRIES = 500

    private val liveCache = LinkedHashMap<String, CacheEntry<List<LiveStream>>>(16, 0.75f, true)
    private val movieCache = LinkedHashMap<String, CacheEntry<List<VodStream>>>(16, 0.75f, true)
    private val seriesCache = LinkedHashMap<String, CacheEntry<List<SeriesStream>>>(16, 0.75f, true)
    private val seriesInfoCache = LinkedHashMap<String, CacheEntry<SeriesInfo>>(16, 0.75f, true)
    private val vodInfoCache = LinkedHashMap<String, CacheEntry<VodInfoResponse>>(16, 0.75f, true)

    private fun <T> LinkedHashMap<String, CacheEntry<T>>.getValid(key: String): T? {
        val entry = this[key] ?: return null
        if (System.currentTimeMillis() - entry.timestamp > CACHE_TTL_MS) {
            remove(key); return null
        }
        return entry.data
    }
    private fun <T> LinkedHashMap<String, CacheEntry<T>>.putBounded(key: String, value: T) {
        if (size >= MAX_CACHE_ENTRIES) entries.iterator().also { it.next(); it.remove() }
        put(key, CacheEntry(value))
    }

    private fun createUnsafeOkHttpClient(): OkHttpClient {
        val fallbackDns = com.matrix.iptv.data.remote.FallbackDns()
        return try {
            val trustAllCerts = arrayOf<javax.net.ssl.TrustManager>(
                object : javax.net.ssl.X509TrustManager {
                    override fun checkClientTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                    override fun checkServerTrusted(chain: Array<out java.security.cert.X509Certificate>?, authType: String?) {}
                    override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> = arrayOf()
                }
            )
            val sslContext = javax.net.ssl.SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())
            OkHttpClient.Builder()
                .dns(fallbackDns)
                .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as javax.net.ssl.X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .connectTimeout(15, TimeUnit.SECONDS)  // was 120 — caused 2min freeze
                .readTimeout(30, TimeUnit.SECONDS)     // was 120
                .writeTimeout(15, TimeUnit.SECONDS)    // was 120
                .retryOnConnectionFailure(false)        // fail fast, handle in fetch()
                .build()
        } catch (e: Exception) {
            OkHttpClient.Builder()
                .dns(fallbackDns)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .build()
        }
    }

    private suspend fun <T> fetch(url: String, serializer: kotlinx.serialization.KSerializer<T>): Result<T> = withContext(Dispatchers.IO) {
        suspend fun attemptRequest(targetUrl: String): Result<T> {
            try {
                val request = Request.Builder()
                    .url(targetUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .header("Accept", "application/json")
                    .header("Connection", "keep-alive")
                    .build()
                
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return Result.failure(Exception("HTTP Error: ${response.code} (url=$targetUrl)"))
                    }
                    val body = response.body ?: return Result.failure(Exception("Empty body (url=$targetUrl)"))
                    
                    try {
                        val inputStream = body.byteStream()
                        val data = json.decodeFromStream(serializer, inputStream)
                        return Result.success(data)
                    } catch (e: Exception) {
                        android.util.Log.e("XtreamRepo", "Parse error for $targetUrl: ${e.message}")
                        return Result.failure(Exception("JSON Parse error: ${e.message}"))
                    }
                }
            } catch (e: Exception) {
                return Result.failure(e)
            }
        }

        val firstAttempt = attemptRequest(url)
        if (firstAttempt.isSuccess) return@withContext firstAttempt

        val e = firstAttempt.exceptionOrNull()
        android.util.Log.w("XtreamRepo", "First attempt failed: ${e?.message} for $url")
        
        val shouldFallback = e is javax.net.ssl.SSLException || e is java.net.SocketTimeoutException || e is java.net.ConnectException || e is java.net.UnknownHostException
        if (shouldFallback && url.startsWith("https://")) {
            val httpUrl = url.replaceFirst("https://", "http://")
            android.util.Log.i("XtreamRepo", "Attempting HTTP fallback: $httpUrl")
            val fallbackAttempt = attemptRequest(httpUrl)
            if (fallbackAttempt.isSuccess) return@withContext fallbackAttempt
            android.util.Log.e("XtreamRepo", "Both HTTPS and HTTP failed for $url")
        }
        
        android.util.Log.e("XtreamRepo", "Fetch error for $url: ${e?.javaClass?.simpleName}: ${e?.message}")
        Result.failure(e ?: Exception("Unknown fetch error"))
    }

    override suspend fun getLiveCategories(baseUrl: String, user: String, pass: String): Result<List<LiveCategory>> {
        val profileId = dataStoreManager.activeProfileId.first()
        ensureCorrectProfileCache(profileId)
        val cached = streamCacheDao.getCategories(profileId, "live")
        if (cached.isNotEmpty()) {
            return Result.success(cached.map { LiveCategory(it.id, it.name) })
        }

        val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_live_categories"
        val result = fetch(url, ListSerializer(LiveCategory.serializer()))
        result.onSuccess { list ->
            streamCacheDao.insertCategories(list.map { 
                com.matrix.iptv.data.local.db.CategoryCacheEntity(
                    uid = "${profileId}_live_${it.id}",
                    profileId = profileId,
                    type = "live",
                    id = it.id,
                    name = it.name
                )
            })
        }
        return result
    }

    override suspend fun getLiveStreams(baseUrl: String, user: String, pass: String, categoryId: String): Result<List<LiveStream>> = withContext(Dispatchers.IO) {
        val profileId = dataStoreManager.activeProfileId.first()
        ensureCorrectProfileCache(profileId)
        
        val cacheKey = "${profileId}_$categoryId"
        liveCache.getValid(cacheKey)?.let { return@withContext Result.success(it) }

        val url = if (categoryId == "-1") {
            "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_live_streams"
        } else {
            "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_live_streams&category_id=$categoryId"
        }
        
        var result = fetch(url, ListSerializer(LiveStream.serializer()))
        
        // Fallback: If "ALL" (-1) returned empty, some servers require category_id=0 for ALL
        if (categoryId == "-1" && (result.isFailure || (result.getOrNull()?.isEmpty() == true))) {
             val fallbackUrl = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_live_streams&category_id=0"
             android.util.Log.d("XtreamRepo", "Live ALL was empty, trying fallback category_id=0")
             val fallbackResult = fetch(fallbackUrl, ListSerializer(LiveStream.serializer()))
             if (fallbackResult.isSuccess && fallbackResult.getOrNull()?.isNotEmpty() == true) {
                 result = fallbackResult
             }
        }

        result.onSuccess { list ->
            // Proactive Memory Management: ONLY clear others if loading the massive "ALL" category.
            // Small categories (like Sports, Movies) can coexist to allow fast switching.
            if (categoryId == "-1") {
                movieCache.clear()
                seriesCache.clear()
            }
            
            // Save to memory for instant access
            liveCache.putBounded("${profileId}_$categoryId", list)
        }
        result
    }

    override suspend fun getVodCategories(baseUrl: String, user: String, pass: String): Result<List<VodCategory>> = withContext(Dispatchers.IO) {
        val profileId = dataStoreManager.activeProfileId.first()
        ensureCorrectProfileCache(profileId)
        val cached = streamCacheDao.getCategories(profileId, "movie")
        if (cached.isNotEmpty()) {
            return@withContext Result.success(cached.map { VodCategory(it.id, it.name) })
        }

        val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_vod_categories"
        val result = fetch(url, ListSerializer(VodCategory.serializer()))
        result.onSuccess { list ->
            val entities = list.map { 
                com.matrix.iptv.data.local.db.CategoryCacheEntity(
                    uid = "${profileId}_movie_${it.id}",
                    profileId = profileId,
                    type = "movie",
                    id = it.id,
                    name = it.name
                )
            }
            streamCacheDao.insertCategories(entities)
        }
        result
    }

    override suspend fun getVodStreams(baseUrl: String, user: String, pass: String, categoryId: String): Result<List<VodStream>> = withContext(Dispatchers.IO) {
        val profileId = dataStoreManager.activeProfileId.first()
        ensureCorrectProfileCache(profileId)
        
        val cacheKey = "${profileId}_$categoryId"
        movieCache.getValid(cacheKey)?.let { return@withContext Result.success(it) }

        val url = if (categoryId == "-1") {
            "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_vod_streams"
        } else {
            "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_vod_streams&category_id=$categoryId"
        }
        
        var result = fetch(url, ListSerializer(VodStream.serializer()))
        
        if (categoryId == "-1" && (result.isFailure || (result.getOrNull()?.isEmpty() == true))) {
            val fallbackUrl = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_vod_streams&category_id=0"
            android.util.Log.d("XtreamRepo", "VOD ALL was empty, trying fallback category_id=0")
            val fallbackResult = fetch(fallbackUrl, ListSerializer(VodStream.serializer()))
            if (fallbackResult.isSuccess && fallbackResult.getOrNull()?.isNotEmpty() == true) {
                result = fallbackResult
            }
        }

        result.onSuccess { list ->
            // Proactive Memory Management: ONLY clear others if loading the massive "ALL" category.
            if (categoryId == "-1") {
                liveCache.clear()
                seriesCache.clear()
            }
            
            movieCache.putBounded("${profileId}_$categoryId", list)
        }
        result
    }

    override suspend fun getSeriesCategories(baseUrl: String, user: String, pass: String): Result<List<SeriesCategory>> = withContext(Dispatchers.IO) {
        val profileId = dataStoreManager.activeProfileId.first()
        ensureCorrectProfileCache(profileId)
        val cached = streamCacheDao.getCategories(profileId, "series")
        if (cached.isNotEmpty()) {
            return@withContext Result.success(cached.map { SeriesCategory(it.id, it.name) })
        }

        val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_series_categories"
        val result = fetch(url, ListSerializer(SeriesCategory.serializer()))
        result.onSuccess { list ->
            val entities = list.map { 
                com.matrix.iptv.data.local.db.CategoryCacheEntity(
                    uid = "${profileId}_series_${it.id}",
                    profileId = profileId,
                    type = "series",
                    id = it.id,
                    name = it.name
                )
            }
            streamCacheDao.insertCategories(entities)
        }
        result
    }

    override suspend fun getSeriesStreams(baseUrl: String, user: String, pass: String, categoryId: String): Result<List<SeriesStream>> = withContext(Dispatchers.IO) {
        val profileId = dataStoreManager.activeProfileId.first()
        ensureCorrectProfileCache(profileId)
        
        val cacheKey = "${profileId}_$categoryId"
        seriesCache.getValid(cacheKey)?.let { return@withContext Result.success(it) }

        val url = if (categoryId == "-1") {
            "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_series"
        } else {
            "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_series&category_id=$categoryId"
        }
        
        var result = fetch(url, ListSerializer(SeriesStream.serializer()))

        if (categoryId == "-1" && (result.isFailure || (result.getOrNull()?.isEmpty() == true))) {
            val fallbackUrl = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_series&category_id=0"
            android.util.Log.d("XtreamRepo", "Series ALL was empty, trying fallback category_id=0")
            val fallbackResult = fetch(fallbackUrl, ListSerializer(SeriesStream.serializer()))
            if (fallbackResult.isSuccess && fallbackResult.getOrNull()?.isNotEmpty() == true) {
                result = fallbackResult
            }
        }

        result.onSuccess { list ->
            // Proactive Memory Management: ONLY clear others if loading the massive "ALL" category.
            if (categoryId == "-1") {
                liveCache.clear()
                movieCache.clear()
            }
            
            seriesCache.putBounded("${profileId}_$categoryId", list)
        }
        result
    }

    override suspend fun getSeriesInfo(baseUrl: String, user: String, pass: String, seriesId: Int): Result<SeriesInfo> {
        val cacheKey = "${user}_$seriesId"
        seriesInfoCache.getValid(cacheKey)?.let { return Result.success(it) }
        
        val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_series_info&series_id=$seriesId"
        val result = fetch(url, SeriesInfo.serializer())
        result.onSuccess { seriesInfoCache.putBounded(cacheKey, it) }
        return result
    }

    override suspend fun getVodInfo(baseUrl: String, user: String, pass: String, vodId: Int): Result<VodInfoResponse> {
        val cacheKey = "${user}_$vodId"
        vodInfoCache.getValid(cacheKey)?.let { return Result.success(it) }
        
        val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_vod_info&vod_id=$vodId"
        val result = fetch(url, VodInfoResponse.serializer())
        result.onSuccess { vodInfoCache.putBounded(cacheKey, it) }
        return result
    }

    override suspend fun getShortEpg(baseUrl: String, user: String, pass: String, streamId: Int): Result<EpgResponse> {
        val url = "${baseUrl}/player_api.php?username=$user&password=$pass&action=get_short_epg&stream_id=$streamId"
        return fetch(url, EpgResponse.serializer())
    }

    override fun searchLive(query: String): List<LiveStream> {
        val all = liveCache.values.flatMap { it.data }.distinctBy { it.streamId }
        return if (query.isBlank()) all else all.filter { it.name.contains(query, ignoreCase = true) }
    }

    override fun searchMovies(query: String): List<VodStream> {
        val all = movieCache.values.flatMap { it.data }.distinctBy { it.streamId }
        return if (query.isBlank()) all else all.filter { it.name.contains(query, ignoreCase = true) }
    }

    override fun searchSeries(query: String): List<SeriesStream> {
        val all = seriesCache.values.flatMap { it.data }.distinctBy { it.seriesId }
        return if (query.isBlank()) all else all.filter { it.name.contains(query, ignoreCase = true) }
    }

    override fun clearCache() {
        liveCache.clear()
        movieCache.clear()
        seriesCache.clear()
        seriesInfoCache.clear()
        vodInfoCache.clear()
        android.util.Log.d("XtreamRepo", "Memory caches cleared")
    }

    // ── Server-side search (when RAM cache is empty) ───────────────────────────

    override suspend fun searchLiveOnServer(baseUrl: String, user: String, pass: String, query: String): List<LiveStream> {
        val cached = searchLive(query)
        if (cached.isNotEmpty()) return cached
        return getLiveStreams(baseUrl, user, pass, "-1")
            .getOrDefault(emptyList())
            .filter { it.name.contains(query, ignoreCase = true) }
    }

    override suspend fun searchMoviesOnServer(baseUrl: String, user: String, pass: String, query: String): List<VodStream> {
        val cached = searchMovies(query)
        if (cached.isNotEmpty()) return cached
        return getVodStreams(baseUrl, user, pass, "-1")
            .getOrDefault(emptyList())
            .filter { it.name.contains(query, ignoreCase = true) }
    }

    override suspend fun searchSeriesOnServer(baseUrl: String, user: String, pass: String, query: String): List<SeriesStream> {
        val cached = searchSeries(query)
        if (cached.isNotEmpty()) return cached
        return getSeriesStreams(baseUrl, user, pass, "-1")
            .getOrDefault(emptyList())
            .filter { it.name.contains(query, ignoreCase = true) }
    }



    private var lastProfileId: String? = null

    private suspend fun ensureCorrectProfileCache(currentProfileId: String) {
        if (lastProfileId != null && lastProfileId != currentProfileId) {
            clearCache()
        }
        lastProfileId = currentProfileId
    }

    // --- Extensions ---

    private fun LiveStream.toEntity(profileId: String, type: String) = com.matrix.iptv.data.local.db.StreamCacheEntity(
        uid = "${profileId}_${type}_$streamId", id = streamId, type = type, name = name, icon = icon, categoryId = categoryId, epgId = epgId, profileId = profileId
    )
    private fun VodStream.toEntity(profileId: String, type: String) = com.matrix.iptv.data.local.db.StreamCacheEntity(
        uid = "${profileId}_${type}_$streamId", id = streamId, type = type, name = name, icon = icon, categoryId = categoryId, extension = extension, profileId = profileId
    )
    private fun SeriesStream.toEntity(profileId: String, type: String) = com.matrix.iptv.data.local.db.StreamCacheEntity(
        uid = "${profileId}_${type}_$seriesId", id = seriesId, type = type, name = name, icon = icon, categoryId = categoryId, profileId = profileId
    )

    private fun com.matrix.iptv.data.local.db.StreamCacheEntity.toLive() = LiveStream(
        streamId = id, streamType = "live_stream", name = name, icon = icon, categoryId = categoryId, epgId = epgId
    )
    private fun com.matrix.iptv.data.local.db.StreamCacheEntity.toVod() = VodStream(
        streamId = id, streamType = "movie", name = name, icon = icon, categoryId = categoryId, extension = extension
    )
    private fun com.matrix.iptv.data.local.db.StreamCacheEntity.toSeries() = SeriesStream(
        seriesId = id, name = name, icon = icon, categoryId = categoryId
    )
}
