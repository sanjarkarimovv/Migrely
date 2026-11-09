# Proposal

## Why

Two S3 issues on Attack details share one cause: the message host is an overlay that knows nothing about what is beneath it or about what happened next.

- **#166 (accessibility).** At 100% font, when a first 10 fails to save, "Couldn't save. Your phone's storage is full." lies over the safety card's
  title, "Sudden, worst-ever pain?". The title is not in uiautomator's tree, and TalkBack does not speak it (7 failed saves seen, 3 spoken). In the
  negative control, a save that passed showed no message, and the title was spoken. At 200% the title is clear. The warning sentence is seen and spoken in
  full every time, so this is not a missing safety warning. D163 (3) accepted it for 1.1.0.
- **#167.** After a failed save, the person frees space and saves again. The 10 is stored and Pain shows "Peak 10/10", yet "Couldn't save" stays, 5 of 5.
  "Storage full" has no duration, and a successful add sends no message, so nothing takes it away. A person may enter the 10 again or think the diary is
  broken.

Measured on `release/1.1.0` 8f275034, debug, emulator API 36, 1080x2400, with `/data` under 64 KB free. Read from the code on c2761cba:
`SafetyCard` is the first child of the scrolling column, and the message is pinned at `MESSAGE_OFFSET_BELOW_TOP_BAR`, exactly over it. Compose removes a
node from the semantics tree when another node fully covers it. So any overlay over the card loses its title at some font or width, and the fix has to stop
the overlap rather than tune an offset.

The design comes from `ux-designer`'s spec (`migrely-work/t53/ux-spec.md`, outside the repo). The decision is BRD D165.

## What Changes

On **Attack details** only:

- **Push, don't cover (D165 (1)).** While the safety card shows, a message stays in its usual place, and the scrolling content starts below the message's
  bottom edge. It uses D52's existing `onMessageBottomChanged`. This "card mode" holds until that message leaves, even if "Got it" closes the card first.
  The message never moves. With no card, D158's overlay is unchanged.
- **The card coming into view is not here.** It ships in hotfix 1.1.1 as D167 (#171, change `fix-safety-card-into-view`). In card mode, D167's
  "top of the content area" starts below the message.
- **TalkBack order (D165 (3)).**
  - The card is spoken first and in full, then the message.
  - The card stays assertive, and in card mode a message is polite.
  - When one action brings both, the message is posted after the card is on screen.
  - The card's title becomes a heading.
- **An error goes when a later save succeeds (D165 (4)).** The write's own message replaces the error, or else the new confirmation "Saved.". "Saved."
  appears only to replace an error. The error clears on success, never on the attempt. Undo and information messages are not taken away. A retry that fails
  again shows the error again and speaks it again.
- **An error belongs to the visit (D165 (5)).** Leaving Attack details, by Back or by going forward, clears its error, and it does not show on return.
  Dialogs, pickers, the background, Android Settings, rotation and process death keep it. Undo and information messages behave as today.
- One new string: "Saved.".
- `in-app-messages` spec fixes found by `ux-designer`. Since D158, "Next month" lies under Undo on History, so the tap-guard requirement's example and two
  of its scenarios were stale.

## Out of scope

- **The other 9 screens with a "Couldn't save" that has no duration:** Home, History, My medications, Edit medication, Log medication, Reminders, Doctor
  report, Your data, Add past migraine. D165 (7) splits them into a follow-up change, and `team-lead` files the issue.
  - **Why split:** neither issue was measured on those screens, and the safety card exists only on Attack details. Most successful writes there send no
    event today, so each screen needs a new success signal from its ViewModel, across 8 modules. That does not fit one reviewable PR with the card
    placement and the TalkBack order, and the safety part should not wait for it.
  - **Known residual until then:** #167 still happens on those screens.
- **D130's new condition** (no review request while an error shows on Home Today). D165 (8) decides it, and the Home follow-up builds it, because that
  change touches Home's message state.
- "Got it" removing the card at once, and the content jumping up. That behaviour is older than this change.
- #88 and #153 (Home), and D158's other accepted overlaps.
- The wording of "storage full". Card text, "Got it" and when the card shows are not changed (D163 (4)).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `in-app-messages`:
  - a message never covers the safety card;
  - TalkBack hears the card first;
  - an error on Attack details goes on a later success and belongs to the visit;
  - "Saved." lasts 8 s;
  - the tap-guard and first-row requirements are corrected.
- `attack-details`: the safety card requirement no longer accepts a covered title.

## Impact

- `docs/BRD.md`: D165, EC-UX-33, EC-UX-34, EC-ATK-22, §13 row 34. D164 and EC-UX-31/32 are taken by `bugfix/161-menu-second-tap`, which is not merged.
  Whichever merges second renumbers.
- `designsystem` `snackbar`: a way for a screen to ask for a polite error, and nothing else. The host already reports the message's bottom.
- `features/attack/presentation`: a success signal for writes that send no message today, so the screen can replace an error.
- `features/attack/ui`:
  - `AttackDetailsScreen.kt`: card mode padding, the card scroll, the message posted after the card, clearing on leave;
  - `AttackMessages.kt` and its mappers: "Saved.";
  - `AttackPainSection.kt`: the heading;
  - the new string `attack_message_saved`.
- No data, schema, permission or Premium change. No health sentence changes, so `medical-reviewer` is not needed.
- BRD ids: D165, EC-ATK-22, EC-UX-33, EC-UX-34. Issues #166 and #167 (`Refs`, never a closing keyword).
