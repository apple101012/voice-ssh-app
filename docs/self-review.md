# Voice SSH App Self-Review

## Captured
2026-03-13 00:07:52 -04:00

## Goal
Re-check the plan against the user's original request, score coverage, and identify anything that could still block a working Android APK.

## Scorecard
- Requirement capture: 9/10
- Feasibility of architecture: 8/10
- Testability planning: 9/10
- Risk coverage: 8/10
- Alignment with Android-only APK goal: 9/10
- Overall: 8.6/10

## What The Plan Covers Well
- Preserves the core two-tab flow: voice drafting plus terminal execution.
- Keeps both speech and manual typing.
- Uses SSH as the core transport to the computer or VPS.
- Treats testability as mandatory, not optional.
- Includes both emulator and real-phone validation.
- Creates a path to a public GitHub repo and implementation kickoff.

## Gaps Found During Review
- Exact terminal-rendering implementation is still undecided.
  - This is the main technical risk.
  - The plan now explicitly requires a spike before deeper feature work.
- Windows command prompt support and Linux/VPS support are not identical.
  - Windows should work through OpenSSH, but terminal behavior may be better against WSL or a VPS.
- Auth UX is not fully specified yet.
  - Need a clear MVP decision between password login, key import, or both.
- Speech testing on emulator may not match real-device behavior.
  - Real-phone verification remains mandatory.
- If the target CLI depends heavily on advanced terminal control, a simple transcript view will not be enough.
  - The fallback plan covers delivery, but not every future CLI UX edge case.

## Corrections Added After Review
- Chose a primary architecture:
  - built-in SSH session owned by the app
- Added a fallback architecture:
  - remote `tmux` session plus pane capture
- Added explicit Windows and Linux host plans.
- Added a phase 0 feasibility spike.
- Added an acceptance checklist tied directly to the original request.

## Re-Check Against Original Request
- Android only and APK-focused: yes
- Two tabs with top navigation: yes
- Voice prompt plus manual editing: yes
- Send prompt into terminal tab: yes
- SSH connection to computer or VPS: yes
- Testable on this Windows 11 setup: yes, with emulator and phone
- Big plan written into notes: yes
- Self-grading and gap review included: yes
- Public GitHub repo requested next: ready to do

## Remaining Questions To Resolve Early
- Which auth modes are in MVP:
  - password only
  - key only
  - both
- Does the first shipped version need a fully interactive terminal, or is a reliable send-and-view session enough?
- Is the main daily target the Windows PC, the VPS, or WSL on the Windows PC?

## Final Assessment
The plan is strong enough to start implementation, but only if terminal rendering is validated first. That spike determines whether the project ships as a true in-app terminal or as a reliable `tmux`-backed send-and-view MVP first.
