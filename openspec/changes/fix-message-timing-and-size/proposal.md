# Proposal

## Why

Tracker T64, release 1.3.0. Every item predates 1.3.0. The decision is BRD D182. Inputs:

- `ux-designer` spec `migrely-work/t66/ux-spec.md` §B (B1–B3) and findings G7–G9, read from `develop` 40536f49 and not run.
- `fix-messages-everywhere` follow-ups 2 and 5 (archived 2026-11-14, `tasks.md` Follow-ups).
- QA finding qa-undo, from code reading only. The emulators did not boot (`migrely-work/qa-undo-d23/boot-failures.txt`).

product-manager re-read every claim in the code at 40536f49 before planning from it. Nothing here was run.

- **Two timeouts for the same kind of message (follow-up 2).**
  - Home, History, Attack details and the medication screens compute their own timeout:
    `calculateRecommendedTimeoutMillis(..., containsControls = <has Undo>)`.
  - So "Saved." and information messages there follow the non-interactive (reading) timeout.
  - The other hosts use `messageTimeoutMillis()`, which passes `containsControls = true`.
  - With "Time to take action" set to 30 s, "Saved." on Reminders stays 30 s. On Home it leaves after 8 s (QA's finding in
    `fix-messages-everywhere`).
- **A countdown that ignores focus (follow-up 5, WCAG 2.2.1).**
  - Each host runs `delay(remainingMillis(now, timeout))` from the moment the message appeared.
  - A keyboard user who Tabs to "Undo", or a TalkBack user who lands on it, can lose it mid-reach.
- **48 dp controls in the dark room (follow-up 5).**
  - `MigrelySnackbar` gives Undo `heightIn(min = 48.dp)` and Dismiss a default 48 dp `IconButton`. The message has a 52 dp
    minimum height.
  - Attack mode's own controls are 64 dp (EC-UX-02, `AttackModeContents.kt:212, 411`). The one control with a deadline is
    the smallest on the screen.
- **A deletion Undo can outlive the copy it restores (QA qa-undo, confirmed by reading).**
  - Home and History offer a deletion up to 30 s after `deletedAt`: `DELETION_OFFER_MAX_AGE_MILLIS = 30_000`
    (`DeletionOffer.kt:4`, `HomeMessages.kt:104`, `HistoryMessages.kt:84`).
  - The message's own countdown starts when it is shown and stretches to the interactive timeout, up to 120 s.
  - The hidden copy lives `DELETED_RETENTION = 2 min` (`AttackDataSourceImpl.kt:33`). It is purged:
    - on `softDelete`;
    - on every Home and History load, through `recentlyDeleted`, which `onResume` also calls (`HomeViewModel.kt:75`,
      `HistoryViewModel.kt:44`);
    - at app start.
  - `AttackDao.restoreIfPossible` does not check age. It returns false once the row is gone.
  - So the Undo can stay up to about 150 s after `deletedAt`.
  - The case: delete on Details, wait about 20 s, go back to Home or History, lock the phone, unlock at about 122 s.
    `onResume` purges the copy while Undo is still on screen. A tap on Undo says "This can no longer be undone.", and the
    migraine the person wanted back stays gone. Nothing else is lost.
  - A second path is likely but not proven: the countdown's `delay` may not count deep sleep, which stretches the window
    further.

## What Changes

- **Every timed message follows the interactive timeout (D182 (1), ux B1).**
  - All hosts use `messageTimeoutMillis()`.
  - Home, History, Attack details and the medication screens drop their own calculation.
  - Base durations stay: Undo 5 s, the rest 8 s, "storage full" with no duration.
