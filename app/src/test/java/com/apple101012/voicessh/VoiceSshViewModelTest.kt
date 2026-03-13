package com.apple101012.voicessh

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class VoiceSshViewModelTest {
    @get:Rule
    val dispatcherRule = MainDispatcherRule()

    @Test
    fun sendDraftPushesTextIntoRepositoryWhenConnected() = runTest {
        val repository = FakeTerminalSessionRepository(
            initialSnapshot = TerminalSessionSnapshot(status = ConnectionStatus.Connected),
        )
        val viewModel = VoiceSshViewModel(repository)

        viewModel.onDraftChange("ls -la")
        viewModel.sendDraft()
        advanceUntilIdle()

        assertThat(repository.sentInputs).containsExactly("ls -la\n")
        assertThat(viewModel.uiState.value.message).isEqualTo("Prompt sent to the terminal.")
    }

    @Test
    fun connectRequiresPasswordInFirstMilestone() {
        val viewModel = VoiceSshViewModel(FakeTerminalSessionRepository())

        viewModel.onHostChange("10.0.2.2")
        viewModel.onUsernameChange("tester")
        viewModel.connect()

        assertThat(viewModel.uiState.value.message).isEqualTo("Password is required for password auth.")
    }

    @Test
    fun connectRequiresPrivateKeyForKeyAuth() {
        val viewModel = VoiceSshViewModel(FakeTerminalSessionRepository())

        viewModel.onHostChange("10.0.2.2")
        viewModel.onUsernameChange("tester")
        viewModel.onAuthModeChange(AuthMode.SshKey)
        viewModel.connect()

        assertThat(viewModel.uiState.value.message).isEqualTo("Private key is required for SSH key auth.")
    }

    @Test
    fun speechResultAppendsToExistingDraft() {
        val viewModel = VoiceSshViewModel(FakeTerminalSessionRepository())

        viewModel.onDraftChange("first line")
        viewModel.onSpeechResult("second line")

        assertThat(viewModel.uiState.value.draft).isEqualTo("first line\nsecond line")
    }
}

private class FakeTerminalSessionRepository(
    initialSnapshot: TerminalSessionSnapshot = TerminalSessionSnapshot(),
) : TerminalSessionRepository {
    private val mutableState = MutableStateFlow(initialSnapshot)
    override val sessionState: StateFlow<TerminalSessionSnapshot> = mutableState
    val sentInputs = mutableListOf<String>()

    override suspend fun connect(profile: ConnectionProfile) {
        mutableState.value = TerminalSessionSnapshot(
            status = ConnectionStatus.Connected,
            targetSummary = profile.summary,
        )
    }

    override suspend fun disconnect() {
        mutableState.value = TerminalSessionSnapshot(status = ConnectionStatus.Disconnected)
    }

    override suspend fun send(text: String) {
        sentInputs += text
    }

    override fun close() = Unit
}
