package com.apple101012.voicessh

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.KeyboardVoice
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LinkOff
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

private enum class MainTab(val label: String) { Prompt("Voice Prompt"), Terminal("Terminal") }

private val TerminalInk = Color(0xFF090A0D)
private val TerminalGlow = Color(0xFF37CAB8)

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
    onSendQuickCommand: (String) -> Unit,
    onDismissMessage: () -> Unit,
) {
    val tabs = remember { MainTab.entries }
    var selectedTab by rememberSaveable { mutableIntStateOf(initialTabIndex.coerceIn(0, tabs.lastIndex)) }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(
                    MaterialTheme.colorScheme.background,
                    MaterialTheme.colorScheme.surface,
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f),
                ),
            ),
        ),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Voice SSH")
                            Text(
                                "Voice-draft prompts and send them into a live SSH terminal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    },
                )
            },
        ) { innerPadding ->
            Column(Modifier.fillMaxSize().padding(innerPadding)) {
                PrimaryTabRow(selectedTabIndex = selectedTab, modifier = Modifier.padding(horizontal = 12.dp)) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            modifier = Modifier.testTag(if (tab == MainTab.Prompt) "promptTab" else "terminalTab"),
                            text = { Text(tab.label) },
                            icon = { Icon(if (tab == MainTab.Prompt) Icons.Outlined.GraphicEq else Icons.Outlined.Terminal, null) },
                        )
                    }
                }
                if (uiState.message != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(uiState.message, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            TextButton(onClick = onDismissMessage) { Text("Dismiss") }
                        }
                    }
                }
                Box(Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 10.dp)) {
                    when (tabs[selectedTab]) {
                        MainTab.Prompt -> PromptTab(uiState, onDraftChange, onClearDraft, onSendDraft, onLaunchSpeech)
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
                            onSendQuickCommand = onSendQuickCommand,
                        )
                    }
                }
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
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)), shape = RoundedCornerShape(30.dp)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusPill(
                    text = when (uiState.terminalSnapshot.status) {
                        ConnectionStatus.Connected -> "Live terminal linked"
                        ConnectionStatus.Connecting -> "Connecting to terminal"
                        ConnectionStatus.Disconnected -> "Terminal disconnected"
                    },
                    active = uiState.terminalSnapshot.status == ConnectionStatus.Connected,
                )
                Text("Compose the prompt before it touches your terminal.", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Speak, revise, then send the final text straight into the SSH workspace.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (uiState.terminalSnapshot.targetSummary != null) {
                    AssistChip(onClick = { }, enabled = false, label = { Text("Target: ${uiState.terminalSnapshot.targetSummary}") })
                }
            }
        }
        Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)), shape = RoundedCornerShape(30.dp), modifier = Modifier.weight(1f)) {
            Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Prompt Draft", style = MaterialTheme.typography.titleLarge)
                OutlinedTextField(
                    value = uiState.draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier.fillMaxSize().testTag("draftField"),
                    label = { Text("Voice or typed prompt") },
                    supportingText = { Text(promptStatusText(uiState)) },
                )
            }
        }
        Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)), shape = RoundedCornerShape(30.dp)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Capture and ship", style = MaterialTheme.typography.titleMedium)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onLaunchSpeech, modifier = Modifier.testTag("micButton")) { IconLabel(Icons.Outlined.KeyboardVoice, "Mic") }
                    OutlinedButton(onClick = onClearDraft) { Text("Clear") }
                    Button(onClick = onSendDraft, enabled = uiState.canSendDraft, modifier = Modifier.testTag("sendDraftButton")) {
                        IconLabel(Icons.AutoMirrored.Outlined.Send, "Send to Terminal")
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
    onSendQuickCommand: (String) -> Unit,
) {
    var showConnectionEditor by rememberSaveable { mutableStateOf(uiState.terminalSnapshot.status != ConnectionStatus.Connected) }
    val terminalOutputScrollState = rememberScrollState()

    LaunchedEffect(uiState.terminalSnapshot.output) {
        terminalOutputScrollState.scrollTo(terminalOutputScrollState.maxValue)
    }
    LaunchedEffect(uiState.terminalSnapshot.status) {
        when (uiState.terminalSnapshot.status) {
            ConnectionStatus.Connected -> showConnectionEditor = false
            ConnectionStatus.Disconnected -> showConnectionEditor = true
            ConnectionStatus.Connecting -> Unit
        }
    }

    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.97f)), shape = RoundedCornerShape(30.dp)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        StatusPill(
                            text = when (uiState.terminalSnapshot.status) {
                                ConnectionStatus.Connected -> "Connected"
                                ConnectionStatus.Connecting -> "Connecting"
                                ConnectionStatus.Disconnected -> "Disconnected"
                            },
                            active = uiState.terminalSnapshot.status == ConnectionStatus.Connected,
                        )
                        Text(uiState.sessionName.ifBlank { "Terminal workspace" }, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            uiState.terminalSnapshot.targetSummary ?: uiState.profile.summary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = if (uiState.terminalSnapshot.status == ConnectionStatus.Connected) onDisconnect else onConnect) {
                            IconLabel(
                                if (uiState.terminalSnapshot.status == ConnectionStatus.Connected) Icons.Outlined.LinkOff else Icons.Outlined.Link,
                                if (uiState.terminalSnapshot.status == ConnectionStatus.Connected) "Disconnect" else "Connect",
                            )
                        }
                        OutlinedButton(onClick = { showConnectionEditor = !showConnectionEditor }) {
                            IconLabel(Icons.Outlined.Tune, if (showConnectionEditor) "Hide Profile" else "Show Profile")
                        }
                    }
                }
                Text(
                    "Saved sessions stay one tap away, but the live terminal remains the main surface once you are connected.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (uiState.terminalSnapshot.status != ConnectionStatus.Connected) {
            ConnectionEditorCard(
                uiState = uiState,
                onSessionNameChange = onSessionNameChange,
                onSaveSession = onSaveSession,
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
            )
        }

        SavedSessionsStrip(uiState, onLoadSession, onQuickConnectSession, onDeleteSession)

        if (uiState.terminalSnapshot.status == ConnectionStatus.Connected) {
            AnimatedVisibility(visible = showConnectionEditor) {
                ConnectionEditorCard(
                    uiState = uiState,
                    onSessionNameChange = onSessionNameChange,
                    onSaveSession = onSaveSession,
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
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = TerminalInk),
            shape = RoundedCornerShape(30.dp),
            modifier = Modifier.fillMaxWidth().weight(1f).testTag("terminalOutput"),
        ) {
            Column(Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier.size(10.dp).background(
                            color = if (uiState.terminalSnapshot.status == ConnectionStatus.Connected) TerminalGlow else Color(0xFFB9845A),
                            shape = CircleShape,
                        ),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(terminalStatusText(uiState), color = Color(0xFFF8F4ED), style = MaterialTheme.typography.titleMedium)
                        Text(
                            uiState.sessionName.ifBlank { uiState.terminalSnapshot.targetSummary ?: "No active saved session" },
                            color = Color(0xFFB7C7C3),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    TextButton(onClick = { showConnectionEditor = !showConnectionEditor }) {
                        Icon(Icons.Outlined.Tune, null)
                        Spacer(Modifier.size(6.dp))
                        Text(if (showConnectionEditor) "Hide Profile" else "Show Profile")
                    }
                }
                SelectionContainer(Modifier.fillMaxSize().verticalScroll(terminalOutputScrollState)) {
                    Text(
                        text = uiState.terminalSnapshot.output,
                        color = Color(0xFFF6F5F1),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.testTag("terminalOutputText"),
                    )
                }
            }
        }

        Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)), shape = RoundedCornerShape(28.dp)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Terminal Input", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.terminalInput,
                        onValueChange = onTerminalInputChange,
                        modifier = Modifier.weight(1f).testTag("terminalInputField"),
                        label = { Text("Manual terminal input") },
                    )
                    Button(
                        onClick = onSendTerminalInput,
                        enabled = uiState.canSendTerminalInput,
                        modifier = Modifier.testTag("sendTerminalInputButton"),
                    ) { IconLabel(Icons.AutoMirrored.Outlined.Send, "Send") }
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickCommandChip("pwd", onSendQuickCommand)
                    QuickCommandChip("ls", onSendQuickCommand)
                    QuickCommandChip("codex", onSendQuickCommand)
                    QuickCommandChip("clear", onSendQuickCommand)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ConnectionEditorCard(
    uiState: VoiceSshUiState,
    onSessionNameChange: (String) -> Unit,
    onSaveSession: () -> Unit,
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
) {
    val scrollModifier = if (uiState.terminalSnapshot.status == ConnectionStatus.Connected) {
        Modifier
            .heightIn(max = 420.dp)
            .verticalScroll(rememberScrollState())
    } else {
        Modifier
    }

    Card(colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)), shape = RoundedCornerShape(28.dp)) {
        Column(
            scrollModifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Connection Details", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = uiState.sessionName,
                onValueChange = onSessionNameChange,
                modifier = Modifier.fillMaxWidth().testTag("sessionNameField"),
                label = { Text("Session name") },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.profile.host,
                    onValueChange = onHostChange,
                    modifier = Modifier.weight(2f).testTag("hostField"),
                    label = { Text("Host") },
                )
                OutlinedTextField(
                    value = uiState.profile.port,
                    onValueChange = onPortChange,
                    modifier = Modifier.weight(1f).testTag("portField"),
                    label = { Text("Port") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
            OutlinedTextField(
                value = uiState.profile.username,
                onValueChange = onUsernameChange,
                modifier = Modifier.fillMaxWidth().testTag("usernameField"),
                label = { Text("Username") },
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(
                    selected = uiState.profile.authMode == AuthMode.Password,
                    onClick = { onAuthModeChange(AuthMode.Password) },
                    label = { Text("Password") },
                    modifier = Modifier.testTag("passwordAuthButton"),
                )
                FilterChip(
                    selected = uiState.profile.authMode == AuthMode.SshKey,
                    onClick = { onAuthModeChange(AuthMode.SshKey) },
                    label = { Text("SSH Key") },
                    modifier = Modifier.testTag("keyAuthButton"),
                )
                OutlinedButton(onClick = onUseEmulatorHost, modifier = Modifier.testTag("emulatorHostButton")) { Text("Use Emulator Host") }
                OutlinedButton(onClick = onPickPrivateKeyFile, modifier = Modifier.testTag("pickKeyFileButton")) {
                    IconLabel(Icons.Outlined.FolderOpen, "Load Key File")
                }
            }
            if (uiState.profile.authMode == AuthMode.Password) {
                OutlinedTextField(
                    value = uiState.profile.password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth().testTag("passwordField"),
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                )
            } else {
                Text(
                    if (uiState.profile.privateKey.isBlank()) "Paste the private key or load it from a file."
                    else "Private key loaded. You can still inspect or replace it below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = uiState.profile.privateKey,
                    onValueChange = onPrivateKeyChange,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 140.dp).testTag("privateKeyField"),
                    label = { Text("Private key") },
                    minLines = 4,
                    maxLines = 4,
                )
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onSaveSession, modifier = Modifier.testTag("saveSessionButton")) { IconLabel(Icons.Outlined.Save, "Save Session") }
                Button(
                    onClick = onConnect,
                    enabled = uiState.terminalSnapshot.status != ConnectionStatus.Connecting,
                    modifier = Modifier.testTag("connectButton"),
                ) { IconLabel(Icons.Outlined.Link, "Connect") }
                OutlinedButton(
                    onClick = onDisconnect,
                    enabled = uiState.terminalSnapshot.status != ConnectionStatus.Disconnected,
                    modifier = Modifier.testTag("disconnectButton"),
                ) { IconLabel(Icons.Outlined.LinkOff, "Disconnect") }
            }
        }
    }
}

