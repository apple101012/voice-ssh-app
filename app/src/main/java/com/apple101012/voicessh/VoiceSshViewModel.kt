package com.apple101012.voicessh

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class VoiceSshUiState(
    val draft: String = "",
    val profile: ConnectionProfile = ConnectionProfile(),
    val terminalInput: String = "",
    val terminalSnapshot: TerminalSessionSnapshot = TerminalSessionSnapshot(),
    val message: String? = null,
) {
    val canSendDraft: Boolean
        get() = draft.isNotBlank() && terminalSnapshot.status == ConnectionStatus.Connected

    val canSendTerminalInput: Boolean
        get() = terminalInput.isNotBlank() && terminalSnapshot.status == ConnectionStatus.Connected
}

class VoiceSshViewModel(
    private val terminalRepository: TerminalSessionRepository,
) : ViewModel() {
    private val mutableUiState = mutableStateOf(
        VoiceSshUiState(terminalSnapshot = terminalRepository.sessionState.value),
    )
    val uiState: State<VoiceSshUiState> = mutableUiState

    init {
        viewModelScope.launch {
            terminalRepository.sessionState.collectLatest { snapshot ->
                mutableUiState.value = mutableUiState.value.copy(terminalSnapshot = snapshot)
            }
        }
    }

    fun onDraftChange(draft: String) {
        mutableUiState.value = mutableUiState.value.copy(draft = draft, message = null)
    }

    fun clearDraft() {
        mutableUiState.value = mutableUiState.value.copy(draft = "", message = null)
    }

    fun onHostChange(host: String) = updateProfile { copy(host = host) }

    fun onPortChange(port: String) = updateProfile { copy(port = port.filter(Char::isDigit).take(5)) }

    fun onUsernameChange(username: String) = updateProfile { copy(username = username) }

    fun onPasswordChange(password: String) = updateProfile { copy(password = password) }

    fun onTerminalInputChange(input: String) {
        mutableUiState.value = mutableUiState.value.copy(terminalInput = input, message = null)
    }

    fun onSpeechResult(result: String?) {
        if (result.isNullOrBlank()) {
            mutableUiState.value = mutableUiState.value.copy(message = "No speech was recognized.")
            return
        }

        val nextDraft = buildString {
            append(mutableUiState.value.draft.trimEnd())
            if (isNotEmpty()) {
                append("\n")
            }
            append(result.trim())
        }
        mutableUiState.value = mutableUiState.value.copy(draft = nextDraft, message = null)
    }

    fun onSpeechError(message: String) {
        mutableUiState.value = mutableUiState.value.copy(message = message)
    }

    fun dismissMessage() {
        mutableUiState.value = mutableUiState.value.copy(message = null)
    }

    fun connect() {
        val profile = mutableUiState.value.profile
        val validationError = validateProfile(profile)
        if (validationError != null) {
            mutableUiState.value = mutableUiState.value.copy(message = validationError)
            return
        }

        viewModelScope.launch {
            terminalRepository.connect(
                profile = profile.copy(
                    host = profile.host.trim(),
                    username = profile.username.trim(),
                ),
                password = profile.password,
            )
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            terminalRepository.disconnect()
        }
    }

    fun sendDraft() {
        val draft = mutableUiState.value.draft.trimEnd()
        if (draft.isBlank()) {
            mutableUiState.value = mutableUiState.value.copy(message = "Draft text is empty.")
            return
        }
        if (mutableUiState.value.terminalSnapshot.status != ConnectionStatus.Connected) {
            mutableUiState.value = mutableUiState.value.copy(message = "Connect the terminal before sending a prompt.")
            return
        }

        viewModelScope.launch {
            terminalRepository.send("$draft\n")
            mutableUiState.value = mutableUiState.value.copy(message = "Prompt sent to the terminal.")
        }
    }

    fun sendTerminalInput() {
        val input = mutableUiState.value.terminalInput.trimEnd()
        if (input.isBlank()) {
            mutableUiState.value = mutableUiState.value.copy(message = "Terminal input is empty.")
            return
        }
        if (mutableUiState.value.terminalSnapshot.status != ConnectionStatus.Connected) {
            mutableUiState.value = mutableUiState.value.copy(message = "Connect the terminal before sending input.")
            return
        }

        viewModelScope.launch {
            terminalRepository.send("$input\n")
            mutableUiState.value = mutableUiState.value.copy(
                terminalInput = "",
                message = "Command sent.",
            )
        }
    }

    override fun onCleared() {
        terminalRepository.close()
        super.onCleared()
    }

    private fun updateProfile(transform: ConnectionProfile.() -> ConnectionProfile) {
        mutableUiState.value = mutableUiState.value.copy(
            profile = mutableUiState.value.profile.transform(),
            message = null,
        )
    }

    private fun validateProfile(profile: ConnectionProfile): String? {
        if (profile.host.isBlank()) return "Host is required."
        if (profile.username.isBlank()) return "Username is required."
        if (profile.password.isBlank()) return "Password is required for the first milestone."
        val port = profile.port.ifBlank { "22" }.toIntOrNull()
        if (port == null || port !in 1..65535) return "Port must be between 1 and 65535."
        return null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { VoiceSshViewModel(JschTerminalSessionRepository()) }
        }
    }
}
