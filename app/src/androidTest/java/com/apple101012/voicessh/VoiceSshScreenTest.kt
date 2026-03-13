package com.apple101012.voicessh

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class VoiceSshScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun promptTabDisablesSendUntilConnected() {
        composeRule.setContent {
            VoiceSshTheme {
                VoiceSshScreen(
                    uiState = VoiceSshUiState(
                        draft = "Implement the feature",
                        terminalSnapshot = TerminalSessionSnapshot(
                            status = ConnectionStatus.Disconnected,
                        ),
                    ),
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
    fun terminalTabShowsConnectionFields() {
        composeRule.setContent {
            VoiceSshTheme {
                VoiceSshScreen(
                    uiState = VoiceSshUiState(),
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
                    onUseEmulatorHost = {},
                    onConnect = {},
                    onDisconnect = {},
                    onTerminalInputChange = {},
                    onSendTerminalInput = {},
                    onDismissMessage = {},
                )
            }
        }

        composeRule.onNodeWithText("Terminal").performClick()
        composeRule.onNodeWithTag("hostField").assertIsDisplayed()
        composeRule.onNodeWithTag("usernameField").assertIsDisplayed()
        composeRule.onNodeWithTag("passwordField").assertIsDisplayed()
        composeRule.onNodeWithTag("terminalOutput").assertIsDisplayed()
    }

    @Test
    fun terminalTabShowsPrivateKeyFieldInSshKeyMode() {
        composeRule.setContent {
            VoiceSshTheme {
                VoiceSshScreen(
                    uiState = VoiceSshUiState(
                        profile = ConnectionProfile(authMode = AuthMode.SshKey),
                    ),
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
                    onUseEmulatorHost = {},
                    onConnect = {},
                    onDisconnect = {},
                    onTerminalInputChange = {},
                    onSendTerminalInput = {},
                    onDismissMessage = {},
                )
            }
        }

        composeRule.onNodeWithText("Terminal").performClick()
        composeRule.onNodeWithTag("privateKeyField").assertIsDisplayed()
    }
}
