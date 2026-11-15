# Proposal

## Why

The 1.3.0 polish pass, tracker T66. Every item predates 1.3.0. The decision is BRD D181. Inputs:

- `ux-designer` spec `migrely-work/t66/ux-spec.md`, sections A1 to A6 and findings G1 to G6, read from `develop` 40536f49
  and not run.
- `legal-finance-analyst` final strings in `reports/2026-11-15-legal-t66-wording.md` (DRAFT, needs a lawyer).

product-manager checked each claim against the code at 40536f49 before planning from it.

- **The app's privacy text breaks its own rule (D179 (2)).**
  - `settings_privacy_offline` says "Migrely has no internet access, so it sends nothing anywhere", and `your_data_note`
    says "no internet access". The regex `internet access|sends nothing|never leaves|nothing leaves|100% offline` finds
    exactly these 2 in `res/values*`. That overclaims:
    - the app holds `ACCESS_NETWORK_STATE` (D166);
    - Android's own backup may copy entries to the Google account, without end-to-end encryption on Android 8
      (`backup_rules.xml`);
    - `<device-transfer>` in `data_extraction_rules.xml` moves entries, including the automatic pre-import copies in
      `file/backups/`, to a new phone;
    - an exported file goes wherever the user sends it.
  - The page says nothing about Google Play. The store listing does, and the app should not say less than the store.
  - The site, listing and policy already use the new form. The app is the last channel.
- **#217 (S4): TalkBack reads onboarding's Skip twice**, as "Skip this step. Skip. Button". `StepHeader`
  (`OnboardingScreen.kt`) sets a `contentDescription` on the `TextButton`, and the child `Text("Skip")` stays in its
  semantics. The same code shape exists in:
  - the message's Undo (`MigrelySnackbar.kt`, `SnackbarEndControl`), on every screen;
  - attack mode's "Details" and the "Started earlier?" chips (`AttackModeContents.kt`);
  - the visit's "Remove" (`DoctorVisitSection.kt`);
  - "Open settings" (`RemindersScreen.kt`). Its spoken label, "Open notification settings", does not contain the visible
    "Open settings" in order, so Voice Access's "Tap Open settings" cannot be relied on.
- **Follow-ups left open by `fix-messages-everywhere`** (archived 2026-11-14, Follow-ups section):
  - **1.** On History, Shift+Tab from the Period switch lands on a stop with no visible focus.
  - **3.** On Home, Shift+Tab skips the message when nothing focusable follows Settings in its column.
    `TodayKeyboardOrder.afterSettings` keys the redirect on that group's own `onExit`, which never fires when the group
    has no focusable child.
  - **4.** A code comment in `TodayKeyboardOrder.kt` about the content's `onExit` is wrong (code-reviewer cr-msgs2).
  - **6.** "Log medication" breaks inside the word ("medicatio / n") at 360 dp and 200% on Home Today. `QuietLinks` always
    places the two links side by side with `weight(1f)`.
  - **7.** Edit medication, Log medication and Add past migraine have no Tab-order test of their own. No code moves
    focus to the field after a failed Save, or to Started after "Change the times".

## What Changes

- **Privacy wording (D181 (1), ux A1, legal-t66).**
  - The 5 strings become legal's final text, verbatim. One string is new: `settings_privacy_purchase`, the page's second
    paragraph.
  - The Privacy page reads in 7 stops: internet permission, Google Play, phone, files, delete, Android backup, policy
    link.
  - `app-settings` lines 17 and 114 stop saying "sends nothing" and "no internet".
