# Migrely — Quality assurance

How Migrely's quality is checked before anything reaches Google Play. The requirements are in
`openspec/specs/quality-gates/spec.md`; the decisions behind them are BRD D40–D44.

## 1. Layers and what each one catches

| Layer | Where | Catches | Runs |
|---|---|---|---|
| Unit and architecture tests | each module's `test`, Konsist in `:app` | logic, mappers, Clean Architecture rules | the gate, every change |
| Instrumented tests | `:datasource:implementation`, `:designsystem` | SQL, migrations, design-system components | per module, every change; on every release candidate in QA (D204) |
| **End-to-end flows** | `.maestro/` (Maestro) | a person's journeys, navigation, persistence, regressions between changes | `qa/maestro.sh` |
| **Accessibility audit** | `qa/a11y_audit.sh` | contrast, touch targets, labels and keyboard reach on real pixels, at 100% and 200% font and in landscape, on every screen (D203) | before a release, after UI changes |
| **Device scenarios** | `qa/device_scenarios.sh` | the clock, time zones, midnight, process death, exact double-tap gaps, upgrades with real data | before a release, after time, lifecycle or database changes |
| **Robustness and privacy** | `qa/monkey.sh`, `qa/permission_audit.sh` | crashes and ANRs under random input, unexpected permissions (never `INTERNET`) | before a release |
| **Split APKs** | `qa/split_apks.sh` | what Play installs from the bundle: language, density and ABI splits, in the profile's language (D117, D204) | every release candidate |
| Exploratory testing | the QA session, by hand and through the Maestro MCP server | everything a script did not think of | every change |

Visual regression (Roborazzi screenshots of the `@Preview`s with pseudo-locales) comes in its own QA change (D42).
The test levels, who owns each, and the risk left per capability are in `docs/qa/test-strategy.md` (D204).

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
  timeout, animator scale, app language, clock, automatic time and time zone. Each restored value is read back, and a
  value that did not take is reported as a warning. This happened when the emulator stopped answering under a host load
  of 56 and then crashed.
- **Baseline.** A QA emulator's baseline is in `qa/lib.sh` (`QA_BASELINE`): auto-rotate on, portrait, font 1.0, no
  accessibility timeout, default animations, automatic time on. A script warns when it starts away from the baseline,
  because it would restore the leftover value. After a crash, run `qa/reset_device.sh`.

## 3. Writing a flow

`.maestro/README.md` has the conventions and the timing rules for the software-GPU emulator. In short:
- one folder per capability;
- `# Covers: <capability> › <requirement> › <scenario>` lines for traceability;
- select controls by visible text or spoken label, never by a test-only tag;
- start from `subflows/launch-clean.yaml`;
- run every new flow three times before keeping it.

`qa/maestro.sh` runs three passes:
1. the default timing;
2. `a11y-timeout` flows with a 20 s accessibility timeout;
3. `known-bug` flows, whose failure is expected.

Only passes 1 and 2 decide the exit status, so an open known bug never hides a new regression. A known-bug flow that
starts passing is reported as "NOW PASSES".

`python3 -I qa/e2e_coverage.py --missing` shows the spec scenarios that have no flow yet. Before the QA of a change
that adds a capability is closed, each user-visible scenario of that capability has a flow or a device scenario. A
scenario without one is listed in the change's tasks with the reason and the follow-up (D61).

Behaviour that depends on time is checked by moving the device clock and logging through the UI (D60). Examples are
"Did it help?" 2–24 h after an intake, overuse over the last 30 days, and intakes that join an attack. The base time is
10:00 on the 15th of the current month (`day_millis` in `qa/device_scenarios.sh`). Writing to the debug database with
`run-as … sqlite3` is a fallback for data the UI cannot create in a reasonable time. Do it only with the app stopped
and on the matching schema version.