@Composable
private fun SavedSessionsStrip(
    uiState: VoiceSshUiState,
    onLoadSession: (SavedTerminalSession) -> Unit,
    onQuickConnectSession: (SavedTerminalSession) -> Unit,
    onDeleteSession: (SavedTerminalSession) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surface.copy(alpha = 0.94f)),
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier.fillMaxWidth().testTag("savedSessionsSection"),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Saved Sessions", style = MaterialTheme.typography.titleLarge)
            Text(
                if (uiState.savedSessions.isEmpty()) "Save your most-used targets here for quick connect."
                else "Quick connect for speed, load when you want to inspect or edit a profile first.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (uiState.savedSessions.isEmpty()) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f), shape = RoundedCornerShape(20.dp)) {
                    Text("No saved sessions yet.", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    uiState.savedSessions.forEach { session ->
                        Card(
                            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.52f)),
                            shape = RoundedCornerShape(22.dp),
                            modifier = Modifier.widthIn(min = 240.dp),
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(session.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    session.profile.summary,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(onClick = { onQuickConnectSession(session) }) { Text("Quick Connect") }
                                    OutlinedButton(onClick = { onLoadSession(session) }) { Text("Load") }
                                    OutlinedButton(onClick = { onDeleteSession(session) }) { IconLabel(Icons.Outlined.DeleteOutline, "Delete") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickCommandChip(
    command: String,
    onSendQuickCommand: (String) -> Unit,
) {
    AssistChip(onClick = { onSendQuickCommand(command) }, label = { Text(command) })
}

@Composable
private fun StatusPill(
    text: String,
    active: Boolean,
) {
    Surface(
        color = if (active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
        )
    }
}

@Composable
private fun IconLabel(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
    Spacer(Modifier.size(8.dp))
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
