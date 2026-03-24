package com.matrix.iptv.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LiveCategory(
    @SerialName("category_id") val id: String,
    @SerialName("category_name") val name: String,
    @SerialName("parent_id") val parentId: Int = 0
)

@Serializable
data class VodCategory(
    @SerialName("category_id") val id: String,
    @SerialName("category_name") val name: String,
    @SerialName("parent_id") val parentId: Int = 0
)

@Serializable
data class SeriesCategory(
    @SerialName("category_id") val id: String,
    @SerialName("category_name") val name: String,
    @SerialName("parent_id") val parentId: Int = 0
)

@Serializable
data class LiveStream(
    @SerialName("num") val num: Int? = null,
    @SerialName("name") val name: String = "",
    @SerialName("stream_type") val streamType: String = "live",
    @SerialName("stream_id") val streamId: Int = -1,
    @SerialName("stream_icon") val icon: String? = null,
    @SerialName("epg_channel_id") val epgId: String? = null,
    @SerialName("category_id") val categoryId: String? = null
)

@Serializable
data class VodStream(
    @SerialName("num") val num: Int? = null,
    @SerialName("name") val name: String = "",
    @SerialName("stream_type") val streamType: String = "movie",
    @SerialName("stream_id") val streamId: Int = -1,
    @SerialName("stream_icon") val icon: String? = null,
    @SerialName("rating") val rating: String? = null,
    @SerialName("added") val added: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("container_extension") val extension: String? = null,
    @SerialName("plot") val plot: String? = null,
    @SerialName("plot_ar") val plotAr: String? = null,
    @SerialName("description_ar") val descriptionAr: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("release_date") val releaseDateSn: String? = null
)

@Serializable
data class SeriesStream(
    @SerialName("num") val num: Int? = null,
    @SerialName("name") val name: String = "",
    @SerialName("series_id") val seriesId: Int = -1,
    @SerialName("cover") val icon: String? = null,
    @SerialName("plot") val plot: String? = null,
    @SerialName("cast") val cast: String? = null,
    @SerialName("director") val director: String? = null,
    @SerialName("genre") val genre: String? = null,
    @SerialName("releaseDate") val releaseDate: String? = null,
    @SerialName("rating") val rating: String? = null,
    @SerialName("category_id") val categoryId: String? = null,
    @SerialName("plot_ar") val plotAr: String? = null,
    @SerialName("description_ar") val descriptionAr: String? = null,
    @SerialName("description") val description: String? = null,
    @SerialName("release_date") val releaseDateSn: String? = null
)

@Serializable
data class SeriesInfo(
    @SerialName("seasons") val seasons: List<Season>? = null,
    @SerialName("episodes") val episodes: Map<String, List<Episode>>? = null,
    @SerialName("info") val info: SeriesDetails? = null
)

@Serializable
data class Season(
    @SerialName("air_date") val airDate: String? = null,
    @SerialName("episode_count") val episodeCount: Int? = null,
    @SerialName("id") val id: Int? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("season_number") val seasonNumber: Int? = null
)

@Serializable
data class Episode(
    @SerialName("id") val id: String? = null,
    @SerialName("episode_num") val num: Int? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("container_extension") val container_extension: String? = null,
    @SerialName("season") val season: Int? = null,
    @SerialName("plot") val plot: String? = null,
    @SerialName("plot_ar") val plotAr: String? = null,
    @SerialName("description_ar") val descriptionAr: String? = null,
    @SerialName("info") val info: EpisodeInfo? = null
)

@Serializable
data class EpisodeInfo(
    @SerialName("movie_image") val movieImage: String? = null,
    @SerialName("plot") val plot: String? = null,
    @SerialName("releasedate") val releaseDate: String? = null,
    @SerialName("duration") val duration: String? = null
)

@Serializable
data class SeriesDetails(
    @SerialName("name") val name: String? = null,
    @SerialName("cover") val cover: String? = null,
    @SerialName("plot") val plot: String? = null,
    @SerialName("genre") val genre: String? = null
)

@Serializable
data class EpgResponse(
    @SerialName("epg_listings") val listings: List<EpgListing> = emptyList()
)

@Serializable
data class EpgListing(
    @SerialName("id") val id: String? = null,
    @SerialName("title") val titleBase64: String? = null, // Sometimes base64
    @SerialName("start") val start: String? = null,
    @SerialName("end") val end: String? = null,
    @SerialName("description") val descriptionBase64: String? = null
) {
    val title: String get() = decode(titleBase64)
    val description: String get() = decode(descriptionBase64)

    private fun decode(base64: String?): String {
        return try {
            if (base64 == null) "" else String(android.util.Base64.decode(base64, android.util.Base64.DEFAULT))
        } catch (e: Exception) {
            base64 ?: ""
        }
    }
}

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

@Serializable
data class VodInfoResponse(
    @SerialName("movie_data") val movieData: VodStream? = null,
    @SerialName("info") val info: VodStream? = null
)
