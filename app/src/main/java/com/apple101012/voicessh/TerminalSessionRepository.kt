package com.apple101012.voicessh

import kotlinx.coroutines.flow.StateFlow

data class ConnectionProfile(
    val host: String = "",
    val port: String = "22",
    val username: String = "",
    val authMode: AuthMode = AuthMode.Password,
    val password: String = "",
    val privateKey: String = "",
) {
    val summary: String
        get() = "${username.ifBlank { "user" }}@${host.ifBlank { "host" }}:${port.ifBlank { "22" }}"
}

enum class AuthMode(val label: String) {
    Password("Password"),
    SshKey("SSH Key"),
}

enum class ConnectionStatus {
    Disconnected,
    Connecting,
    Connected,
}

data class TerminalSessionSnapshot(
    val status: ConnectionStatus = ConnectionStatus.Disconnected,
    val output: String = "Terminal output will appear here after you connect.\n",
    val targetSummary: String? = null,
    val lastError: String? = null,
)

interface TerminalSessionRepository {
    val sessionState: StateFlow<TerminalSessionSnapshot>

    suspend fun connect(profile: ConnectionProfile)

    suspend fun disconnect()

    suspend fun send(text: String)

    fun close()
}
