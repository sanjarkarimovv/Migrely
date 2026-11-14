# Proposal

## Why

D165 fixed the in-app message on Attack details and left the rest for this change. Four open issues share one component,
`MigrelySnackbarHost`, and the 13 screens that use it. All four predate 1.2.1. They were confirmed by reading `develop` 52c12ce5
and were not run here.

- **#181 (S3, data trust).** On 9 other screens, "Couldn't save. Your phone's storage is full." has no duration. When a later
  write succeeds, the message stays. Those screens send nothing to the UI on a silent success. Only `AttackDetailsViewModel`
  emits `Saved`. A person may enter the same thing again or decide the diary is broken. Home also starts the in-app review request
  (D130) while the error shows, which D165 (8) ruled out.
- **#185 (S3, accessibility).** Every screen places its message host after its content (`AttackDetailsScreen.kt:313-331`,
  `SettingsScreen.kt:218`, `HomeScreen.kt:321` and the rest). Compose's Tab order follows placement, so a message's Dismiss or
  Undo is the last stop. On Attack details, Tab inside the note types a tab character, so QA never reached Dismiss in 25 presses.
- **#183 (S3, accessibility).** `MigrelySnackbar.kt:353-357` fades a message in from alpha 0. Compose hides a transparent node from
  accessibility, so a polite live region can be announced with no text. QA proved this on Attack details (TB12). D165 fixed it
  there only, through `replaceInPlace` in card mode. It has not been reproduced on another screen yet.
- **#88 (S4, accessibility).** D158 fixed portrait. In a short window (a phone in landscape), Home Today keeps the message
  centred at the top (`HomeScreen.kt:199-200`, `314-321`). Settings sits under it. For the whole Undo window (5 s, or up to the
  accessibility timeout, which can be 2 minutes) Settings does not respond to a tap and, when fully covered, is missing from
  TalkBack's tree. A tap no longer fires Undo, which D158 measured. #153 has the same cause: at 274 dp and 200% font the
  message covers the top of "Migraine started".

The decision is BRD D178. The UX answers are in `migrely-work/t63/ux-spec.md` (ux-designer, 2026-11-14). `team-lead` accepted
its findings F1–F8.

## What Changes

- **An error goes when a later save succeeds, on 9 screens (D178 (1)).** These are Home, History, My medications, Edit
  medication, Log medication, Reminders, Doctor report, Your data and Add past migraine.
  - The write's own message replaces the error.
  - On Home, History and Reminders, where some writes succeed silently, the new confirmation "Saved." replaces it instead.
  - On Doctor report, a created report clears the error with no message.
  - The rule is the same as D165 (4) on Attack details: it clears on success, never on the attempt. Undo and information messages
    stay. A retry that fails is announced again.
  - On Reminders, a visit date that triggers a permission request also counts as a save (F4).
- **An error belongs to the visit, on the same 9 screens (D178 (2)).** Back or going forward clears it. Dialogs, pickers, the
  system file picker, the share sheet, the background, Android Settings, rotation and process death keep it.
- **Review request on Home Today (D178 (3), F3).** It waits while any message shows, and its 2 seconds start when the message
  leaves. After an error, no request comes for the rest of that Home visit.
- **Order of messages for keyboard, Switch Access and TalkBack, on all 13 screens (D178 (4)).** The order is the top bar first,
  then the message's text and its Dismiss or Undo, then the content. On a screen with no top bar the message comes first. One
  shared mechanism in `designsystem` provides this. Focus clears when the message leaves or is replaced, and a leaving message is
  not a stop (F8). The message's place, size and drawing do not change. After a replacement, the tap guard covers the taller of
  the two messages (F2).
- **A message is never announced empty, on all 13 screens (D178 (5)).**
  - A replacement switches in place with no cross-fade on every screen. This is the path D165 already uses in card mode.
  - When the new message is shorter, content tied to its bottom eases up over 250 ms. When it is taller, that content moves at
    once (F1).
  - A message that arrives when none is showing still slides in, starting at alpha 0.02, so it is never fully transparent while in
    the accessibility tree.
  - No new flashing and no faster motion.
- **Home Today in a short window (D178 (6)).** The message moves to the start column, as landscape attack mode already does,
  and sits 10 dp below the Settings row's measured height. It then covers neither Settings nor "Migraine started". This replaces
  D158's exception for that window. The same measured offset applies in portrait Home Today (F6).
- **A fit rule in the host (F5).** A message is always whole inside the safe area. It rises only as far as needed, and never
  above where it sits today.
- **New string:** "Saved." in the home, history and reminders UI modules, the same text as in the attack module.

## Out of scope

- **Keyboard and cursor behaviour:** the field following the cursor, `imePadding`, `windowSoftInputMode` and the top bar panning
  off (#141, #83, #145). These belong to the next change, `fix-keyboard-follows-cursor`. That change also edits
  `AttackDetailsScreen.kt`, so this one merges first.
- **Timed errors:** Settings, onboarding and the paywall. Their errors leave after 8 s by themselves, so #181 does not apply.
- The paywall trigger while an error shows on Home. D165 (8) named only the review request.
- Closing a message with Esc, and any change to message text other than the new "Saved.".
- **Ux F9, deferred to a later change** (`team-lead` adds a tracker row):
  - pausing a message's countdown while it holds keyboard or TalkBack focus (WCAG 2.2.1);
  - growing the message controls in attack mode from 48 dp to 64 dp, to match the dark-room bar.
  No message size changes here.
- D158's other accepted overlaps: Remove in row 1 of My medications, the History chevrons and the trial banner's close button.
- The cramped short-window layout of #153 itself (the button's top under the status bar). Only the message part goes. #153
  stays open for QA to retest.
- #179, #149, #153's layout part, #118, #150 and #82.
- Any domain or data change, any stored value, permission or migration.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `in-app-messages`:
  - an error on the 9 screens goes on a later success and belongs to the visit;
  - the order for keyboard, Switch Access and TalkBack;
  - never announced empty, with a replacement switching in place;
  - the Home Today short-window placement;
  - durations and motion reworded to match.
- `in-app-review`: home does not settle while an error, or the "Saved." that replaced it, shows.

## Impact

- `docs/BRD.md`: D178, EC-UX-35…38, EC-REV-13, §13 row 46. EC-UX-16 and EC-UX-34 updated. All numbers were free on `develop`
  and every remote branch on 2026-11-14.
- `designsystem/snackbar`:
  - the host never shows a transparent message node;
  - a replacement switches in place by default;
  - a shared layout or modifier sets the focus and traversal order.
- `features/{home,history,reminders}/presentation`: a "write saved" event for writes that send no message today.
- `features/{home,history,reminders}/ui`: a `Saved` message and its string.
- `features/{home,history,medication,reminders,report,backup,attack}/ui`:
  - replace the error on success;
  - clear it on leave with the existing `ScreenLeaveObserver`.
- `features/{settings,subscription,onboarding,…}/ui`: the order mechanism only.
- `features/home/ui`:
  - the review slot is gated on the message;
  - the short-window message sits in the start column, below the Settings row.
- No new permission, no network, no SDK, no stored data. Free and Premium identical.
