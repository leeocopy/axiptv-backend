package com.matrix.iptv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*

val LocalMatrixColors = staticCompositionLocalOf { MatrixColors() }

/** Access custom Matrix color tokens via MaterialTheme.matrixColors */
val MaterialTheme.matrixColors: MatrixColors
    @Composable get() = LocalMatrixColors.current

@Composable
fun MatrixIptvTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalMatrixColors provides MatrixColors()) {
        MaterialTheme(
            colorScheme = matrixDarkColorScheme,
            typography  = MatrixTypography,
            shapes      = MatrixShapes,
            content     = content
        )
    }
}
