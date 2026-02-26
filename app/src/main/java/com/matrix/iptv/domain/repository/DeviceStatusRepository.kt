package com.matrix.iptv.domain.repository

import com.matrix.iptv.domain.model.DeviceStatus

/**
 * Interface for device activation/trial status.
 * Currently implemented by FakeDeviceStatusService.
 * TODO: Replace with RetrofitDeviceStatusService when backend is ready.
 */
interface DeviceStatusRepository {
    suspend fun getStatus(deviceHash: String): DeviceStatus
    /** DEBUG only — simulates activating device for +30 days. No-op in release. */
    suspend fun debugActivate(deviceHash: String)
}
