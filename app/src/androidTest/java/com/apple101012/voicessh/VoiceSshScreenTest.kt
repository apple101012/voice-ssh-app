package com.apple101012.voicessh

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test

class VoiceSshScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun promptSectionDisablesSendUntilConnected() {
        composeRule.setContent {
            VoiceSshTheme {
                VoiceSshScreen(
                    uiState = VoiceSshUiState(
                        draft = "Implement the feature",
                        terminalSnapshot = TerminalSessionSnapshot(
                            status = ConnectionStatus.Disconnected,
                        ),
                    ),
                    onSessionNameChange = {},
                    onSaveSession = {},
                    onLoadSession = {},
                    onQuickConnectSession = {},
                    onDeleteSession = {},
                    onDraftChange = {},
                    onClearDraft = {},
                    onSendDraft = {},
                    onLaunchSpeech = {},
                    onHostChange = {},
                    onPortChange = {},
                    onUsernameChange = {},
                    onAuthModeChange = {},
                    onPasswordChange = {},
                    onPrivateKeyChange = {},
                    onPickPrivateKeyFile = {},
                    onUseEmulatorHost = {},
                    onConnect = {},
                    onDisconnect = {},
                    onTerminalInputChange = {},
                    onSendTerminalInput = {},
                    onDismissMessage = {},
                )
            }
        }

        composeRule.onNodeWithTag("sendDraftButton").assertIsNotEnabled()
    }

    @Test
    fun promptTabIsDefaultAndTerminalTabShowsConnectionFields() {
        composeRule.setContent {
            VoiceSshTheme {
                VoiceSshScreen(
                    uiState = VoiceSshUiState(),
                    onSessionNameChange = {},
                    onSaveSession = {},
                    onLoadSession = {},
                    onQuickConnectSession = {},
                    onDeleteSession = {},
                    onDraftChange = {},
                    onClearDraft = {},
                    onSendDraft = {},
                    onLaunchSpeech = {},
                    onHostChange = {},
                    onPortChange = {},
                    onUsernameChange = {},
                    onAuthModeChange = {},
                    onPasswordChange = {},
                    onPrivateKeyChange = {},
                    onPickPrivateKeyFile = {},
                    onUseEmulatorHost = {},
                    onConnect = {},
                    onDisconnect = {},
                    onTerminalInputChange = {},
                    onSendTerminalInput = {},
                    onDismissMessage = {},
                )
            }
        }

        composeRule.onNodeWithTag("draftField").assertIsDisplayed()
        composeRule.onNodeWithTag("terminalTab").performClick()
        composeRule.onNodeWithTag("savedSessionsSection").assertIsDisplayed()
        composeRule.onNodeWithTag("sessionNameField").assertIsDisplayed()
        composeRule.onNodeWithTag("usernameField").assertIsDisplayed()
        composeRule.onNodeWithTag("passwordField").assertIsDisplayed()
    }

    @Test
    fun sshKeyModeCanRevealPrivateKeyField() {
        composeRule.setContent {
            VoiceSshTheme {
                VoiceSshScreen(
                    uiState = VoiceSshUiState(
                        profile = ConnectionProfile(authMode = AuthMode.SshKey),
                    ),
                    onSessionNameChange = {},
                    onSaveSession = {},
                    onLoadSession = {},
                    onQuickConnectSession = {},
                    onDeleteSession = {},
                    onDraftChange = {},
                    onClearDraft = {},
                    onSendDraft = {},
                    onLaunchSpeech = {},
                    onHostChange = {},
                    onPortChange = {},
                    onUsernameChange = {},
                    onAuthModeChange = {},
                    onPasswordChange = {},
                    onPrivateKeyChange = {},
                    onPickPrivateKeyFile = {},
                    onUseEmulatorHost = {},
                    onConnect = {},
                    onDisconnect = {},
                    onTerminalInputChange = {},
                    onSendTerminalInput = {},
                    onDismissMessage = {},
                )
            }
        }

        composeRule.onNodeWithTag("terminalTab").performClick()
        composeRule.onNodeWithText("Edit Key").performClick()
        composeRule.onNodeWithTag("privateKeyField").assertIsDisplayed()
    }
}
