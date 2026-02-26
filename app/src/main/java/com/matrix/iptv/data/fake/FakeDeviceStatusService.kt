package com.matrix.iptv.data.fake

import com.matrix.iptv.data.local.prefs.DataStoreManager
import com.matrix.iptv.domain.model.DeviceStatus
import com.matrix.iptv.domain.model.DeviceStatusReason
import com.matrix.iptv.domain.repository.DeviceStatusRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private const val TRIAL_DAYS = 7L
private const val DAY_MS = 24 * 60 * 60 * 1000L

/**
 * Fake implementation of DeviceStatusRepository.
 * All data is stored locally in DataStore keyed to the device hash.
 * TODO: Replace with RetrofitDeviceStatusService when backend is ready.
 *
 * Logic:
 *  - First call: start trial (store trialStartMs)
 *  - Within TRIAL_DAYS: allowed=true, reason=TRIAL_ACTIVE
 *  - After TRIAL_DAYS: check isActivated + activeUntilMs > now -> ACTIVE
 *  - Otherwise: TRIAL_EXPIRED
 */
@Singleton
class FakeDeviceStatusService @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : DeviceStatusRepository {

    override suspend fun getStatus(deviceHash: String): DeviceStatus {
        return try {
            val storedHash    = dataStoreManager.deviceHash.first()
            val trialStartMs  = dataStoreManager.trialStartMs.first()
            val isActivated   = dataStoreManager.isActivated.first()
            val activeUntilMs = dataStoreManager.activeUntilMs.first()
            val now           = System.currentTimeMillis()

            // If activated via dev-tools or future backend
            if (isActivated && activeUntilMs > now) {
                return DeviceStatus(
                    allowed = true,
                    reason = DeviceStatusReason.ACTIVE,
                    activeUntilMs = activeUntilMs
                )
            }

            // New device / first launch — start trial
            if (storedHash.isEmpty() || storedHash != deviceHash || trialStartMs == 0L) {
                val startMs = if (storedHash == deviceHash && trialStartMs != 0L) trialStartMs else now
                dataStoreManager.setDeviceHash(deviceHash)
                dataStoreManager.setTrialStartMs(startMs)

                val elapsed   = now - startMs
                val remaining = ((TRIAL_DAYS * DAY_MS - elapsed) / DAY_MS).coerceAtLeast(0).toInt()

                return if (remaining > 0) {
                    DeviceStatus(true, DeviceStatusReason.TRIAL_ACTIVE, trialDaysRemaining = remaining)
                } else {
                    DeviceStatus(false, DeviceStatusReason.TRIAL_EXPIRED)
                }
            }

            // Existing device — check trial
            val elapsed   = now - trialStartMs
            val remaining = ((TRIAL_DAYS * DAY_MS - elapsed) / DAY_MS).coerceAtLeast(0).toInt()

            if (remaining > 0) {
                DeviceStatus(true, DeviceStatusReason.TRIAL_ACTIVE, trialDaysRemaining = remaining)
            } else {
                DeviceStatus(false, DeviceStatusReason.TRIAL_EXPIRED)
            }
        } catch (e: Exception) {
            DeviceStatus(false, DeviceStatusReason.ERROR, errorMessage = e.message)
        }
    }

    /**
     * DEBUG only — simulates activating device for +30 days.
     * Accessible via long-press on version text in DEBUG builds.
     */
    override suspend fun debugActivate(deviceHash: String) {
        val now = System.currentTimeMillis()
        dataStoreManager.setIsActivated(true)
        dataStoreManager.setActiveUntilMs(now + 30 * DAY_MS)
        dataStoreManager.setDeviceHash(deviceHash)
    }
}
