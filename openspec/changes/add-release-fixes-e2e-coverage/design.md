# Design

## Context

See proposal.md (Why).
- **Build under test:** build 4, `release/1.0.0` @ 29aadb3, with the same product code as develop 438be90. Its debug
  and R8 APKs are in `../migraine-app-qa-rc` and are named with `QA_APK` (D201). So is its debug-signed bundle
  (`app/build/outputs/bundle/release/app-release.aab`).
- **Play:** build 4 is live: the user pressed "Publish changes" on 2026-10-27, before QA's verdict and before the
  closed track's pre-launch report. The sweep and the report are checks after the release.
- **The bundle:**
  - `bundletool dump config` shows the default splits (language, density, ABI);
  - the one native library is stored uncompressed with 16 KB alignment;
  - for a ru, he, uz or en arm64-v8a phone at 420 dpi, `bundletool extract-apks` gives the base, the language, the
    xxhdpi and the arm64-v8a APKs.
- **The audit:** `qa/a11y_audit.sh` drives every flow in `.maestro/a11y-tour/` with Maestro. It captures each state 3
  times (100% portrait, 200% portrait, 100% landscape) and checks the captures with `qa/a11y_audit.py`.
  - The rules are contrast, 48 dp targets, labels and keyboard focus.
  - Today's 10 states give 30 captures.
- **Browsers on the QA emulator:** only Chrome answers the privacy link. `cmd package query-activities` for VIEW +
  BROWSABLE on https://migrely.health/privacy/ lists `com.android.chrome` alone.
- **The report's notes cap (#67, D84):** with "Include notes" on, the notes follow the table. Only the latest N
  migraines' notes are kept, under "Notes from the latest N migraines.".
  - The `report-heavy` seed numbers its notes from HEAVY-0001, the newest.
  - With that seed, N is 308.

## Goals / Non-Goals

**Goals:**
- every release-fix scenario named in the proposal has a check, or a D61 reason;
- every screen the app shows is captured by the audit;
- exploratory findings on build 4 are filed before users meet them;
- a release candidate is checked at every test level the project has, on the APKs users install;
- no Go/No-Go record reads as a stronger verdict than its checks support.

**Non-Goals:**
- the old audit states change only if a new rule needs it;
- no new audit rules.

## Decisions

### D1. Tour states for the screens never audited (D203)

Each tour flow starts with `subflows/launch-clean.yaml` and ends on its screen, scrolled to its top, as today's states
do. Landscape and 200% font use the same flow, so each scroll waits for its element instead of assuming a position.

| State | How it is reached |
|---|---|
| `onboarding-first` | a clean install, without the Skip of `launch-clean` |
| `settings-main` | Settings |
| `settings-warning-signs` | Settings › Warning signs |
| `settings-privacy` | Settings › What Migrely keeps, and where, at its top |
| `settings-privacy-link` | the same page scrolled to "Read the full privacy policy", since its end never shows at the top |
| `settings-theme` | Settings › Theme |
| `medications-list` | My medications, with one medication added |
| `reminders` | Settings › Reminders |
| `your-data` | Settings › Your data |
| `paywall` | History › Doctor report, as a free user. The yearly plan is chosen first |
| `report-period` | Premium bought (`subflows/buy.yaml`), then History › Doctor report |
| `insights` | Premium bought and 5 past migraines added last month (`subflows/add-past-attack-on.yaml`), then History › Insights with its patterns, so the full screen is audited, not only the "more migraines" card |

- **Premium in a tour flow:** the purchase goes through the simulated sheet, so these states need the debug APK. The
  audit already runs on it. On the R8 APK the premium states are not reachable, and the audit says so.
- **Rejected: one state per page of a long screen.** The audit's "not judged" rule already re-checks nodes cut off at an
  edge on a capture where they are fully in view. A long page gets a second state only when a part never comes into
  view.

### D2. The privacy link and "No browser" (device scenario `privacy_link`)

