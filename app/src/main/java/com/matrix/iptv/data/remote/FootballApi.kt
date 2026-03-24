package com.matrix.iptv.data.remote

import com.matrix.iptv.data.remote.dto.RapidResponse
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Path

interface FootballApi {
    @Headers(
        "x-rapidapi-host: sportapi7.p.rapidapi.com",
        "x-rapidapi-key: c409315d3fmsh8e716c5143c4205p1769b7jsn37de79d34b1b"
    )
    @GET("api/v1/sport/football/scheduled-events/{date}")
    suspend fun getScheduledEvents(
        @Path("date") date: String
    ): RapidResponse
}
