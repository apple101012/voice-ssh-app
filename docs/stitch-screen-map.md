# Stitch Screen Map (Navy Direction)

This app UI pass maps to Stitch project `12924080037052556185`.

## Primary Navy screens used

- `a400d7d8749b4b0a955739626bee4ec2` - **Navy Voice Prompt Composer**
  - Implemented in `PromptTab` in `VoiceSshScreen.kt`.
- `30479d91e1214da0b8a98e95896bf4c1` - **Navy Terminal Session Manager**
  - Implemented in `SessionManagerView` in `VoiceSshScreen.kt`.
- `cf8c8a8efd934f0f8d8c0062b576c47a` - **Navy Connected Terminal Workspace**
  - Implemented in `ConnectedWorkspaceView` and `TerminalWorkspaceCard`.

## Implementation notes

- Terminal tab now has explicit disconnected and connected layouts.
- Saved sessions are presented as a dedicated section with quick-connect actions.
- Once connected, the terminal workspace remains the primary visible surface.
- Existing test tags and SSH behavior are preserved for automated test coverage.
