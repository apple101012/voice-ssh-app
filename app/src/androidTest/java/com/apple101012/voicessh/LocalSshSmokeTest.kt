package com.apple101012.voicessh

import android.content.Intent
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
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
class LocalSshSmokeTest {
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
            "local-ssh-smoke",
        ).apply {
            mkdirs()
            listFiles()?.forEach(File::delete)
        }
    }

    @Test
    fun connectsToLocalLoopbackSshAndSendsCommand() {
        assumeTrue(
            "Pass voiceSshPrivateKeyBase64 to run this smoke test.",
            loadPrivateKey().isNotBlank(),
        )

        val host = runnerArgs.getString(ARG_HOST) ?: DEFAULT_HOST
        val port = runnerArgs.getString(ARG_PORT) ?: DEFAULT_PORT
        val username = runnerArgs.getString(ARG_USERNAME) ?: DEFAULT_USERNAME
        val command = runnerArgs.getString(ARG_COMMAND) ?: DEFAULT_COMMAND

        captureScreenshot("01-profile-ready")

        activityRule.scenario.onActivity { activity ->
            activity.connectForTesting()
        }
        try {
            composeRule.waitUntil(CONNECT_TIMEOUT_MS) {
                hasNodeWithText("Connected to $username@$host:$port.")
            }
        } catch (error: Throwable) {
            captureScreenshot("02-connect-failed")
            val uiState = currentUiState()
            throw AssertionError(
                "Connect hook did not reach Connected. " +
                    "status=${uiState.terminalSnapshot.status} " +
                    "message=${uiState.message} " +
                    "lastError=${uiState.terminalSnapshot.lastError}",
                error,
            )
        }
        assertTrue(hasNodeWithText("Connected to $username@$host:$port."))
        captureScreenshot("02-connected")

        activityRule.scenario.onActivity { activity ->
            activity.sendTerminalInputForTesting(command)
        }
        try {
            composeRule.waitUntil(COMMAND_TIMEOUT_MS) {
                currentUiState().terminalSnapshot.output.contains(command)
            }
        } catch (error: Throwable) {
            captureScreenshot("03-command-failed")
            val uiState = currentUiState()
            throw AssertionError(
                "Command hook did not reach terminal output. " +
                    "status=${uiState.terminalSnapshot.status} " +
                    "message=${uiState.message} " +
                    "lastError=${uiState.terminalSnapshot.lastError}",
                error,
            )
        }
        captureScreenshot("03-command-sent")

        assertTrue(screenshotDir.resolve("02-connected.png").exists())
        assertTrue(screenshotDir.resolve("03-command-sent.png").exists())
    }

    private fun hasNodeWithText(text: String): Boolean {
        return runCatching {
            composeRule.onAllNodes(hasText(text, substring = true))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }.getOrDefault(false)
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
        private const val ARG_COMMAND = "voiceSshCommand"
        private const val ARG_HOST = "voiceSshHost"
        private const val ARG_PORT = "voiceSshPort"
        private const val ARG_PRIVATE_KEY_BASE64 = "voiceSshPrivateKeyBase64"
        private const val ARG_USERNAME = "voiceSshUsername"
        private const val CONNECT_TIMEOUT_MS = 30_000L
        private const val COMMAND_TIMEOUT_MS = 10_000L
        private const val DEFAULT_COMMAND = "codex"
        private const val DEFAULT_HOST = "10.0.2.2"
        private const val DEFAULT_PORT = "2222"
        private const val DEFAULT_USERNAME = "Apple"

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
