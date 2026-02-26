package com.matrix.iptv.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matrix.iptv.domain.model.Profile
import com.matrix.iptv.ui.components.FocusableCard
import com.matrix.iptv.ui.components.TopBar
import com.matrix.iptv.ui.theme.AccentOrange
import com.matrix.iptv.ui.theme.AccentPink
import com.matrix.iptv.ui.theme.AccentPurple
import com.matrix.iptv.ui.theme.matrixColors
import java.text.SimpleDateFormat
import java.util.*

/**
 * Profile Picker — skill_2.md §6.3
 * Grid of profile cards, empty state, Add button, long-press Edit/Delete
 */
@Composable
fun ProfilePickerScreen(
    onProfileSelected: (Profile) -> Unit,
    onAddProfile: () -> Unit,
    onEditProfile: (String) -> Unit,
    viewModel: ProfilePickerViewModel = hiltViewModel()
) {
    val profiles by viewModel.profiles.collectAsState()
    val mx = MaterialTheme.matrixColors
    var profileToDelete by remember { mutableStateOf<Profile?>(null) }

    // Delete confirmation dialog
    profileToDelete?.let { p ->
        AlertDialog(
            onDismissRequest = { profileToDelete = null },
            containerColor = mx.bgElevated,
            title = { Text("Delete ${p.name.ifEmpty { p.host }}", color = mx.textPrimary) },
            text = { Text("This cannot be undone.", color = mx.textSecondary) },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteProfile(p.id); profileToDelete = null }) {
                    Text("Delete", color = mx.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { profileToDelete = null }) {
                    Text("Cancel", color = mx.textSecondary)
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(mx.bgPrimary)) {
        Column {
            TopBar(title = "Select Profile")
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 48.dp, vertical = 27.dp)) {
                if (profiles.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PersonAdd, null, tint = mx.textMuted, modifier = Modifier.size(64.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No profiles yet", style = MaterialTheme.typography.titleLarge, color = mx.textMuted)
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = onAddProfile,
                            colors = ButtonDefaults.buttonColors(containerColor = mx.accentPink),
                            shape = RoundedCornerShape(50)
                        ) {
                            Icon(Icons.Default.Add, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Add Profile")
                        }
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(4),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(profiles) { profile ->
                            ProfileCard(
                                profile = profile,
                                onClick = { viewModel.selectProfile(profile, { onProfileSelected(profile) }) },
                                onEdit  = { onEditProfile(profile.id) },
                                onDelete = { profileToDelete = profile }
                            )
                        }
                        // Add new profile card
                        item {
                            FocusableCard(
                                onClick = onAddProfile,
                                modifier = Modifier.aspectRatio(1f)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize().padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = mx.accentPink,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text("Add Profile", style = MaterialTheme.typography.bodyLarge, color = mx.accentPink)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(
    profile: Profile,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val mx = MaterialTheme.matrixColors
    var showMenu by remember { mutableStateOf(false) }
    val initials = profile.name.take(2).uppercase().ifEmpty {
        profile.host.removePrefix("http://").removePrefix("https://").take(2).uppercase()
    }

    FocusableCard(
        onClick = onClick,
        modifier = Modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Brush.horizontalGradient(listOf(AccentPurple, AccentPink)),
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, style = MaterialTheme.typography.titleLarge, color = mx.textPrimary)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                profile.name.ifEmpty { profile.host },
                style = MaterialTheme.typography.bodyLarge,
                color = mx.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                profile.username,
                style = MaterialTheme.typography.bodyMedium,
                color = mx.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (profile.lastUsed > 0L) {
                Text(
                    "Last: ${SimpleDateFormat("dd/MM", Locale.getDefault()).format(Date(profile.lastUsed))}",
                    style = MaterialTheme.typography.labelSmall,
                    color = mx.textMuted
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = mx.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = mx.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
