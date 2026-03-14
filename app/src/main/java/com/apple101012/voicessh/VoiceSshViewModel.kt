package com.apple101012.voicessh

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class VoiceSshUiState(
    val draft: String = "",
    val sessionName: String = "",
    val savedSessions: List<SavedTerminalSession> = emptyList(),
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
    private val savedSessionRepository: SavedSessionRepository,
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
        viewModelScope.launch {
            savedSessionRepository.sessions.collectLatest { sessions ->
                mutableUiState.value = mutableUiState.value.copy(savedSessions = sessions)
            }
        }
    }

    fun onDraftChange(draft: String) {
        mutableUiState.value = mutableUiState.value.copy(draft = draft, message = null)
    }

    fun clearDraft() {
        mutableUiState.value = mutableUiState.value.copy(draft = "", message = null)
    }

    fun onSessionNameChange(sessionName: String) {
        mutableUiState.value = mutableUiState.value.copy(sessionName = sessionName, message = null)
    }

    fun onHostChange(host: String) = updateProfile { copy(host = host) }

    fun onPortChange(port: String) = updateProfile { copy(port = port.filter(Char::isDigit).take(5)) }

    fun onUsernameChange(username: String) = updateProfile { copy(username = username) }

    fun onAuthModeChange(authMode: AuthMode) = updateProfile { copy(authMode = authMode) }

    fun onPasswordChange(password: String) = updateProfile { copy(password = password) }

    fun onPrivateKeyChange(privateKey: String) = updateProfile { copy(privateKey = privateKey) }

    fun onPrivateKeyImported(privateKey: String) {
        mutableUiState.value = mutableUiState.value.copy(
            profile = mutableUiState.value.profile.copy(privateKey = privateKey),
            message = "Private key loaded.",
        )
    }

    fun useEmulatorHost() = updateProfile { copy(host = EMULATOR_HOST) }

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

    fun saveSession() {
        val sessionName = mutableUiState.value.sessionName.trim()
        if (sessionName.isBlank()) {
            mutableUiState.value = mutableUiState.value.copy(message = "Session name is required.")
            return
        }

        val profile = mutableUiState.value.profile
        val validationError = validateProfile(profile)
        if (validationError != null) {
            mutableUiState.value = mutableUiState.value.copy(message = validationError)
            return
        }

        val sanitizedProfile = profile.copy(
            host = profile.host.trim(),
            username = profile.username.trim(),
        )

        viewModelScope.launch {
            savedSessionRepository.saveSession(
                SavedTerminalSession(
                    name = sessionName,
                    profile = sanitizedProfile,
                ),
            )
            mutableUiState.value = mutableUiState.value.copy(message = "Session saved.")
        }
    }

    fun loadSession(session: SavedTerminalSession) {
        mutableUiState.value = mutableUiState.value.copy(
            sessionName = session.name,
            profile = session.profile,
            message = "Session loaded.",
        )
    }

    fun quickConnect(session: SavedTerminalSession) {
        loadSession(session)
        connect()
    }

    fun deleteSession(session: SavedTerminalSession) {
        viewModelScope.launch {
            savedSessionRepository.deleteSession(session.name)
            val nextSessionName = if (mutableUiState.value.sessionName == session.name) "" else mutableUiState.value.sessionName
            mutableUiState.value = mutableUiState.value.copy(
                sessionName = nextSessionName,
                message = "Session deleted.",
            )
        }
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
            terminalRepository.send("$draft\r")
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
            terminalRepository.send("$input\r")
            mutableUiState.value = mutableUiState.value.copy(
                terminalInput = "",
                message = "Command sent.",
            )
        }
    }

    fun sendQuickCommand(command: String) {
        mutableUiState.value = mutableUiState.value.copy(terminalInput = command, message = null)
        sendTerminalInput()
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
        val port = profile.port.ifBlank { "22" }.toIntOrNull()
        if (port == null || port !in 1..65535) return "Port must be between 1 and 65535."
        if (profile.authMode == AuthMode.Password && profile.password.isBlank()) {
            return "Password is required for password auth."
        }
        if (profile.authMode == AuthMode.SshKey && profile.privateKey.isBlank()) {
            return "Private key is required for SSH key auth."
        }
        return null
    }

    companion object {
        const val EMULATOR_HOST = "10.0.2.2"

        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                VoiceSshViewModel(
                    terminalRepository = JschTerminalSessionRepository(),
                    savedSessionRepository = DataStoreSavedSessionRepository(context.applicationContext),
                )
            }
        }
    }
}
