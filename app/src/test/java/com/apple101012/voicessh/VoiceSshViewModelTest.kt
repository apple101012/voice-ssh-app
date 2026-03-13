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
        val viewModel = VoiceSshViewModel(repository, FakeSavedSessionRepository())

        viewModel.onDraftChange("ls -la")
        viewModel.sendDraft()
        advanceUntilIdle()

        assertThat(repository.sentInputs).containsExactly("ls -la\n")
        assertThat(viewModel.uiState.value.message).isEqualTo("Prompt sent to the terminal.")
    }

    @Test
    fun connectRequiresPasswordInFirstMilestone() {
        val viewModel = VoiceSshViewModel(FakeTerminalSessionRepository(), FakeSavedSessionRepository())

        viewModel.onHostChange("10.0.2.2")
        viewModel.onUsernameChange("tester")
        viewModel.connect()

        assertThat(viewModel.uiState.value.message).isEqualTo("Password is required for password auth.")
    }

    @Test
    fun connectRequiresPrivateKeyForKeyAuth() {
        val viewModel = VoiceSshViewModel(FakeTerminalSessionRepository(), FakeSavedSessionRepository())

        viewModel.onHostChange("10.0.2.2")
        viewModel.onUsernameChange("tester")
        viewModel.onAuthModeChange(AuthMode.SshKey)
        viewModel.connect()

        assertThat(viewModel.uiState.value.message).isEqualTo("Private key is required for SSH key auth.")
    }

    @Test
    fun speechResultAppendsToExistingDraft() {
        val viewModel = VoiceSshViewModel(FakeTerminalSessionRepository(), FakeSavedSessionRepository())

        viewModel.onDraftChange("first line")
        viewModel.onSpeechResult("second line")

        assertThat(viewModel.uiState.value.draft).isEqualTo("first line\nsecond line")
    }

    @Test
    fun saveSessionStoresCurrentProfile() = runTest {
        val savedSessionRepository = FakeSavedSessionRepository()
        val viewModel = VoiceSshViewModel(FakeTerminalSessionRepository(), savedSessionRepository)

        viewModel.onSessionNameChange("Local Windows")
        viewModel.onHostChange("10.0.2.2")
        viewModel.onPortChange("2222")
        viewModel.onUsernameChange("Apple")
        viewModel.onAuthModeChange(AuthMode.SshKey)
        viewModel.onPrivateKeyChange("private-key")
        viewModel.saveSession()
        advanceUntilIdle()

        assertThat(savedSessionRepository.sessions.value).hasSize(1)
        assertThat(savedSessionRepository.sessions.value.first().name).isEqualTo("Local Windows")
        assertThat(savedSessionRepository.sessions.value.first().profile.host).isEqualTo("10.0.2.2")
    }

    @Test
    fun quickConnectLoadsSavedSessionAndConnects() = runTest {
        val terminalRepository = FakeTerminalSessionRepository()
        val savedSession = SavedTerminalSession(
            name = "Local Windows",
            profile = ConnectionProfile(
                host = "10.0.2.2",
                port = "2222",
                username = "Apple",
                authMode = AuthMode.SshKey,
                privateKey = "private-key",
            ),
        )
        val savedSessionRepository = FakeSavedSessionRepository(listOf(savedSession))
        val viewModel = VoiceSshViewModel(terminalRepository, savedSessionRepository)

        viewModel.quickConnect(savedSession)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.sessionName).isEqualTo("Local Windows")
        assertThat(viewModel.uiState.value.terminalSnapshot.status).isEqualTo(ConnectionStatus.Connected)
        assertThat(viewModel.uiState.value.terminalSnapshot.targetSummary).isEqualTo(savedSession.profile.summary)
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

private class FakeSavedSessionRepository(
    initialSessions: List<SavedTerminalSession> = emptyList(),
) : SavedSessionRepository {
    private val mutableSessions = MutableStateFlow(initialSessions)
    override val sessions: StateFlow<List<SavedTerminalSession>> = mutableSessions

    override suspend fun saveSession(session: SavedTerminalSession) {
        mutableSessions.value = buildList {
            add(session)
            addAll(mutableSessions.value.filterNot { it.name.equals(session.name, ignoreCase = true) })
        }
    }

    override suspend fun deleteSession(name: String) {
        mutableSessions.value = mutableSessions.value.filterNot { it.name.equals(name, ignoreCase = true) }
    }
}
