# Design

## Context

Base: `origin/develop` 476f7a27. 1.4.0 (vc16) is in Google review; this change is the first content of the 1.4.1 patch.
Both fixes live in `features/settings/ui`:

- `settings_warning_signs_summary` in `values/strings.xml:26` and `values-de/strings.xml:23`, shown by `SettingsScreen`
  (`LinkRow`, line 340). No test or Maestro flow pins today's text (`git grep` on develop finds it only in the two
  resource files).
- `ExternalAppLauncher` builds three intents: `storePageInPlay()` (`market://`, package `com.android.vending`),
  `storePageOnWeb()` (https) and `shareChooser()` (`Intent.createChooser` around `ACTION_SEND`). `SettingsScreen` passes
  `context::startActivity` of the activity. None sets a task flag, so each outside screen joins Migrely's task.

The real moment for #280 is rare (an outside app that crashes at launch), but its cost is the person's whole place in
the app, without a word. The real moment for #288 is someone scanning Settings for "what do I do now?": the row must
point to both actions, the same words they will find on the page.

## Goals and non-goals

Goals: the row summary says both tiers in the page's words (EN, DE); an outside app's crash never closes Migrely; no
other visible change except that Google Play, the browser or the share target is now its own card in Recents.

Non-goals: other outside starts (report PDF share, paywall "Manage", notification settings); crash detection; new
TalkBack focus code; the site (already fixed by PR #290); release notes.

## Decisions

### D1. One change for both fixes

Same module, same screen, same capability (`app-settings`), two ADDED requirements that do not touch each other's text.
One branch gives one gate, one code review and one QA pass on a loaded host. Spec hygiene does not suffer: each fix has
its own requirement, scenarios and EC rows, and either can be reverted alone (two files each).

### D2. No new D-number

#288 breaks D171's two tiers; #280 breaks D185's "the app keeps running" (EC-SHR-01). The BRD records them as EC-SAF-18,
EC-SHR-14…16 and §13 row 62. The only new choices (the strings, the task flags) are inside those decisions.

### D3. The strings come from the page, and "or" is not allowed

| Key | Value | Status |
|---|---|---|
| EN `settings_warning_signs_summary` | When to call your local emergency number and when to get urgent medical advice | medical-reviewer PASS 2026-11-27 |
| DE `settings_warning_signs_summary` | Wann den Notruf wählen, wann dringend ärztlichen Rat holen | medical-reviewer PASS 2026-11-27 |
| EN shorter line, only if the 200% walk demands it | When to call your emergency number and when to get urgent medical advice | medical-reviewer allowed |

ux-designer's EN candidates with "or" failed medical review: "or" reads as "either action is fine", and the page never
offers a choice. The row has no `maxLines`, so a 78-character line wraps (about 4 lines at 200% on 360 dp, an estimate).
The shorter line is used only if the device walk shows a defect, and then the spec and EC-SAF-18 change first. Source:
`migrely-work/288/ux-copy.md`, `migrely-work/288/medical-verdict.md`. Not a clinician review (T31 open); no screen may
call it medically reviewed.

### D4. Rate: `FLAG_ACTIVITY_NEW_TASK` on both intents

Both `storePageInPlay()` and `storePageOnWeb()` add the flag, so the fallback is covered too (the #280 crash was on the
https fallback). The order of tries, the `SecurityException` and `ActivityNotFoundException` handling and the messages
do not change. The "One opening per tap" guard is unchanged: Settings still pauses when the other task comes in front.

### D5. Share: the flag on the inner `ACTION_SEND`, not on the chooser

| Option | Effect | Choice |
|---|---|---|
| Flag on the chooser | The chooser becomes a separate task: a Recents card for a system sheet; Back from an empty sheet leaves a task, not a dialog | No |
| Flag on the inner `ACTION_SEND` | The chooser stays a sheet over Settings in Migrely's task; Android's chooser starts the picked app with the target intent's flags, so it gets its own task | **Yes** |
| No flag on Share | A share target that crashes can close Migrely, as in #280 | No |

That the platform chooser keeps the target intent's flags is expected from AOSP's `ResolverActivity`, not proven here.
The engineer proves it on a device with `adb shell dumpsys activity activities` (the chosen app's task id differs from
`MainActivity`'s) on API 36 (emulator-5570) and QA on API 26. If it does not hold on either, stop and report to PM:
putting the flag on the chooser changes Back and Recents and is a new decision.

### D6. Not `NEW_DOCUMENT` or `MULTIPLE_TASK`

They would force a fresh task, so Back would always return to Settings even when Play was already open. The cost is a new
Recents card per tap. Accepted instead: with `NEW_TASK` the page may join Play's or the browser's existing task, and Back
first shows that app's earlier page (EC-SHR-15). Migrely does not manage another app's history.

### D7. No crash detection, no message

`startActivity` returns before the other app runs. A later crash is the other app's; Migrely shows nothing, as after any
successful start. Settings has no message on return (EC-SHR-06).

### D8. TalkBack focus on return: measure, do not code

Returning from another task is a window change; TalkBack decides the focus. The engineer records develop's focus on
return (same task) and the candidate's (own task) on API 36. Pass: the activated row or the top of Settings. If it lands
on "Delete all data" (directly below "Share and rate"), stop and report to PM before writing focus code (EC-SHR-16).

## Risks

| # | Risk | Mitigation |
|---|---|---|
| R1 | A platform chooser drops the target's `NEW_TASK` | Device proof on API 26 and 36 (D5); stop rule |
| R2 | Back from a Play page that joined Play's existing task shows Play's earlier page | Accepted (EC-SHR-15); stated in the spec |
| R3 | TalkBack focus on return differs with a separate task | Measured against develop (D8); stop rule |
| R4 | The 78-character English line wraps to 4 lines at 200% | No `maxLines`, row grows; walk at 360 dp EN and DE; one allowed shorter line (D3) |
| R5 | The German line has no native-speaker or clinician review | Recorded limit (T31); medical-reviewer re-checks the shipped file |
| R6 | emulator-5570 may have no Google Play app | Then the Play case is walked on QA's Play image or the owner's phone later; recorded in NOT CHECKED, not assumed |
