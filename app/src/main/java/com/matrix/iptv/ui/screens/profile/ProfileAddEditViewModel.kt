package com.matrix.iptv.ui.screens.profile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.matrix.iptv.domain.model.Profile
import com.matrix.iptv.domain.model.ValidationError
import com.matrix.iptv.domain.repository.ProfileRepository
import com.matrix.iptv.domain.validation.XtreamValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class ProfileFormState(
    val name:          String = "",
    val host:          String = "",
    val username:      String = "",
    val password:      String = "",
    val nameError:     String? = null,
    val hostError:     String? = null,
    val usernameError: String? = null,
    val passwordError: String? = null,
    val globalError:   String? = null,
    val isSaving:      Boolean = false,
    val isSaved:       Boolean = false,
    val snackbarMessage: String? = null,
    val isSuccess:     Boolean = false
)

@HiltViewModel
class ProfileAddEditViewModel @Inject constructor(
    private val repository: ProfileRepository,
    private val xtreamValidator: XtreamValidator,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val profileId: String? = savedStateHandle["profileId"]

    private val _form = MutableStateFlow(ProfileFormState())
    val form: StateFlow<ProfileFormState> = _form.asStateFlow()

    init {
        profileId?.let { loadProfile(it) }
    }

    private fun loadProfile(id: String) = viewModelScope.launch {
        val profile  = repository.getProfile(id) ?: return@launch
        val password = repository.getPassword(id)
        _form.update { it.copy(
            name = profile.name, host = profile.host,
            username = profile.username, password = password
        ) }
    }

    fun onNameChange(v: String)     { _form.update { it.copy(name = v, nameError = null) } }
    fun onHostChange(v: String)     { _form.update { it.copy(host = v, hostError = null) } }
    fun onUsernameChange(v: String) { _form.update { it.copy(username = v, usernameError = null) } }
    fun onPasswordChange(v: String) { _form.update { it.copy(password = v, passwordError = null) } }

    fun clearSnackbar() { _form.update { it.copy(snackbarMessage = null) } }

    fun save() = viewModelScope.launch {
        val f = _form.value
        if (f.isSaving) return@launch

        // ── Local Validation ──────────────────────────────────────────────────
        var hasError = false
        val hostInput = f.host.trim()
        val userTrimmed = f.username.trim()
        val passwordInput = f.password

        if (hostInput.isEmpty()) {
            _form.update { it.copy(hostError = "Host URL is required") }
            hasError = true
        }
        if (userTrimmed.isEmpty()) {
            _form.update { it.copy(usernameError = "Username is required") }
            hasError = true
        }
        if (passwordInput.isEmpty()) {
            _form.update { it.copy(passwordError = "Password is required") }
            hasError = true
        }

        if (hasError) return@launch

        // ── Server Validation ─────────────────────────────────────────────────
        _form.update { it.copy(isSaving = true, globalError = null, snackbarMessage = null) }

        val validation = xtreamValidator.validate(hostInput, userTrimmed, passwordInput)

        if (!validation.ok) {
            val errorMsg = validation.error?.toUserMessage() ?: "Validation failed"
            _form.update { it.copy(
                isSaving = false,
                snackbarMessage = errorMsg,
                isSuccess = false,
                // Assign focus hints based on error type
                hostError = if (validation.error == ValidationError.InvalidUrl || validation.error == ValidationError.Unreachable) errorMsg else null,
                usernameError = if (validation.error == ValidationError.InvalidCredentials) "Check credentials" else null
            ) }
            return@launch
        }

        // ── Persistence ───────────────────────────────────────────────────────
        val profile = Profile(
            id       = profileId ?: UUID.randomUUID().toString(),
            name     = f.name.trim().ifEmpty { hostInput.removePrefix("http://").removePrefix("https://").take(20) },
            host     = if (hostInput.startsWith("http")) hostInput else "http://$hostInput",
            username = userTrimmed,
            lastUsed = System.currentTimeMillis()
        )

        val saveError = repository.saveProfile(profile, passwordInput)
        if (saveError != null) {
            _form.update { it.copy(isSaving = false, snackbarMessage = saveError, isSuccess = false) }
        } else {
            _form.update { it.copy(
                isSaving = false,
                isSuccess = true,
                snackbarMessage = "Server OK — Saved",
                isSaved = true
            ) }
        }
    }
}