- **The countdown pauses while the message holds focus (D182 (2), ux B2).**
  - It pauses while keyboard focus is on the message's control, or TalkBack focus is on its text or control.
  - Focus has left after 300 ms with neither, or at once when the window loses focus.
  - On resume, at least 3 s are left.
  - The time-left line stops while paused. Nothing is spoken and no label appears.
  - The host saves the time already shown, so rotation and process death keep it.
  - One mechanism in `designsystem/snackbar` serves every host.
  - TalkBack focus needed a spike first. It passed on API 36 (3 runs, 10 of 10). Our API 26 image has no TalkBack, so
    there the TalkBack pause is built but not verified: a known limit in the BRD. The keyboard pause is verified on both.
  - Settings, Onboarding and Paywall keep their message only in memory, so rotation drops it there, as today (#236).
- **An Undo never outlives what it restores (D182 (3), ux B2 ceiling, QA qa-undo).**
  - An Undo leaves no later than 115 s after the action it undoes, paused or not. That is D23 minus 5 s.
  - For a deletion the action time is `deletedAt`. For every other Undo it is the moment the message appeared.
  - Two bounds apply, whichever comes first:
    - the wall clock from the action;
    - a clock that counts sleep and cannot be set, from the moment the message appeared.
  - The bound is re-checked on resume and when the window regains focus.
  - A test ties the ceiling to `DELETED_RETENTION`.
  - Home and History each get a red-first test for the lock and unlock at 122 s case. `develop` 40536f49 is the negative
    control.
- **64 dp message controls in the dark room (D182 (4), ux B3).**
  - Inside an active `MigrelyDarkRoom`:
    - Undo is at least 64 x 64 dp;
    - Dismiss is 64 x 64 dp, with the same 20 dp icon;
    - the message is at least 64 dp tall.
  - Outside the dark room, nothing changes.
  - The host supplies the size, so screens need no code of their own.
- **No new or changed text.** No `medical-reviewer` or legal read is needed.

## Out of scope

- Any change to D23, `DELETED_RETENTION`, the purge, `restoreIfPossible` or the 30 s offer window (D31). The data rule
  stays the same. Only the offer leaves before it.
- Base durations (5 s, 8 s) and the "storage full" message with no duration.
- The message's text, label sizes, colours, placement rules, the 8 dp gap, the tap guard rule and the Tab order (D178).
- A visual or spoken "paused" cue, and any speech change.
- Closing a message with Esc. Making a message indefinite under TalkBack.
- Switch Access highlight as a pause trigger. It may pause if the mechanism sees it as accessibility focus, but nothing
  requires it.
- 64 dp outside the dark room, and any other control on any screen.
- The keyboard work of D180 (`fix-keyboard-follows-cursor`) and the T66 items of D181 (`fix-privacy-wording-and-focus`),
  including the Undo's spoken name. Both merge first.
- Any domain, data, permission, manifest, Room schema or backup change.
- Saving the message across rotation on Settings, Onboarding and Paywall (#236, pre-existing).
- Verifying the TalkBack pause on API 26: our image has no TalkBack and we do not sideload it.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `in-app-messages`:
  - durations follow the interactive timeout;
  - the countdown pauses on keyboard and TalkBack focus;
  - the Undo ceiling;
  - the saved time survives rotation and process death, except on Settings, Onboarding and Paywall (#236);
  - 64 dp controls in the dark room.
- `attack-mode`: the message's controls meet attack mode's 64 dp bar.

## Impact

- **Code:**
  - `designsystem/snackbar`: `MessageTimeout.kt`, `DeletionOffer.kt`, `MigrelySnackbar.kt`, `MessageHostParts.kt`, and a
    new countdown holder.
  - `designsystem/theme/DarkRoom.kt`: a composition local for "inside an active dark room".
  - The message hosts of home, history, attack, medication, reminders, report, backup, settings, onboarding and
    subscription.
  - No domain, data or datasource code.
- **Tests:**
  - designsystem unit tests for the countdown (fake clocks);
  - Robolectric UI tests per host;
  - red-first ceiling tests on Home and History;
  - an `:app` JVM test that ties the ceiling to `DELETED_RETENTION`;
  - uiautomator bounds checks for 64 dp.
- **Docs:** BRD D182, EC-UX-02, EC-UX-16, EC-UX-52…59, EC-ATK-24, D23, §13 row 50, §18.3 B.
- **BRD ids:** D182, D23, D31, D52, D178, EC-UX-02, EC-UX-08, EC-UX-16, EC-UX-52…59, EC-ATK-14, EC-ATK-24, EC-HIS-04.
- **Overlap:**
  - `bugfix/141-keyboard-follows-cursor` (D180) and `bugfix/217-privacy-wording-and-focus` (D181) both edit
    `MigrelySnackbar.kt` and add `in-app-messages` requirements. Neither delta conflicts with this one: they add a keyboard
    requirement and a Home Tab-order requirement.
  - Their BRD rows sit next to these (EC-UX-39…51, §13 rows 48 and 49).
  - Task 0.1 merges `develop` after both land.
- **Risk:** low for data, since nothing stored changes. Medium for UX feel: the 3 s floor and the 300 ms grace are
  judgements that only a device walk can confirm.
