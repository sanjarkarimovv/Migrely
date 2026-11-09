# Design

The source is `ux-designer`'s spec, `migrely-work/t53/ux-spec.md`: its rules are named P1–P8, A1–A5 and C1–C6 below. The decisions are BRD D165. The code
facts are read from `develop` c2761cba and not run. The measurements come from #166 and #167.

## Context

- `ReadyContents` in `AttackDetailsScreen.kt` is a `Box`. It holds a `verticalScroll` column, and `SafetyCard` is the column's first child when
  `safetyCardVisible`. `AttackMessages` is drawn over the column, with `topOffset = MESSAGE_OFFSET_BELOW_TOP_BAR`.
- `MigrelySnackbarHost` already reports `onMessageBottomChanged` in root coordinates. The area grows with the slide-in and shrinks only after the fade-out.
  Landscape attack mode uses it to push its start column (D52). Attack details does not pass it today.
- `MigrelySnackbar` picks its live region from the tone: `Error` is assertive, and every other tone is polite. `SafetyCard` is assertive.
- `shownMessage` and `safetyCardVisible` are `rememberSaveable(attackId)`. So they survive a rotation, a process death and a forward trip, and they go when
  the entry is popped.
- `FirstMaximumRecorded` and then `Message(StorageFull)` arrive as two events. QA heard the message 36 ms before the card.
- `AttackDetailsViewModel.presentWrite`: `Written` with no notification only calls `loadDetails`. So a successful add, a note, a tag or an effect sends
  nothing to the screen.
- `AttackMessageBehaviourUiMapper` gives `StorageFull` `baseDurationMillis = null` and `SomethingWentWrong` `READING_MILLIS` (8 s).

## Decisions

### D1. Card mode pushes the content (P1–P7)

`ReadyContents` takes the message's bottom from `onMessageBottomChanged`. While card mode is on, it gives the column a top padding equal to that bottom
minus the column's own top in root coordinates, never less than 0. Outside card mode the padding is 0, as today.

| Question | Decision | Why |
|---|---|---|
| When card mode starts | When the card shows, or when a message appears while it shows | P1 |
| When it ends | When the card is gone **and** no message that appeared in card mode is still on screen. The host's reported bottom reaching 0 is that moment | P2. Content must never slide back under a message that is still there |
| Animation | None of its own. The padding follows the reported bottom, which already follows the host's timing (250 ms in, 200 ms fade, then 250 ms to shrink) | P6. A second animation would fight the first |
| Taps on the freed band | The host's existing 0.8 s `TapGuard` | P7, D114. Nothing new |
| Where it lives | `features/attack/ui`, not the host | Only this screen has a card. The host stays generic |

Rejected options, as `ux-designer` lists them: a larger offset, the message in the column, and the message at the bottom (D165 (1)).

### D2. The card comes into view: D167, not here

