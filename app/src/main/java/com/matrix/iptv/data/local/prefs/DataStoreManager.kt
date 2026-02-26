package com.matrix.iptv.data.local.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "matrix_prefs")

private const val TRIAL_DAYS   = 7L
private const val MS_PER_DAY   = 24 * 60 * 60 * 1000L

@Singleton
class DataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_IS_ACTIVATED      = booleanPreferencesKey("is_activated")
        val KEY_TRIAL_START_MS    = longPreferencesKey("trial_start_ms")
        val KEY_TRIAL_END_MS      = longPreferencesKey("trial_end_ms")
        val KEY_ACTIVE_UNTIL_MS   = longPreferencesKey("active_until_ms")
        val KEY_DEVICE_HASH       = stringPreferencesKey("device_hash")
        val KEY_DEVICE_ID_CACHED  = stringPreferencesKey("device_id_cached")
        val KEY_ACTIVE_PROFILE    = stringPreferencesKey("active_profile_id")
        val KEY_SORT_ALPHA        = booleanPreferencesKey("sort_alpha")
        val KEY_AUTO_START        = booleanPreferencesKey("auto_start")
        val KEY_LANGUAGE          = stringPreferencesKey("language")
        
        val KEY_LAST_STATUS_ALLOWED = booleanPreferencesKey("last_status_allowed")
        val KEY_LAST_STATUS_REASON  = stringPreferencesKey("last_status_reason")
        val KEY_LAST_SERVER_CHECK_MS = longPreferencesKey("last_server_check_ms")
        val KEY_LAST_TRIAL_END_AT   = stringPreferencesKey("last_trial_end_at")
        val KEY_LAST_ACTIVE_UNTIL   = stringPreferencesKey("last_active_until")
    }

    // ── Flows ────────────────────────────────────────────────────────────────
    val isActivated: Flow<Boolean>    = context.dataStore.data.map { it[KEY_IS_ACTIVATED]     ?: false }
    val trialStartMs: Flow<Long>      = context.dataStore.data.map { it[KEY_TRIAL_START_MS]   ?: 0L }
    val trialEndMs: Flow<Long>        = context.dataStore.data.map { it[KEY_TRIAL_END_MS]     ?: 0L }
    val activeUntilMs: Flow<Long>     = context.dataStore.data.map { it[KEY_ACTIVE_UNTIL_MS]  ?: 0L }
    val deviceHash: Flow<String>      = context.dataStore.data.map { it[KEY_DEVICE_HASH]      ?: "" }
    val deviceIdCached: Flow<String>  = context.dataStore.data.map { it[KEY_DEVICE_ID_CACHED] ?: "" }
    val activeProfileId: Flow<String> = context.dataStore.data.map { it[KEY_ACTIVE_PROFILE]   ?: "" }

    val lastStatusAllowed: Flow<Boolean> = context.dataStore.data.map { it[KEY_LAST_STATUS_ALLOWED] ?: false }
    val lastStatusReason: Flow<String> = context.dataStore.data.map { it[KEY_LAST_STATUS_REASON] ?: "" }
    val lastServerCheckMs: Flow<Long> = context.dataStore.data.map { it[KEY_LAST_SERVER_CHECK_MS] ?: 0L }
    val lastTrialEndAt: Flow<String> = context.dataStore.data.map { it[KEY_LAST_TRIAL_END_AT] ?: "" }
    val lastActiveUntil: Flow<String> = context.dataStore.data.map { it[KEY_LAST_ACTIVE_UNTIL] ?: "" }

    // ── Setters ──────────────────────────────────────────────────────────────
    suspend fun setIsActivated(v: Boolean)    = context.dataStore.edit { it[KEY_IS_ACTIVATED]     = v }
    suspend fun setTrialStartMs(v: Long)      = context.dataStore.edit { it[KEY_TRIAL_START_MS]   = v }
    suspend fun setTrialEndMs(v: Long)        = context.dataStore.edit { it[KEY_TRIAL_END_MS]     = v }
    suspend fun setActiveUntilMs(v: Long)     = context.dataStore.edit { it[KEY_ACTIVE_UNTIL_MS]  = v }
    suspend fun setDeviceHash(v: String)      = context.dataStore.edit { it[KEY_DEVICE_HASH]      = v }
    suspend fun setDeviceIdCached(v: String)  = context.dataStore.edit { it[KEY_DEVICE_ID_CACHED] = v }
    suspend fun setActiveProfileId(v: String) = context.dataStore.edit { it[KEY_ACTIVE_PROFILE]   = v }

    suspend fun setLastStatusAllowed(v: Boolean) = context.dataStore.edit { it[KEY_LAST_STATUS_ALLOWED] = v }
    suspend fun setLastStatusReason(v: String)   = context.dataStore.edit { it[KEY_LAST_STATUS_REASON] = v }
    suspend fun setLastServerCheckMs(v: Long)    = context.dataStore.edit { it[KEY_LAST_SERVER_CHECK_MS] = v }
    suspend fun setLastTrialEndAt(v: String)     = context.dataStore.edit { it[KEY_LAST_TRIAL_END_AT] = v }
    suspend fun setLastActiveUntil(v: String)    = context.dataStore.edit { it[KEY_LAST_ACTIVE_UNTIL] = v }

    // ── Trial helpers ────────────────────────────────────────────────────────

    /**
     * If trialStartMs == 0 → initialise trial (now + 7 days). Returns trialEndMs.
     */
    suspend fun ensureTrialInitialised(now: Long = System.currentTimeMillis()): Long {
        val start = trialStartMs.first()
        if (start == 0L) {
            val end = now + TRIAL_DAYS * MS_PER_DAY
            context.dataStore.edit {
                it[KEY_TRIAL_START_MS] = now
                it[KEY_TRIAL_END_MS]   = end
            }
            return end
        }
        return trialEndMs.first().let {
            // Migrate: if trialEndMs not set yet but trialStart is, compute it
            if (it == 0L) {
                val end = start + TRIAL_DAYS * MS_PER_DAY
                context.dataStore.edit { prefs -> prefs[KEY_TRIAL_END_MS] = end }
                end
            } else it
        }
    }

    /** Days remaining, rounded up. Returns 0 when expired. */
    fun remainingTrialDays(trialEndMillis: Long, now: Long = System.currentTimeMillis()): Int {
        if (trialEndMillis == 0L) return 0
        val diff = trialEndMillis - now
        if (diff <= 0) return 0
        return ((diff + MS_PER_DAY - 1) / MS_PER_DAY).toInt()
    }

    fun isTrialValid(trialEndMillis: Long, now: Long = System.currentTimeMillis()) =
        trialEndMillis > 0L && now <= trialEndMillis

    // ── DEBUG (only called with ENABLE_DEV_TOOLS flag) ───────────────────────
    suspend fun debugActivate(now: Long = System.currentTimeMillis()) {
        context.dataStore.edit {
            it[KEY_IS_ACTIVATED]    = true
            it[KEY_ACTIVE_UNTIL_MS] = now + 30 * MS_PER_DAY
        }
    }

    suspend fun clearAll() = context.dataStore.edit { it.clear() }

    suspend fun getSnapshot(): Map<Preferences.Key<*>, Any> {
        var prefs: Preferences? = null
        context.dataStore.data.collect { prefs = it }
        return prefs?.asMap() ?: emptyMap()
    }
}
