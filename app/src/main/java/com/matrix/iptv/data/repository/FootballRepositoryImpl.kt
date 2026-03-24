package com.matrix.iptv.data.repository

import com.matrix.iptv.data.remote.FootballApi
import com.matrix.iptv.domain.model.FootballMatch
import com.matrix.iptv.domain.repository.FootballRepository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FootballRepositoryImpl @Inject constructor(
    private val api: FootballApi
) : FootballRepository {

    // ── In-memory cache with 2h TTL (prevents HTTP 429) ──────────────────
    private var cachedMatches: List<FootballMatch>? = null
    private var cacheTimestamp: Long = 0L
    private val cacheTtlMs = 2 * 60 * 60 * 1000L // 2 hours

    // After a 429, block further calls until TTL expires
    private var rateLimitedUntil: Long = 0L

    private fun isCacheValid(): Boolean =
        cachedMatches != null && (System.currentTimeMillis() - cacheTimestamp) < cacheTtlMs

    private fun isRateLimited(): Boolean = System.currentTimeMillis() < rateLimitedUntil

    fun clearMatchCache() {
        cachedMatches = null
        cacheTimestamp = 0L
        rateLimitedUntil = 0L
    }

    private val majorLeagues = listOf(
        "Champions League", "Premier League", "La Liga", "Serie A", "Bundesliga",
        "Ligue 1", "Botola Pro", "Saudi Pro League", "Major League Soccer",
        "Europa League", "Conference League", "FA Cup", "Copa del Rey",
        "Coppa Italia", "DFB-Pokal", "Coupe de France", "Copa Libertadores",
        "Club World Cup", "World Cup", "Euro", "Africa Cup of Nations",
        "Copa América", "Asian Cup", "Nations League", "Qualifiers",
        "Botola", "Morocco", "Egypt", "Tunisia", "Algeria", "Jordan"
    )

    override suspend fun getMatchesToday(): Result<List<FootballMatch>> =
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {

            // 1. Valid cache → return immediately
            if (isCacheValid()) {
                android.util.Log.d("FootballRepo", "Cache hit: ${cachedMatches!!.size} matches")
                return@withContext Result.success(cachedMatches!!)
            }

            // 2. Rate-limited → return stale or empty
            if (isRateLimited()) {
                android.util.Log.w("FootballRepo", "Rate-limited, skipping network call")
                return@withContext cachedMatches?.let { Result.success(it) }
                    ?: Result.success(emptyList())
            }

            try {
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val response = api.getScheduledEvents(todayStr)
                val now = System.currentTimeMillis() / 1000

                val matches = response.events
                    .filter { event ->
                        val league = event.tournament?.name ?: ""
                        val isMajor = majorLeagues.any { league.contains(it, ignoreCase = true) }
                        val startTime = event.startTimestamp ?: 0
                        val isRelevant = event.status.type == "inprogress" || startTime > (now - 7200)
                        isMajor && isRelevant
                    }
                    .sortedByDescending { it.status.type == "inprogress" }
                    .map { event ->
                        FootballMatch(
                            id = event.id.toString(),
                            competition = event.tournament?.name ?: "Unknown League",
                            homeTeam = event.homeTeam.name,
                            awayTeam = event.awayTeam.name,
                            homeLogo = "https://api.sofascore.app/api/v1/team/${event.homeTeam.id}/image",
                            awayLogo = "https://api.sofascore.app/api/v1/team/${event.awayTeam.id}/image",
                            homeScore = event.homeScore?.display,
                            awayScore = event.awayScore?.display,
                            isLive = event.status.type == "inprogress",
                            status = event.status.description ?: "UPCOMING",
                            timeInfo = event.startTimestamp?.let {
                                SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(it * 1000))
                            } ?: "",
                            channels = listOf("Live TV")
                        )
                    }.take(30)

                cachedMatches = matches
                cacheTimestamp = System.currentTimeMillis()
                android.util.Log.d("FootballRepo", "Fetched ${matches.size} matches, TTL=2h")
                Result.success(matches)

            } catch (e: retrofit2.HttpException) {
                if (e.code() == 429) {
                    rateLimitedUntil = System.currentTimeMillis() + cacheTtlMs
                    android.util.Log.w("FootballRepo", "429 → blocked for 2h")
                }
                cachedMatches?.let {
                    android.util.Log.w("FootballRepo", "HTTP error ${e.code()}, returning stale cache")
                    return@withContext Result.success(it)
                }
                Result.failure(e)

            } catch (e: Exception) {
                cachedMatches?.let { return@withContext Result.success(it) }
                android.util.Log.e("FootballRepo", "Fetch failed: ${e.message}")
                Result.failure(e)
            }
        }
}
