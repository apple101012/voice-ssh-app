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
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

private enum class MainTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Prompt("Voice", Icons.Outlined.GraphicEq),
    Sessions("Sessions", Icons.Outlined.History),
    Terminal("Terminal", Icons.Outlined.Terminal),
}

private val TerminalInk = Color(0xFF050B13)
private val TerminalOutput = Color(0xFF13EC5B)
private val TerminalMeta = Color(0xFF92ABCA)
private val TerminalConnectedDot = Color(0xFF13EC5B)
private val TerminalDisconnectedDot = Color(0xFF385372)

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
    var selectedTab by rememberSaveable {
        mutableIntStateOf(initialTabIndex.coerceIn(0, tabs.lastIndex))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "VOICE SSH",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Navy mobile workspace",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        modifier = Modifier.testTag(
                            when (tab) {
                                MainTab.Prompt -> "promptTab"
                                MainTab.Sessions -> "terminalTab"
                                MainTab.Terminal -> "liveTerminalTab"
                            },
                        ),
                        label = { Text(tab.label) },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.secondary,
                            unselectedTextColor = MaterialTheme.colorScheme.secondary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            if (uiState.message != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = uiState.message,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        TextButton(onClick = onDismissMessage) {
                            Text("OK", color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                when (tabs[selectedTab]) {
                    MainTab.Prompt -> {
                        PromptTab(
                            uiState = uiState,
                            onDraftChange = onDraftChange,
                            onClearDraft = onClearDraft,
                            onSendDraft = onSendDraft,
                            onLaunchSpeech = onLaunchSpeech,
                        )
                    }
                    MainTab.Sessions -> {
                        SessionsTab(
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
                        )
                    }
                    MainTab.Terminal -> {
                        TerminalTab(
                            uiState = uiState,
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
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatusPill(
                    text = when (uiState.terminalSnapshot.status) {
                        ConnectionStatus.Connected -> "CONNECTED"
                        ConnectionStatus.Connecting -> "CONNECTING"
                        ConnectionStatus.Disconnected -> "OFFLINE"
                    },
                    active = uiState.terminalSnapshot.status == ConnectionStatus.Connected,
                )
                Text(
                    text = "Voice Prompt Composer",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Speak or type your command, then ship it to the active terminal.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("DRAFT", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    TextButton(onClick = onClearDraft) {
                        Text("CLEAR", color = MaterialTheme.colorScheme.error)
                    }
                }
                
                OutlinedTextField(
                    value = uiState.draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("draftField"),
                    placeholder = { Text("Enter prompt or use voice...") },
                    shape = RoundedCornerShape(16.dp),
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onLaunchSpeech,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp)
                    .testTag("micButton"),
                shape = RoundedCornerShape(16.dp)
            ) {
                IconLabel(Icons.Outlined.KeyboardVoice, "VOICE")
            }
            Button(
                onClick = onSendDraft,
                enabled = uiState.canSendDraft,
                modifier = Modifier
                    .weight(1.5f)
                    .heightIn(min = 56.dp)
                    .testTag("sendDraftButton"),
                shape = RoundedCornerShape(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                IconLabel(Icons.AutoMirrored.Outlined.Send, "SEND TO TERMINAL")
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SessionsTab(
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
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("savedSessionsSection"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Saved Sessions",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            if (uiState.savedSessions.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "No saved sessions yet.",
                        modifier = Modifier.padding(20.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                uiState.savedSessions.forEach { session ->
                    SessionCard(
                        session = session,
                        onQuickConnect = { onQuickConnectSession(session) },
                        onLoad = { onLoadSession(session) },
                        onDelete = { onDeleteSession(session) }
                    )
                }
            }
        }

        Spacer(Modifier.size(8.dp))
        Text(
            text = "CREATE NEW PROFILE",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

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
        )
    }
}

@Composable
private fun SessionCard(
    session: SavedTerminalSession,
    onQuickConnect: () -> Unit,
    onLoad: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(session.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Outlined.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
            Text(
                session.profile.summary,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(Modifier.size(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onQuickConnect,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("CONNECT")
                }
                OutlinedButton(
                    onClick = onLoad,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("EDIT")
                }
            }
        }
    }
}

@Composable
private fun TerminalTab(
    uiState: VoiceSshUiState,
    onDisconnect: () -> Unit,
    onTerminalInputChange: (String) -> Unit,
    onSendTerminalInput: () -> Unit,
    onSendQuickCommand: (String) -> Unit,
) {
    val terminalOutputScrollState = rememberScrollState()

    LaunchedEffect(uiState.terminalSnapshot.output) {
        terminalOutputScrollState.scrollTo(terminalOutputScrollState.maxValue)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (uiState.terminalSnapshot.status == ConnectionStatus.Connected) "CONNECTED" else "DISCONNECTED",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (uiState.terminalSnapshot.status == ConnectionStatus.Connected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = uiState.sessionName.ifBlank { uiState.terminalSnapshot.targetSummary ?: "Active Terminal" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (uiState.terminalSnapshot.status == ConnectionStatus.Connected) {
                OutlinedButton(
                    onClick = onDisconnect,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    IconLabel(Icons.Outlined.LinkOff, "DISCONNECT")
                }
            }
        }

        TerminalWorkspaceCard(
            uiState = uiState,
            terminalOutputScrollState = terminalOutputScrollState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        )

        TerminalInputCard(
            uiState = uiState,
            onTerminalInputChange = onTerminalInputChange,
            onSendTerminalInput = onSendTerminalInput,
            onSendQuickCommand = onSendQuickCommand,
        )
    }
}

@Composable
private fun TerminalWorkspaceCard(
    uiState: VoiceSshUiState,
    terminalOutputScrollState: androidx.compose.foundation.ScrollState,
    modifier: Modifier = Modifier,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = TerminalInk),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.testTag("terminalOutput"),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SelectionContainer(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(terminalOutputScrollState)
                    .padding(16.dp),
            ) {
                Text(
                    text = uiState.terminalSnapshot.output.ifBlank { "Terminal output will appear here..." },
                    color = TerminalOutput,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.testTag("terminalOutputText"),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TerminalInputCard(
    uiState: VoiceSshUiState,
    onTerminalInputChange: (String) -> Unit,
    onSendTerminalInput: () -> Unit,
    onSendQuickCommand: (String) -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = uiState.terminalInput,
                    onValueChange = onTerminalInputChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("terminalInputField"),
                    placeholder = { Text("Command...") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (uiState.canSendTerminalInput) {
                                onSendTerminalInput()
                            }
                        },
                    ),
                )
                Button(
                    onClick = onSendTerminalInput,
                    enabled = uiState.canSendTerminalInput,
                    modifier = Modifier
                        .testTag("sendTerminalInputButton")
                        .widthIn(min = 108.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Send, contentDescription = "Enter and send")
                    Spacer(Modifier.size(6.dp))
                    Text("ENTER")
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                QuickCommandChip("pwd", onSendQuickCommand)
                QuickCommandChip("ls -la", onSendQuickCommand)
                QuickCommandChip("top", onSendQuickCommand)
                QuickCommandChip("clear", onSendQuickCommand)
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
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = uiState.sessionName,
                onValueChange = onSessionNameChange,
                modifier = Modifier.fillMaxWidth().testTag("sessionNameField"),
                label = { Text("Session Name") },
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = uiState.profile.host,
                    onValueChange = onHostChange,
                    modifier = Modifier.weight(2f).testTag("hostField"),
                    label = { Text("Host") },
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = uiState.profile.port,
                    onValueChange = onPortChange,
                    modifier = Modifier.weight(1f).testTag("portField"),
                    label = { Text("Port") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = uiState.profile.username,
                onValueChange = onUsernameChange,
                modifier = Modifier.fillMaxWidth().testTag("usernameField"),
                label = { Text("Username") },
                shape = RoundedCornerShape(12.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = uiState.profile.authMode == AuthMode.Password,
                    onClick = { onAuthModeChange(AuthMode.Password) },
                    label = { Text("Password") },
                    modifier = Modifier.testTag("passwordAuthButton"),
                    shape = RoundedCornerShape(8.dp)
                )
                FilterChip(
                    selected = uiState.profile.authMode == AuthMode.SshKey,
                    onClick = { onAuthModeChange(AuthMode.SshKey) },
                    label = { Text("SSH Key") },
                    modifier = Modifier.testTag("keyAuthButton"),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            if (uiState.profile.authMode == AuthMode.Password) {
                OutlinedTextField(
                    value = uiState.profile.password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth().testTag("passwordField"),
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp)
                )
            } else {
                OutlinedTextField(
                    value = uiState.profile.privateKey,
                    onValueChange = onPrivateKeyChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp)
                        .testTag("privateKeyField"),
                    label = { Text("Private Key") },
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedButton(
                    onClick = onPickPrivateKeyFile,
                    modifier = Modifier.fillMaxWidth().testTag("pickKeyFileButton"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    IconLabel(Icons.Outlined.FolderOpen, "LOAD KEY FILE")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onSaveSession,
                    modifier = Modifier.weight(1f).testTag("saveSessionButton"),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    IconLabel(Icons.Outlined.Save, "SAVE")
                }
                Button(
                    onClick = onConnect,
                    modifier = Modifier.weight(1f).testTag("connectButton"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    IconLabel(Icons.Outlined.Link, "CONNECT")
                }
            }
            
            OutlinedButton(
                onClick = onUseEmulatorHost,
                modifier = Modifier.fillMaxWidth().testTag("emulatorHostButton"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("USE EMULATOR HOST (10.0.2.2)")
            }
        }
    }
}

@Composable
private fun QuickCommandChip(
    command: String,
    onSendQuickCommand: (String) -> Unit,
) {
    AssistChip(
        onClick = { onSendQuickCommand(command) },
        label = { Text(command) },
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun StatusPill(
    text: String,
    active: Boolean,
) {
    Surface(
        color = if (active) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        } else {
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
        },
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (active) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.secondary
            },
        )
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
    Spacer(Modifier.size(8.dp))
    Text(text, style = MaterialTheme.typography.labelLarge)
}

@Composable
private fun IconButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.material3.IconButton(onClick = onClick, content = content)
}
