package com.apple101012.voicessh

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
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
    onUseEmulatorHost: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onTerminalInputChange: (String) -> Unit,
    onSendTerminalInput: () -> Unit,
    onDismissMessage: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val tabs = remember { MainTab.entries }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Voice SSH")
                        Text(
                            text = "Speak a prompt, send it to an SSH shell.",
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
                        modifier = Modifier.testTag(if (tab == MainTab.Prompt) "promptTab" else "terminalTab"),
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(tab.label) },
                        icon = {
                            Icon(
                                imageVector = if (tab == MainTab.Prompt) Icons.Outlined.KeyboardVoice else Icons.Outlined.Terminal,
                                contentDescription = null,
                            )
                        },
                    )
                }
            }

            if (uiState.message != null) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(18.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
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
                MainTab.Prompt -> VoicePromptTab(
                    uiState = uiState,
                    onDraftChange = onDraftChange,
                    onClearDraft = onClearDraft,
                    onSendDraft = onSendDraft,
                    onLaunchSpeech = onLaunchSpeech,
                )
                MainTab.Terminal -> TerminalTab(
                    uiState = uiState,
                    onHostChange = onHostChange,
                    onPortChange = onPortChange,
                    onUsernameChange = onUsernameChange,
                    onAuthModeChange = onAuthModeChange,
                    onPasswordChange = onPasswordChange,
                    onPrivateKeyChange = onPrivateKeyChange,
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
private fun VoicePromptTab(
    uiState: VoiceSshUiState,
    onDraftChange: (String) -> Unit,
    onClearDraft: () -> Unit,
    onSendDraft: () -> Unit,
    onLaunchSpeech: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Draft with your voice, then ship it into the shell.",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "This first milestone keeps the loop simple: speak or type in one tab, send to the live SSH session in the other tab.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                ConnectionStatusChip(uiState = uiState)
            }
        }

        Card {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Prompt Draft",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                OutlinedTextField(
                    value = uiState.draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp)
                        .testTag("draftField"),
                    label = { Text("Speak or type your coding prompt") },
                    supportingText = {
                        Text("The app sends the current text plus Enter to the active SSH shell.")
                    },
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Button(
                        onClick = onLaunchSpeech,
                        modifier = Modifier.testTag("micButton"),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardVoice,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Mic")
                    }

                    OutlinedButton(onClick = onClearDraft) {
                        Text("Clear")
                    }

                    Button(
                        onClick = onSendDraft,
                        enabled = uiState.canSendDraft,
                        modifier = Modifier.testTag("sendDraftButton"),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Send to Terminal")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TerminalTab(
    uiState: VoiceSshUiState,
    onHostChange: (String) -> Unit,
    onPortChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onAuthModeChange: (AuthMode) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPrivateKeyChange: (String) -> Unit,
    onUseEmulatorHost: () -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onTerminalInputChange: (String) -> Unit,
    onSendTerminalInput: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Connection",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
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
                }
                OutlinedTextField(
                    value = uiState.profile.host,
                    onValueChange = onHostChange,
                    label = { Text("Host") },
                    modifier = Modifier.fillMaxWidth().testTag("hostField"),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.profile.port,
                        onValueChange = onPortChange,
                        label = { Text("Port") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("portField"),
                    )
                    OutlinedTextField(
                        value = uiState.profile.username,
                        onValueChange = onUsernameChange,
                        label = { Text("Username") },
                        modifier = Modifier.weight(2f).testTag("usernameField"),
                    )
                }
                if (uiState.profile.authMode == AuthMode.Password) {
                    OutlinedTextField(
                        value = uiState.profile.password,
                        onValueChange = onPasswordChange,
                        label = { Text("Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().testTag("passwordField"),
                    )
                } else {
                    OutlinedTextField(
                        value = uiState.profile.privateKey,
                        onValueChange = onPrivateKeyChange,
                        label = { Text("Private key") },
                        supportingText = {
                            Text("Paste an OpenSSH or PEM private key. On the Android emulator, your Windows host is usually 10.0.2.2.")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp)
                            .testTag("privateKeyField"),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = onConnect,
                        enabled = uiState.terminalSnapshot.status != ConnectionStatus.Connecting,
                        modifier = Modifier.testTag("connectButton"),
                    ) {
                        Icon(imageVector = Icons.Outlined.Link, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            when (uiState.terminalSnapshot.status) {
                                ConnectionStatus.Connecting -> "Connecting..."
                                ConnectionStatus.Connected -> "Reconnect"
                                ConnectionStatus.Disconnected -> "Connect"
                            },
                        )
                    }
                    OutlinedButton(
                        onClick = onDisconnect,
                        enabled = uiState.terminalSnapshot.status != ConnectionStatus.Disconnected,
                        modifier = Modifier.testTag("disconnectButton"),
                    ) {
                        Icon(imageVector = Icons.Outlined.LinkOff, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Disconnect")
                    }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Terminal Session",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                        .background(
                            color = MaterialTheme.colorScheme.inverseSurface,
                            shape = RoundedCornerShape(20.dp),
                        )
                        .padding(16.dp)
                        .testTag("terminalOutput"),
                ) {
                    Text(
                        text = uiState.terminalSnapshot.output,
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                    )
                }
                OutlinedTextField(
                    value = uiState.terminalInput,
                    onValueChange = onTerminalInputChange,
                    modifier = Modifier.fillMaxWidth().testTag("terminalInputField"),
                    label = { Text("Manual terminal input") },
                )
                Button(
                    onClick = onSendTerminalInput,
                    enabled = uiState.canSendTerminalInput,
                ) {
                    Icon(imageVector = Icons.Outlined.Send, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Send Command")
                }
            }
        }
    }
}

@Composable
private fun ConnectionStatusChip(uiState: VoiceSshUiState) {
    val label = when (uiState.terminalSnapshot.status) {
        ConnectionStatus.Connected -> "Connected: ${uiState.terminalSnapshot.targetSummary}"
        ConnectionStatus.Connecting -> "Connecting to ${uiState.terminalSnapshot.targetSummary ?: "host"}"
        ConnectionStatus.Disconnected -> "Terminal is disconnected"
    }

    AssistChip(
        onClick = {},
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = if (uiState.terminalSnapshot.status == ConnectionStatus.Connected) {
                    Icons.Outlined.Link
                } else {
                    Icons.Outlined.LinkOff
                },
                contentDescription = null,
            )
        },
    )
}
