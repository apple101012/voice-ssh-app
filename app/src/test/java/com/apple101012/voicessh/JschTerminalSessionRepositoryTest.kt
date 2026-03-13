package com.apple101012.voicessh

import com.google.common.truth.Truth.assertThat
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import kotlin.concurrent.thread
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.channel.ChannelSession
import org.apache.sshd.server.command.Command
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.junit.Test

class JschTerminalSessionRepositoryTest {
    @Test
    fun connectAndSendReceivesShellOutput() = runBlocking {
        val hostKeyPath = Files.createTempFile("voice-ssh-host-key", ".ser")
        val server = SshServer.setUpDefaultServer().apply {
            port = 0
            keyPairProvider = SimpleGeneratorHostKeyProvider(hostKeyPath)
            passwordAuthenticator = { username, password, _ ->
                username == TEST_USERNAME && password == TEST_PASSWORD
            }
            shellFactory = TestShellFactory()
            start()
        }

        val repository = JschTerminalSessionRepository()

        try {
            repository.connect(
                profile = ConnectionProfile(
                    host = "127.0.0.1",
                    port = server.port.toString(),
                    username = TEST_USERNAME,
                    password = TEST_PASSWORD,
                ),
                password = TEST_PASSWORD,
            )

            assertThat(awaitState(repository) { it.status == ConnectionStatus.Connected }).isTrue()
            repository.send("echo hello\n")

            assertThat(
                awaitState(repository) { it.output.contains("echo: echo hello") },
            ).isTrue()
        } finally {
            repository.disconnect()
            repository.close()
            server.stop(true)
            Files.deleteIfExists(hostKeyPath)
        }
    }

    private suspend fun awaitState(
        repository: JschTerminalSessionRepository,
        predicate: (TerminalSessionSnapshot) -> Boolean,
    ): Boolean {
        repeat(40) {
            if (predicate(repository.sessionState.value)) {
                return true
            }
            delay(100)
        }
        return false
    }

    private class TestShellFactory : org.apache.sshd.server.shell.ShellFactory {
        override fun createShell(channel: ChannelSession): Command = EchoShellCommand()
    }

    private class EchoShellCommand : Command {
        private var inputStream: InputStream? = null
        private var outputStream: OutputStream? = null
        private var exitCallback: ExitCallback? = null
        private var workerThread: Thread? = null

        override fun setInputStream(inputStream: InputStream) {
            this.inputStream = inputStream
        }

        override fun setOutputStream(outputStream: OutputStream) {
            this.outputStream = outputStream
        }

        override fun setErrorStream(errorStream: OutputStream) = Unit

        override fun setExitCallback(exitCallback: ExitCallback) {
            this.exitCallback = exitCallback
        }

        override fun start(channel: ChannelSession, environment: Environment) {
            workerThread = thread(name = "voice-ssh-test-shell", isDaemon = true) {
                val reader = BufferedReader(
                    InputStreamReader(inputStream ?: return@thread, StandardCharsets.UTF_8),
                )
                val writer = OutputStreamWriter(
                    outputStream ?: return@thread,
                    StandardCharsets.UTF_8,
                )

                writer.appendLine("ready")
                writer.flush()

                while (!Thread.currentThread().isInterrupted) {
                    val line = reader.readLine() ?: break
                    writer.appendLine("echo: $line")
                    writer.flush()
                }

                exitCallback?.onExit(0)
            }
        }

        override fun destroy(channel: ChannelSession) {
            workerThread?.interrupt()
        }
    }

    private companion object {
        private const val TEST_USERNAME = "tester"
        private const val TEST_PASSWORD = "secret"
    }
}
