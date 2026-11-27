# Tasks

Base: `feature/fix-warning-summary-and-outside-apps` from `origin/develop` 476f7a27 (team-lead decision 2026-11-27: 1.4.1
patch, one change for #288 and #280). BRD EC-SAF-18, EC-SHR-14…16, §13 row 62; no new D-number. One writer at a time:
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
- [x] 3.3 Scope check. Verify: `git diff --stat origin/develop` lists only the two strings files, `ExternalAppLauncher.kt`,
  the two test files, and this change's docs; `ReportScreen.kt` and `PlayBillingDataSourceImpl.kt` untouched.

## 4. Docs

- [ ] 4.1 No user doc changes. Record the verification below. Release-branch items are not in this change (see
  proposal: de-DE release notes, the Android 13–15 self-check QA row).

## 5. Verify

- [x] 5.1 Gate. Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exit 0 (check
  the host load first; a timeout under load is rerun, not called a result).
- [x] 5.2 Permissions. Verify: `./gradlew :app:checkReleasePermissions` exit 0, and its permission list equals the one
  from develop 476f7a27 (diff empty); `grep -c '<queries' app/build/intermediates/merged_manifest/release/**/AndroidManifest.xml`
  is 0. What proves "no change": both, plus the diff of 3.3 touching no manifest or Gradle file.
- [ ] 5.3 Device walk, emulator-5570 (API 36), debug build of this branch; restore every setting changed:
  - (a) Settings row in EN and DE at 200% font on a 360 dp wide window (`wm size`), portrait and landscape: screenshot
    of the row each; no "…", no cut line. If the EN line does not fit, stop and tell PM (spec allows one shorter line).
  - (b) TalkBack reads the row once: title, summary, button (EN and DE), from a `uiautomator dump` and TalkBack speech.
  - (c) Rate and Share: `adb shell dumpsys activity activities` shows the other app's activity in another task than
    `MainActivity`; the chooser in Migrely's task; Recents shows the other app as its own card.
  - (d) Back from the other app returns Settings at the same place, no message. Then with Play (or the browser) already
    open on another page: record what Back shows (EC-SHR-15).
  - (e) Force-stop the share target and the browser while they show (`am force-stop <pkg>`), and kill Migrely's process
    while away (`am kill`): Settings shows on return, no message.
  - (f) TalkBack focus on return: first on develop's build, then on this branch; pass = activated row or top of
    Settings, never "Delete all data". If it fails, stop and report to PM (design D8).
  - (g) Double tap on each row opens one page or one sheet; split screen opens the other app without closing Migrely.
  - If emulator-5570 has no Google Play app, say so: the Play case is then NOT CHECKED here and goes to QA.
- [ ] 5.4 medical-reviewer re-checks the shipped `features/settings/ui/src/main/res/values/strings.xml` and
  `values-de/strings.xml` (the diff, not a table) after 2.2. Verify: a PASS file in `migrely-work/288/`.
- [ ] 5.5 Independent code review (`code-reviewer`) on `git diff origin/develop...HEAD`, briefed with this spec and
  risks R1–R3. Fix real findings and record them here.
- [ ] 5.6 QA independent check (Go/No-Go scope: only what this diff reaches, with negative controls):
  - API 26, emulator-5582, the #280 image (no Play Store, a browser that crashes at launch): develop's build closes
    Migrely (negative control, reproduces #280); this branch leaves Settings in place. Same with a share target that is
    force-stopped.
  - API 36: the task check of 5.3 (c) and the TalkBack return focus of 5.3 (f).
  - The summary row in EN and DE at 200% font, 360 dp, portrait and landscape (screenshots), and TalkBack speech.
- [ ] 5.7 Archive: `openspec archive fix-warning-summary-and-outside-apps --yes`; keep `app-settings` Purpose real;
  `openspec validate --specs --strict` passes.

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
The requirement "no `<queries>` entry is added" holds: the merged manifests are byte-identical. product-manager to fix the
verify line at archive.

Not checked in phase 1: everything in 5.3 (no emulator in this phase), in particular that Android's chooser keeps the
inner `NEW_TASK` (D5, R1) and the TalkBack return focus (D8, R3). The JVM tests prove the flags on the built intents only,
not what a launcher, Play or a browser does with them.
