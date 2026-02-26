package com.matrix.iptv.domain.validation

import com.matrix.iptv.domain.model.ValidationResult

interface XtreamValidator {
    suspend fun validate(baseUrl: String, user: String, pass: String): ValidationResult
}