Keyboard behaviour is checked with real key events (D62). `qa/device_scenarios.sh` sends Tab, Shift+Tab
(`input keycombination 59 61`), the arrows and Enter through adb, and reads the labels under the focused node from
uiautomator. Maestro's `focused` selector cannot do this, because Compose keeps a control's label on a child of the
focused node. The scenarios are `keyboard_home`, `keyboard_history` and `keyboard_rtl`; the last one sets the app
language to Hebrew for right-to-left. The audit's keyboard rule still checks that every clickable, including each day
cell, is focusable for accessibility services. The Tab order is checked only by key events.

Reduce motion is the animator scale (`global animator_duration_scale 0`). `landscape_band` checks the landscape message
band with and without it. Like the rotation, the accessibility timeout and the app language, the scripts save the
scale and restore it on exit.

**The accessibility audit has a state for every screen (D203).** `.maestro/a11y-tour/` holds:
- home (calm, with attacks), attack mode with a message, a custom-tag dialog;
- attack details (top and bottom), History (first use and a month), the past-migraine screen and its overlap card;
- onboarding's first step, Settings, Warning signs, the privacy page (its top and its end), Theme;
- My medications, Reminders, Your data;
- the paywall, the report's period screen and Insights. These three need the simulated store and carry the tag
  `simulated-store`. On a release build the audit lists them as not run; if nothing else is left, it fails.
A change that adds a screen adds its state.

**`privacy_link`** checks the privacy policy link: a VIEW intent to migrely.health that a browser opens. With every
browser disabled for the user it then checks "No browser": Migrely stays in front without a crash, and the page shows
the address. The browsers are enabled again at the end, in `restore()` and by `qa/reset_device.sh`.

`# Covers:` lines may also sit above a scenario in `qa/device_scenarios.sh`, when the script itself makes the check. The
coverage report also reads the delta specs of open changes, so a change can cover the scenarios it adds.

Notifications are checked black-box (D64). `qa/device_scenarios.sh` works as follows:
- It sets the clock just before the reminder time, opens the app (which re-plans its work on every start), and runs
  the app's WorkManager jobs on demand (`cmd jobscheduler run -f -n androidx.work.systemjobscheduler`).
- It reads notifications from `dumpsys notification --noredact` and taps their actions in the open shade. A collapsed
  notification is first swiped open.
- It reads the planned time from `dumpsys jobscheduler` ("Run time: earliest=").
- Steps that must keep jobs and notifications never force-stop the app. Maestro's `stopApp`, and `launchApp` without
  `stopApp: false`, force-stop it, and Android then cancels the app's jobs and removes its notifications.
- Maestro's `launchApp` grants every permission unless told otherwise. So permission checks open the app through its
  launcher intent and reset POST_NOTIFICATIONS with `pm revoke` and `pm clear-permission-flags`.
- `reminder_replan` restarts the emulator to check that the planned work survives a reboot.

How deep each run goes (D63):
- **Every QA change:** its new and changed flows run 3 times, the device scenarios it touches run twice, and the
  smoke flows run.
- **The end of each QA cycle and every release candidate:** the full regression, every device scenario, the audit
  and monkey.
- **Edge cases:** each BRD edge case of the dev changes under test gets a flow, a device scenario, or a reason in the
  change's tasks.

