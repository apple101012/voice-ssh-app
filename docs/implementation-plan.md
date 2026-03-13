# Voice SSH App Implementation Plan

## Captured
2026-03-13 00:07:52 -04:00

## Context
This plan turns the Android SSH coding idea into a buildable, testable project. It prioritizes a working Android APK, reliable SSH transport, and a UX that keeps voice drafting and terminal execution tightly connected.

## Recommended Product Decision
- Build the app natively with Kotlin, Jetpack Compose, and Material 3.
- Treat the in-app `Terminal` tab as the primary SSH session instead of trying to paste into an external terminal app.
- Support both voice capture and manual text entry from day one.
- Make testability a first-class requirement by keeping speech and SSH behind interfaces that can be faked in tests.

## Why This Shape
- Native Android gives the cleanest path to APK builds, emulator support, device testing, and official documentation.
- Compose is the current Android-first UI path and matches a tabbed app well.
- Owning the terminal session inside the app is more reliable than trying to inject text into another Android app or a random desktop terminal window.
- The app can still target the same remote hosts the user already uses: Windows PC over OpenSSH and Linux VPS over SSH.

## Planned App Structure

### Screen Model
- `Voice Prompt` tab
  - editable prompt field
  - microphone start/stop action
  - last transcript preview
  - `Send to Terminal` button
  - optional prompt history later
- `Terminal` tab
  - host/profile selector
  - connect/disconnect button
  - terminal output area
  - optional manual command input
  - session status and error state
- `Settings` sheet or screen
  - saved hosts
  - auth method
  - newline/send behavior
  - speech settings

### Core Layers
- `ui`
  - Compose screens, state holders, tab navigation
- `domain`
  - send-prompt use case
  - connect/disconnect use cases
  - validation and prompt formatting rules
- `data.ssh`
  - SSH session manager
  - host profile persistence
  - output stream adapter
- `data.speech`
  - speech recognition adapter
  - typed-input fallback
- `data.storage`
  - preferences/data store
  - secure secret/key references

## Technical Architecture

### SSH Session Strategy
- Primary plan:
  - the app opens and owns one live SSH session per selected host
  - the terminal tab renders that session
  - sending from the voice tab writes the prompt plus newline into the active SSH stream
- Why:
  - works for Windows OpenSSH and Linux/VPS targets
  - avoids clipboard hacks
  - aligns with the user's "two tabs in one app" workflow

### Terminal Rendering Risk
- Highest-risk technical area:
  - rendering an interactive shell well on Android
- Decision path:
  - spike full SSH shell rendering first
  - if shell rendering is stable enough, keep the built-in terminal as the main UX
  - if shell rendering is not good enough for Codex CLI interaction, fallback MVP becomes:
    - send text into a remote `tmux` session
    - show captured pane output in the terminal tab
    - keep full interactive terminal support as phase 2
- Reason for the fallback:
  - it stays testable
  - it still satisfies the core send-by-voice workflow
  - it reduces risk from terminal-emulation complexity

### Speech Input Strategy
- Use Android speech recognition for voice capture.
- Keep a normal text editor available at all times.
- Treat speech as an input source, not a required dependency for the rest of the app.
- This keeps emulator tests practical because the app can still be exercised without live microphone input.

### Persistence And Security
- Store non-secret settings such as hosts, usernames, and UI preferences in DataStore.
- Store credentials or imported key references in Android-secure storage backed by the platform keystore where possible.
- Keep raw secrets out of logs and screenshots.
- Add explicit host-key verification behavior before beta.

## MVP Scope

### Included In MVP
- Android APK build
- two main tabs
- one saved SSH host profile
- connect/disconnect
- speech-to-text capture
- typed prompt editing
- send current prompt into active SSH session
- visible terminal output
- automated unit tests
- instrumented UI tests with fake speech/SSH providers
- manual emulator test path
- manual real-device test path

