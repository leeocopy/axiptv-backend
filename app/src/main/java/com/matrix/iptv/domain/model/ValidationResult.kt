package com.matrix.iptv.domain.model

sealed class ValidationError {
    object InvalidUrl : ValidationError()
    object NoInternet : ValidationError()
    object Unreachable : ValidationError()
    object InvalidCredentials : ValidationError()
    object Unknown : ValidationError()

    fun toUserMessage(): String = when (this) {
        InvalidUrl -> "Invalid URL"
        NoInternet -> "No internet connection"
        Unreachable -> "Server unreachable"
        InvalidCredentials -> "Wrong username or password"
        Unknown -> "An unexpected error occurred"
    }
}

data class ValidationResult(
    val ok: Boolean,
    val error: ValidationError? = null
)
