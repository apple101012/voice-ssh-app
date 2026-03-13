package com.apple101012.voicessh

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
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
    private val viewModel by viewModels<VoiceSshViewModel> { VoiceSshViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                    onDraftChange = viewModel::onDraftChange,
                    onClearDraft = viewModel::clearDraft,
                    onSendDraft = viewModel::sendDraft,
                    onLaunchSpeech = launchSpeech,
                    onHostChange = viewModel::onHostChange,
                    onPortChange = viewModel::onPortChange,
                    onUsernameChange = viewModel::onUsernameChange,
                    onPasswordChange = viewModel::onPasswordChange,
                    onConnect = viewModel::connect,
                    onDisconnect = viewModel::disconnect,
                    onTerminalInputChange = viewModel::onTerminalInputChange,
                    onSendTerminalInput = viewModel::sendTerminalInput,
                    onDismissMessage = viewModel::dismissMessage,
                )
            }
        }
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
}
