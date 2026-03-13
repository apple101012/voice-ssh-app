package com.apple101012.voicessh

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardVoice
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

private enum class MainTab(val label: String) {
    Prompt("Voice Prompt"),
    Terminal("Terminal"),
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VoiceSshScreen(
    uiState: VoiceSshUiState,
    initialTabIndex: Int = 0,
    onDraftChange: (String) -> Unit,
    onClearDraft: () -> Unit,
    onSendDraft: () -> Unit,
    onLaunchSpeech: () -> Unit,
    onSessionNameChange: (String) -> Unit,
    onSaveSession: () -> Unit,
    onLoadSession: (SavedTerminalSession) -> Unit,
    onQuickConnectSession: (SavedTerminalSession) -> Unit,
    onDeleteSession: (SavedTerminalSession) -> Unit,
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
    val tabs = remember { MainTab.entries }
    val initialIndex = initialTabIndex.coerceIn(0, tabs.lastIndex)
    var selectedTab by rememberSaveable { mutableIntStateOf(initialIndex) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Voice SSH")
                        Text(
                            text = "Prompt on the left flow, terminal on the right flow.",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier.testTag(if (tab == MainTab.Prompt) "promptTab" else "terminalTab"),
                        text = { Text(tab.label) },
                        icon = {
                            Icon(
                                imageVector = if (tab == MainTab.Prompt) {
                                    Icons.Outlined.KeyboardVoice
                                } else {
                                    Icons.Outlined.Terminal
                                },
                                contentDescription = null,
                            )
                        },
                    )
                }
            }

