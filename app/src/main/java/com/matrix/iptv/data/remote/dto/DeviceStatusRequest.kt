package com.matrix.iptv.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeviceStatusRequest(
    val device_hash: String
)
