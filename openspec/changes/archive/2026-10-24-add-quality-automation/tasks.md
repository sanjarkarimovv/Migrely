# Tasks

QA tooling only: no domain, presentation, data or UI code changes. The groups follow the order the checks depend on:
requirements first, then the end-to-end base, the checks built on it, the docs, and the verify gate.

## 1. Requirements (BRD)

- [x] 1.1 BRD §13: add row 2b `add-quality-automation` after 2a. Verify: `grep -n "| 2b |" docs/BRD.md`
- [x] 1.2 BRD §17: add D40–D44 (QA process and bug flow, Maestro E2E and traceability, accessibility audit on pixels,
  device scenarios through adb, robustness and privacy checks with the Go/No-Go gate). Verify:
  `grep -c "^| D4[0-4] " docs/BRD.md` prints 5

## 2. End-to-end base

- [x] 2.1 `qa/maestro.sh`: refuse any device but `emulator-5580`, analytics off, `--tags`, `--install`, JUnit report
  in `build/qa/maestro/<run>/`. Verify: `ANDROID_SERIAL=emulator-5570 qa/maestro.sh` exits 2 without running a flow
- [x] 2.2 `.maestro/config.yaml`, `README.md` and subflows (`launch-clean`, `start-attack` and `end-attack` with the
  TapGuard wait, `wait-message-gone`, `log-attack-today`, `add-past-attack-last-month`). Verify:
  `qa/maestro.sh --tags smoke` passes
- [x] 2.3 Flows for app-shell, attack-logging, attack-mode, today-sky, in-app-messages, past-attacks, attack-details,
  custom-tags and attack-history, each with `# Covers:` lines. Every new flow runs three times before it is kept.
  Verify: `qa/maestro.sh` passes; known-bug flows fail only where their issue is open
- [x] 2.4 `qa/e2e_coverage.py` (per-capability summary, `--missing`, exit 1 on stale names). Verify:
  `python3 -I qa/e2e_coverage.py` exits 0; renaming a scenario locally makes it exit 1

## 3. Accessibility audit

- [x] 3.1 `.maestro/a11y-tour/` states: home calm, home with attacks, attack mode with its message, details top and
  bottom, past screen, overlap card, custom tag dialog, History. Verify: each state flow passes alone
- [x] 3.2 `qa/a11y_audit.py` (contrast, 48 dp, label, keyboard) and `qa/a11y_audit.sh` (font 100%, font 200%,
  landscape; settings restored on exit). Verify: on the develop @ 50ce583 captures it reports exactly #13 and #14;
  after the run, `settings get system font_scale` is 1.0

## 4. Device scenarios

- [x] 4.1 `qa/device_scenarios.sh` with `long`, `history_death`, `past_death`, `details_kill`, `midnight` and `travel`,
  and the `.maestro/device/` steps. The clock, zone and automatic time are restored on exit and after each scenario.
  Verify: all scenarios pass except `travel` while #19 is open; `adb shell date` matches the host afterwards

## 5. Robustness and privacy

- [x] 5.1 `qa/monkey.sh [seed] [events]`: a seeded run that fails on any crash or ANR of the app. Verify:
  `qa/monkey.sh 20261024 5000` reports 0 crashes and 0 ANRs
- [x] 5.2 `qa/permission_audit.sh [apk]`: an allowlist, and it always fails on INTERNET. Verify: it passes on the
  debug APK and fails on a copy of the manifest list with INTERNET added (a dry run on a text fixture)

## 6. Minimum Android version

- [x] 6.1 AVD `Migrely_QA_API_26` (android-26 default arm64, port 5582), and a `QA_SERIAL` override in `qa/maestro.sh`
  limited to `emulator-5580` and `emulator-5582`. Verify: `QA_SERIAL=emulator-5582 qa/maestro.sh --install --tags smoke`
  passes on Android 8.0

## 7. Documents

- [x] 7.1 `docs/QA.md`:
  - the strategy (layers and what each catches);
  - the device and session rules;
  - the severity scale;
  - the bug and retest flow;
  - the Go/No-Go checklist with a command per line.

  Verify: every command in the checklist runs as written
- [x] 7.2 `docs/ARCHITECTURE.md` §11 points to `docs/QA.md`, and README gets a short "Quality checks" section. Verify:
  `grep -n "docs/QA.md" docs/ARCHITECTURE.md README.md`

## 8. Retest of PR #18

- [x] 8.1 On develop @ a8ba935:
  - #12: double-tap flows, plus adb trials at 0.05–0.7 s, cases A and B;
  - #13: audit contrast;
  - #14 and #17: audit keyboard rule and a Tab walk with Enter;
  - #15: strict elapsed check;
  - the full regression.

  Comment the results on PR #18 and file anything new. Verify: the PR comment exists

## 9. Verify

- [x] 9.1 The gate `./gradlew assembleDebug test ktlintCheck detekt` passes on the branch (no product code changed;
  this proves the merge with develop is clean)
- [x] 9.2 Release Go/No-Go dry run on the branch:
  - smoke on API 26 and API 36;
  - full regression;
  - accessibility audit;
  - device scenarios;
  - monkey;
  - permission audit.

  Record the results and anything not checked in a "Verification record" section below
- [x] 9.3 `openspec validate add-quality-automation --strict` passes

## 10. Review findings

