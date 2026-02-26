package com.matrix.iptv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.matrix.iptv.ui.theme.matrixColors

private val InputShape = RoundedCornerShape(50)

/**
 * TV-optimised text input — skill_2.md §1.9
 * White bg pill, pink border on focus, eye-toggle for password
 */
@Composable
fun TvInput(
    value: String,
    onValueChange: (String) -> Unit,
    hint: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    errorMessage: String? = null,
    imeAction: ImeAction = ImeAction.Next,
    enabled: Boolean = true,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onImeAction: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    val mx = MaterialTheme.matrixColors

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.Color.White, InputShape)
                .border(
                    width = if (isFocused) 2.dp else 0.dp,
                    color = if (isFocused) mx.accentPink else androidx.compose.ui.graphics.Color.Transparent,
                    shape = InputShape
                )
                .padding(horizontal = 20.dp, vertical = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                visualTransformation = if (isPassword && !showPassword)
                    PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
                    imeAction = imeAction
                ),
                keyboardActions = KeyboardActions(onAny = { onImeAction() }),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = mx.bgPrimary
                ),
                cursorBrush = SolidColor(mx.accentPink),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { isFocused = it.isFocused }
                    .padding(end = if (isPassword) 36.dp else 0.dp),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(hint, style = MaterialTheme.typography.bodyLarge, color = mx.textMuted)
                    }
                    inner()
                }
            )
            if (isPassword) {
                IconButton(
                    onClick = { showPassword = !showPassword },
                    modifier = Modifier.align(Alignment.CenterEnd).size(28.dp)
                ) {
                    Icon(
                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (showPassword) "Hide" else "Show",
                        tint = mx.textMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.labelSmall,
                color = mx.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
