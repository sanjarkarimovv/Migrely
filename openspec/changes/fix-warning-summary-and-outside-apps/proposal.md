# Proposal

## Why

Two small Settings defects, planned together for the 1.4.1 patch (team-lead decision 2026-11-27: one branch, one gate,
one review, one QA pass under a loaded host). Both are bugs against existing decisions, so no new D-number is taken.

**#288 (S4, safety copy precision).** The summary under "Warning signs" reads "When a headache needs urgent care". In the
US "urgent care" is a walk-in clinic. The page it opens has two tiers (D171): call the local emergency number, or get
urgent medical advice. "Urgent care" matches neither, and for emergency signs it points to the wrong place. The German
summary, "Wann Kopfschmerzen dringend ärztliche Hilfe brauchen", names only the second tier and never says "Notruf".

**#280 (S4).** "Rate Migrely on Google Play" and "Share Migrely" start Google Play, the browser or the share target
without `FLAG_ACTIVITY_NEW_TASK`, so the other app's screen runs in Migrely's task. On QA's API 26 emulator (no Play
Store) the only https handler crashed at launch and Android closed Migrely's MainActivity with it: the person landed on
the launcher. D185 and EC-SHR-01 promise that the app keeps running when the other side fails. The Privacy page's policy
link takes the same path (Compose's URI handler, no task flag; code review finding 4).

Code review (2026-11-27, `migrely-work/patch141/code-review-ab416647.md`, AOSP source) narrowed what the fix can promise:
- On API 28 and later a crash stays inside the crashed task, so an own task keeps Migrely open.
- On API 26 and 27 all full-screen tasks share one stack and the crash handler also finishes the paused activity below,
  so Migrely's screen still closes there. That is a recorded platform limit (EC-SHR-17), with no workaround.
- On AOSP the share sheet already starts the chosen app in its own task, so Share was not affected; its inner flag is a
  guard for other choosers.

## What Changes

- `settings_warning_signs_summary` becomes, verbatim (medical-reviewer PASS 2026-11-27,
  `migrely-work/288/medical-verdict.md`):
  - English: "When to call your local emergency number and when to get urgent medical advice"
  - German: "Wann den Notruf wählen, wann dringend ärztlichen Rat holen"
- Rate starts both its intents (`market://` in Google Play, then the https page) with `FLAG_ACTIVITY_NEW_TASK`.
- The Privacy page's policy link opens through the launcher with `FLAG_ACTIVITY_NEW_TASK`; with no browser it still does
  nothing, as today (the address is printed under the link).
- Share puts `FLAG_ACTIVITY_NEW_TASK` on the `ACTION_SEND` intent inside the chooser, not on the chooser: a guard for
  choosers that do not add their own task flags (AOSP's does).
- A JVM test pins each of these and fails on `develop` (negative control).

## Out of scope

- The site sentences quoted in #288: PR #290 (39d93ba8) already made them two-tier and they are live; nothing to do.
- Every other string on the Warning signs page and the About page: unchanged.
- Other places that start an outside app in Migrely's task: the doctor report's PDF share (`ReportScreen`), the
  paywall's "Manage subscription" when an activity is at hand (`PlayBillingDataSourceImpl`), and Reminders' link to the
  notification settings. Same pattern, other modules and risks (a URI grant, billing): filed as #291. No other
  `LocalUriHandler` or `openUri` use exists in main code.
- A workaround for API 26 and 27, where an outside crash at launch can still close Migrely's screen (EC-SHR-17).
- Detecting a crash of the other app or showing a message after one: Migrely cannot know, and the start succeeded.
- Moving TalkBack focus on return with code: only if the walk shows Android lands it on "Delete all data", and then as a
  new PM decision (EC-SHR-16).
- Release items for 1.4.1 that are not product code: the de-DE release-notes block in `docs/release/whats-new.md` and the
  QA row for the Android 13–15 permission self-check path. They belong to the release branch and QA.
- The a11y patch set (#279, #269, #272, #273, #264, #262): a separate change after the engineer's #279 root cause.

## Capabilities

### Modified capabilities

- `app-settings`: two ADDED requirements, "The Warning signs row names both tiers" and "Outside apps run in their own
  task". No existing requirement text changes.

## Impact

- Code: `features/settings/ui` only: `values/strings.xml`, `values-de/strings.xml`, `ExternalAppLauncher.kt`,
  `SettingsPages.kt` (the policy link), and tests in `src/testDebug` (`ExternalAppLauncherTest`, a new
  `WarningSignsSummaryTest`, a privacy link test).
- No permission, `<queries>`, manifest, dependency, data, migration, backup or Premium change.
- BRD: EC-SAF-18, EC-SHR-14, EC-SHR-15, EC-SHR-16, EC-SHR-17, §13 row 62; against D171, D185, SAF-F1, SET-F2. No new
  D-number: the API 26–27 limit is a platform limit of D185's promise, recorded as an EC row.
- 1.5.0 (T78 German doctor report, T70 silent "Did it help?") is not blocked: no shared file, string key or decision.
