package com.matrix.iptv.data.local.prefs

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePrefs @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "matrix_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private fun passwordKey(profileId: String) = "pass_$profileId"
    private val parentalPinKey = "parental_pin"

    fun savePassword(profileId: String, password: String) {
        prefs.edit().putString(passwordKey(profileId), password).apply()
    }

    fun getPassword(profileId: String): String =
        prefs.getString(passwordKey(profileId), "") ?: ""

    fun deletePassword(profileId: String) {
        prefs.edit().remove(passwordKey(profileId)).apply()
    }

    fun saveParentalPin(pin: String) {
        prefs.edit().putString(parentalPinKey, pin).apply()
    }

    fun getParentalPin(): String? = prefs.getString(parentalPinKey, null)

    fun clearAll() { prefs.edit().clear().apply() }
}
