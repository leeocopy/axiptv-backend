package com.matrix.iptv.data.validation

import com.matrix.iptv.domain.model.ValidationError
import com.matrix.iptv.domain.model.ValidationResult
import com.matrix.iptv.domain.validation.XtreamValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class XtreamValidatorImpl @Inject constructor() : XtreamValidator {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun validate(baseUrl: String, user: String, pass: String): ValidationResult = withContext(Dispatchers.IO) {
        val normalizedUrl = normalizeUrl(baseUrl)
        if (normalizedUrl == null) {
            return@withContext ValidationResult(false, ValidationError.InvalidUrl)
        }

        val url = "$normalizedUrl/player_api.php?username=$user&password=$pass"

        val request = Request.Builder()
            .url(url)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext ValidationResult(false, ValidationError.Unreachable)
            }

            val body = response.body?.string() ?: return@withContext ValidationResult(false, ValidationError.Unknown)
            
            try {
                val jsonElement = json.parseToJsonElement(body)
                val isOk = jsonElement.jsonObject.containsKey("user_info")
                
                if (isOk) {
                    ValidationResult(true)
                } else {
                    ValidationResult(false, ValidationError.InvalidCredentials)
                }
            } catch (e: Exception) {
                // If it's valid JSON but no user_info, or not JSON at all
                ValidationResult(false, ValidationError.InvalidCredentials)
            }

        } catch (e: IOException) {
            ValidationResult(false, ValidationError.NoInternet)
        } catch (e: Exception) {
            ValidationResult(false, ValidationError.Unknown)
        }
    }

    private fun normalizeUrl(url: String): String? {
        var trimmed = url.trim().removeSuffix("/")
        if (trimmed.isBlank()) return null
        
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            trimmed = "http://$trimmed"
        }
        
        return try {
            URL(trimmed).toString().removeSuffix("/")
        } catch (e: Exception) {
            null
        }
    }
}
