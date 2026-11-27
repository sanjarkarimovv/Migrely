# Tasks

Base: `feature/fix-warning-summary-and-outside-apps` from `origin/develop` 476f7a27 (team-lead decision 2026-11-27: 1.4.1
patch, one change for #288 and #280). BRD EC-SAF-18, EC-SHR-14…17, §13 row 62; no new D-number. Plan corrected
2026-11-27 after code review `migrely-work/patch141/code-review-ab416647.md` (findings 1–7, lead decisions): the code of
2.x and 3.1–3.3 stays; 3.4–3.6 are new. One writer at a time:
product-manager wrote the plan; android-engineer takes the branch next. Commit messages say `Refs #288` / `Refs #280`,
never a closing keyword (QA retests and closes).

## 1. Plan

- [x] 1.1 BRD EC-SAF-18, EC-SHR-14, EC-SHR-15, EC-SHR-16, §13 row 62; this change. Strings are final (medical-reviewer
  PASS 2026-11-27, `migrely-work/288/medical-verdict.md`). Verify: `openspec validate
  fix-warning-summary-and-outside-apps --strict`.

There is no domain, data or datasource layer in this change, and no wiring change: `SettingsScreen` already passes the
activity's `startActivity` to the launcher.

## 2. Presentation resources (#288)

- [x] 2.1 Red first: new Robolectric `WarningSignsSummaryTest` in `features/settings/ui/src/testDebug`, default and
  `@Config(qualifiers = "de")`: the summary equals the spec's string; EN has no "care" and no " or "; DE has no "oder",
  no "Hilfe" and no digit. Negative control: it fails on develop's strings (record the red run, 2 of 2 failing). Verify:
  `./gradlew :features:settings:ui:testDebugUnitTest --tests '*WarningSignsSummaryTest*'` red, then green after 2.2.
- [x] 2.2 Set `settings_warning_signs_summary` verbatim: `values/strings.xml` "When to call your local emergency number
  and when to get urgent medical advice"; `values-de/strings.xml` "Wann den Notruf wählen, wann dringend ärztlichen Rat
  holen". Mind the XML: no apostrophe, so no escape. Verify: `git diff origin/develop -- '*/res/values*/strings.xml'`
  shows exactly 2 changed lines, one per file, the same key.

## 3. UI: outside apps in their own task (#280)

- [x] 3.1 Red first in `ExternalAppLauncherTest`: (a) `openStorePage()` starts the Google Play intent with
  `FLAG_ACTIVITY_NEW_TASK`; (b) the https fallback has it too; (c) the inner `ACTION_SEND` (`EXTRA_INTENT`) has it;
  (d) the chooser intent does not. Existing W3 asserts (no grant flag, no stream, no URI ClipData) stay. Negative
  control: (a), (b), (c) fail on develop's launcher (record the red run). Verify:
  `./gradlew :features:settings:ui:testDebugUnitTest --tests '*ExternalAppLauncherTest*'`.
- [x] 3.2 `ExternalAppLauncher`: add the flag in `storePageInPlay()`, `storePageOnWeb()` and on the `send` intent in
  `shareChooser()`, never on the chooser. Update the KDoc (EC-SHR-14, design D4, D5). Verify: 3.1 green.
- [x] 3.3 Scope check (phase 1, before 3.4–3.6). Verify: `git diff --stat origin/develop` lists only the two strings
  files, `ExternalAppLauncher.kt`, the two test files, and this change's docs; `ReportScreen.kt` and
  `PlayBillingDataSourceImpl.kt` untouched. Final scope after 3.4–3.6 (e9e0db66): the two strings files,
  `ExternalAppLauncher.kt`, `SettingsPages.kt`, three test changes (`WarningSignsSummaryTest` and `PrivacyPolicyLinkTest`
  added, `ExternalAppLauncherTest` edited, `PolicyLinkTest` deleted), `docs/BRD.md` and this change's docs.
- [x] 3.4 Red first: a Robolectric test in `features/settings/ui/src/testDebug` that clicks the Privacy page's policy link
  and asserts the started intent (`shadowOf(application).nextStartedActivity`) is `ACTION_VIEW` on the policy URL with
  `FLAG_ACTIVITY_NEW_TASK`; plus the no-browser case: `ActivityNotFoundException` or `SecurityException` from the start
  shows nothing and throws nothing. Named negative control: red on develop's `SettingsPages.kt` (Compose's
  `AndroidUriHandler` sets no flag; record the run). Verify:
  `./gradlew :features:settings:ui:testDebugUnitTest --tests '*PrivacyPolicyLink*'`.
