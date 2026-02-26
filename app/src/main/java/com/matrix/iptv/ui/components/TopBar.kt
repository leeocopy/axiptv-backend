package com.matrix.iptv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.matrix.iptv.ui.theme.matrixColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * TopBar — skill_2.md §1 Layout
 * 64dp height, time(left) · icon+title(center) · date(right)
 */
@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    rightContent: @Composable RowScope.() -> Unit = {}
) {
    val mx = MaterialTheme.matrixColors
    val now = remember { Date() }
    val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
    val date = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(now)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(mx.bgPrimary)
            .padding(horizontal = 48.dp),  // TV safe margin
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left — clock
        Text(
            text = time,
            style = MaterialTheme.typography.bodyMedium,
            color = mx.textSecondary,
            modifier = Modifier.weight(1f)
        )
        // Center — title
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = mx.textPrimary,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(2f)
        )
        // Right — date + custom slot
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            rightContent()
            Spacer(Modifier.width(8.dp))
            Text(
                text = date,
                style = MaterialTheme.typography.bodyMedium,
                color = mx.textSecondary
            )
        }
    }
}