- **Link:**
  1. `logcat -c`.
  2. The device step `privacy-policy-link.yaml` taps the link.
  3. The check reads `ActivityTaskManager`'s START line: a VIEW intent to `migrely.health` that resolves to Chrome.
     Android shortens the logged URI, so the full address is read from the page's own text, which shows it (D119).
- **No browser:**
  1. `pm disable-user --user 0 com.android.chrome`, then the intent query must list no activity.
  2. The same tap.
  3. The checks:
     - Migrely is still the resumed activity;
     - logcat has no `FATAL EXCEPTION` for `com.migrely.app`;
     - the page still shows the address.
  4. `restore()` runs `pm enable com.android.chrome`, also when the scenario fails.
- **Rejected: uninstalling Chrome's updates or using a browserless AVD.** The first changes the QA image for every
  later run. The second costs a third emulator.

### D3. Track periods off (flow `day-markers/track-periods-off-marks.yaml`)

One flow, in this order. Marks are checked by the cell's spoken label (`<day>: nothing logged. Period.`), as
`mark-and-remove.yaml` does.
1. With tracking on, mark today from home and mark one empty past day in History. Keep its date in `output.marked`.
2. Turn "Track periods" off in Settings, and go back.
3. Home still shows "Period today", since today has a mark. Tap it:
   - the mark is gone;
   - "Period today" is still there.
   Then go to History and back, and the toggle is gone ("Removing today's mark with tracking off").