- **One control, one spoken name (D181 (2), ux A2 and A2b, #217).**
  - When a control's spoken label differs from its visible text, the visible text leaves the semantics.
  - The spoken label contains the visible words in order (label in name).
  - Onboarding's Skip is read "Skip this step, Button".
  - `reminders_open_settings_description` becomes "Open settings for notifications".
  - The engineer sweeps every site of the shape and proves each double reading with a red test before fixing it.
- **Every keyboard stop is visible and in view (D181 (3), ux A3).**
  - On History, Shift+Tab from the first control of the chosen day's section lands on the chosen day, with its ring
    visible and the cell on screen.
  - The engineer first proves which node is the invisible stop. ux's two causes are guesses.
- **Home Today's order is symmetric (D181 (4), ux A4).**
  - With nothing focusable after Settings: Settings, message, "Migraine started", and the exact reverse.
  - The comment from follow-up 4 is corrected.
- **No label breaks inside a word (D181 (5), ux A5).** The quiet links stack, full width and at least 48 dp each, when
  either label's longest word does not fit its half.
- **Tab order for three screens (D181 (6), ux A6).**
  - Each of Edit medication, Log medication and Add past migraine gets order tests for Tab and Shift+Tab.
  - Focus never selects a radio option.
  - New focus moves:
    - a failed Save moves focus to Name or Usual dose;
    - "Change the times" moves focus to Started;
    - logging by keyboard clears focus and moves it nowhere;
    - a picker returns focus to the row that opened it.

## Out of scope

- **T64, ux G7 to G9** (follow-ups 2 and 5), a separate change with its own D-number:
  - every message following Android's interactive timeout;
  - pausing the countdown while a message holds focus;
  - 64 dp message controls in the dark room.
- A disabled Pain-free switch is not a keyboard stop, so a keyboard user never meets its reason. It goes to the backlog
  (ux A3 observation).
- The privacy policy's text and date, and the site's strings (`website/build.py`, which already follow the rule).
- `docs/release/data-safety.md` (the `ACCESS_NETWORK_STATE` line) and `docs/release/content-rating.md:18` ("the app has
  no internet access"). These are findings for `aso-seo-specialist`. The form answers stay correct.
- Re-capturing store slide 06 as `06-private-v2` (`aso-seo-specialist`, only after 1.3.0 is live).
- The About page's "Ask for the internet permission" promise.
- Checking Android 8's backup on API 26 with `bmgr`. The wording says "may" and is true either way.
- Any Undo spoken label's words. All already start with "Undo".
- Any change to permissions, stored data, backups, prices, safety text or health wording.
- Right-to-left mirroring. The app is English only and does not mirror. Tab order is logical, so the rules hold
  either way.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-settings`: the settings list's reason and the Privacy page's text and order.
- `data-backup`: Your data's note names every way entries can leave.
- `onboarding`: Skip has one spoken name.
- `app-shell`: one spoken name per control, every keyboard stop visible and in view, and no word broken inside.
- `attack-history`: Shift+Tab from the chosen day's section returns to the chosen day.
- `in-app-messages`: Home Today's keyboard order when nothing follows Settings.
- `today-sky`: the quiet links stack instead of breaking a word.
- `medication-list`: Edit medication's keyboard order and focus after a failed Save.
- `medication-intakes`: Log medication's keyboard order and focus after logging.
- `past-attacks`: Add past migraine's keyboard order and focus after "Change the times".

## Impact

- **Code:**
  - strings in `features/settings/ui`, `features/backup/ui` and `features/reminders/ui`;
  - `SettingsPages.kt` (one paragraph);
  - semantics at the swept sites (`OnboardingScreen.kt`, `MigrelySnackbar.kt`, `AttackModeContents.kt`,
    `DoctorVisitSection.kt`, `RemindersScreen.kt`, and any site the sweep confirms);
  - `TodayKeyboardOrder.kt`, `TodayContents.kt` (`QuietLinks`);
  - History's calendar or day section, depending on what the proof shows;
  - `EditMedicationScreen.kt`, `LogMedicationScreen.kt`, `AddPastAttackScreen.kt`.
  - No domain, presentation, data, DI or navigation change.
- **Order:** code starts after `fix-keyboard-follows-cursor` (D180) merges into `develop`, because it edits Edit
  medication, Log medication, Home Today and Settings insets. Task 0.1 merges `develop` in first.
- **Tests:** new Compose tests per item, each red on 40536f49. `DoctorVisitScreenTest.kt:217` changes with the string.
- **QA-owned:** `.maestro/a11y-tour/settings-privacy.yaml:13` asserts the old first paragraph. Task 6.1 is for
  `qa-engineer`, not the engineer.
- **Other owners:**
  - slide 06 re-capture, data-safety and content-rating wording (`aso-seo-specialist`);
  - tracker rows (`team-lead`);
  - owner approval of the 5 strings, which legal asked for (`team-lead`, task 0.2).
- **Not affected:** permissions (`:app:checkReleasePermissions` stays the same), Free and Premium, the dark room's
  colours, the policy.
- **No health sentence changes**, so no `medical-reviewer` read is needed.
- **BRD:** D181; EC-STO-11, EC-STO-12, EC-ONB-07, EC-UX-45 to EC-UX-51; §13 row 49. D93, D97 and §11.3 now point to
  D181.
