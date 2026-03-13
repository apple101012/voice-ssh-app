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
- SSH shell connection over password auth using JSch
- Live terminal output panel plus manual terminal input
- Emulator-tested debug APK build

## Test Status

- `./gradlew.bat testDebugUnitTest`
- `./gradlew.bat assembleDebug`
- `./gradlew.bat connectedDebugAndroidTest`
- `./gradlew.bat installDebug`

The JVM test suite includes a real SSH integration test using an embedded SSH server. The Android instrumentation suite covers the Compose tab flow and disabled/enabled send behavior.

## APK

- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`

## Known Limits In This Milestone

- Password auth only
- Host key checking is currently disabled for speed while the flow is being proven
- Terminal rendering is text-stream based, not a full terminal emulator yet

## Documents

- [Requirements](docs/requirements.md)
- [Implementation Plan](docs/implementation-plan.md)
- [Self-Review](docs/self-review.md)