4. Restart the app. Home shows no "Period today" ("Period today from home › Period tracking off").
5. In History, choose `output.marked`. "Period" shows, and a tap removes the mark ("Removing a mark with tracking
   off").
6. Choose an empty past day. Below its heading, only "Pain-free day" shows ("Mark a chosen day › Period tracking off").
- **Limit:** like `mark-and-remove`, the flow needs a past day of the current month, so it cannot run on the 1st.

### D4. No note among the migraines shown (seed `report-heavy-old-notes`)

- **The seed:** like `report-heavy`, 900 migraines in 12 months, but only the oldest 100 have a note (HEAVY-0801 to
  HEAVY-0900).
- **The check:** `report_heavy` imports it with Replace, after its own two reports, and makes a 12-month report with
  notes on. The text must have:
  - "Showing the latest N of 900 migraines.";
  - "Notes from the latest N migraines.", with the same N;
  - no "HEAVY-" note at all.
- **Rejected: a separate scenario.** It would buy Premium and import 900 migraines again for one check.

### D5. Google Play offers no products (D110, D122)

D122 (PR #85, merged into develop at d156579) gives the simulated store the command `no-products`. Billing connects and
reads what is owned, but no product is offered. A new scenario `premium_no_products` checks, on a develop debug build:
- no paywall shows itself after onboarding, or after the 5th migraine with patterns ready, 8 days on;
- in that same state with products back, the patterns paywall does show, so the "none" was the store's doing;
- a tap on Doctor report opens the paywall with "Premium isn't available in your region yet", no buy button and no
  price;
- Premium bought before stays Premium once the app asks the store again (the clock 20 minutes on, past
  `CHECK_INTERVAL`).
- **Its own scenario, not part of `premium_store_states`:** it needs the clock and an import, which the other states do
  not.
- **Not checked:** "Google Play is asked again at most once an hour". The store does not count the asks, and the
  app's unit tests hold the rule.

### D6. Exploratory testing on build 4

- **Every defect is filed with:** its steps, two reproductions (D40), a screenshot and the dev's launch-blocker
  verdict:
  - **blocker:** a crash or ANR in a core flow, data loss, a wrong or missing safety warning, a broken purchase or
    entitlement, or data leaving the device;
  - **not a blocker:** everything else.
  A blocker goes to the dev session at once, before the sweep ends.
- **The sweep:**
  - the 92 scenarios of attack-details, past-attacks, medication-intakes, attack-mode, attack-logging and
    attack-history that have no flow, on the R8 APK;
  - plus these edge cases: emoji, right-to-left and over-long notes; "Don't keep activities" with unsaved input;
    rotation with a picker open; back with unsaved edits; repeated taps on Save, Delete, Undo and End.
  - Its results are the input of change 13d and of the risk register.

Each charter below is 30 to 45 minutes on the debug APK, or on the R8 APK for the launch path.
1. **Rotation and process death** on the paywall, the purchase sheet, the report while it is being made, and Insights.
   The questions: are the chosen plan, the chosen period and any message kept? Does the state come back after
   `am kill`?
2. **200% font and dark theme** on the report and Insights flows, end to end, beyond the audit's single captures.
3. **Restarts around a purchase:** the app is force-stopped while the sheet is open and while a payment is pending. Is
   Premium right on the next start?
4. **First launch of the R8 APK as a new user:** onboarding, the first migraine, the Settings pages, then the privacy
   link. This is the path every new user takes on day one.

### D11. The 1.0.1 hotfix candidate

Build 4 went live before QA's verdict. Its sweep found #77 to #81, and the dev session fixed them on `hotfix/1.0.1`,
cut from `main` (versionCode 5, D125). The candidate's commit is named in its record, `docs/qa/go-no-go-1.0.1.md`.
- **Why it belongs to this change:** the change's device tasks are this candidate's Go/No-Go lines, so each check runs
  once:
  - the new flows and scenarios;
  - the full accessibility audit (line 4);
  - the split APKs (line 2a).
- **Which builds:**
  - QA builds the debug and R8 APKs in `../migraine-app-qa-hotfix` at the candidate.
  - Lines 2a and 10 use the upload-signed bundle that goes to Play. bundletool turns it into split APKs, and into a
    universal APK signed with the debug key.
  - Line 6a upgrades from build 4's APKs.
- **The retests:** each fixed issue twice on the debug APK and twice on the R8 APK, plus the regression case the dev
  session found in its own fix of #81. An issue is closed by the hotfix PR only after its retest passes.
- **No exceptions:** line 1a has none, because the requirement now applies (D7). Lint errors on the candidate make it
  No-Go, unless the user records a decision to accept them.
- **Not part of 1.0.1:**
  - `premium_no_products` needs D122, which is on `develop` and not in the hotfix. It runs on a `develop` debug build
    (task 2.4).
  - The rest of sweep 2 (batch B after data-backup) runs on the 1.0.1 build once it is out. Its results go to QA 13d.

### D7. Lint and the instrumented tests on the candidate (D204)

Both run in the candidate's worktree (`../migraine-app-qa-rc`), before the device lines:
- `./gradlew lint --continue --no-daemon`. Every error is either fixed before Go or a S1/S2 issue makes the candidate
  No-Go. Warnings are read for user-visible effects (Compose state, plurals, API levels). Each such warning gets an
  issue or a line in the record saying why it is harmless.
- `ANDROID_SERIAL=emulator-5580 ./gradlew connectedDebugAndroidTest --continue --no-daemon`. The run installs and then
  removes the app, so `qa/reset_device.sh` and the candidate's install follow it.
- **Build 4:**
  - Lint has 3 errors (#74), none reachable at runtime:
    - the NewApi call is behind a check lint cannot see, and QA checks that path on API 26;
    - the other error is in test code.
    The dev session fixes them in 1.0.1 with D123. Build 4's record states this as a known exception, because the
    requirement applies from the next candidate.
  - The instrumented tests pass: datasource 100/100, designsystem 37/37, app 1/1.
- **Rejected: running lint in `qa/` with its own rules.** Lint belongs to the build. The dev session adds it to the gate
  (D123), and QA's line only reads its result on the candidate.

### D8. The split APKs users install (`qa/split_apks.sh`, D204)

Play builds split APKs from the bundle: base, language, density and ABI. The universal APK holds every language and
density, so a mistake in a split, such as a missing resource or a locale change that needs a missing language, never
shows there.
- **The script:**
  - It reads `<aab> <profile>`. A profile is a bundletool device spec in `qa/fixtures/device-specs/`:
    - `ru-26`, `he-36`, `uz-36`, `en-36`;
    - each with arm64-v8a, the QA AVDs' ABI, and 420 dpi.
  - It runs `bundletool build-apks` with the debug key, since QA has no upload key (D102).
  - It runs `extract-apks` for the profile and checks that the profile's language split is in the set.
  - It installs the set with `adb install-multiple`, after uninstalling the app. That is the same install as
    `install-apks`, and it shows which files went in.
  - It refuses every serial except emulator-5580 and emulator-5582.
- **The check:**
  - `pm path com.migrely.app` lists the base and its 3 splits;
  - the profile's language is set and read back from the device. On API 33 and later it is the app's own language;
    on Android 8.0 it is the system's;
  - the D117 checks:
    - Home, History and a date picker show English month and day names;
    - home shows the region's date order (day first for en-IL, month first for en-US, en-RU and en-UZ);
    - the date picker starts the week on the region's first weekday;
    - the app's own text has no Cyrillic or Hebrew;
  - with `--smoke`, the smoke suite runs without `--install`. On API 33 and later its clean starts clear the app's
    own language, so the suite checks the install, and the steps above check the language.
  - The language is put back on exit, and the debug APK under test is installed again, since the other QA scripts
    need a debuggable build.
- **Rejected: Play's internal app sharing.** It needs the upload key and Play itself. bundletool builds the same split
  set from the same bundle.

### D9. Residual risk in every Go/No-Go record (D204)

The record gets a section "What these checks do not prove". It holds:
- a table with one row per check, saying what passing it does not cover. For example: the regression covers only the
  scenarios that have a flow, and the audit covers only the states in its tour;
- the capabilities with under half of their scenarios checked, from `qa/e2e_coverage.py`, each with the follow-up in
  the test strategy;
- the deferred QA changes: D42 visual regression; the audit's C3 and C4, which wait for C1; and C5, which waits for
  GitHub Actions;
- the checks that could not run, and why (for example: CI, Play's grace period).

The verdict then reads "Go, with these risks open", never a bare "Go". Line 9 changes its pass condition to "exits 0
(no stale names). The share per capability goes to the residual risks".

### D10. The test strategy and its risk register (`docs/qa/test-strategy.md`, D204)

- **Levels:** unit, architecture rules, screen state, visual, instrumented, end-to-end, device scenarios, device audits,
  static analysis, exploratory and Play's pre-launch report. Each has its tool, owner, where it runs and when, with a
  "today / target" column, so the audit's gaps (C1–C6) are visible.
- **Risk register:** one row per capability of `openspec/specs/`. Each row has:
  - its E2E share;
  - its other levels;
  - what is not checked;
  - the user risk (high for attack logging and details, medications and backup);
  - the follow-up.
  Rows come from the coverage report and the sweep's results. The follow-up of a high-risk row is a named QA change
  (13d for the swept core flows).
- **When it is updated:** by every QA change that changes a level or a capability's checks, and at every Go/No-Go.

## Risks / Trade-offs

- **Chrome left disabled:** a failed scenario that skips `restore()` would leave the QA emulator without a browser. So
  `restore()` re-enables Chrome, and `qa/reset_device.sh` enables it too.
- **The audit's run time grows** from 30 captures to 66, about 40 minutes more. It runs at the Go/No-Go and after
  UI changes, not on every flow change.
- **New states may fail on old screens.** They are expected to find real issues. Those are filed, not worked around
  (D203).
- **The system language left changed after a split-APK run:** `qa/split_apks.sh` restores it on exit.
  `qa/reset_device.sh` resets the app's language, and on Android 8.0 the system's too. Every QA script warns at its
  start when the system language is not the baseline's.
- **Build 4 fails the new lint line.** The requirement is new, and the 3 errors are shown not to reach users. So build
  4's record states the exception, with #74 and D123, instead of a hidden pass. From the next candidate on (1.0.1) there is no exception.
