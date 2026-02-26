package com.matrix.iptv.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.matrix.iptv.ui.components.PrimaryButton
import com.matrix.iptv.ui.components.SecondaryButton
import com.matrix.iptv.ui.components.TvInput
import com.matrix.iptv.ui.components.TopBar
import com.matrix.iptv.ui.theme.matrixColors
import kotlinx.coroutines.delay

/**
 * Add / Edit Profile — skill_2.md §6.3
 * Enhanced TV focus navigation + mouse/keyboard support + server validation feedback
 */
@Composable
fun ProfileAddEditScreen(
    profileId: String?,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: ProfileAddEditViewModel = hiltViewModel()
) {
    val form by viewModel.form.collectAsState()
    val mx   = MaterialTheme.matrixColors
    val keyboardController = LocalSoftwareKeyboardController.current

    // ── Focus Chain ─────────────────────────────────────────────────────────
    val nameFr      = remember { FocusRequester() }
    val hostFr      = remember { FocusRequester() }
    val userFr      = remember { FocusRequester() }
    val passFr      = remember { FocusRequester() }
    val cancelFr    = remember { FocusRequester() }
    val saveFr      = remember { FocusRequester() }

    // Initial Focus
    LaunchedEffect(Unit) {
        delay(300)
        if (form.name.isNotEmpty()) {
            hostFr.requestFocus() // Focus host if editing existing
        } else {
            nameFr.requestFocus() // New profile
        }
    }

    // Snackbar Host
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(form.snackbarMessage) {
        form.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSnackbar()
            if (form.isSuccess && form.isSaved) {
                delay(800)
                onSaved()
            }
        }
    }

    Scaffold(
        containerColor = mx.bgPrimary,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = if (form.isSuccess) mx.success else mx.error,
                    contentColor = mx.textPrimary,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        topBar = {
            TopBar(
                title = if (profileId == null) "Add Profile" else "Edit Profile",
                rightContent = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = mx.textSecondary)
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 560.dp)
                    .fillMaxWidth(0.6f),
                colors = CardDefaults.cardColors(containerColor = mx.bgSurface),
                shape = MaterialTheme.shapes.large
            ) {
                Column(
                    modifier = Modifier.padding(40.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        if (profileId == null) "Server Details" else "Update Details",
                        style = MaterialTheme.typography.headlineSmall,
                        color = mx.textSecondary
                    )

                    TvInput(
                        value = form.name,
                        onValueChange = viewModel::onNameChange,
                        hint = "Profile Name (Personal Reference)",
                        errorMessage = form.nameError,
                        imeAction = ImeAction.Next,
                        enabled = !form.isSaving,
                        onImeAction = { hostFr.requestFocus() },
                        focusRequester = nameFr
                    )

                    TvInput(
                        value = form.host,
                        onValueChange = viewModel::onHostChange,
                        hint = "Server Host (e.g. http://host:port)",
                        errorMessage = form.hostError,
                        imeAction = ImeAction.Next,
                        enabled = !form.isSaving,
                        onImeAction = { userFr.requestFocus() },
                        focusRequester = hostFr
                    )

                    TvInput(
                        value = form.username,
                        onValueChange = viewModel::onUsernameChange,
                        hint = "Username",
                        errorMessage = form.usernameError,
                        imeAction = ImeAction.Next,
                        enabled = !form.isSaving,
                        onImeAction = { passFr.requestFocus() },
                        focusRequester = userFr
                    )

                    TvInput(
                        value = form.password,
                        onValueChange = viewModel::onPasswordChange,
                        hint = "Password",
                        isPassword = true,
                        errorMessage = form.passwordError,
                        imeAction = ImeAction.Done,
                        enabled = !form.isSaving,
                        onImeAction = { 
                            keyboardController?.hide()
                            saveFr.requestFocus()
                            viewModel.save() 
                        },
                        focusRequester = passFr
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SecondaryButton(
                            text = "Cancel",
                            onClick = onBack,
                            enabled = !form.isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(cancelFr)
                        )
                        PrimaryButton(
                            text = "Save & Connect",
                            onClick = { 
                                keyboardController?.hide()
                                viewModel.save() 
                            },
                                enabled = !form.isSaving,
                            loading = form.isSaving,
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(saveFr)
                        )
                    }
                }
            }
        }
    }
}
