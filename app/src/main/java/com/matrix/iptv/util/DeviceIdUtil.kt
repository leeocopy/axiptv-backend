package com.matrix.iptv.util

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.provider.Settings
import com.matrix.iptv.BuildConfig
import java.security.MessageDigest

/**
 * Device-ID utility — skill_2.md §6.2
 * deviceIdFull  = SHA-256( ANDROID_ID + TRIAL_SALT ) — 64 hex chars
 * deviceIdShort = first 16 chars of deviceIdFull, lowercase
 *
 * No MAC address is used (not accessible on modern Android).
 */
object DeviceIdUtil {

    @SuppressLint("HardwareIds")
    fun computeFull(context: Context): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        ) ?: "unknown"
        val input  = androidId + BuildConfig.TRIAL_SALT
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    fun short(full: String): String = full.take(16).lowercase()

    /** Copy [text] to clipboard. Returns true on success. */
    fun copyToClipboard(context: Context, label: String, text: String): Boolean {
        return try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText(label, text))
            true
        } catch (e: Exception) { false }
    }

    /** Activation URL for QR code and "Open link" button. */
    fun activationUrl(deviceIdShort: String) =
        "https://axiptv-backend.vercel.app/activate?d=$deviceIdShort"
}
