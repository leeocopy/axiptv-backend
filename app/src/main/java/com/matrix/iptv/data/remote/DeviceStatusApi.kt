package com.matrix.iptv.data.remote

import com.matrix.iptv.data.remote.dto.DeviceStatusRequest
import com.matrix.iptv.data.remote.dto.DeviceStatusResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface DeviceStatusApi {

    @POST("api/device/status")
    suspend fun getDeviceStatus(@Body request: DeviceStatusRequest): DeviceStatusResponse

}
