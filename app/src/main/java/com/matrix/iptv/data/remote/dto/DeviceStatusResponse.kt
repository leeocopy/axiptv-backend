package com.matrix.iptv.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class DeviceStatusResponse(
    val server_time: String,
    val trial_end_at: String,
    val is_active: Boolean,
    val active_until: String?,
    val allowed: Boolean,
    val reason: String
)