`python3 -I qa/resource_names.py` fails when two modules define the same string resource name (#41). Android merges
the modules' resources, so one module would show the other's text. This happened once with Log medication's picker
title.

A fresh install opens onboarding (change 8). `subflows/launch-clean.yaml` skips its four steps with "Skip this step",
waiting out its 0.8 s tap guard.

Files through the system picker are checked by their bytes (D200).
- **Exports:** `backup_export` saves the JSON backup and the CSV to Download, pulls them with adb and checks them with
  `qa/backup_files.py`:
  - the format and schema version, epoch millis and the counts;
  - the BOM, CRLF and the header;
  - the separator per app language: "," in English, ";" in Uzbek and Russian;
  - a `'` before a note that starts with `=+-@`.
- **Imports:** `backup_import` writes broken, newer and older files from a real export and pushes them to Download. The
  picker opens on "Recent", so the flows find a file with the picker's search and pick it under its "Files…" heading.
- **Result messages:** they are read with uiautomator right after the step. The outcome is also checked on the data.
  How long a message stays is measured by `message_duration`. It taps the file through adb, takes screenshots back to
  back, and runs `qa/message_timing.py`, which is precise to about ±0.3 s (#56: 8 s, or 20 s with the accessibility
  timeouts).
- **Debug database:** when a result needs explaining, the debug build's database is read with `run-as … sqlite3`, read
  only. That is how #58 (a soft-deleted migraine blocking its own restore) was found.

**The build under test.** `QA_APK=<apk>` names the APK that `qa/maestro.sh --install` and the scenarios' reinstalls
use; without it, the worktree's own debug APK is used.
- **A release candidate:** it is built in a detached worktree, `../migraine-app-qa-rc`, at the candidate's commit. That
  worktree has no `keystore.properties`, so its R8 release APK is signed with the debug key (D102).
- **Building:** use `--no-daemon`. A Gradle daemon shared with another checkout once made KSP fail to resolve a new
  class.
- **The record:** a Go/No-Go is recorded in `docs/qa/go-no-go-<version>.md`.

**Developer options on the QA emulators stay on.**
- Turning them off, even with `settings delete global development_settings_enabled`, turns USB debugging off too, and
  adb goes offline (2026-10-27).
- The way back is the emulator's own screen: Build number seven times, then USB debugging.
- "Don't keep activities" is set with `always_finish_activities` and its switch in Developer options. The setting alone
  does not take effect.

**Premium is checked through the simulated store (D201).** Debug builds hold a simulated Google Play. The paywall's
products, its sheet ("Complete the purchase", "Pay later (pending)", "Cancel") and its subscriptions page are the
app's own screens. QA taps through them (`subflows/buy.yaml`, `device/premium-buy.yaml`).
- **Store states:** `qa_store <command>` sets what the store answers through an adb broadcast:
  - available, unavailable, unreachable;
  - no-products (D122): billing connects and reads what is owned, but no product is offered, as Google Play does
    before the products are set up. `premium_no_products` checks it;
  - complete-pending and decline-pending, for a payment settled "at a shop";
  - reset, which empties the store, as Google Play no longer reporting a purchase after a refund. Premium already
    shown stays until the app asks the store again, after 15 minutes or on "Restore purchases".
  - Maestro's `clearState` empties the store too; "Delete all data" does not.
- **Rules that depend on time** use the emulator clock: 3 days offline, the 7 days between self-shown paywalls, and the
  day before a trial ends. The app asks the store again for Premium when its last answer is over 15 minutes old
  (`CHECK_INTERVAL`) or on "Restore purchases". It keeps the store's products for an hour before a paywall shows itself.
  So every time step force-stops it, and a step that needs a fresh Premium answer moves the clock 20 minutes on.
- **The paywall after onboarding:** every flow that finishes onboarding closes it (`subflows/close-onboarding-paywall.yaml`);
  `onboarding/paywall-after-onboarding.yaml` checks it.
- **Release builds have no simulated store:** `qa/release_apk.py <release.apk> <debug.apk>` checks that, with the debug
  APK as a control.
- **The entitlement cache:** `qa/backup_rules.py <apk> migrely_entitlement.xml` reads the backup rules from the APK and
  checks that every rule leaves the cache out (D115).
- **Only on a phone:** a real purchase, Play's grace-period message and a backup to a new phone. They are checked on
  the internal testing track (see the Go/No-Go record).

**Reports and Insights get their data from seeds (D202).**
- **Seeds:** `qa/seed_backup.py <seed> <out.json> --today <emulator date>` writes a v6 backup with known content: dates,
  zones, triggers, medications and answers. It also prints what the app must show. `--list` names the seeds, and
  `--self-test` recounts them independently. `report-heavy` has a note on each of its 900 migraines;
  `report-heavy-old-notes` has the same migraines with notes on the oldest 100 only, for a shortened table that shows
  none of them (#67).
- **Import:** `seed_import` pushes the seed to Download and imports it through Your data.
- **What to expect:** `qa/seed_expect.py` turns the printed expectations into the texts to find in the PDF or on
  Insights.
- **The PDF:** it is pulled from `cache/reports/<id>/` with `run-as` and checked by `qa/report_pdf.py`: the text after
  `pdftotext -layout`, the footer on every page, and the page count. A table cell wraps in the PDF, and an en-US phone
  writes 12-hour times, so a migraine row is checked by a pattern.
- **Insights:** the scenario scrolls the screen, collects every text and TalkBack label, and checks each expected line
  is one of them.

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
- **Doubts about a test.** When a flow fails, QA proves whether the app or the test is at fault before filing anything.
  It reproduces the failure by hand with adb and reads the Maestro step timings, the screenshot and the hierarchy.
  So far, test timing on the software-GPU emulator explained every red flow that was not a filed issue.

## 5. Release Go/No-Go

A release candidate goes to a Play track only when every line passes (D44). Run them in this order and record the
results in the release PR.

| # | Check | Command | Pass when |
|---|---|---|---|
| 1 | Build gate, with Android Lint since D123 (develop 18b8b9b5) | `./gradlew assembleDebug test ktlintCheck detekt lint` | BUILD SUCCESSFUL: a Lint error fails it |
| 1a | Lint's warnings (D204) | every module's `*/build/reports/lint-results-debug.txt` from line 1 (each module writes its own; the app's has only app/ and gradle/ files). A candidate cut before D123 runs `./gradlew lint --continue` | each warning with a user-visible effect has an issue or a line in the record |
| 1b | Instrumented tests on the candidate (D204) | `ANDROID_SERIAL=emulator-5580 ./gradlew connectedDebugAndroidTest --continue`, then `qa/reset_device.sh` | every test passes |
| 2 | Smoke on Android 8.0 | `QA_SERIAL=emulator-5582 qa/maestro.sh --install --tags smoke` | every flow passes |
| 2a | Split APKs from the candidate's bundle (D204) | `qa/split_apks.sh --smoke <aab> ru-26` (emulator-5582), then `he-36`, `uz-36` and `en-36` (emulator-5580) | `Split APKs passed` for each profile |
| 3 | Full regression on the target version | `qa/maestro.sh --install` | exit 0 (passes 1 and 2 green). Pass 3 lists the open known bugs; none of them may be S1 or S2 |
| 4 | Accessibility audit | `qa/a11y_audit.sh` | `0 failure(s)` and no state that could not be captured; every warning and every "not judged" line looked at |
| 5 | Device scenarios | `qa/device_scenarios.sh` | `All scenarios passed` |
| 6 | Random input | `qa/monkey.sh 20261024 5000` | 0 crashes, 0 ANRs |
| 6a | Upgrade from the previous release | `QA_OLD_APK=<previous release APK> qa/device_scenarios.sh upgrade` | every attack, pain level and symptom logged on the old build is still there |
| 7 | Permissions | `qa/permission_audit.sh` (and on the release APK) | `Permission audit passed` |
| 8 | Open defects | `gh issue list --label qa --state open` | no S1 or S2 |
| 9 | Coverage report | `python3 -I qa/e2e_coverage.py --below 50 --register docs/qa/test-strategy.md` | exits 0 (no stale scenario names; the risk register has a row for every capability and the same counts as the flows). Exit 0 says nothing about how much is covered: the capabilities under 50% go to the record's residual risks (D204) |

From change 13 `prepare-release` on, these lines are added:

| # | Check | Pass when |
|---|---|---|
| 10 | R8 release build: lines 3 and 7 on the minified release APK | every flow passes, only allowed permissions |
| 11 | Play pre-launch report (internal testing track) | no crash, no critical accessibility finding |
| 12 | Internal testing with people for at least a week (BRD §14.1) | no new S1 or S2 |

Any failing line makes the candidate No-Go. The release PR names the failing line and the issue filed for it.

**Every Go/No-Go record has a section "What these checks do not prove" (D204).** It holds:
- what passing each line does not cover;
- the capabilities under 50% from line 9's `--below 50`, each with its follow-up from `docs/qa/test-strategy.md`;
- the deferred QA changes;
- the checks that could not run, and why.

The verdict reads "Go, with these risks open", never a bare "Go".
