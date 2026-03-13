package com.apple101012.voicessh

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<VoiceSshViewModel> { VoiceSshViewModel.factory(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val isDebuggable = (applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
        val initialTabIndex = if (isDebuggable && intent.getBooleanExtra(EXTRA_DEBUG_START_ON_TERMINAL, false)) {
            1
        } else {
            0
        }

        enableEdgeToEdge()
        if (isDebuggable) {
            applyDebugPrefill(intent)
        }

        setContent {
            VoiceSshTheme {
                val context = LocalContext.current
                val uiState by viewModel.uiState
                val latestViewModel by rememberUpdatedState(viewModel)

                val speechLauncher =
                    rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                        if (result.resultCode != Activity.RESULT_OK) {
                            latestViewModel.onSpeechError("Speech input was cancelled.")
                            return@rememberLauncherForActivityResult
                        }

                        val transcript = result.data
                            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                            ?.firstOrNull()
                        latestViewModel.onSpeechResult(transcript)
                    }

                val permissionLauncher =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                        if (granted) {
                            launchSpeechRecognizer(
                                launch = speechLauncher::launch,
                                onUnavailable = latestViewModel::onSpeechError,
                            )
                        } else {
                            latestViewModel.onSpeechError("Microphone permission is required for speech input.")
                    }
                }

                val privateKeyPicker =
                    rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                        if (uri == null) {
                            latestViewModel.onSpeechError("No key file was selected.")
                            return@rememberLauncherForActivityResult
                        }

                        runCatching {
                            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                        }.onSuccess { contents ->
                            if (contents.isNullOrBlank()) {
                                latestViewModel.onSpeechError("The selected key file was empty.")
                            } else {
                                latestViewModel.onPrivateKeyImported(contents)
                            }
                        }.onFailure {
                            latestViewModel.onSpeechError("Unable to read the selected key file.")
                        }
                    }

                val launchSpeech = remember(context) {
                    {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO,
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            launchSpeechRecognizer(
                                launch = speechLauncher::launch,
                                onUnavailable = latestViewModel::onSpeechError,
                            )
                        } else {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                }

                VoiceSshScreen(
                    uiState = uiState,
                    initialTabIndex = initialTabIndex,
                    onDraftChange = viewModel::onDraftChange,
                    onClearDraft = viewModel::clearDraft,
                    onSendDraft = viewModel::sendDraft,
                    onLaunchSpeech = launchSpeech,
                    onSessionNameChange = viewModel::onSessionNameChange,
                    onSaveSession = viewModel::saveSession,
                    onLoadSession = viewModel::loadSession,
                    onQuickConnectSession = viewModel::quickConnect,
                    onDeleteSession = viewModel::deleteSession,
                    onHostChange = viewModel::onHostChange,
                    onPortChange = viewModel::onPortChange,
                    onUsernameChange = viewModel::onUsernameChange,
                    onAuthModeChange = viewModel::onAuthModeChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onPrivateKeyChange = viewModel::onPrivateKeyChange,
                    onPickPrivateKeyFile = { privateKeyPicker.launch("*/*") },
                    onUseEmulatorHost = viewModel::useEmulatorHost,
                    onConnect = viewModel::connect,
                    onDisconnect = viewModel::disconnect,
                    onTerminalInputChange = viewModel::onTerminalInputChange,
                    onSendTerminalInput = viewModel::sendTerminalInput,
                    onSendQuickCommand = viewModel::sendQuickCommand,
                    onDismissMessage = viewModel::dismissMessage,
                )
            }
        }
    }

    internal fun populatePrivateKeyForTesting(privateKey: String) {
        viewModel.onPrivateKeyChange(privateKey)
    }

    internal fun connectForTesting() {
        viewModel.connect()
    }

    internal fun sendTerminalInputForTesting(input: String) {
        viewModel.onTerminalInputChange(input)
        viewModel.sendTerminalInput()
    }

    internal fun currentUiStateForTesting(): VoiceSshUiState {
        return viewModel.uiState.value
    }

    private fun applyDebugPrefill(intent: Intent) {
        intent.getStringExtra(EXTRA_DEBUG_HOST)?.let(viewModel::onHostChange)
        intent.getStringExtra(EXTRA_DEBUG_PORT)?.let(viewModel::onPortChange)
        intent.getStringExtra(EXTRA_DEBUG_USERNAME)?.let(viewModel::onUsernameChange)
        intent.getStringExtra(EXTRA_DEBUG_AUTH_MODE)
            ?.let { value -> AuthMode.entries.firstOrNull { it.name == value } }
            ?.let(viewModel::onAuthModeChange)
        intent.getStringExtra(EXTRA_DEBUG_PRIVATE_KEY)?.let(viewModel::onPrivateKeyChange)
    }

    private fun launchSpeechRecognizer(
        launch: (Intent) -> Unit,
        onUnavailable: (String) -> Unit,
    ) {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your prompt")
        }

        try {
            launch(intent)
        } catch (_: ActivityNotFoundException) {
            onUnavailable("No speech recognition service is available on this device.")
        }
    }

    companion object {
        const val EXTRA_DEBUG_AUTH_MODE = "com.apple101012.voicessh.extra.DEBUG_AUTH_MODE"
        const val EXTRA_DEBUG_HOST = "com.apple101012.voicessh.extra.DEBUG_HOST"
        const val EXTRA_DEBUG_START_ON_TERMINAL = "com.apple101012.voicessh.extra.DEBUG_START_ON_TERMINAL"
        const val EXTRA_DEBUG_PORT = "com.apple101012.voicessh.extra.DEBUG_PORT"
        const val EXTRA_DEBUG_PRIVATE_KEY = "com.apple101012.voicessh.extra.DEBUG_PRIVATE_KEY"
        const val EXTRA_DEBUG_USERNAME = "com.apple101012.voicessh.extra.DEBUG_USERNAME"
    }
}