P8 ships in hotfix 1.1.1 as D167 (#171, S1, change `fix-safety-card-into-view`). This change keeps that behaviour as it is. In card mode, D167's
scroll target, the top of the content area, is below the message, because the content starts there. When the hotfix merges into `develop`, rebase this
branch's work on it and keep D167's tests green.

### D3. TalkBack order (A1–A5)

- `MigrelySnackbar` gets one optional parameter that overrides the live region (polite). `AttackMessages` passes it while card mode is on. Every other
  caller is unchanged, so errors elsewhere stay assertive.
- **The message comes after the card (as shipped, 970de3a6).** When `FirstMaximumRecorded` and a message come from one action, and that includes
  "Saved.", the screen stores the message at once and holds only its display. It shows the message 200 ms after the card becomes a live region.
  - The card becomes a live region after D167's 300 ms scroll when D167 scrolls the content back to its top first, and otherwise one frame after the card
    is laid out.
  - The message's 8 s, or an Undo's 5 s, starts when it becomes visible, not when it was stored.
  - A rotation during the hold shows the stored message at once. Leaving the screen clears it, as design D5 says.
  - The order of the events from the ViewModel does not change.
  - **Why 200 ms and not one frame:** Compose 1.9.3 sends accessibility events in 100 ms batches, and in one batch TalkBack spoke the message first
    (#166, 36 ms). Measured on `emulator-5570`: "Saved." spoken 274 ms after the card (W1), that is 200 ms plus the 100 ms batching.
- `SafetyCard`'s title gets `semantics { heading() }`. No text changes.
- **Fallback, decided now:** if QA finds that TalkBack *drops* a polite message after the assertive card (dropped, not delayed), the message goes back to
  assertive and is posted only after the card's announcement has started, and QA measures again. The card is never delayed.

### D4. An error goes on a later success (C1, C2)

| Part | Decision |
|---|---|
| Signal | `AttackDetailsViewModel` emits one new event when a write succeeds with no notification of its own (`presentWrite`'s `Written` branch with `onWritten == null`). It is not a message: it says "a write saved" |
| Who decides | The screen. If `shownMessage` is an error (`StorageFull` or `SomethingWentWrong`), it replaces it with `Saved`. Otherwise it ignores the event. Writes that bring their own message already replace whatever shows (newest message wins) |
| "Saved." | A new `AttackMessageUiModel.Saved`, confirmation tone (check icon), polite, `READING_MILLIS` stretched by the accessibility timeout, body tap closes it, no haptic |
| On the attempt | Nothing is cleared. A retry that fails posts a new error with a new `shownAt`, so the host swaps it in place and the live region announces it again (C2, measured by QA) |

**Why the screen decides.** What is on screen is UI state, and the ViewModel is stateless (`docs/ARCHITECTURE.md`, skill rule 4). The ViewModel only says
that a write succeeded.

### D5. An error belongs to the visit (C3–C5)

When the screen's back-stack entry stops being the current entry, the screen clears `shownMessage` if it is an error. This means Back, and forward to Log
medication, Edit medication or anything else. Undo and information messages are left alone, so nobody loses an Undo by going forward.

It must not fire on a stop that is not a navigation: a dialog, a picker, Home, Recents, the screen turning off, Android Settings, a rotation or a process
death. So it reads the navigation state (the current entry), not the lifecycle. A lifecycle `ON_STOP` would clear on Home, which breaks #167's main path.
The engineer picks the mechanism, and tests both sides.

### D6. Edge cases

| # | Case | Decision |
|---|---|---|
| 1 | Loading, not found, attack deleted | No card, no card mode (D163 (5)) |
| 2 | Card, no message | Card first, D167 scrolls it if needed |
| 3 | A failed first 10 | Card, then the message 200 ms after the card is a live region (D3), polite, content pushed. Both spoken whole, card first |
| 4 | Card showing, then an Undo, information or error message | Card mode: pushed, nothing covered, wherever the person has scrolled |
| 5 | "Got it" while a message shows | The card goes, the message stays put, and the content stays below it until it leaves (P2) |
| 6 | The message leaves in card mode | 200 ms fade, then 250 ms close, with the 0.8 s guard |
| 7 | Error, then a successful write | "Saved." or the write's own message |
| 8 | Error, then a failed write | The error again, spoken again |
| 9 | Error, leave and come back | Gone. The card stays if it was not closed |
| 10 | Error, then Home, Android Settings, back | It stays |
| 11 | Rotation, process death | It stays with its time. Card mode is recomputed from the restored state. No extra saved flag |
| 12 | "Saved." while the card shows | Card mode, polite, so it is heard after the card |
| 13 | Two quick successful writes after an error | The first one shows "Saved."; the second finds no error and is silent. One "Saved." |
| 14 | A late success event after leaving | The screen is not current, so nothing shows. On return the error is already cleared |
| 15 | Free and Premium | Identical. Safety is never Premium |
| 16 | During an attack | The same screen and theme. Nothing new is bright. Attack mode and Dark room are untouched |
| 17 | A message replaces another in card mode (a failed retry, "Saved.") | The host swaps it in place (`replaceInPlace`, keyed per message, no cross-fade), so the new node is never transparent and TalkBack never announces an empty live region (QA TB12, fixed in febbfad3). A message that arrives when none shows still slides in. Outside card mode the fade is unchanged |
| 18 | A tap on the message's area during the 200 ms hold | Ignored. Only the previous message, if any, is on screen, and Dismiss, a body tap and Undo act only on the stored message, which is the held new one (`isShown`, `onClear`). The new message shows when the hold ends |
| 17 | Dark and light, right to left | No new colours. The push is vertical, so the layout mirrors as before |
| 18 | 100, 130, 200% and landscape | Nothing is covered, by construction. A 200% "Couldn't save" in a 411 dp landscape window leaves a short content area, which still scrolls, and "Dismiss" frees it |
| 19 | "Remove animations" | The message, the push and the scroll all happen at once |
| 20 | Clock moved, zone, DST | `shownAt` uses the wall clock as today for every message. Not changed here |
| 21 | Full storage | "Saved." needs no write of its own |
| 22 | Upgrade | No stored data. Saved instance state does not survive an app update |
| 23 | A message posted after the return | It shows as usual |

### D7. Scope (C6) and D130

D165 (7) and (8). This change covers Attack details only. A follow-up applies D4 and D5 to the 9 other screens and adds D130's condition on Home Today.
Attack details is the pattern for it: one success event from each ViewModel, and the screen replaces an error.

## Acceptance (QA)

Setup, from ux §6:
- API 36, 1080x2400, a debug build of this change, and `/data` filled under 64 KB with `fallocate`;
- a migraine with no 10 yet;
- speech from TalkBack's `action=SPEAK` log (VERBOSE), and geometry from uiautomator bounds;
- every check at 100%, 130% and 200% in portrait unless it says otherwise;
- restore font scale, rotation, animator scale, the accessibility timeout and storage afterwards.

Checks 1–17 and 19–23 are ux §6's, with the same pass conditions. Check 18 (the other screens) moves to the follow-up. In short:

| # | Check |
|---|---|
| 1 | Failed first 10: the card's title, sentence and "Got it" are in the tree, the message and card bounds do not intersect, and no content is above the message's bottom |
| 2 | Speech: the full title and sentence come first, then the full message, and neither cuts the other off |
| 3 | A successful first 10 while "Pain level removed / Undo" shows: the card is not covered, and Undo works |
| 4 | Scrolled away, remove a point: Undo is visible and works, and scrolling back, the card is not covered |
| 5 | D167's rows still pass with card mode on: scrolled down, a failed first 10 brings the card's top below the message |
| 6 | "Got it" while the message shows: the message does not move (±1 px), and the content stays below it until it is dismissed |
| 7 | Dismiss in card mode, then tap its place within 0.8 s: the "Started" picker does not open. After 0.8 s it does |
| 8 | Landscape, 100% and 200%: 1 and 2 hold. Record the content height left at 200% in the 609 x 274 dp window |
| 9 | No card: the message lies over the first row exactly as on c2761cba |
| 10 | Fail a 10, free space, add it again: "Saved." within 1 s, the 10 in the DB, "Saved." spoken, and gone after 8 s or the timeout. 5 of 5 |
| 11 | The same with a note, a tag and an effect |
| 12 | Fail, keep the disk full, retry: the error stays and is spoken a second time |
| 13 | Fail, then remove a point: "Pain level removed / Undo", and no "Saved." |
| 14 | Fail, Add medication, Back: no message, and the card is still there. Fail, Back to History, reopen: no message |
| 15 | Fail, Home, Android Settings, return: the error still shows |
| 16 | Fail, rotate: the error stays and the card is not covered. Fail, background, `am kill`, reopen: the same. Record any re-speaking |
| 17 | A normal save with no error: no "Saved." |
| 19–20 | Switch access and Tab order: Back, the message's control, Got it, the content. Separate highlights, each control works |
| 21 | `ar-XB`: mirrored, and 1 and 6 hold |
| 22 | "Remove animations": everything happens at once |
| 23 | Light and dark screenshots at 100% and 200%. A 60 fps capture of 10 and 12 has no frame visibly brighter than its neighbours |

**Negative controls.** Each one shows that its check can fail.

| # | Build | Run | Must show |
|---|---|---|---|
| N1 | Base c2761cba, debug | Check 1 at 100% | The title is not in the tree, as #166 says. If it is, check 1 proves nothing |
| N2 | Base | Check 10 | "Couldn't save" stays after the successful save, as #167 says |
| N3 | Base | Check 14 | The error comes back after the forward trip. That shows D5 does something |
| N5 | This change with only the card-mode padding removed, local | Check 1 at 100% | The title leaves the tree again |
| N6 | This change with only the polite override removed, local | Check 2 | Record the order. This shows what A2 and A3 buy |
| N7 | This change | Check 17 | No "Saved.", the control for D4 |
| N8 | This change | A first 10 that saves, no message | The title is spoken, as in #166's control |

## What the check will not prove

- A human hand, a real phone, or TalkBack's queueing on other versions. These are emulator runs with injected input.
- How the push feels at 200% with a 3–4 line message. It needs eyes on a screen.
- 130%, and landscape windows smaller than 411 dp.
- The 9 screens left for the follow-up, where #167 still happens.

## Risks

- **TalkBack drops the polite message.** Fallback in D3.
- **A clear on leave that fires on Home** would break #167's main path. Checks 15 and 14 test both sides, and D5 says to use navigation, not lifecycle.
- **Padding from root coordinates** could be off when the window has insets or is in landscape. Check 1 is run in landscape, and N5 shows the check can
  fail.
- **The BRD numbers clash with `bugfix/161-menu-second-tap`** only if the same numbers are taken. That branch has D164, EC-UX-31/32 and row 33, and this one
  has D165, EC-UX-33/34 and row 34. Whichever merges second keeps numeric order.

## Migration

None. No data and no setting. To back out, drop the padding, the scroll, the polite override and the success event.
