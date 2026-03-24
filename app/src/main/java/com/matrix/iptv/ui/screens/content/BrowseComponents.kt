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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.matrix.iptv.ui.screens.home.DottedBackground
import com.matrix.iptv.ui.theme.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MatrixBrowseLayout(
    backdropUrl: String? = null,
    left: @Composable ColumnScope.() -> Unit,
    center: @Composable ColumnScope.() -> Unit,
    right: @Composable ColumnScope.() -> Unit
) {
    val mx = MaterialTheme.matrixColors
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF141414), // Netflix dark top
                        Color(0xFF0C111D), // Deep blue-black mid
                        Color(0xFF000000)  // Deep black bottom
                    )
                )
            )
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 1. Sidebar (Fixed width)
            Column(
                modifier = Modifier
                    .width(280.dp)
                    .fillMaxHeight()
                    .background(mx.bgPrimary.copy(alpha = 0.4f))
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
                    .background(mx.bgPrimary.copy(alpha = 0.2f))
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
                        .background(mx.accentPrimary.copy(alpha = 0.1f))
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
                modifier = Modifier.clip(CircleShape).background(if (isSearchMode) mx.accentPink else mx.accentPrimary.copy(alpha = 0.05f))
            ) {
                Icon(if (isSearchMode) Icons.Default.Close else Icons.Default.Search, null, tint = mx.accentPrimary)
            }
            
            if (isSearchMode) {
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        onSearchQueryChanged(it)
                    },
                    placeholder = { Text("Search...", color = mx.accentPrimary.copy(alpha = 0.5f)) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = mx.accentPrimary.copy(alpha = 0.1f),
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = mx.accentPrimary,
                        unfocusedIndicatorColor = mx.accentPrimary.copy(alpha = 0.2f),
                        cursorColor = mx.accentPrimary,
                        focusedTextColor = mx.accentPrimary,
                        unfocusedTextColor = mx.accentPrimary
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                var isFocused by remember { mutableStateOf(false) }
                
                Surface(
                    onClick = { onCategorySelected(category) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { isFocused = it.isFocused },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isFocused) mx.accentHover else if (isSelected) mx.accentHover.copy(alpha = 0.2f) else Color.Transparent,
                    tonalElevation = if (isFocused) 4.dp else 0.dp
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        val isArabic = com.matrix.iptv.ui.utils.TextUtils.isArabic(category)
                        CompositionLocalProvider(
                            androidx.compose.ui.platform.LocalLayoutDirection provides (if (isArabic) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    textDirection = if (isArabic) androidx.compose.ui.text.style.TextDirection.Rtl else androidx.compose.ui.text.style.TextDirection.Ltr
                                ),
                                color = if (isFocused) mx.bgPrimary else if (isSelected) mx.accentHover else mx.accentPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
        
        // Settings Icon
        IconButton(
            onClick = {}, 
            modifier = Modifier.padding(top = 16.dp).clip(CircleShape).background(mx.accentPrimary.copy(alpha = 0.05f))
        ) {
            Icon(Icons.Default.Settings, null, tint = mx.accentPrimary)
        }
    }
}

@Composable
fun ContentCardWide(
    title: String,
    subtitle: String, // Treat subtitle as Channel ID or Number now
    imageUrl: String?,
    badges: List<String> = emptyList(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val mx = MaterialTheme.matrixColors

    Box(
        modifier = modifier
            .padding(vertical = 2.dp)
            .fillMaxWidth()
            .height(56.dp)
            .background(if (isFocused) mx.accentHover else mx.bgElevated, RoundedCornerShape(6.dp))
            .border(
                width = if (isFocused) 2.dp else 0.dp,
                color = if (isFocused) mx.accentPrimary else Color.Transparent,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            // Icon
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(4.dp),
                color = mx.bgPrimary.copy(alpha = 0.5f)
            ) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    contentScale = ContentScale.Fit
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                val isArabicTitle = com.matrix.iptv.ui.utils.TextUtils.isArabic(title)
                CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalLayoutDirection provides (if (isArabicTitle) androidx.compose.ui.unit.LayoutDirection.Rtl else androidx.compose.ui.unit.LayoutDirection.Ltr)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textDirection = if (isArabicTitle) androidx.compose.ui.text.style.TextDirection.Rtl else androidx.compose.ui.text.style.TextDirection.Ltr
                        ),
                        color = if (isFocused) mx.bgPrimary else mx.accentPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(Modifier.width(8.dp))
            
            // Channel Number Box (Right aligned)
            val channelNum = subtitle.replace(Regex("[^0-9]"), "").takeIf { it.isNotEmpty() } ?: "0"
            Box(
                modifier = Modifier
                    .background(mx.accentPink, RoundedCornerShape(4.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = channelNum,
                    color = mx.accentPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
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
    val currentTime = com.matrix.iptv.ui.components.LocalClock.current
    
    val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, MMM dd")

    Column(modifier = modifier.fillMaxSize()) {
        // Date & Time
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currentTime.format(timeFormatter),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = mx.accentPrimary
                )
                Text(
                    text = currentTime.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = mx.accentPrimary.copy(alpha = 0.6f)
                )
            }
        }
        
        Spacer(Modifier.weight(0.1f))
        
        // Large Cover if exists (Faded background would be nice, but let's keep it in panel for now)
        if (coverUrl != null) {
            Surface(
                modifier = Modifier.fillMaxWidth().aspectRatio(16f/9f),
                shape = RoundedCornerShape(16.dp),
                color = mx.bgPrimary.copy(alpha = 0.3f)
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
            color = Color(0xFF89E900),
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
        
        // EPG Block (HotPlayers style)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(mx.accentHover, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, null, tint = mx.accentPink, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Live Broadcast", 
                        color = mx.bgPrimary, 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = mx.bgPrimary.copy(alpha = 0.8f),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(16.dp))
                
                // Progress line
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text(currentTime.minusMinutes(30).format(timeFormatter), color = Color(0xFF222222).copy(alpha=0.6f), fontSize=12.sp)
                    Spacer(Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f).height(4.dp).background(mx.bgPrimary.copy(alpha=0.2f), CircleShape)) {
                        Box(modifier = Modifier.fillMaxWidth(0.6f).fillMaxHeight().background(mx.error, CircleShape))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(currentTime.plusMinutes(90).format(timeFormatter), color = Color(0xFF222222).copy(alpha=0.6f), fontSize=12.sp)
                }
            }
        }
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
    val currentTime = com.matrix.iptv.ui.components.LocalClock.current
    
    val timeFormatter = java.time.format.DateTimeFormatter.ofPattern("hh:mm a")
    val dateFormatter = java.time.format.DateTimeFormatter.ofPattern("EEE, MMM dd")
    
    val isArabic = com.matrix.iptv.ui.utils.TextUtils.isArabic(description)

    Box(modifier = modifier.fillMaxSize().clipToBounds()) {
        // Disabled massive image rendering on the right as it is now in full screen background
        // Scrim overlay for readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, mx.bgPrimary.copy(alpha = 0.6f))
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
                        color = mx.accentPrimary
                    )
                    Text(
                        text = currentTime.format(dateFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = mx.accentPrimary.copy(alpha = 0.6f)
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
                color = mx.bgPrimary.copy(alpha = 0.5f),
                tonalElevation = 8.dp
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
                color = Color(0xFF89E900),
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
                        color = mx.accentPrimary.copy(alpha = 0.85f),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// HotPlayers-Style Components (used by LiveBrowseScreen)
// ═══════════════════════════════════════════════════════════════════

private val HpPink   = AccentPink
private val HpPurple = AccentPurple

/** Full-width top bar: time | icon + title | date */
@Composable
fun HotTopBar(screenTitle: String) {
    val mx = MaterialTheme.matrixColors
    val currentTime = com.matrix.iptv.ui.components.LocalClock.current
    val timeStr = remember(currentTime) { currentTime.format(DateTimeFormatter.ofPattern("hh:mm a")) }
    val dateStr = remember(currentTime) { currentTime.format(DateTimeFormatter.ofPattern("dd MMM, yyyy | EEEE")) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(color = mx.bgSurface)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(timeStr, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(brush = Brush.linearGradient(listOf(HpPurple, HpPink))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Favorite, null, tint = Color.White, modifier = Modifier.size(13.dp))
            }
            Spacer(Modifier.width(8.dp))
            Text(screenTitle, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Text(dateStr, color = Color.White.copy(alpha = 0.60f), fontSize = 13.sp)
    }
}

/** Gradient header with ◄ category name ► arrows */
@Composable
fun HotCategoryHeader(
    categoryName: String,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(Brush.horizontalGradient(listOf(HpPurple, HpPink)))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
                    .clickable { onPrev() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ChevronLeft, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Text(
                text       = categoryName,
                color      = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize   = 15.sp,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
                textAlign  = TextAlign.Center,
                modifier   = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
                    .clickable { onNext() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.ChevronRight, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }
}

/** Compact channel row: [0001] | [▌] Channel Name */
@Composable
fun HotChannelRow(
    number: String,
    name: String,
    isSelected: Boolean,
    onFocused: () -> Unit,
    onClick: () -> Unit,
    iconUrl: String? = null,
    isArabic: Boolean = false,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }
    val mx = MaterialTheme.matrixColors

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                when {
                    isFocused  -> mx.textPrimary.copy(alpha = 0.15f)
                    isSelected -> mx.textPrimary.copy(alpha = 0.1f)
                    else       -> Color.Transparent
                }
            )
            .clickable { onClick() }
            .focusable()
            .onFocusChanged { 
                isFocused = it.isFocused
                if (it.isFocused) onFocused()
            }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Channel Number
        Text(
            text       = number,
            color      = if (isFocused || isSelected) mx.textPrimary else mx.textPrimary.copy(alpha = 0.4f),
            fontSize   = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier   = Modifier.width(44.dp)
        )

        // Indicator/Logo
        if (iconUrl != null) {
            AsyncImage(
                model = iconUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(0.2f)),
                contentScale = ContentScale.Fit
            )
            Spacer(Modifier.width(12.dp))
        } else if (isSelected) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(mx.textPrimary)
            )
            Spacer(Modifier.width(12.dp))
        } else {
            Spacer(Modifier.width(16.dp))
        }

        // Channel Name
        Text(
            text       = name,
            color      = if (isFocused || isSelected) mx.textPrimary else mx.textPrimary.copy(alpha = 0.8f),
            fontSize   = 15.sp,
            fontWeight = if (isFocused || isSelected) FontWeight.Bold else FontWeight.Normal,
            maxLines   = 1,
            overflow   = TextOverflow.Ellipsis,
            modifier   = Modifier.weight(1f),
            textAlign  = if (isArabic) androidx.compose.ui.text.style.TextAlign.Right else androidx.compose.ui.text.style.TextAlign.Left,
            style      = androidx.compose.ui.text.TextStyle(
                textDirection = if (isArabic) androidx.compose.ui.text.style.TextDirection.Rtl else androidx.compose.ui.text.style.TextDirection.Ltr
            )
        )
    }
}

/** Info bar below video: [Name + EPG] [○ icon] [num] [logo] */
@Composable
fun HotChannelInfoBar(
    channelName: String,
    channelNumber: Int,
    logoUrl: String?,
    epgText: String,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .clickable { onSelect() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text       = channelName,
                color      = HpPink,
                fontWeight = FontWeight.Bold,
                fontSize   = 17.sp,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Text(epgText, color = Color.White.copy(alpha = 0.40f), fontSize = 12.sp)
        }

        Spacer(Modifier.width(12.dp))

        // Circular channel icon
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(HpPurple, Color(0xFFAB47BC)))),
            contentAlignment = Alignment.Center
        ) {
            if (logoUrl != null) {
                AsyncImage(
                    model = logoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(34.dp).clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(Icons.Default.Tv, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
        }

        Spacer(Modifier.width(14.dp))

        // Channel number
        Text(
            text       = channelNumber.toString(),
            color      = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize   = 22.sp,
            textAlign  = TextAlign.Center,
            modifier   = Modifier.width(44.dp)
        )

        Spacer(Modifier.width(14.dp))

        // Logo box (right)
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (logoUrl != null) {
                AsyncImage(
                    model = logoUrl,
                    contentDescription = null,
                    modifier = Modifier.size(54.dp).padding(4.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Icon(Icons.Default.Tv, null, tint = Color.White.copy(alpha = 0.25f), modifier = Modifier.size(28.dp))
            }
        }
    }
}

/** Bottom navigation hints bar */
@Composable
fun HotBottomHints() {
    val mx = MaterialTheme.matrixColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(color = mx.bgSurface)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HotHintChip("▲▼", "Selector")
        HotHintChip("◄►", "Category")
        HotHintChip("OK",  "Select")
        HotHintChip("≡",   "Menu")
        HotHintChip("↩",   "Exit")
    }
}

@Composable
private fun HotHintChip(symbol: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(symbol, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
        Text(label, color = Color.White.copy(alpha = 0.55f), fontSize = 11.sp)
    }
}

/**
 * Simple category row for the LEFT categories panel.
 * Shows only the category name; selected item is highlighted in pink.
 */
@Composable
fun HotCategoryRow(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isFocused by remember { mutableStateOf(false) }

    val bgColor = when {
        isFocused  -> Color.White.copy(alpha = 0.10f)
        isSelected -> HpPink.copy(alpha = 0.09f)
        else       -> Color.Transparent
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(bgColor)
            .clickable { onClick() }
            .focusable()
            .onFocusChanged { isFocused = it.isFocused }
            .padding(start = 10.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pink left indicator bar
        if (isSelected) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(HpPink)
            )
            Spacer(Modifier.width(8.dp))
        } else {
            Spacer(Modifier.width(11.dp))
        }

        val isArabic = com.matrix.iptv.ui.utils.TextUtils.isArabic(name)
        CompositionLocalProvider(
            androidx.compose.ui.platform.LocalLayoutDirection provides
                (if (isArabic) androidx.compose.ui.unit.LayoutDirection.Rtl
                 else          androidx.compose.ui.unit.LayoutDirection.Ltr)
        ) {
            Text(
                text       = name,
                color      = if (isSelected) HpPink else Color.White.copy(alpha = 0.80f),
                fontSize   = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                maxLines   = 1,
                overflow   = TextOverflow.Ellipsis,
                modifier   = Modifier.weight(1f)
            )
        }
    }
}
