# Tasks

QA tooling only: no domain, presentation, data or UI code changes. The groups follow the order the checks depend on:
requirements first, then the end-to-end base, the checks built on it, the docs, and the verify gate.

## 1. Requirements (BRD)

- [x] 1.1 BRD §13: add row 2b `add-quality-automation` after 2a. Verify: `grep -n "| 2b |" docs/BRD.md`
- [x] 1.2 BRD §17: add D40–D44 (QA process and bug flow, Maestro E2E and traceability, accessibility audit on pixels,
  device scenarios through adb, robustness and privacy checks with the Go/No-Go gate). Verify:
  `grep -c "^| D4[0-4] " docs/BRD.md` prints 5

## 2. End-to-end base

- [ ] 2.1 `qa/maestro.sh`: refuse any device but `emulator-5580`, analytics off, `--tags`, `--install`, JUnit report
  in `build/qa/maestro/<run>/`. Verify: `ANDROID_SERIAL=emulator-5570 qa/maestro.sh` exits 2 without running a flow
- [ ] 2.2 `.maestro/config.yaml`, `README.md` and subflows (`launch-clean`, `start-attack` and `end-attack` with the
  TapGuard wait, `wait-message-gone`, `log-attack-today`, `add-past-attack-last-month`). Verify:
  `qa/maestro.sh --tags smoke` passes
- [ ] 2.3 Flows for app-shell, attack-logging, attack-mode, today-sky, in-app-messages, past-attacks, attack-details,
  custom-tags and attack-history, each with `# Covers:` lines. Every new flow runs three times before it is kept.
  Verify: `qa/maestro.sh` passes; known-bug flows fail only where their issue is open
- [ ] 2.4 `qa/e2e_coverage.py` (per-capability summary, `--missing`, exit 1 on stale names). Verify:
  `python3 -I qa/e2e_coverage.py` exits 0; renaming a scenario locally makes it exit 1

## 3. Accessibility audit

- [ ] 3.1 `.maestro/a11y-tour/` states: home calm, home with attacks, attack mode with its message, details top and
  bottom, past screen, overlap card, custom tag dialog, History. Verify: each state flow passes alone
- [ ] 3.2 `qa/a11y_audit.py` (contrast, 48 dp, label, keyboard) and `qa/a11y_audit.sh` (font 100%, font 200%,
  landscape; settings restored on exit). Verify: on the develop @ 50ce583 captures it reports exactly #13 and #14;
  after the run, `settings get system font_scale` is 1.0

## 4. Device scenarios

- [ ] 4.1 `qa/device_scenarios.sh` with `long`, `history_death`, `past_death`, `details_kill`, `midnight` and `travel`,
  and the `.maestro/device/` steps. The clock, zone and automatic time are restored on exit and after each scenario.
  Verify: all scenarios pass except `travel` while #19 is open; `adb shell date` matches the host afterwards

## 5. Robustness and privacy

- [ ] 5.1 `qa/monkey.sh [seed] [events]`: a seeded run that fails on any crash or ANR of the app. Verify:
  `qa/monkey.sh 20261024 5000` reports 0 crashes and 0 ANRs
- [ ] 5.2 `qa/permission_audit.sh [apk]`: an allowlist, and it always fails on INTERNET. Verify: it passes on the
  debug APK and fails on a copy of the manifest list with INTERNET added (a dry run on a text fixture)

## 6. Minimum Android version

- [ ] 6.1 AVD `Migrely_QA_API_26` (android-26 default arm64, port 5582), and a `QA_SERIAL` override in `qa/maestro.sh`
  limited to `emulator-5580` and `emulator-5582`. Verify: `QA_SERIAL=emulator-5582 qa/maestro.sh --install --tags smoke`
  passes on Android 8.0

## 7. Documents

- [ ] 7.1 `docs/QA.md`:
  - the strategy (layers and what each catches);
  - the device and session rules;
  - the severity scale;
  - the bug and retest flow;
  - the Go/No-Go checklist with a command per line.

  Verify: every command in the checklist runs as written
- [ ] 7.2 `docs/ARCHITECTURE.md` §11 points to `docs/QA.md`, and README gets a short "Quality checks" section. Verify:
  `grep -n "docs/QA.md" docs/ARCHITECTURE.md README.md`

## 8. Retest of PR #18

- [ ] 8.1 On develop @ a8ba935:
  - #12: double-tap flows, plus adb trials at 0.05–0.7 s, cases A and B;
  - #13: audit contrast;
  - #14 and #17: audit keyboard and the Tab walk;
  - #15: strict elapsed check;
  - the full regression.

  Comment the results on PR #18 and file anything new. Verify: the PR comment exists

## 9. Verify

- [ ] 9.1 The gate `./gradlew assembleDebug test ktlintCheck detekt` passes on the branch (no product code changed;
  this proves the merge with develop is clean)
- [ ] 9.2 Release Go/No-Go dry run on the branch:
  - smoke on API 26 and API 36;
  - full regression;
  - accessibility audit;
  - device scenarios;
  - monkey;
  - permission audit.

  Record the results and anything not checked in a "Verification record" section below
- [ ] 9.3 `openspec validate add-quality-automation --strict` passes
