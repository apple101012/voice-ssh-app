package com.apple101012.voicessh

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardVoice
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VoiceSshScreen(
    uiState: VoiceSshUiState,
    onDraftChange: (String) -> Unit,
    onClearDraft: () -> Unit,
    onSendDraft: () -> Unit,
    onLaunchSpeech: () -> Unit,
    onHostChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onAuthModeChange: (AuthMode) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPrivateKeyChange: (String) -> Unit,
    onPickPrivateKeyFile: () -> Unit,
    onUseEmulatorHost: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onTerminalInputChange: (String) -> Unit,
    onSendTerminalInput: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Voice SSH")
                        Text(
                            text = "Testing layout: connection, terminal, and prompt on one screen.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val terminalOutputScrollState = rememberScrollState()

        LaunchedEffect(uiState.terminalSnapshot.output) {
            terminalOutputScrollState.scrollTo(terminalOutputScrollState.maxValue)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (uiState.message != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = uiState.message,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        OutlinedButton(onClick = onDismissMessage) {
                            Text("Dismiss")
                        }
                    }
                }
            }

            SectionSurface {
                SectionTitle("Connection")
                Text(
                    text = connectionStatusText(uiState),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("connectionStatusText"),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (uiState.profile.authMode == AuthMode.Password) {
                        Button(
                            onClick = { onAuthModeChange(AuthMode.Password) },
                            modifier = Modifier.testTag("passwordAuthButton"),
                        ) {
                            Text("Password")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onAuthModeChange(AuthMode.Password) },
                            modifier = Modifier.testTag("passwordAuthButton"),
                        ) {
                            Text("Password")
                        }
                    }

                    if (uiState.profile.authMode == AuthMode.SshKey) {
                        Button(
                            onClick = { onAuthModeChange(AuthMode.SshKey) },
                            modifier = Modifier.testTag("keyAuthButton"),
                        ) {
                            Text("SSH Key")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onAuthModeChange(AuthMode.SshKey) },
                            modifier = Modifier.testTag("keyAuthButton"),
                        ) {
                            Text("SSH Key")
                        }
                    }

                    OutlinedButton(
                        onClick = onUseEmulatorHost,
                        modifier = Modifier.testTag("emulatorHostButton"),
                    ) {
                        Text("Use Emulator Host")
                    }

                    OutlinedButton(
                        onClick = onPickPrivateKeyFile,
                        modifier = Modifier.testTag("pickKeyFileButton"),
                    ) {
                        Text("Load Key File")
                    }
                }

                OutlinedTextField(
                    value = uiState.profile.host,
                    onValueChange = onHostChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hostField"),
                    label = { Text("Host") },
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.profile.port,
                        onValueChange = onPortChange,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("portField"),
                        label = { Text("Port") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    OutlinedTextField(
                        value = uiState.profile.username,
                        onValueChange = onUsernameChange,
                        modifier = Modifier
                            .weight(2f)
                            .testTag("usernameField"),
                        label = { Text("Username") },
                    )
                }

                if (uiState.profile.authMode == AuthMode.Password) {
                    OutlinedTextField(
                        value = uiState.profile.password,
                        onValueChange = onPasswordChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("passwordField"),
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                    )
                } else {
                    OutlinedTextField(
                        value = uiState.profile.privateKey,
                        onValueChange = onPrivateKeyChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 180.dp)
                            .testTag("privateKeyField"),
                        label = { Text("Private key") },
                        minLines = 5,
                        maxLines = 5,
                        supportingText = {
                            Text("Paste the private key or use Load Key File.")
                        },
                    )
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onConnect,
                        enabled = uiState.terminalSnapshot.status != ConnectionStatus.Connecting,
                        modifier = Modifier.testTag("connectButton"),
                    ) {
                        IconLabel(Icons.Outlined.Link, "Connect")
                    }
                    OutlinedButton(
                        onClick = onDisconnect,
                        enabled = uiState.terminalSnapshot.status != ConnectionStatus.Disconnected,
                        modifier = Modifier.testTag("disconnectButton"),
                    ) {
                        IconLabel(Icons.Outlined.LinkOff, "Disconnect")
                    }
                }
            }

            SectionSurface {
                SectionTitle("Terminal")
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF111827),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                        .testTag("terminalOutput"),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(terminalOutputScrollState)
                            .padding(16.dp),
                    ) {
                        Text(
                            text = uiState.terminalSnapshot.output,
                            color = Color(0xFFF9FAFB),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.testTag("terminalOutputText"),
                        )
                    }
                }

                OutlinedTextField(
                    value = uiState.terminalInput,
                    onValueChange = onTerminalInputChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("terminalInputField"),
                    label = { Text("Manual terminal input") },
                )

                Button(
                    onClick = onSendTerminalInput,
                    enabled = uiState.canSendTerminalInput,
                    modifier = Modifier.testTag("sendTerminalInputButton"),
                ) {
                    IconLabel(Icons.Outlined.Terminal, "Send Command")
                }
            }

            SectionSurface {
                SectionTitle("Prompt")
                OutlinedTextField(
                    value = uiState.draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp)
                        .testTag("draftField"),
                    label = { Text("Voice or typed prompt") },
                    supportingText = {
                        Text("Sends the full draft into the active SSH shell.")
                    },
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onLaunchSpeech,
                        modifier = Modifier.testTag("micButton"),
                    ) {
                        IconLabel(Icons.Outlined.KeyboardVoice, "Mic")
                    }
                    OutlinedButton(onClick = onClearDraft) {
                        Text("Clear")
                    }
                    Button(
                        onClick = onSendDraft,
                        enabled = uiState.canSendDraft,
                        modifier = Modifier.testTag("sendDraftButton"),
                    ) {
                        Text("Send Prompt")
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionSurface(
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
    )
}

@Composable
private fun IconLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    androidx.compose.material3.Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
    )
    Spacer(modifier = Modifier.size(8.dp))
    Text(text)
}

private fun connectionStatusText(uiState: VoiceSshUiState): String {
    return when (uiState.terminalSnapshot.status) {
        ConnectionStatus.Connected -> "Connected to ${uiState.terminalSnapshot.targetSummary}"
        ConnectionStatus.Connecting -> "Connecting to ${uiState.terminalSnapshot.targetSummary ?: "host"}"
        ConnectionStatus.Disconnected -> "Disconnected"
    }
}
