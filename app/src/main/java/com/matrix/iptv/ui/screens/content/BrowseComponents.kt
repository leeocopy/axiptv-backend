package com.matrix.iptv.ui.screens.content

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.matrix.iptv.ui.screens.home.DottedBackground
import com.matrix.iptv.ui.theme.FocusGlow
import com.matrix.iptv.ui.theme.matrixColors
import com.matrix.iptv.ui.theme.TextPrimary
import com.matrix.iptv.ui.theme.TextSecondary
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun MatrixBrowseLayout(
    left: @Composable ColumnScope.() -> Unit,
    center: @Composable ColumnScope.() -> Unit,
    right: @Composable ColumnScope.() -> Unit
) {
    val mx = MaterialTheme.matrixColors
    Box(modifier = Modifier.fillMaxSize().background(mx.bgPrimary)) {
        DottedBackground()
        
        Row(modifier = Modifier.fillMaxSize()) {
            // 1. Sidebar (Fixed width)
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                content = left
            )
            
            // 2. Content List (Main area)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                content = center
            )
            
            // 3. Details Panel (Fixed width, clipped)
            Box(
                modifier = Modifier
                    .width(360.dp)
                    .fillMaxHeight()
                    .clipToBounds()
                    .background(Color.Black.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    content = right
                )
                
                // Vertical Divider
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                        .align(Alignment.CenterStart)
                )
            }
        }
    }
}

@Composable
fun CategoriesSidebar(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit = {},
    isSearchMode: Boolean = false,
    onSearchToggle: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val mx = MaterialTheme.matrixColors
    var searchQuery by remember { mutableStateOf("") }
    
    Column(modifier = modifier.fillMaxHeight()) {
        // Search Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            IconButton(
                onClick = { onSearchToggle(!isSearchMode) }, 
                modifier = Modifier.clip(CircleShape).background(if (isSearchMode) mx.accentPink else Color.White.copy(alpha = 0.05f))
            ) {
                Icon(if (isSearchMode) Icons.Default.Close else Icons.Default.Search, null, tint = Color.White)
            }
            
            if (isSearchMode) {
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQueryChanged(it)
                    },
                    placeholder = { Text("Search...", color = Color.White.copy(alpha = 0.5f)) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = mx.accentPink,
                        unfocusedIndicatorColor = Color.White.copy(alpha = 0.2f),
                        cursorColor = mx.accentPink,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                var isFocused by remember { mutableStateOf(false) }
                
                Surface(
                    onClick = { onCategorySelected(category) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isFocused) mx.accentPink else if (isSelected) mx.accentPink.copy(alpha = 0.2f) else Color.Transparent,
                    tonalElevation = if (isFocused) 8.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontWeight = if (isSelected || isFocused) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isFocused) Color.White else if (isSelected) mx.accentPink else Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isSelected && !isFocused) {
                            Spacer(Modifier.weight(1f))
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(mx.accentPink))
                        }
                    }
                }
            }
        }
        
        // Settings Icon
        IconButton(
            onClick = {}, 
            modifier = Modifier.padding(top = 16.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f))
        ) {
            Icon(Icons.Default.Settings, null, tint = Color.White)
        }
    }
}

@Composable
fun ContentCardWide(
    title: String,
    subtitle: String,
    imageUrl: String?,
    badges: List<String> = emptyList(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.03f else 1.0f)
    val mx = MaterialTheme.matrixColors

    Box(
        modifier = modifier
            .padding(vertical = 4.dp)
            .scale(scale)
            .fillMaxWidth()
            .height(100.dp)
            .shadow(if (isFocused) 12.dp else 0.dp, RoundedCornerShape(16.dp))
            .background(if (isFocused) Color(0xFF2C2C4E) else mx.bgSurface, RoundedCornerShape(16.dp))
            .border(
                width = if (isFocused) 2.dp else 0.5.dp,
                color = if (isFocused) FocusGlow else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() }
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(12.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Icon
            Surface(
                modifier = Modifier.size(76.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isFocused) Color.White else TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isFocused) Color.White.copy(alpha = 0.8f) else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (badges.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        badges.forEach { badge ->
                            Surface(
                                color = Color.White.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = badge,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            if (isFocused) {
                Icon(
                    Icons.Default.PlayArrow, 
                    null, 
                    tint = mx.accentPink,
                    modifier = Modifier.size(32.dp).padding(end = 8.dp)
                )
            }
        }
    }
}

@Composable
fun DetailsPanel(
    title: String,
    description: String,
    metadata: String = "",
    coverUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val mx = MaterialTheme.matrixColors
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            kotlinx.coroutines.delay(1000)
        }
    }

    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM dd")

    Column(modifier = modifier.fillMaxSize()) {
        // Date & Time
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currentTime.format(timeFormatter),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = currentTime.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(Modifier.weight(0.1f))
        
        // Large Cover if exists (Faded background would be nice, but let's keep it in panel for now)
        if (coverUrl != null) {
            Surface(
                modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.height(24.dp))
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f/9f)
                    .background(
                        Brush.linearGradient(listOf(mx.bgSurface, mx.bgElevated)),
                        RoundedCornerShape(16.dp)
                    )
            )
            Spacer(Modifier.height(24.dp))
        }

        Text(
            text = "NOW PLAYING",
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
            color = mx.accentPink
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        
        if (metadata.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = metadata,
                style = MaterialTheme.typography.bodyMedium,
                color = mx.accentOrange
            )
        }
        
        Spacer(Modifier.height(16.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 8,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MediaDetailsPanel(
    title: String,
    description: String,
    metadata: String = "",
    posterUrl: String? = null,
    backdropUrl: String? = null,
    modifier: Modifier = Modifier
) {
    val mx = MaterialTheme.matrixColors
    var currentTime by remember { mutableStateOf(java.time.LocalDateTime.now()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = java.time.LocalDateTime.now()
            kotlinx.coroutines.delay(1000)
        }
    }

    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
    val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("EEE, MMM dd")
    
    val isArabic = com.matrix.iptv.ui.utils.TextUtils.isArabic(description)

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        // 1. Backdrop (Blurred background) - Caching scale to avoid bleed
        if (backdropUrl != null || posterUrl != null) {
            AsyncImage(
                model = backdropUrl ?: posterUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().scale(1.2f),
                contentScale = ContentScale.Crop,
                alpha = 0.25f
            )
        }

        // Dark Gradient Scrim overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            // 2. Date & Time
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = currentTime.format(timeFormatter),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = currentTime.format(dateFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // 3. Large Poster (Fully visible)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .padding(horizontal = 40.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color.Black.copy(alpha = 0.5f),
                tonalElevation = 8.dp,
                shadowElevation = 12.dp
            ) {
                AsyncImage(
                    model = posterUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(24.dp))

            // 4. Info Section
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                softWrap = false
            )
            
            if (metadata.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = metadata,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        color = mx.accentPink
                    )
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // 5. Scrollable Description (RTL support)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalLayoutDirection provides (if (isArabic) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr)
                ) {
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            lineHeight = 26.sp,
                            textAlign = if (isArabic) androidx.compose.ui.text.style.TextAlign.Right else androidx.compose.ui.text.style.TextAlign.Left,
                            textDirection = if (isArabic) androidx.compose.ui.text.style.TextDirection.Rtl else androidx.compose.ui.text.style.TextDirection.Ltr
                        ),
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
