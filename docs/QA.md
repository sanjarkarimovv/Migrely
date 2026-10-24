# Migrely — Quality assurance

How Migrely's quality is checked before anything reaches Google Play. The requirements are in
`openspec/specs/quality-gates/spec.md`; the decisions behind them are BRD D40–D44.

## 1. Layers and what each one catches

| Layer | Where | Catches | Runs |
|---|---|---|---|
| Unit and architecture tests | each module's `test`, Konsist in `:app` | logic, mappers, Clean Architecture rules | the gate, every change |
| Instrumented tests | `:datasource:implementation`, `:designsystem` | SQL, migrations, design-system components | per module, every change |
| **End-to-end flows** | `.maestro/` (Maestro) | a person's journeys, navigation, persistence, regressions between changes | `qa/maestro.sh` |
| **Accessibility audit** | `qa/a11y_audit.sh` | contrast, touch targets, labels and keyboard reach on real pixels, at 100% and 200% font and in landscape | before a release, after UI changes |
| **Device scenarios** | `qa/device_scenarios.sh` | the clock, time zones, midnight, process death | before a release, after time or lifecycle changes |
| **Robustness and privacy** | `qa/monkey.sh`, `qa/permission_audit.sh` | crashes and ANRs under random input, unexpected permissions (never `INTERNET`) | before a release |
| Exploratory testing | the QA session, by hand and through the Maestro MCP server | everything a script did not think of | every change |

Visual regression (Roborazzi screenshots of the `@Preview`s with pseudo-locales) comes in its own QA change (D42).

## 2. Devices and sessions

- **Dev and QA sessions.** The dev session works in the main checkout on `emulator-5570`. The QA session works in the
  worktree `migraine-app-qa` (branch `feature/add-quality-automation` until it merges).
- **QA emulators.** The QA session uses `emulator-5580` (AVD `Migrely_QA_API_36`) and `emulator-5582` (AVD
  `Migrely_QA_API_26`, Android 8.0, the minimum). Every script refuses other devices, because flows clear the app's
  data.
- **Starting an emulator** (one QA emulator at a time on the shared host):

  ```bash
  ~/Library/Android/sdk/emulator/emulator -avd Migrely_QA_API_36 -port 5580 \
    -gpu swiftshader_indirect -no-snapshot-save -no-boot-anim -no-audio -no-window
  ```
- **Restoring the device.** Scripts restore every device setting they change: font size, rotation, accessibility
  timeout, clock, automatic time and time zone.

## 3. Writing a flow

`.maestro/README.md` has the conventions and the timing rules for the software-GPU emulator. In short:
- one folder per capability;
- `# Covers: <capability> › <requirement> › <scenario>` lines for traceability;
- select controls by visible text or spoken label, never by a test-only tag;
- start from `subflows/launch-clean.yaml`;
- run every new flow three times before keeping it.

`python3 -I qa/e2e_coverage.py --missing` shows the spec scenarios that have no flow yet.

## 4. Bugs

| Severity | Meaning | Release |
|---|---|---|
| S1 critical | Crash, data loss, privacy breach or a blocked core flow | Blocks |
| S2 major | A core flow gives a wrong result or stores wrong data; a workaround exists | Blocks |
| S3 minor | A non-core defect or a barrier with a workaround | Fix before production if cheap |
| S4 trivial | Cosmetic | When convenient |

- **Before filing.** Reproduce a defect at least twice and measure it (tap points, intervals, colours, timings) before
  filing it. A suspicion that the measurement disproves is not filed.
- **The issue.** It gets the labels `bug`, `qa` and `severity: …`, plus `accessibility` when it applies. It contains:
  - the steps;
  - the expected and actual results;
  - the build (commit);
  - the device;
  - the evidence;
  - the spec scenario it breaks.
- **Who fixes.** QA never edits product code. The dev session fixes the issue on its own branch.
- **Retest.** When the fix merges, QA merges develop into its branch and reruns the reproducing flows, the related
  audits and the full regression. It comments the results on the fix PR and removes `known-bug` tags that now pass.

## 5. Release Go/No-Go

A release candidate goes to a Play track only when every line passes (D44). Run them in this order and record the
results in the release PR.

| # | Check | Command | Pass when |
|---|---|---|---|
| 1 | Build gate | `./gradlew assembleDebug test ktlintCheck detekt` | BUILD SUCCESSFUL |
| 2 | Smoke on Android 8.0 | `QA_SERIAL=emulator-5582 qa/maestro.sh --install --tags smoke` | every flow passes |
| 3 | Full regression on the target version | `qa/maestro.sh --install` | every flow passes |
| 4 | Accessibility audit | `qa/a11y_audit.sh` | `0 failure(s)`; every warning looked at |
| 5 | Device scenarios | `qa/device_scenarios.sh` | `All scenarios passed` |
| 6 | Random input | `qa/monkey.sh 20261024 5000` | 0 crashes, 0 ANRs |
| 7 | Permissions | `qa/permission_audit.sh` (and on the release APK) | `Permission audit passed` |
| 8 | Open defects | `gh issue list --label qa --state open` | no S1 or S2 |
| 9 | Coverage report | `python3 -I qa/e2e_coverage.py` | exits 0 (no stale scenario names) |

From change 13 `prepare-release` on, these lines are added:

| # | Check | Pass when |
|---|---|---|
| 10 | R8 release build: lines 3 and 7 on the minified release APK | every flow passes, only allowed permissions |
| 11 | Play pre-launch report (internal testing track) | no crash, no critical accessibility finding |
| 12 | Internal testing with people for at least a week (BRD §14.1) | no new S1 or S2 |

Any failing line makes the candidate No-Go. The release PR names the failing line and the issue filed for it.