### Explicitly Deferred
- multiple simultaneous host sessions
- polished ANSI-perfect terminal emulation if the spike slips
- SFTP/file sync
- prompt templates/history sync
- biometric unlock
- desktop companion app

## Build Plan

### Phase 0: Feasibility Spike
- Create a minimal Android project.
- Prove a live SSH connection from Android emulator or device to:
  - Windows OpenSSH on the dev PC
  - Linux VPS or WSL Ubuntu
- Prove one of these rendering paths:
  - interactive shell terminal view
  - transcript/pane-capture fallback
- Exit condition:
  - prompt text can be programmatically sent and terminal output is visible

### Phase 1: Project Scaffold
- Initialize Android project with Kotlin and Compose.
- Set up package structure, linting, formatting, and basic CI checks.
- Create abstractions for:
  - speech service
  - SSH client/session
  - host profile storage
- Add baseline tests before feature wiring.

### Phase 2: Connection Layer
- Implement host profile creation and validation.
- Implement connect/disconnect state machine.
- Add failure states for:
  - auth error
  - timeout
  - host unreachable
  - host-key mismatch
- Make all session state observable to the UI.

### Phase 3: Voice Prompt Tab
- Add editable prompt field.
- Add microphone action and transcript insertion.
- Add clear and resend behavior.
- Add `Send to Terminal` action that targets the active session.
- Preserve draft text across rotation and process recreation.

### Phase 4: Terminal Tab
- Render terminal output area.
- Show connection status and selected host.
- Add reconnect and clear-output actions.
- If the full terminal spike succeeded, allow direct input from this tab too.
- If fallback mode is chosen, show the remote pane/transcript clearly and poll or stream updates.

### Phase 5: Reliability And Security
- Harden reconnection handling.
- Prevent duplicate sends on rapid tapping.
- Add secure storage for auth details.
- Add telemetry/logging only for safe, non-secret operational events.

### Phase 6: Packaging And Release
- Build signed debug APK for emulator and phone testing.
- Create a manual test checklist.
- Create release notes and install instructions.
- Prepare GitHub Actions or equivalent build automation if practical.

## Host Environment Plan

### Windows 11 Path
- Preferred:
  - enable Windows OpenSSH Server
  - use PowerShell or cmd as the remote shell
- Better developer path if terminal compatibility is weak:
  - run the CLI workload inside WSL
  - connect to WSL/Linux via SSH or use the VPS

### VPS/Linux Path
- Use SSH directly.
- If fallback mode is needed, use `tmux` as the stable target session for:
  - pane capture
  - send-keys style injection

## Testing Plan

### Automated Tests
- Unit tests for:
  - prompt validation
  - newline/send behavior
  - connection state transitions
  - host profile validation
  - prompt-to-session routing
- JVM integration tests for the SSH adapter using a controllable test double or embedded SSH server.
- Compose UI tests for:
  - tab switching
  - typing/editing prompts
  - send button behavior
  - connection state messaging
  - retry/error flows

### Manual Tests On Emulator
- Connect to a local test SSH endpoint.
- Verify typed prompts can be sent repeatedly.
- Verify app recovery across rotation, backgrounding, and reconnects.
- Use a fake speech provider for deterministic runs.

### Manual Tests On Real Phone
- Verify microphone capture quality and permissions.
- Verify send-to-terminal workflow over local network and over internet to VPS.
- Verify auth setup and host switching.
- Verify the APK works outside the IDE install path.

## Acceptance Checklist
- Two-tab workflow exists and is understandable.
- User can speak or type a prompt.
- User can edit the prompt before sending.
- User can connect to their machine or VPS with SSH.
- User can send the prompt from one tab into the terminal session in the other tab.
- User can observe the remote session output.
- The build is testable on Windows 11, emulator, and real Android hardware.

## Current Recommendation
- Start with the native Android app plus SSH session ownership model.
- Run the first technical spike around terminal rendering before committing to a final library choice.
- Keep the `tmux` fallback ready so the project stays deliverable even if full terminal emulation turns into a time sink.
