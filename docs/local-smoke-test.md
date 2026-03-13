# Local Smoke Test

This app now has a repeatable emulator smoke test for the SSH flow. The current build uses a simplified single-screen testing layout so the connection profile, terminal output, and prompt input are all visible in one vertical flow.

The smoke test proves:

- the Terminal tab opens
- SSH key auth connects from the Android emulator to a loopback-only Windows SSH server
- sending `codex` through the app reaches the shell
- screenshots are captured during the run

## Fastest Run

1. Start an Android emulator.
2. From the repo root, run `.\scripts\run-local-smoke-test.ps1`.
3. Review the screenshots in `artifacts\local-smoke\local-ssh-smoke`.

The script will:

- start the local Windows SSH test server on `127.0.0.1:2222`
- use emulator host `10.0.2.2`
- install the debug app and androidTest APK
- run `LocalSshSmokeTest`
- send the command `codex`
- pull screenshots back into the repo workspace

## Manual Emulator Test

Use this when you want to verify the UI yourself instead of running the smoke script.

1. Run `.\scripts\start-local-test-sshd.ps1`.
2. Install the debug APK from `app\build\outputs\apk\debug\app-debug.apk`, or run `.\gradlew.bat installDebug`.
3. Open the app and switch to the `Terminal` tab.
4. Tap `SSH Key`.
5. Tap `Use Emulator Host`.
6. Enter:
   - Host: `10.0.2.2`
   - Port: `2222`
   - Username: `Apple`
7. Load the private key from `%LOCALAPPDATA%\VoiceSshApp\local-sshd\client_rsa`, or paste that file's contents into the private key field.
8. Tap `Connect`.
9. Confirm the terminal output shows `Connected to Apple@10.0.2.2:2222.`
10. In `Manual terminal input`, enter `codex`.
11. Tap `Send Command`.
12. Confirm `codex` appears in the terminal output.

## Manual Phone Test

The same UI flow works on a real Android phone, but `10.0.2.2` is emulator-only.

1. Keep using `SSH Key`.
2. Use your computer's reachable LAN address or your SSH tunnel address instead of `10.0.2.2`.
3. Use the same username and private key.
4. Connect and send `codex`.

## Notes

- The automated smoke test uses debug-only intent extras to prefill host, port, username, auth mode, and the SSH key.
- Those debug extras are ignored outside debug builds.
- No private key or personal IP is written into the public repo.