- [x] 3.5 Route the policy link through `ExternalAppLauncher` (a method for an https page with `FLAG_ACTIVITY_NEW_TASK`,
  catching both exceptions); drop `LocalUriHandler` and `openInBrowserIfAny` from `SettingsPages.kt` (design D10). Verify:
  3.4 green; `git grep -n -E "LocalUriHandler|openUri" -- '*/src/main/*.kt'` finds nothing.
- [x] 3.6 Rename the inner-flag test and its KDoc to what it checks ("the ACTION_SEND intent carries NEW_TASK"), and say
  in `assertNewTask`'s and the launcher's KDoc that AOSP's chooser already adds its own task flags, so this is a guard
  (design D5). The launcher KDoc's "no `<queries>`" reads "adds no `<queries>`". Verify: the module's tests pass.

## 4. Docs

- [x] 4.1 No user doc changes. Phase 1 verification recorded below (2.1–3.3, 5.1, 5.2). Release-branch items are not in
  this change (see proposal: de-DE release notes, the Android 13–15 self-check QA row).
- [ ] 4.2 Final verification record after 3.4–3.6 and 5.3–5.6, with the scenario map below filled in.

## 5. Verify

- [x] 5.1 Gate. Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exit 0 (check
  the host load first; a timeout under load is rerun, not called a result).
- [x] 5.2 Permissions. Verify: `./gradlew :app:checkReleasePermissions` exit 0, and its permission list equals the one
  from develop 476f7a27 (diff empty); no `<queries>` entry added: the merged release manifest
  (`app/build/intermediates/merged_manifest/release/**/AndroidManifest.xml`) is identical to develop's (`cmp` exit 0).
  Develop already has one `<queries>` block, from `com.android.billingclient:billing:8.3.0`, so a count is not the proof.
  What proves "no change": both, plus the diff of 3.3 touching no manifest or Gradle file.
- [x] 5.3 Device walk, emulator-5570 (API 36), debug build of this branch; restore every setting changed. The walk
  proves task separation, focus and layout; it does not prove the crash (that needs QA's crash fixture, 5.6):
  - (a) Settings row in EN and DE at 200% font on a 360 dp wide window (`wm size`), portrait and landscape: screenshot
    of the row each; no "…", no cut line. If the EN line does not fit, stop and tell PM (spec allows one shorter line).
  - (b) TalkBack reads the row once: title, summary, button (EN and DE), from a `uiautomator dump` and TalkBack speech.
  - (c) Task ids, on develop's build first (negative control), then this branch: Rate (Play or the https page) and the
    privacy policy link: `adb shell dumpsys activity activities` shows the other app's activity in Migrely's task on
    develop and in another task on this branch. Share: record the chosen app's task on both builds (expected own task on
    both, AOSP chooser, design D5); the chooser in Migrely's task. Recents shows the other app as its own card.
  - (d) Back from the other app returns Settings at the same place, no message. Then with Play (or the browser) already
    open on another page: record what Back shows (EC-SHR-15).
  - (e) While the other app shows: rotate, and separately kill Migrely's process (`am kill`); Settings shows on return,
    no message (EC-SHR-06). No `am force-stop` row: it finishes only that package and cannot fail on develop (review
    finding 2).
  - (f) TalkBack focus on return: first on develop's build, then on this branch; pass = activated row or top of
    Settings, never "Delete all data". If it fails, stop and report to PM (design D8).
  - (g) Double tap on each row opens one page or one sheet; split screen opens the other app without closing Migrely
    and Back or Recents returns Settings (which half is not promised).
  - If emulator-5570 has no Google Play app, say so: the Play case is then NOT CHECKED here and goes to QA.
- [ ] 5.4 medical-reviewer re-checks the shipped `features/settings/ui/src/main/res/values/strings.xml` and
  `values-de/strings.xml` (the diff, not a table) after 2.2. Verify: a PASS file in `migrely-work/288/`.
- [ ] 5.5 Independent code review (`code-reviewer`) on `git diff origin/develop...HEAD`, briefed with this spec and
  risks R1–R3. Done: APPROVE WITH FIXES at ab416647 (`migrely-work/patch141/code-review-ab416647.md`); findings 1, 2, 3,
  5, 6, 7 answered in this plan, finding 4 taken into scope (3.4, 3.5). A short re-review of 3.4–3.6 before QA.
