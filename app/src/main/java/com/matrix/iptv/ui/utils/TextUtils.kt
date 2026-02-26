package com.matrix.iptv.ui.utils

import androidx.compose.ui.text.style.TextDirection

object TextUtils {
    /**
     * Checks if a string contains Arabic characters.
     */
    fun isArabic(text: String): Boolean {
        return text.any { it in '\u0600'..'\u06FF' || it in '\u0750'..'\u077F' || it in '\u08A0'..'\u08FF' || it in '\uFB50'..'\uFDFF' || it in '\uFE70'..'\uFEFF' }
    }

    /**
     * Returns the appropriate TextDirection based on the presence of Arabic characters.
     */
    fun getTextDirection(text: String): TextDirection {
        return if (isArabic(text)) TextDirection.Rtl else TextDirection.Ltr
    }
}
