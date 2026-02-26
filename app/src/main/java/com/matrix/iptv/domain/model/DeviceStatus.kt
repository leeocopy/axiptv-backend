package com.matrix.iptv.domain.model

enum class DeviceStatusReason {
    ACTIVE,
    TRIAL_ACTIVE,
    TRIAL_EXPIRED,
    NOT_REGISTERED,
    ERROR
}

data class DeviceStatus(
    val allowed: Boolean,
    val reason: DeviceStatusReason,
    val trialDaysRemaining: Int = 0,
    val activeUntilMs: Long = 0L,
    val errorMessage: String? = null
)
