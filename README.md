# Voice SSH App

Android app concept for speaking or typing prompts on a phone and sending them into an SSH-connected terminal session on a computer or VPS.

## Current Status

First working milestone is implemented.

## Core Product Goal

- Android-only APK
- Two main tabs:
  - `Voice Prompt`
  - `Terminal`
- Speak or type a prompt on the phone
- Send that prompt into an SSH-connected terminal session
- Make the workflow faster than manual copy/paste between separate apps

## Current Milestone

- Jetpack Compose app with `Voice Prompt` and `Terminal` tabs
- Speech-to-text via Android `RecognizerIntent`
- Manual prompt editing and `Send to Terminal`
- SSH shell connection over password auth or pasted private-key auth using JSch
- Live terminal output panel plus manual terminal input
- Emulator-host shortcut for `10.0.2.2`
- Emulator-tested debug APK build

## Test Status

- `./gradlew.bat testDebugUnitTest`
- `./gradlew.bat assembleDebug`
- `./gradlew.bat connectedDebugAndroidTest`
- `./gradlew.bat installDebug`

The JVM test suite includes real SSH integration tests for both password auth and SSH key auth using an embedded SSH server. The Android instrumentation suite covers the Compose tab flow, password mode, and SSH-key mode rendering.

## APK

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## Local Windows SSH Test

- Start a loopback-only Windows SSH test server with `./scripts/start-local-test-sshd.ps1`
- Stop it with `./scripts/stop-local-test-sshd.ps1`
- The script generates host/client test keys under `%LOCALAPPDATA%\VoiceSshApp\local-sshd`
- Nothing in that directory is committed to the repo
- When testing from the Android emulator on the same Windows machine, use host `10.0.2.2`

## Known Limits In This Milestone

- Host key checking is currently disabled for speed while the flow is being proven
- Private-key auth currently expects an unencrypted pasted key
- Terminal rendering is text-stream based, not a full terminal emulator yet

## Documents

- [Requirements](docs/requirements.md)
- [Implementation Plan](docs/implementation-plan.md)
- [Self-Review](docs/self-review.md)
