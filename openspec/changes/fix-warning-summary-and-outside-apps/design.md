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
- `SettingsPages.kt:185`, `:209-211`: the Privacy page's policy link opens through Compose's `AndroidUriHandler`, which
  calls `startActivity(ACTION_VIEW)` with no task flag (ui-android 1.9.3): the same path as #280 (code review finding 4).
  No other `LocalUriHandler` or `openUri` use exists in main code.

The real moment for #280 is rare (an outside app that crashes at launch), but its cost is the person's whole place in
the app, without a word. The real moment for #288 is someone scanning Settings for "what do I do now?": the row must
point to both actions, the same words they will find on the page.

## Goals and non-goals

Goals: the row summary says both tiers in the page's words (EN, DE); on API 28 and later an outside app's crash at
launch never closes Migrely; no other visible change except that Google Play and the browser are now their own card in
Recents (the share target already was, on AOSP).

Non-goals: other outside starts (report PDF share, paywall "Manage", notification settings; filed as #291); a
workaround for API 26–27 (D9); crash detection; new TalkBack focus code; the site (already fixed by PR #290); release
notes.

## Decisions

### D1. One change for both fixes

Same module, same screen, same capability (`app-settings`), two ADDED requirements that do not touch each other's text.
One branch gives one gate, one code review and one QA pass on a loaded host. Spec hygiene does not suffer: each fix has
its own requirement, scenarios and EC rows, and either can be reverted alone (two files each).

### D2. No new D-number

#288 breaks D171's two tiers; #280 breaks D185's "the app keeps running" (EC-SHR-01). The BRD records them as EC-SAF-18,
EC-SHR-14…17 and §13 row 62. The only new choices (the strings, the task flags) are inside those decisions.

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

### D5. Share: the flag on the inner `ACTION_SEND` stays, as a guard only

Code review (finding 3, AOSP source in `migrely-work/patch141/review-sources/`) corrected the plan's premise. The system
chooser already adds `FLAG_ACTIVITY_NEW_DOCUMENT | FLAG_ACTIVITY_MULTIPLE_TASK` to an `ACTION_SEND` target (API 26
`ChooserActivity.modifyTargetIntent`; API 36 IntentResolver `maybeAddSendActionFlags`), and with no result receiver the
starter turns NEW_DOCUMENT into NEW_TASK. So on AOSP the share target already ran in its own task on `develop`, with a
new Recents card per share, and Share was not affected by #280.

| Option | Effect | Choice |
|---|---|---|
| Flag on the chooser | The chooser becomes a separate task: a Recents card for a system sheet | No |
| Flag on the inner `ACTION_SEND` | No change on AOSP; guards a non-AOSP chooser that drops the system's flags (unproven either way) | **Yes, as a guard** |
| No flag on Share | Same as today on AOSP | No: the guard costs one line |

The chooser copies the target intent with its flags (`ResolverActivity`, IntentResolver `createResolvedIntent`), so the
guard reaches the app. There is no "develop closes Migrely" control for Share: it would not reproduce. The test that
checks the inner flag is named for what it checks ("the ACTION_SEND intent carries NEW_TASK").

### D6. Not `NEW_DOCUMENT` or `MULTIPLE_TASK` for Rate and the privacy page

They would force a fresh task, so Back would always return to Settings even when Play was already open. The cost is a new
Recents card per tap. Accepted instead: with `NEW_TASK` the page may join Play's or the browser's existing task, and Back
first shows that app's earlier page (EC-SHR-15). Migrely does not manage another app's history. (Share keeps what the
system chooser does, D5.)

### D7. No crash detection, no message

`startActivity` returns before the other app runs. A later crash is the other app's; Migrely shows nothing, as after any
successful start. Settings has no message on return (EC-SHR-06).

### D8. TalkBack focus on return: measure, do not code

Returning from another task is a window change; TalkBack decides the focus. The engineer records develop's focus on
return (same task) and the candidate's (own task) on API 36. Pass: the activated row or the top of Settings. If it lands
on "Delete all data" (directly below "Share and rate"), stop and report to PM before writing focus code (EC-SHR-16).

### D9. API 26 and 27: a recorded limit, no workaround (EC-SHR-17)

On Android 8.0 every full-screen app task shares one stack. When an activity crashes, the crash handler finishes it and
then the top activity of the previous task in that stack if it is still paused (`ActivityStack.finishTopRunningActivityLocked`,
oreo). Migrely's MainActivity is still paused while the browser starts, so it is finished as on `develop`. From API 28
(one stack per standard full-screen task) and on Android 11 and 16 the walk stays inside the crashed task, so the fix
works there. API 27 is assumed the same as 26 (source not fetched). Traced in source, not yet seen on a device.

No workaround (KISS): the trigger is rare (an outside app that crashes at launch), nothing is lost (Settings holds no
unsaved entry; every diary entry is saved when made), and opening Migrely again starts at Home. A workaround would mean
delaying or relaunching around another app's crash. QA records the API 26 device result; if it contradicts the source,
PM rewords. No new D-number: it is a platform limit of D185's promise, recorded as EC-SHR-17.

### D10. The privacy policy link goes through the launcher (code review finding 4)

The Privacy page's policy link is the same #280 path in the same module, so it is in scope. It opens through a launcher
method that builds the https `ACTION_VIEW` with `FLAG_ACTIVITY_NEW_TASK` and treats `ActivityNotFoundException` and
`SecurityException` as nothing opening. The behaviour with no browser stays as it is: nothing opens and no message
shows, since the address is printed under the link (`openInBrowserIfAny`'s rule). A Robolectric test clicks the link and
asserts the started intent's flag; it is red on `develop` (Compose's handler sets no flag). The three other places with
this pattern are outside the module and filed as #291.

## Risks

| # | Risk | Mitigation |
|---|---|---|
| R1 | A non-AOSP chooser starts the target in Migrely's task | Inner flag as a guard (D5); unproven on OEM choosers, not testable on our images |
| R7 | API 26–27 still close Migrely on an outside crash at launch | Recorded limit (D9, EC-SHR-17); QA records the device result |
| R2 | Back from a Play page that joined Play's existing task shows Play's earlier page | Accepted (EC-SHR-15); stated in the spec |
| R3 | TalkBack focus on return differs with a separate task | Measured against develop (D8); stop rule |
| R4 | The 78-character English line wraps to 4 lines at 200% | No `maxLines`, row grows; walk at 360 dp EN and DE; one allowed shorter line (D3) |
| R5 | The German line has no native-speaker or clinician review | Recorded limit (T31); medical-reviewer re-checks the shipped file |
| R6 | emulator-5570 may have no Google Play app | Then the Play case is walked on QA's Play image or the owner's phone later; recorded in NOT CHECKED, not assumed |