- [x] 10.1 An independent review (superpowers:code-reviewer, read-only, it did not touch the device) found 2 critical,
  12 important and several minor issues. Each one was checked and the real ones were fixed in 19692fa:
  - critical: `device_scenarios.sh` scenarios ran inside `||`, so `set -e` ignored failing steps. Every step now ends
    with `|| return 1`, and a kill that leaves the process alive fails;
  - critical: the audit's clipping rule also skipped nodes touching a container's sides, which is every list row. Now
    only the scroll axis counts. Re-checked: home rows, the month card and 24/24 History days are judged and focusable;
  - large text is measured on the glyphs (≥ 22 dp), not on the node height. The audit lists what it does not judge:
    cut-off nodes, and text inside merged nodes;
  - every restore runs with `set +e`; the lock is atomic and released only by its owner; the audit locks before it
    reads settings, rejects empty dumps and reads the density in effect;
  - `maestro.sh`:
    - `--install` always builds;
    - tags match exactly;
    - `known-bug` flows run in a third pass that does not decide the exit status;
    - `geometry` flows are skipped below API 29;
    - rotation is restored;
  - `monkey.sh` catches native crashes and `am_anr`, and restores quick settings (Do Not Disturb included: a 12:45 run
    had left it on); `permission_audit.sh` reads `uses-permission-sdk-23`; `e2e_coverage.py` fails on malformed lines;
  - flows:
    - `doubleTapOn` replaces two `tapOn` steps that were 2.1 s apart under load;
    - the disabled month buttons compare the title;
    - `add-for-empty-day` checks the chosen day;
    - the Undo flows run with the long timeout;
    - `onFlowComplete` restores portrait;
    - `Covers` lines claim only what is asserted.
- [x] 10.2 Not changed, and why:
  - `swipe-dismiss` after the long timeout keeps its 3 s wait: the message cannot time out on its own any more.
  - `custom-tags`: the duplicate check uses `index: 1` and stays. Duplicates are also rejected in the domain unit tests.
  - The first 2 h after midnight on the 1st moves `add-past-attack-last-month` two months back. This is a known window
    and is written in its comment.

## Verification record

Builds tested:
- develop @ 50ce583 (phase 1);
- ce3d8cc (change 2);
- a8ba935 (PR #18);
- db80e79 (change 3), merged into this branch at e01cd7f.

Emulators:
- `Migrely_QA_API_36` (API 36, 1080×2400, 420 dpi, software GPU);
- `Migrely_QA_API_26` (Android 8.0).

| Check | Result on db80e79 (change 3) |
|---|---|
| Gate `./gradlew assembleDebug test ktlintCheck detekt` | BUILD SUCCESSFUL (795 tasks) |
| Regression, pass 1 (27 flows) | 26/27 at first. `note-saved-on-back` failed only because change 3 pushed the note counter below the fold (the note was saved). After a scroll was added: 3/3 |
| Regression, pass 2 (7 flows, 20 s accessibility timeout) | 7/7 |
| Regression, pass 3 (known bugs) | `landscape-message-clear` still reproduces #20, as expected |
| Smoke on Android 8.0 (API 26) | 11/11 + 3/3 (2 `geometry` flows skipped by design; no accessibility timeout setting on API 26, so pass 2 ran with 5 s) |
| Device scenarios | 7 scenarios: long, history_death, past_death, details_kill, midnight and double_tap pass. double_tap covers 0.15 / 0.4 / 0.7 s, lower and upper part, and the new "+ Medication taken". `travel` fails as expected on #19 |
| Upgrade with real data | Data logged on the database-v2 build (an attack last month; one today with Severe and Nausea), then change 3 (database v3) installed with `adb install -r`: everything kept, no crash (`device/upgrade-kept`) |
| Accessibility audit | 0 failures in 27 states (font 100%, 200%, landscape); 3 landscape states not capturable because of #20 |
| Monkey | API 36: seed 20261024, 5000 events: 0 crashes, 0 ANRs. API 26: seeds 20261024 and 777: 0/0 for the app; the AOSP keyboard of the image crashed 3 times, which is not the app |
| Permissions | debug APK: only the AndroidX receiver permission; a fixture with INTERNET (also as `uses-permission-sdk-23`) and CAMERA fails |
| Coverage | `qa/e2e_coverage.py` exit 0: 76 of 242 scenarios (change 3 added 30 new medication scenarios, which the next QA change will cover) |
| Retest of PR #18 | commented on the PR: #12 A 10/10, B 4/4; #13 #14 #15 #17 fixed |

Issues filed by QA:
- #12 S2, #13 S3, #14 S3, #15 S4, #17 S3: fixed in PR #18;
- #19 S3 (travel day attribution) and #20 S3 (landscape message covers controls): open, planned on
  `feature/fix-today-zone-and-landscape-message`;
- #22 S4 (about 30 Tab stops before "Migraine started"): product decision.

Suspicions that the measurements disproved, so nothing was filed for them:
- the end message inherits the start message's remaining time;
- the History "Undone." is too short;
- process death loses the History state;
- the snackbar sits under the attack card;
- the landscape controls are missing.

Not checked, and why:
- **TalkBack speech.** Labels were read from the accessibility tree; the screen reader was not driven.
- **Real hardware and cold-start time.** No physical phone is available.
- **R8 release build and Baseline Profile.** R8 is off until change 13.
- **Play pre-launch report.** It needs an uploaded AAB.
- **`upgrade` against a real previous release.** No release exists yet, so the scenario was run by hand from the
  database-v2 build.
- **Change 3 medication screens.** Next QA change.