            if (uiState.message != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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

            when (tabs[selectedTab]) {
                MainTab.Prompt -> PromptTab(
                    uiState = uiState,
                    onDraftChange = onDraftChange,
                    onClearDraft = onClearDraft,
                    onSendDraft = onSendDraft,
                    onLaunchSpeech = onLaunchSpeech,
                )
                MainTab.Terminal -> TerminalTab(
                    uiState = uiState,
                    onSessionNameChange = onSessionNameChange,
                    onSaveSession = onSaveSession,
                    onLoadSession = onLoadSession,
                    onQuickConnectSession = onQuickConnectSession,
                    onDeleteSession = onDeleteSession,
                    onHostChange = onHostChange,
                    onPortChange = onPortChange,
                    onUsernameChange = onUsernameChange,
                    onAuthModeChange = onAuthModeChange,
                    onPasswordChange = onPasswordChange,
                    onPrivateKeyChange = onPrivateKeyChange,
                    onPickPrivateKeyFile = onPickPrivateKeyFile,
                    onUseEmulatorHost = onUseEmulatorHost,
                    onConnect = onConnect,
                    onDisconnect = onDisconnect,
                    onTerminalInputChange = onTerminalInputChange,
                    onSendTerminalInput = onSendTerminalInput,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PromptTab(
    uiState: VoiceSshUiState,
    onDraftChange: (String) -> Unit,
    onClearDraft: () -> Unit,
    onSendDraft: () -> Unit,
    onLaunchSpeech: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Prompt Draft",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = promptStatusText(uiState),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("promptStatusText"),
                )
                if (uiState.sessionName.isNotBlank()) {
                    Text(
                        text = "Current session: ${uiState.sessionName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.draft,
            onValueChange = onDraftChange,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("draftField"),
            label = { Text("Voice or typed prompt") },
            supportingText = {
                Text("Speak, edit, then send the prompt into the connected terminal.")
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
                IconLabel(Icons.Outlined.Send, "Send to Terminal")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TerminalTab(
    uiState: VoiceSshUiState,
    onSessionNameChange: (String) -> Unit,
    onSaveSession: () -> Unit,
    onLoadSession: (SavedTerminalSession) -> Unit,
    onQuickConnectSession: (SavedTerminalSession) -> Unit,
    onDeleteSession: (SavedTerminalSession) -> Unit,
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
) {
    var showPrivateKeyEditor by rememberSaveable { mutableStateOf(false) }
    val terminalOutputScrollState = rememberScrollState()

    LaunchedEffect(uiState.terminalSnapshot.output) {
        terminalOutputScrollState.scrollTo(terminalOutputScrollState.maxValue)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("savedSessionsSection"),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Saved Sessions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (uiState.savedSessions.isEmpty()) {
                    Text(
                        text = "No saved sessions yet. Save the current profile to quick connect later.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        uiState.savedSessions.forEach { session ->
                            SavedSessionCard(
                                session = session,
                                onLoadSession = { onLoadSession(session) },
                                onQuickConnectSession = { onQuickConnectSession(session) },
                                onDeleteSession = { onDeleteSession(session) },
                            )
                        }
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Current Session",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = terminalStatusText(uiState),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.testTag("connectionStatusText"),
                )
                OutlinedTextField(
                    value = uiState.sessionName,
                    onValueChange = onSessionNameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sessionNameField"),
                    label = { Text("Session name") },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.profile.host,
                        onValueChange = onHostChange,
                        modifier = Modifier
                            .weight(2f)
                            .testTag("hostField"),
                        label = { Text("Host") },
                    )
                    OutlinedTextField(
                        value = uiState.profile.port,
                        onValueChange = onPortChange,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("portField"),
                        label = { Text("Port") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                OutlinedTextField(
                    value = uiState.profile.username,
                    onValueChange = onUsernameChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("usernameField"),
                    label = { Text("Username") },
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
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedButton(
                            onClick = { showPrivateKeyEditor = !showPrivateKeyEditor },
                        ) {
                            Text(if (showPrivateKeyEditor) "Hide Key" else "Edit Key")
                        }
                        Text(
                            text = if (uiState.profile.privateKey.isBlank()) {
                                "Private key not loaded."
                            } else {
                                "Private key loaded."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (showPrivateKeyEditor) {
                        OutlinedTextField(
                            value = uiState.profile.privateKey,
                            onValueChange = onPrivateKeyChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 140.dp)
                                .testTag("privateKeyField"),
                            label = { Text("Private key") },
                            minLines = 4,
                            maxLines = 4,
                        )
                    }
                }

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onSaveSession,
                        modifier = Modifier.testTag("saveSessionButton"),
                    ) {
                        IconLabel(Icons.Outlined.Save, "Save Session")
                    }
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
        }

        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF111827),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
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

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = uiState.terminalInput,
                onValueChange = onTerminalInputChange,
                modifier = Modifier
                    .weight(1f)
                    .testTag("terminalInputField"),
                label = { Text("Manual terminal input") },
            )
            Button(
                onClick = onSendTerminalInput,
                enabled = uiState.canSendTerminalInput,
                modifier = Modifier.testTag("sendTerminalInputButton"),
            ) {
                IconLabel(Icons.Outlined.Terminal, "Send")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SavedSessionCard(
    session: SavedTerminalSession,
    onLoadSession: () -> Unit,
    onQuickConnectSession: () -> Unit,
    onDeleteSession: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.widthIn(min = 220.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = session.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = session.profile.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(onClick = onQuickConnectSession) {
                    Text("Quick Connect")
                }
                OutlinedButton(onClick = onLoadSession) {
                    Text("Load")
                }
                OutlinedButton(onClick = onDeleteSession) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun IconLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
    )
    Spacer(modifier = Modifier.size(8.dp))
    Text(text)
}

private fun promptStatusText(uiState: VoiceSshUiState): String {
    return when (uiState.terminalSnapshot.status) {
        ConnectionStatus.Connected -> "Connected to ${uiState.terminalSnapshot.targetSummary}. Prompt can be sent directly."
        ConnectionStatus.Connecting -> "Connecting to ${uiState.terminalSnapshot.targetSummary ?: "host"}."
        ConnectionStatus.Disconnected -> "Terminal is disconnected. Connect from the Terminal tab first."
    }
}

private fun terminalStatusText(uiState: VoiceSshUiState): String {
    return when (uiState.terminalSnapshot.status) {
        ConnectionStatus.Connected -> "Connected to ${uiState.terminalSnapshot.targetSummary}"
        ConnectionStatus.Connecting -> "Connecting to ${uiState.terminalSnapshot.targetSummary ?: "host"}"
        ConnectionStatus.Disconnected -> "Disconnected"
    }
}
