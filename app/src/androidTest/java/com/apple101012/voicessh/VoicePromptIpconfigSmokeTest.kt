package com.apple101012.voicessh

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import java.io.File
import java.util.Base64
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoicePromptIpconfigSmokeTest {
    private val activityRule = ActivityScenarioRule<MainActivity>(buildLaunchIntent())

    @get:Rule
    val composeRule = AndroidComposeTestRule(
        activityRule,
        ::getActivity,
    )

    private val instrumentation
        get() = InstrumentationRegistry.getInstrumentation()
    private val runnerArgs
        get() = InstrumentationRegistry.getArguments()
    private val device
        get() = UiDevice.getInstance(instrumentation)
    private lateinit var screenshotDir: File

    @Before
    fun setUp() {
        screenshotDir = File(
            instrumentation.targetContext.getExternalFilesDir("test-screenshots"),
            "prompt-command-smoke",
        ).apply {
            mkdirs()
            listFiles()?.forEach(File::delete)
        }
    }

    @Test
    fun typedPromptSendsIpconfigToTerminal() {
        assumeTrue(
            "Pass voiceSshPrivateKeyBase64 to run this smoke test.",
            loadPrivateKey().isNotBlank(),
        )

        val promptCommand = runnerArgs.getString(ARG_PROMPT_COMMAND) ?: DEFAULT_PROMPT_COMMAND
        val expectedMarker = runnerArgs.getString(ARG_EXPECTED_MARKER) ?: DEFAULT_EXPECTED_MARKER

        captureScreenshot("01-start-terminal")

        activityRule.scenario.onActivity { activity ->
            activity.connectForTesting()
        }
        composeRule.waitUntil(CONNECT_TIMEOUT_MS) {
            val snapshot = currentUiState().terminalSnapshot
            snapshot.status == ConnectionStatus.Connected &&
                snapshot.output.contains("Connected to")
        }
        composeRule.waitUntil(COMMAND_TIMEOUT_MS) {
            currentUiState().terminalSnapshot.output.contains(">")
        }
        composeRule.waitForIdle()
        captureScreenshot("02-connected")

        composeRule.onNodeWithTag("promptTab").performClick()
        composeRule.onNodeWithTag("draftField").performTextInput(promptCommand)
        composeRule.waitUntil(COMMAND_TIMEOUT_MS) {
            currentUiState().draft.contains(promptCommand)
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText(promptCommand, substring = true).assertIsDisplayed()
        captureScreenshot("03-prompt-typed-command")

        composeRule.onNodeWithTag("sendDraftButton").performClick()
        try {
            composeRule.waitUntil(COMMAND_TIMEOUT_MS) {
                currentUiState().terminalSnapshot.output.contains(expectedMarker)
            }
        } catch (error: Throwable) {
            captureScreenshot("03b-prompt-send-timeout")
            val outputTail = currentUiState().terminalSnapshot.output.takeLast(600)
            throw AssertionError(
                "Expected marker '$expectedMarker' not found after prompt send. " +
                    "Output tail:\n$outputTail",
                error,
            )
        }

        composeRule.onNodeWithText("Terminal").performClick()
        composeRule.onNodeWithTag("terminalOutput").assertIsDisplayed()
        composeRule.waitUntil(COMMAND_TIMEOUT_MS) {
            currentUiState().terminalSnapshot.output.contains(expectedMarker)
        }
        captureScreenshot("04-terminal-after-prompt-send")

        val terminalOutput = currentUiState().terminalSnapshot.output
        assertTrue(
            terminalOutput.contains(expectedMarker),
        )
    }

    private fun captureScreenshot(name: String) {
        instrumentation.waitForIdleSync()
        val screenshotFile = screenshotDir.resolve("$name.png")
        val captured = device.takeScreenshot(screenshotFile)
        assertTrue(captured)
    }

    private fun currentUiState(): VoiceSshUiState {
        lateinit var uiState: VoiceSshUiState
        activityRule.scenario.onActivity { activity ->
            uiState = activity.currentUiStateForTesting()
        }
        return uiState
    }

    companion object {
        private const val ARG_HOST = "voiceSshHost"
        private const val ARG_PORT = "voiceSshPort"
        private const val ARG_PRIVATE_KEY_BASE64 = "voiceSshPrivateKeyBase64"
        private const val ARG_PROMPT_COMMAND = "voiceSshPromptCommand"
        private const val ARG_EXPECTED_MARKER = "voiceSshExpectedMarker"
        private const val ARG_USERNAME = "voiceSshUsername"
        private const val CONNECT_TIMEOUT_MS = 30_000L
        private const val COMMAND_TIMEOUT_MS = 20_000L
        private const val DEFAULT_HOST = "10.0.2.2"
        private const val DEFAULT_PORT = "2222"
        private const val DEFAULT_USERNAME = "Apple"
        private const val DEFAULT_PROMPT_COMMAND = "ver"
        private const val DEFAULT_EXPECTED_MARKER = "Microsoft Windows"

        private fun getActivity(rule: ActivityScenarioRule<MainActivity>): MainActivity {
            var activity: MainActivity? = null
            rule.scenario.onActivity { activity = it }
            return checkNotNull(activity)
        }

        private fun buildLaunchIntent(): Intent {
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val args = InstrumentationRegistry.getArguments()

            return Intent(
                instrumentation.targetContext,
                MainActivity::class.java,
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                putExtra(MainActivity.EXTRA_DEBUG_AUTH_MODE, AuthMode.SshKey.name)
                putExtra(MainActivity.EXTRA_DEBUG_HOST, args.getString(ARG_HOST) ?: DEFAULT_HOST)
                putExtra(MainActivity.EXTRA_DEBUG_PORT, args.getString(ARG_PORT) ?: DEFAULT_PORT)
                putExtra(MainActivity.EXTRA_DEBUG_USERNAME, args.getString(ARG_USERNAME) ?: DEFAULT_USERNAME)
                putExtra(MainActivity.EXTRA_DEBUG_PRIVATE_KEY, loadPrivateKey())
                putExtra(MainActivity.EXTRA_DEBUG_START_ON_TERMINAL, true)
            }
        }

        private fun loadPrivateKey(): String {
            val encodedPrivateKey = InstrumentationRegistry.getArguments()
                .getString(ARG_PRIVATE_KEY_BASE64)
                .orEmpty()
                .trim()
            if (encodedPrivateKey.isBlank()) {
                return ""
            }

            return runCatching {
                String(Base64.getDecoder().decode(encodedPrivateKey), Charsets.UTF_8)
            }.getOrDefault("")
        }
    }
}
