package com.matrix.iptv.data.remote

import android.util.Log
import com.matrix.iptv.BuildConfig
import com.matrix.iptv.data.local.prefs.DataStoreManager
import com.matrix.iptv.data.remote.dto.DeviceStatusRequest
import com.matrix.iptv.domain.model.DeviceStatus
import com.matrix.iptv.domain.model.DeviceStatusReason
import com.matrix.iptv.domain.repository.DeviceStatusRepository
import kotlinx.coroutines.flow.first
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

/** Holds raw diagnostic data surfaced on the debug panel */
data class NetworkDiagnostic(
    val url: String,
    val method: String,
    val requestBody: String,
    val responseCode: Int?,        // null = no response (exception)
    val responseBody: String?,
    val exceptionMessage: String?
)

/** Thread-safe singleton that always stores the last call's diagnostics */
object DiagnosticsStore {
    @Volatile var last: NetworkDiagnostic? = null
}

@Singleton
class RemoteDeviceStatusService @Inject constructor(
    private val api: DeviceStatusApi,
    private val dataStoreManager: DataStoreManager
) : DeviceStatusRepository {

    override suspend fun getStatus(deviceHash: String): DeviceStatus {
        val url        = "${BuildConfig.AXIPTV_BACKEND_URL}api/device/status"
        val method     = "POST"
        val reqBody    = """{"device_hash":"$deviceHash"}"""

        Log.d("AXIPTV", "─────────────────────────────────────────")
        Log.d("AXIPTV", "API_BASE_URL : ${BuildConfig.AXIPTV_BACKEND_URL}")
        Log.d("AXIPTV", "Full URL     : $url")
        Log.d("AXIPTV", "Method       : $method")
        Log.d("AXIPTV", "Request body : $reqBody")

        return try {
            val response = api.getDeviceStatus(DeviceStatusRequest(device_hash = deviceHash))

            val respBody = "allowed=${response.allowed}, reason=${response.reason}, " +
                    "trial_end_at=${response.trial_end_at}, active_until=${response.active_until}"

            Log.d("AXIPTV", "Response     : 200 OK")
            Log.d("AXIPTV", "Body         : $respBody")
            Log.d("AXIPTV", "─────────────────────────────────────────")

            DiagnosticsStore.last = NetworkDiagnostic(
                url           = url,
                method        = method,
                requestBody   = reqBody,
                responseCode  = 200,
                responseBody  = respBody,
                exceptionMessage = null
            )

            // Cache response
            val nowMs = System.currentTimeMillis()
            dataStoreManager.setLastStatusAllowed(response.allowed)
            dataStoreManager.setLastStatusReason(response.reason)
            dataStoreManager.setLastServerCheckMs(nowMs)
            dataStoreManager.setLastTrialEndAt(response.trial_end_at)
            dataStoreManager.setLastActiveUntil(response.active_until ?: "")

            val reasonEnum = when (response.reason) {
                "active"  -> DeviceStatusReason.ACTIVE
                "trial"   -> DeviceStatusReason.TRIAL_ACTIVE
                "expired" -> DeviceStatusReason.TRIAL_EXPIRED
                "blocked" -> DeviceStatusReason.ERROR
                else      -> DeviceStatusReason.ERROR
            }

            val trialEndMs = try { Instant.parse(response.trial_end_at).toEpochMilli() } catch (_: Exception) { 0L }
            val serverTimeMs = try { Instant.parse(response.server_time).toEpochMilli() } catch (_: Exception) { System.currentTimeMillis() }

            val remainingDays = if (trialEndMs > serverTimeMs)
                ((trialEndMs - serverTimeMs) / (1000 * 60 * 60 * 24)).coerceAtLeast(0).toInt() + 1
            else 0

            val activeUntilMs = try {
                response.active_until?.let { Instant.parse(it).toEpochMilli() } ?: 0L
            } catch (_: Exception) { 0L }

            DeviceStatus(
                allowed            = response.allowed,
                reason             = reasonEnum,
                trialDaysRemaining = remainingDays,
                activeUntilMs      = activeUntilMs
            )

        } catch (e: Exception) {
            val exMsg = buildString {
                append(e.javaClass.simpleName)
                if (e.message != null) append(": ${e.message}")
                e.cause?.let { append(" | cause: ${it.javaClass.simpleName}: ${it.message}") }
            }
            Log.e("AXIPTV", "Network ERROR: $exMsg", e)
            Log.e("AXIPTV", "─────────────────────────────────────────")

            DiagnosticsStore.last = NetworkDiagnostic(
                url              = url,
                method           = method,
                requestBody      = reqBody,
                responseCode     = null,
                responseBody     = null,
                exceptionMessage = exMsg
            )

            handleOfflineFallback()
        }
    }

    private suspend fun handleOfflineFallback(): DeviceStatus {
        val lastAllowed  = dataStoreManager.lastStatusAllowed.first()
        val lastReason   = dataStoreManager.lastStatusReason.first()
        val lastCheckMs  = dataStoreManager.lastServerCheckMs.first()

        val now           = System.currentTimeMillis()
        val isWithin24H   = (now - lastCheckMs) < (24 * 60 * 60 * 1000L)

        if (lastAllowed && isWithin24H) {
            val reasonEnum = when (lastReason) {
                "active" -> DeviceStatusReason.ACTIVE
                "trial"  -> DeviceStatusReason.TRIAL_ACTIVE
                else     -> DeviceStatusReason.TRIAL_ACTIVE
            }
            return DeviceStatus(allowed = true, reason = reasonEnum, trialDaysRemaining = 0)
        }

        return DeviceStatus(
            allowed      = false,
            reason       = DeviceStatusReason.ERROR,
            errorMessage = "No internet connection"
        )
    }

    override suspend fun debugActivate(deviceHash: String) {
        val now = System.currentTimeMillis()
        dataStoreManager.setIsActivated(true)
        dataStoreManager.setActiveUntilMs(now + 30L * 24 * 60 * 60 * 1000)
    }
}
