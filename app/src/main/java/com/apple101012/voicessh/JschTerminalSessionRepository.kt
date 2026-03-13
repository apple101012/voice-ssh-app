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

    override suspend fun connect(profile: ConnectionProfile, password: String) {
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
                val nextSession = jsch.getSession(profile.username, profile.host, port)
                nextSession.setPassword(password)
                nextSession.timeout = CONNECT_TIMEOUT_MS
                nextSession.setConfig(
                    Properties().apply {
                        put("StrictHostKeyChecking", "no")
                        put("PreferredAuthentications", "password,keyboard-interactive")
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

            _sessionState.value = _sessionState.value.copy(
                status = ConnectionStatus.Connected,
                output = appendLine(
                    _sessionState.value.output,
                    "Connected to ${profile.summary}.",
                ),
                targetSummary = profile.summary,
                lastError = null,
            )
        } catch (error: Exception) {
            disconnect()
            _sessionState.value = _sessionState.value.copy(
                status = ConnectionStatus.Disconnected,
                output = appendLine(
                    _sessionState.value.output,
                    "Connection failed: ${error.message ?: "Unknown error"}",
                ),
                lastError = error.message ?: "Unknown error",
                targetSummary = profile.summary,
            )
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
            _sessionState.value = _sessionState.value.copy(
                status = ConnectionStatus.Disconnected,
                output = appendLine(_sessionState.value.output, "Disconnected."),
            )
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

            _sessionState.value = _sessionState.value.copy(
                output = trimOutput(_sessionState.value.output + chunk),
                lastError = null,
            )
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

    private companion object {
        private const val CONNECT_TIMEOUT_MS = 10_000
        private const val MAX_OUTPUT_CHARS = 16_000
        private val ANSI_ESCAPE_REGEX = Regex("\\u001B\\[[;?0-9]*[ -/]*[@-~]")
    }
}
