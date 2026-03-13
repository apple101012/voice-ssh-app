package com.apple101012.voicessh

import com.jcraft.jsch.ChannelShell
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.Properties
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class JschTerminalSessionRepository : TerminalSessionRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _sessionState = MutableStateFlow(TerminalSessionSnapshot())
    override val sessionState: StateFlow<TerminalSessionSnapshot> = _sessionState.asStateFlow()

    private var session: Session? = null
    private var channel: ChannelShell? = null
    private var terminalInput: OutputStream? = null
    private var readJob: Job? = null

    override suspend fun connect(profile: ConnectionProfile) {
        disconnect()
        _sessionState.value = TerminalSessionSnapshot(
            status = ConnectionStatus.Connecting,
            output = appendLine(
                _sessionState.value.output,
                "Connecting to ${profile.summary}...",
            ),
            targetSummary = profile.summary,
        )

        try {
            withContext(Dispatchers.IO) {
                val port = profile.port.toInt()
                val jsch = JSch()
                if (profile.authMode == AuthMode.SshKey) {
                    jsch.addIdentity(
                        KEY_IDENTITY_NAME,
                        profile.privateKey.trim().toByteArray(StandardCharsets.UTF_8),
                        null,
                        null,
                    )
                }
                val nextSession = jsch.getSession(profile.username, profile.host, port)
                if (profile.authMode == AuthMode.Password) {
                    nextSession.setPassword(profile.password)
                }
                nextSession.timeout = CONNECT_TIMEOUT_MS
                nextSession.setConfig(
                    Properties().apply {
                        put("StrictHostKeyChecking", "no")
                        put(
                            "server_host_key",
                            appendAlgorithms(
                                JSch.getConfig("server_host_key"),
                                listOf("ssh-ed25519", "rsa-sha2-512", "rsa-sha2-256", "ssh-rsa"),
                            ),
                        )
                        put(
                            "PubkeyAcceptedAlgorithms",
                            appendAlgorithms(
                                JSch.getConfig("PubkeyAcceptedAlgorithms"),
                                listOf("rsa-sha2-512", "rsa-sha2-256", "ssh-rsa"),
                            ),
                        )
                        put(
                            "PreferredAuthentications",
                            if (profile.authMode == AuthMode.SshKey) {
                                "publickey"
                            } else {
                                "password,keyboard-interactive"
                            },
                        )
                    },
                )
                nextSession.connect(CONNECT_TIMEOUT_MS)

                val nextChannel = nextSession.openChannel("shell") as ChannelShell
                nextChannel.setPtyType("xterm")
                val remoteOutput = nextChannel.inputStream
                val remoteInput = nextChannel.outputStream
                nextChannel.connect(CONNECT_TIMEOUT_MS)

                session = nextSession
                channel = nextChannel
                terminalInput = remoteInput

                readJob = scope.launch {
                    readLoop(remoteOutput)
                }
            }

            _sessionState.update { current ->
                current.copy(
                    status = ConnectionStatus.Connected,
                    output = appendLine(
                        current.output,
                        "Connected to ${profile.summary}.",
                    ),
                    targetSummary = profile.summary,
                    lastError = null,
                )
            }
        } catch (error: Exception) {
            disconnect()
            _sessionState.update { current ->
                current.copy(
                    status = ConnectionStatus.Disconnected,
                    output = appendLine(
                        current.output,
                        "Connection failed: ${error.message ?: "Unknown error"}",
                    ),
                    lastError = error.message ?: "Unknown error",
                    targetSummary = profile.summary,
                )
            }
        }
    }

    override suspend fun disconnect() {
        withContext(Dispatchers.IO) {
            readJob?.cancel()
            readJob = null
            terminalInput?.closeQuietly()
            terminalInput = null
            channel?.disconnect()
            channel = null
            session?.disconnect()
            session = null
        }

        if (_sessionState.value.status != ConnectionStatus.Disconnected) {
            _sessionState.update { current ->
                current.copy(
                    status = ConnectionStatus.Disconnected,
                    output = appendLine(current.output, "Disconnected."),
                )
            }
        }
    }

    override suspend fun send(text: String) {
        val output = terminalInput ?: error("No active SSH session.")
        withContext(Dispatchers.IO) {
            output.write(text.toByteArray(StandardCharsets.UTF_8))
            output.flush()
        }
    }

    override fun close() {
        scope.cancel()
    }

    private suspend fun readLoop(inputStream: InputStream) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (scope.isActive && channel?.isConnected == true) {
            val bytesRead = inputStream.read(buffer)
            if (bytesRead <= 0) {
                continue
            }

            val chunk = sanitizeOutput(String(buffer, 0, bytesRead, StandardCharsets.UTF_8))
            if (chunk.isBlank()) {
                continue
            }

            _sessionState.update { current ->
                current.copy(
                    output = trimOutput(current.output + chunk),
                    lastError = null,
                )
            }
        }
    }

    private fun sanitizeOutput(raw: String): String {
        return ANSI_ESCAPE_REGEX.replace(raw, "")
            .replace("\r\n", "\n")
            .replace('\r', '\n')
    }

    private fun appendLine(existing: String, line: String): String {
        return trimOutput(existing + line + "\n")
    }

    private fun trimOutput(output: String): String {
        return if (output.length <= MAX_OUTPUT_CHARS) {
            output
        } else {
            output.takeLast(MAX_OUTPUT_CHARS)
        }
    }

    private fun OutputStream.closeQuietly() {
        runCatching { close() }
    }

    private fun appendAlgorithms(existing: String?, algorithms: List<String>): String {
        val values = existing
            .orEmpty()
            .split(',')
            .map(String::trim)
            .filter(String::isNotBlank)
            .toMutableList()
        algorithms.forEach { algorithm ->
            if (algorithm !in values) {
                values += algorithm
            }
        }
        return values.joinToString(",")
    }

    private companion object {
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val MAX_OUTPUT_CHARS = 16_000
        private const val KEY_IDENTITY_NAME = "voice-ssh-inline-key"
        private val ANSI_ESCAPE_REGEX = Regex("\\u001B\\[[;?0-9]*[ -/]*[@-~]")
    }
}
