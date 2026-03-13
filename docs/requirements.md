# Voice SSH App Requirements

## Captured
2026-03-13 00:07:52 -04:00

## Context
This expands the existing note in `app ideas/app-ideas.md` for the Android SSH coding app idea. The goal is to capture the user's current vision in one place before implementation starts.

## Product Vision
Build an Android-only app that lets the user speak or type a prompt on their phone, then send that prompt into a terminal session connected to their computer or VPS over SSH so they can work with Codex and other CLIs from the phone.

## Required UX
- Top navigation with two tabs:
  - `Voice Prompt`
  - `Terminal`
- `Voice Prompt` tab:
  - microphone action for speech-to-text
  - editable text area so the transcript can be corrected before sending
  - ability to type instead of speaking
  - clear send action such as `Send to Terminal`
- `Terminal` tab:
  - SSH connection associated with the computer or VPS
  - visible terminal/session output so the user can see what is happening
  - connection controls and status
- Fast flow:
  - speak prompt
  - review/edit prompt
  - tap send
  - prompt appears in the terminal session on the other tab and runs

## User Intent To Preserve
- The app should feel like coding by voice from the phone.
- The right-side or second tab is the terminal tied to the remote machine.
- The left-side or first tab is where the user drafts the voice prompt.
- The app should make prompt transfer easier than manual copy/paste between separate apps.
- The main target is Android phones, distributed as an APK.

## Constraints
- Android only.
- Windows 11 development machine.
- The user already has SSH workflows to a VPS and to their computer.
- The user can test on both an Android emulator and a real Android phone.
- The project must stay testable end to end; avoid designs that cannot be validated locally.
- It is acceptable to install tools required for development and testing.

## Delivery Expectations
- Create a large implementation plan in the notes repo.
- Re-evaluate that plan against the original request.
- Grade the plan and look for gaps that were missed.
- Create a public GitHub repo for the project after planning.
- Repo naming direction:
  - repository slug: `voice-ssh-app`
  - app/display name: `Voice SSH App`

## Success Criteria
- User can open the APK on Android.
- User can connect to a saved SSH profile.
- User can capture speech or type text in the prompt tab.
- User can send the prompt into the terminal session with one tap.
- User can verify the result from the terminal tab.
- Core behavior is covered by automated tests plus manual phone verification.
