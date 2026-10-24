# Design

## Context

- **Two sessions.** A dev session builds the changes in the main checkout on `emulator-5570`. This QA session works in its
  own worktree (`migraine-app-qa`, branch `feature/add-quality-automation`) on `emulator-5580`.
- **Host.** One Mac (24 GB, 14 cores) shared with other projects' emulators. The emulators use the software GPU, so
  every UI step takes 1–4 s and any wait shorter than a few seconds is unreliable.
- **No CI.** GitHub Actions are blocked for billing, so every check runs locally.
- **The app's own constraints shape what a test may do:**
  - there is no `INTERNET` permission;
  - health data never leaves the device;
  - the 5-second undo window follows Android's accessibility timeout (EC-UX-16);
  - home ignores taps for 1–1.2 s after a mode switch (D32, the #12 fix).
- **No product code changes.** The QA change adds only tooling and documents. See proposal.md for the motivation.

## Goals / Non-Goals

**Goals:**
- Every check can be repeated with one command and leaves the device as it found it.
- A failing check tells a person where to look: the flow, the screenshot, the hierarchy, the measured values.
- The checks test the app as a person meets it: real pixels, the accessibility tree, the real clock and the real
  process lifecycle.

**Non-Goals:**
- Replacing the unit, Konsist and instrumented tests the dev changes already write (BRD §10 Test).
- Running on CI. The scripts can run there, but no CI exists now.

## Decisions

### D1. Maestro for end-to-end flows (D41)

| Option | Verdict |
|---|---|
| Espresso or Compose UI tests in `:app` `androidTest` | Needs test code and Hilt test wiring in the app. Running `:app` instrumented tests uninstalls the app (memory rule), and they are slow to write for whole journeys. Rejected |
| UI Automator scripts | Fine-grained, but verbose, and no flow format a reviewer can read. Rejected |
| **Maestro 2.11 YAML flows** | Black-box through the accessibility layer, readable, no app changes. Its MCP server lets the QA session explore by hand too |

- **Selectors.** Selectors use visible text first, then the spoken label (content description). A merged card is
  matched with a regex on its full label, for example `".*Calm so far today.*"`. `testTag` is not used. A control that
  cannot be found by its label is reported as an accessibility issue, as for #14 and #17.
- **Clean start.** Every flow starts with `launchApp: clearState: true`, through `subflows/launch-clean.yaml`. Data is
  built through the UI only, so the flows never depend on the database layout.
- **Device pin.** `qa/maestro.sh` refuses any `ANDROID_SERIAL` other than `emulator-5580` and turns Maestro analytics
  off (`MAESTRO_CLI_NO_ANALYTICS=1`).

**Timing rules learned on this emulator** (also in `.maestro/README.md`):

| Situation | Rule |
|---|---|
| Inside a 5 s undo window | Tap Undo as the very next step, with `waitToSettleTimeoutMs: 0` and `retryTapIfNoChange: false`. Check "Undone." (8 s) before anything else |
| After a mode switch | `subflows/start-attack.yaml` and `end-attack.yaml` end with `waitForAnimationToEnd: 1500`. This waits out the TapGuard (1–1.2 s), because the rain or the countdown line keeps the screen moving |
| A chip's checked state | `checked: false` and `enabled: false` do not match when the label sits on a child node. Use `assertNotVisible` with `checked: true`, or check the behaviour: a disabled button leaves the screen as it was |
| Process death | Maestro `killApp` is a force stop and clears saved state, so it is only used for "the app was closed". Process death runs in the device scenarios (D4) |
| A double tap | Two `tapOn: point` steps with no settle wait, at 88% of the height inside the old overlap. `doubleTapOn` at 87% passed while #12 still reproduced |

### D2. Traceability by comment, checked by a script (D41)

Every flow has one `# Covers: <capability> › <requirement> › <scenario>` line for each scenario it checks.
`qa/e2e_coverage.py`:
- reads `openspec/specs/*/spec.md`;
- matches the names after lower-casing and removing a trailing "(…)" note;
- prints the coverage per capability;
- exits 1 on a stale name.

A comment keeps the flow YAML plain Maestro. A custom key or a separate mapping file would drift from the flow it
describes.

The goal is not 100%. Scenarios that only a unit test can reach (DST arithmetic, storage full, ten years of data) stay
with the dev tests. The report lists them with `--missing`, so the choice is visible.

### D3. Accessibility audit on device pixels (D42)

| Option | Verdict |
|---|---|
| ATF through `enableAccessibilityChecks()` in Compose tests | Needs test code. Reports differ on whether it is reliable under Robolectric. Rejected for now |
| Roborazzi accessibility add-on | Same Robolectric question, and the rendering is not the device's. Deferred with visual regression |
| **Capture the real screen (screencap) and the accessibility tree (`uiautomator dump`), check them in Python** | Measures what a person sees. It found #13 and #14 before it existed as a script, and it reports exactly those two on the screens where they were found by hand |

`qa/a11y_audit.py` uses only the Python standard library: it decodes the PNG itself.

| Check | Rule |
|---|---|
| Contrast | The text colour is the glyph core, the pixel with the highest contrast inside the node. The background is the most common colour inside the node. Normal text needs 4.5:1. A single-line node at least 32 dp high counts as large text: 3–4.5:1 is a warning there, under 3:1 a failure. Disabled nodes are exempt (WCAG 1.4.3) |
| Target size | Every enabled clickable node is at least 48 × 48 dp |
| Label | Every enabled clickable node has a text or a spoken label on itself or a descendant |
| Keyboard | Every enabled clickable node is focusable |

`qa/a11y_audit.sh` runs each `.maestro/a11y-tour/*.yaml` state three times:
- font 1.0 portrait;
- font 2.0 portrait;
- font 1.0 landscape.

It sets a 30 s accessibility timeout, so messages are still on screen at capture, and restores every setting in a
`trap … EXIT`.

### D4. Device scenarios through adb between flows (D43)

`qa/device_scenarios.sh` runs short flows from `.maestro/device/`. Between them, adb does what only the device can do:

| Need | How |
|---|---|
| Time passes | `cmd alarm set-time <ms>` with automatic time off. No root is needed on the API 36 image |
| Travel | `cmd alarm set-timezone <zone>` |
| The system ends the app | HOME, then `run-as com.migrely.app kill -9 <pid>`, then reopen with the launcher intent (`monkey -c android.intent.category.LAUNCHER 1`) so the task and its saved state come back |
| Killed while open | `run-as … kill -9` in the foreground, then reopen |

- **Midnight.** The midnight scenario sets the clock to 23:55 in the device's zone. If the setup runs past midnight, it
  reports INCONCLUSIVE instead of a false pass.
- **Clean state between scenarios.** After each scenario the zone and the real time are restored, so one scenario's
  clock never leaks into the next. A `trap … EXIT` restores the clock, the zone and automatic time at the end.

### D5. Robustness and privacy checks (D44)

- **`qa/monkey.sh`:**
  - runs `monkey -p com.migrely.app -s <seed> --throttle 50 --pct-syskeys 0 -v 5000`;
  - clears logcat first, then fails on any `FATAL EXCEPTION`, `ANR in com.migrely.app` or crash-buffer line for the
    app's process;
  - takes the seed as an argument, so a failure can be replayed.
- **`qa/permission_audit.sh`:**
  - reads the APK with `aapt2 dump permissions`, from the newest installed build-tools;
  - fails on any permission outside the allowlist. Today the allowlist holds only the app's own
    `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, which AndroidX adds;
  - always fails on `android.permission.INTERNET`;
  - when a later change needs a permission (POST_NOTIFICATIONS for reminders, BILLING), that change adds it to the
    allowlist with its BRD reference.

### D6. Bugs, severities and retests (D40)

| Severity | Meaning | Release |
|---|---|---|
| S1 critical | Crash, data loss, privacy breach or a blocked core flow | Blocks |
| S2 major | A core flow gives a wrong result or stores wrong data; a workaround exists | Blocks |
| S3 minor | A non-core defect or a barrier with a workaround | Fix before production if cheap |
| S4 trivial | Cosmetic | When convenient |

- **Before filing,** an issue is reproduced at least twice and measured: tap points, intervals, colours, timings.
  Suspected defects that the measurement disproves are not filed. Three were disproved this way: the inherited undo
  time, the History "Undone." and process death on History.
- **QA never edits product code.** The dev session fixes issues on its own branch. When the fix merges, QA:
  - merges develop into its branch;
  - runs the reproducing flows, the related audits and the full regression;
  - comments the results on the fix PR.

### D7. Release Go/No-Go gate (D44)

`docs/QA.md` lists the gate as a checklist with the command for each line, in this order:
1. `./gradlew assembleDebug test ktlintCheck detekt`;
2. the smoke suite on API 26 and API 36;
3. the full regression on API 36;
4. `qa/a11y_audit.sh` with zero failures;
5. `qa/device_scenarios.sh` with all scenarios passing;
6. `qa/monkey.sh` with zero crashes and ANRs;
7. `qa/permission_audit.sh`;
8. no open S1 or S2 qa issue.

From change 13 on, two more lines apply: the R8 release-build smoke, and the Play pre-launch report with no crash and
no critical accessibility finding. A No-Go names the failing line.

### D8. Minimum Android version on a second QA emulator

`Migrely_QA_API_26` (`system-images;android-26;default;arm64-v8a`, port 5582) runs the smoke suite. The app has no Play
services dependency, so the image without Google APIs is enough. Only one QA emulator runs at a time, to keep the
shared host responsive.

## Risks / Trade-offs

- [The software GPU makes steps slow and timing-sensitive] → The timing rules in D1. Every new flow is run three times
  before it joins the suite.
- [Regex selectors on spoken labels break when copy changes] → A copy change is a spec change. The flow fails loudly
  and names the missing label, and the coverage report keeps the mapping honest.
- [The large-text rule in the audit is a height heuristic] → Between 3:1 and 4.5:1 on large text it only warns, so a
  person decides. Normal text fails strictly.
- [Date-dependent flows: an empty day this month, the 12th of last month] → They are written against the calendar, not
  fixed dates. `add-for-empty-day` needs today not to be the 1st and says so in a comment.
- [Clock and zone changes could leak to later runs] → `trap … EXIT` plus a reset after every scenario. Every script
  prints what it restored.
- [Two sessions on one host] → Separate worktree, emulator ports and BRD number blocks (D40–D44 for QA, D33–D39 for
  change 3). All coordination goes through messages and issues.

## Migration Plan

Nothing to migrate: new files only, and BRD/doc additions. Rolling back means reverting the change's commits; the app
is unaffected.