- [ ] 5.6 QA independent check (Go/No-Go scope: only what this diff reaches, with negative controls):
  - Crash fixture (QA-owned, under `qa/`): a small app that throws in `onCreate` and handles https `ACTION_VIEW` (and
    `market://` if cheap), installed as the only handler (Google Play disabled or absent). `am force-stop` and a late
    `am crash` are not valid proofs (review finding 2).
  - API 36 (emulator-5580), positive proof: develop's build closes Migrely's screen on Rate and on the privacy link
    (negative control); this branch leaves Settings and the Privacy page in place.
  - API 26 (emulator-5582), the recorded limit: expected "still closes" on both builds (EC-SHR-17). Record what happens
    and whether Migrely reopens at Home. If this branch keeps Migrely open there, report it to PM for a rewording.
  - Share: no crash control (AOSP chooser, design D5); record the chosen app's task id on API 36.
  - API 36: the task check of 5.3 (c) and the TalkBack return focus of 5.3 (f).
  - The summary row in EN and DE at 200% font, 360 dp, portrait and landscape (screenshots), and TalkBack speech.
- [ ] 5.7 Archive: `openspec archive fix-warning-summary-and-outside-apps --yes`; keep `app-settings` Purpose real;
  `openspec validate --specs --strict` passes.

## Scenario map (review finding 7)

| Scenario | Proof |
|---|---|
| Reading the row in English / German; old wording fails | `WarningSignsSummaryTest` (2.1) |
| The page itself is unchanged | 2.2 diff: only the summary key changes |
| 200% font, narrow and landscape; shorter line | 5.3 (a), 5.6 |
| TalkBack reads the row once | 5.3 (b), 5.6 |
| Free, Premium, offline and during an attack | No row: the summary is one unconditional `stringResource` (`SettingsScreen.kt:340`) with no theme, entitlement or network branch; the attack theme is D94's, unchanged |
| Upgrade from a shipped version | No row: a resource value only, no data, migration or backup code |
| The intents carry the flag; without the flag | `ExternalAppLauncherTest` (3.1), privacy link test (3.4) |
| Browser / privacy page crashes at launch, API 28+ | 5.6 API 36 with the crash fixture |
| API 26 and 27, a recorded limit | 5.6 API 26 |
| Share on the system chooser; its own task on a device | 5.3 (c), 5.6 |
| Back returns; already open; split screen | 5.3 (d), (g) |
| TalkBack focus on return | 5.3 (f), 5.6 |
| Nothing can open it; no browser for the privacy policy | `ExternalAppLauncherTest` (existing), 3.4 |
| One opening per tap | 5.3 (g); existing `OpeningGuard` tests |
| Rotation or process death while away | 5.3 (e) |
| No permission or manifest change | 5.2 |

## Verification record

(Filled by android-engineer, then QA.)

android-engineer, 2026-11-27, phase 1 (no device). Evidence in `migrely-work/patch141/`.

| Task | Command | Result |
|---|---|---|
| 2.1, 3.1 red | `:features:settings:ui:testDebugUnitTest --tests '*WarningSignsSummaryTest*' --tests '*ExternalAppLauncherTest*'` on develop's strings and launcher | exit 1, 14 tests, 5 failed: both summary tests (`but was: When a headache needs urgent care`, `but was: Wann Kopfschmerzen dringend ärztliche Hilfe brauchen`), and (a), (b), (c) (`expected 268435456 but was 0`). (d) passes on develop, as it should: it guards against the flag on the chooser |
| 2.1, 3.1 green | same, after 2.2 and 3.2 | exit 0; WarningSignsSummaryTest 2/2, ExternalAppLauncherTest 12/12 (8 before + 4 new), 0 failures |
| 2.2 | `git diff origin/develop -- '*/res/values*/strings.xml'` | 2 files, 1 line each, key `settings_warning_signs_summary`; values equal `medical-verdict.md` lines 10–11 |
| 3.3 | `git diff --stat origin/develop` | the 2 strings files, `ExternalAppLauncher.kt`, the 2 test files, this change's docs and `docs/BRD.md` (plan commit); no manifest, Gradle, `ReportScreen.kt` or `PlayBillingDataSourceImpl.kt` |
| 5.1 | `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` (1-min load 6.8 at start) | exit 0, BUILD SUCCESSFUL in 5m 22s; 4014 tests in the worktree's result files, 0 failures, 0 errors, 0 skipped |
| 5.2 | `:app:checkReleasePermissions` on this branch and on a detached develop 476f7a27 worktree | both exit 0; `reports/permissions/release.txt` identical (6 entries: ACCESS_NETWORK_STATE, POST_NOTIFICATIONS, RECEIVE_BOOT_COMPLETED, WAKE_LOCK, com.android.vending.BILLING, DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION; no INTERNET); the merged release manifests are byte-identical (`diff` empty) |

