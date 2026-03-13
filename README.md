# Voice SSH App

Android app concept for speaking or typing prompts on a phone and sending them into an SSH-connected terminal session on a computer or VPS.

## Current Status

Planning repo. The first pass focuses on requirements capture, architecture, testability, and early risk reduction before implementation starts.

## Core Product Goal

- Android-only APK
- Two main tabs:
  - `Voice Prompt`
  - `Terminal`
- Speak or type a prompt on the phone
- Send that prompt into an SSH-connected terminal session
- Make the workflow faster than manual copy/paste between separate apps

## Documents

- [Requirements](docs/requirements.md)
- [Implementation Plan](docs/implementation-plan.md)
- [Self-Review](docs/self-review.md)

## Recommended Build Direction

- Native Android with Kotlin and Jetpack Compose
- In-app SSH terminal session as the primary workflow
- Testable architecture with fake speech and SSH providers
- Terminal-rendering spike first, with a `tmux` fallback plan if needed

## Next Step

Run the phase 0 feasibility spike to validate terminal rendering, SSH session control, and the emulator/device testing path.