Deviation in 5.2: the planned `grep -c '<queries'` gives 1, not 0, on develop and on this branch alike. The one
`<queries>` block comes from `com.android.billingclient:billing:8.3.0` (manifest merger report) and was already there.
The requirement "no `<queries>` entry is added" holds: the merged manifests are byte-identical. product-manager fixed the
verify line on 2026-11-27 (plan error, not a product finding).

Not checked in phase 1: everything in 5.3 (no emulator in this phase), in particular that Android's chooser keeps the
inner `NEW_TASK` (D5, R1) and the TalkBack return focus (D8, R3). The JVM tests prove the flags on the built intents only,
not what a launcher, Play or a browser does with them.

android-engineer, 2026-11-27, phase 1b (no device), after code review. Evidence in `migrely-work/patch141/`
(`phase1b-evidence.txt`).

| Task | Command | Result |
|---|---|---|
| 3.4 red | `:features:settings:ui:testDebugUnitTest --tests '*PrivacyPolicyLink*'` on develop's `SettingsPages.kt` | exit 1, 3 tests, 2 failed: "opens in its own task" (`expected 268435456 but was 0`; action and URL already right) and the work-profile case (`List is empty`: Compose's handler never reaches `LocalContext`). The no-browser case passes on develop, as it must: that behaviour stays |
| 3.4, 3.5 green | `:features:settings:ui:testDebugUnitTest` (whole module) | exit 0; 20 suites, 84 tests, 0 failed; PrivacyPolicyLinkTest 3/3, ExternalAppLauncherTest 12/12 |
| 3.5 | `git grep -n -E "LocalUriHandler\|openUri\|openInBrowserIfAny" -- '*.kt'` | nothing (exit 1). `ExternalAppLauncher.openWebPage(url)`; `storePageOnWeb()` and the policy link share one private `webPage(url)` builder. JVM `PolicyLinkTest` (tested the removed `openInBrowserIfAny`) deleted; its two cases live in `PrivacyPolicyLinkTest` |
| 3.6 | same module run | test renamed "The ACTION_SEND intent inside the chooser carries NEW_TASK"; its KDoc, `assertNewTask`'s and the launcher's say the inner flag is a guard on AOSP; launcher KDoc says "adds no `<queries>`" and API 28+ / EC-SHR-17 |
| 5.1 rerun | gate, 1-min load 4.4 at start | exit 0, BUILD SUCCESSFUL in 44s (unchanged modules up to date); 4013 tests, 0 failed (phase 1: 4014, minus `PolicyLinkTest`'s 2 in debug and release unit tests, plus 3) |
| 5.2 rerun | `:app:checkReleasePermissions` on this branch and on a detached develop 476f7a27 worktree | both exit 0; `cmp` merged release manifest exit 0; `cmp` permission report exit 0; no INTERNET. No manifest, Gradle, `ReportScreen.kt`, `PlayBillingDataSourceImpl.kt` or `RemindersScreen.kt` in the branch diff |

Not checked in phase 1b: everything in 5.3. The Robolectric test proves the flag on the intent the link starts, not what a
browser or the crash handler does with it (QA's crash fixture, 5.6).

android-engineer, 2026-11-27, phase 2: device walk 5.3 on emulator-5570 (`Migrely_API_36`, API 36), debug builds of
develop 476f7a27 (control) and this branch (code of e9e0db66, APK built at 5a0ba93c). Evidence:
`migrely-work/patch141/walk/` (`walk.log`, `shots/`, scripts `k.py`, `row_*.py`). Task ids come from `dumpsys activity
activities`, intent flags from `ActivityTaskManager: START u0` lines, and the task of every created activity, trampolines
included, from the `wm_create_activity` event log.

| Row | Develop (control) | This branch |
|---|---|---|
| (c) Rate | `market://` resolves nothing on this image (Play is a stub: `pm resolve-activity` "No activity found", START result -91), so Rate takes the https fallback in Chrome. Migrely's START has no `flg`; Chrome's `IntentDispatcher` is created **in Migrely's task 1583**, then moves itself to task 1587 (`flg=0x10800000`). The final dumpsys shows Chrome in its own task even on develop: Chrome's dispatcher does that, so only the event log shows the shared task | Both starts carry `flg=0x10000000`; `IntentDispatcher` is created directly in task 1591, never in Migrely's task 1590 |
| (c) Privacy link | Same as Rate: no `flg`, `IntentDispatcher` created in task 1583, then task 1589 | `flg=0x10000000`; `IntentDispatcher` created in task 1593 |
| (c) Share | Chooser (`ChooserActivityLauncher`) in Migrely's task 1583. The SEND reaches Messages with `flg=0xb080000` (the system chooser adds NEW_DOCUMENT and MULTIPLE_TASK), in its own task 1588: no #280 risk on develop, as design D5 says | Chooser in Migrely's task 1590; SEND `flg=0x1b080000` (adds our NEW_TASK), Messages in its own task 1592 |
| (c) Recents | | While Chrome shows: Recents lists Chrome (1594) above Migrely (1590), two cards |
| (d) Back | Rate, Share, policy: one Back returns Settings (or the Privacy page) with the same rows on top, no message | Same |
| (d) Already open | Chrome is not set up on 5570 (first-run screen; setup not finished, to keep device state). With Chrome left open by Rate (Home pressed), the policy link opened Chrome in a new task (1606), and one Back returned the Privacy page | Same (1595), Back returned the Privacy page. A browser or Play already open **on another page** was NOT CHECKED: it needs Chrome set up |
| (e) Rotate in Chrome | Back returns Settings in portrait, same rows, no message | Same |
| (e) Process death | `am kill` while in Chrome (pid gone); Back starts a new process in task 1601: Settings, no message, **scrolled to the top** | Same, task 1590, Settings scrolled to the top. Existing behaviour, same on both builds; the spec promises "Settings shows, no message", which holds |
| (f) TalkBack focus on return | After Back: "Migrely", then the activated row: "Rate Migrely on Google Play", "Share Migrely", "Read the full privacy policy" | Identical for all three (rerun after the emulator crash below). Never "Delete all data". Pass, no focus code |
| (g) Double tap | | Rate: one Chrome page (one `IntentDispatcher`; the two Migrely STARTs are `market://` then https from one tap). Share: one chooser. Policy link: two STARTs reach the system, the second returns code 3 (delivered to the top instance): one page, one task. The link has no opening guard (recorded by PM in 5a0ba93c) |
| (g) Split screen | | Recents → Migrely → Split screen, TalkBack settings as the other half. Rate from Migrely's half opens Chrome in that half in its own task 1618; Migrely's task 1610 stays; Back returns Settings in place (`shots/split-open-small.png`, `split-back2-small.png`). The `WMShell splitscreen moveToSideStage` shell command did not enter split on this image |
| (a) Layout, 945x2100 (360 dp) | | EN and DE, 100% portrait and 200% portrait and landscape (2100x945, 360 dp tall): the summary node's text equals the spec string, lies inside the row node, no "…". Row heights: 100% EN 77 dp; 200% EN 210 dp (4 lines), DE 171 dp (3 lines); landscape EN 3 lines, DE 2 lines. Crops checked by eye: no cut line, no overlap (`shots/a-*-crop.png`). No layout defect, so no shorter line |
| (b) TalkBack | | Console touch on the row, 12 s of speech: EN "Warning signs", "When to call your local emergency number and when to get urgent medical advice", "Button", "Double-tap to activate"; DE "Warnzeichen", "Wann den Notruf wählen, wann dringend ärztlichen Rat holen", "Button", "Double-tap to activate" (TalkBack's own words in English: only the app is German). One node, read once |

Environment: the emulator crashed at 16:29 under host load (1-minute load 72, `mediaanalysisd` at 266% CPU; crash report
in `walk/emu.log`) during the first branch run of (f). Environmental; rerun at load 5 after a restart: the result above.

Restored on 5570: font 1.0, size and density reset, rotation 0 with auto-rotate on, animator scales 1, TalkBack off,
TalkBack log level ERROR, app locale reset, Migrely uninstalled (it was not installed at the start); `settings-before.txt`
equals `settings-after.txt`. Emulator off.

Not checked here: the Google Play app path (stub on this image; goes to QA or the owner's phone); a browser already open
on a real page (EC-SHR-15); the crash at launch (QA's fixture, 5.6); API 26–28 (QA).
