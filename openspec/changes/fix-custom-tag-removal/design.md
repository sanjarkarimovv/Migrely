# Design

## Context

Base `origin/develop` 476f7a27. Design of record: ux-designer's ruling `migrely-work/patch141b/ux-rulings.md` sections 2
and 3 (2026-11-27). Code read:

- `designsystem/.../dialog/MigrelyMenus.kt`: `MigrelyDropdownMenu` wraps Material's `DropdownMenu` with the D164 lift
  guard. Its only caller is `ChoiceChip` in `features/attack/ui/.../AttackChoiceSections.kt` (~line 288).
- `ChoiceChip`: long press opens the menu; the menu item and a `CustomAccessibilityAction` both call `onArchive()`, which
  reaches `AttackDetailsViewModel.onArchiveCustomTagAction` → `ArchiveCustomTagUseCase` →
  `setCustomTagArchived(id, true)`; `presentWrite` reports only failures.
- Archived tags still used by an attack stay on it (`AttackDetailsPresentationMapper`). `AddCustomTagUseCase` brings back an
  archived match by name and enforces the 100 limit (`LimitReached`).
- Attack details already shows "Pain level removed" and "Medication removed" with Undo (`AttackMessages.kt`,
  `snackbar_undo_*_description`), "Undone." (`attack_message_undone`) and "Your list is full…" (`message_tag_limit`).

The person's moment: during or after an attack, at large text, they long-press a tag to tidy their list, or a TalkBack user
explores the actions menu. A slip must never silently delete what they entered.

## Goals and non-goals

Goals: the menu never sits on its chip; every removal is announced with a way back, and spoken on every path; an
archived chip offers no second remove; #262's wording matches the code and QA's threshold.

Non-goals: other menus (there are none), other Undo messages, tag editing, any keyboard-follow change, and focus after a
removal or an Undo (team-lead scope decision 2026-11-27: the keyboard and focus family is where #259 came from; moved to
#296 for 1.5.0, D4).

## Decisions

### D1. Its own change, parallel to #275

Different modules (`designsystem` menus and `features/attack` against `designsystem` sky and `features/history`) and
different decisions (D199 against D198). #262 rides here because it is about Attack details (the note) and this change
already modifies `app-shell`; it is a docs-only part of this plan (no code task), so it can merge even if the #179 code
slips, by reverting nothing.

### D2. Placement in the shared menu helper, by a position provider

Replace Material's default placement in `MigrelyDropdownMenu` with a `PopupPositionProvider` (or Material's
`DropdownMenu` `offset` plus a measured cap, engineer's choice) that implements P1–P3: below if the menu fits between the
anchor's bottom and the safe bottom with 8 dp; else above with 8 dp; else the larger side, height capped to it (the
menu column already scrolls). Safe edges: `WindowInsets.systemBars`, `displayCutout` and `ime`. The lift guard (D164) and
the "every popup goes through a helper" rule stay; no wait, no new motion.

| Option | Result | Choice |
|---|---|---|
| Own position provider in the helper | Exact rule, one place, testable with bounds | **Yes** |
| Lower Material's margin | Still overlaps when neither side fits | No |
| Open a dialog instead of a menu | New pattern, more taps | No |

Device fact (walk 2026-11-27, emulator-5570, API 36): the "no room for one item" strip is not reachable by a finger. The
menu is a focusable popup, as Material's always was, so it takes window focus; Gboard closes by itself and the menu opens
full size below the chip, partly under the sliding keyboard for about 3 frames, never on its chip. Robolectric reaches
the strip only because the test pins the keyboard inset, so that test proves the placement arithmetic, not what a person
meets. ux-designer's rule N1 (hide the keyboard first, then open the menu) is therefore dropped: Android already does it,
and no code is added for it (team-lead decision 2026-11-27).

### D3. Message and Undo through the existing message system (D199 (2), (3))

A new `AttackMessageUiModel` case for a removed tag carries the tag id and name. The view model emits it after a successful
archive. Undo calls a new `RestoreCustomTagUseCase` (domain) that counts the active tags of that kind first: at 100 it
returns `LimitReached` (→ `message_tag_limit`), else `setCustomTagArchived(id, false)` (→ "Undone."). Failures map to the
screen's existing save-error messages. Timing, replacement and leaving rules are the existing ones (D178, D182): a second
removal replaces the message, leaving the screen ends it. The restore use case reuses `CountActiveCustomTagsRepository`
and `SetCustomTagArchivedRepository`, as `AddCustomTagUseCase` does; the restore needs the tag's kind, which the message
carries.

Strings (`features/attack/ui`):

| Key | EN | DE |
|---|---|---|
| `message_tag_removed` | Removed from your list | Aus deiner Liste entfernt |
| `snackbar_undo_tag_description` | Undo removing %1$s | Rückgängig: %1$s zurück in deine Liste |

The visible button reuses `attack_snackbar_undo`. The German follows the spoken-name rule (starts with the visible word,
EC-UX-45); the existing German medication Undo name does not, so it is not a model. ux-designer's shipped-file check
(ux rulings §5.3, 2026-11-27): `message_tag_removed` PASS; `snackbar_undo_tag_description` changes one word, "die Liste"
→ "deine Liste", to match "Aus deiner Liste entfernt" and "Deine Liste ist voll". No health statement: no
medical-reviewer.

**Spoken on every path.** The device walk found the message silent on TalkBack's menu path (0 of 6 runs; spoken on the
custom-action path). TalkBack's log: the content-change events sent while the popup closes fail with "node is null", and
no live-region event is handled, so the announcement is dropped, not interrupted. The message must be spoken once on
every path. The engineer picks the mechanism (for example, show or announce the message only after the popup's window is
gone); the acceptance is on a device: TalkBack's log shows the live-region utterance, 3 of 3 on the menu path and 3 of 3
on the action path. The JVM test pins the chosen mechanism's observable condition and is red on 2d3ca53f.

**Message placement (PM decision 2026-11-27).** The walk saw the message over the page's first row of chips, and at 200%
over part of the top bar's date line. This message follows the screen's existing placement ("Messages appear at the top
and stay dim", D158, D178), as "Pain level removed" does; the spec says so instead of promising "covers no control",
which no message on this screen keeps. Whether the 200% overlap of the top bar's date line breaks that existing rule is a
question for the message host, not for #179: QA compares it with develop's "Pain level removed" at 200% and files it if
the same (pre-existing).

### D4. Focus: out of this change (#296)

Team-lead scope decision 2026-11-27. The device walk showed focus after a removal is pre-existing (develop equals the
branch, 2 of 2 each): TalkBack and keyboard focus land on the chip that slides into the removed chip's place; TalkBack says
its state ("Not selected") about 1 s before its name; a double tap in the first ~2 s selects that neighbour on this attack
(an `attack_tag` row). After the last chip, focus lands on "Add your own symptom". Fixing it needs focus code in the chip
group, the family where #259 came from, so it moves to #296 (S3, 1.5.0) with ux-designer's rules K1, K3, K4 (keyboard)
and K5 (TalkBack's landing line). This change adds no focus code and promises no focus target. D199 (5) states the limit
truthfully: the promise "the next double tap never toggles another chip" is withdrawn, since it cannot be met and is not
a regression; a toggle is visible, saved and reversible.

### D5. #262: wording only

The reopen scenario, the requirement text and the "Remove animations and API 26" scenario all say: less at most 1 px of
rounding, never below 1 px, measured against what showed before the keyboard, not frame to frame. BRD D194 H2 is reworded
in place. QA's threshold: `kept_px ≥ max(1, min(shown_px, line_with_clearance_px) − 1)`, with the measured line for the
device (154 px is only the 200%, 360 dp landscape value). No code; #262 can close after QA re-reads its rows against the
new threshold.

### D6. If the patch must shrink

Phase 1 = placement (tasks 2) and the message without Undo (tasks 3.1–3.3, 4.1–4.2). Phase 2 = Undo (3.4–3.6, 4.3). The
task list is ordered so phase 1 can merge alone; then the PM moves the Undo scenarios to a follow-up change before
archive.

### D7. An archived chip offers no remove (EC-ATK-28; ux K2)

`ChoiceGroup` and `ChoiceChip` never read `archived`, so a tag that is archived but still shown on an attack (EC-ATK-11)
keeps the long-press menu, its label and the TalkBack action, on develop and on the branch. Using it shows a second
"Removed from your list" with an Undo whose effect is not established. Fix: pass no `onArchive` for an archived tag, so it
has no menu, no long-press label and no custom action until Undo un-archives it. A pure fix under EC-ATK-11 and D199 (3):
an EC row (EC-ATK-28) and one clause in D199 (3), no new D-number. Red-first Compose test on 2d3ca53f.

Code review d4b2dcac (S1, lead decision 2026-11-28): with `onLongClick = null`, Foundation 1.9.3's tap detector waits for
the finger to lift however long it was held and calls the tap, so a long press on an archived chip deselected it, and
the chip then left the attack with no message and no Undo: new in this change (on develop the long press opened the menu
and toggled nothing). Rule: a long press on an archived chip does nothing, with no long-click semantics (so no long-press
label) and no haptic promise; a plain tap keeps toggling as before (pre-existing, EC-ATK-11). Engineer's mechanism, for
example a long-press handler that consumes and does nothing, with no `onLongClickLabel`; the test asserts the selection
is unchanged after a finger long press and after TalkBack's long-click action is absent.

### D8. The item's effect runs once the menu has gone, never after a cut-short closing (code review S2)

Since 4.4 the menu item's effect (the removal) runs when the menu's closing has finished, so the message is spoken
(about 0.3 s later; at once with "Remove animations"). Input is still taken at once, so "A popup menu answers at once"
holds; the ADDED requirement now says the effect waits for the window to go (review N2). The review found that `onClosed`
also ran when the composition was disposed partway through the closing: a rotation, a split-screen resize, or a uiMode or
font change in that ~0.3 s archived the tag while the message was lost with the old screen (a `MutableSharedFlow` with no
replay), which is #179's silent removal again. Rule (lead decision 2026-11-28): the effect runs only when the closing
finished; a pick cut short by a configuration change is dropped, and the tag stays visibly listed, so the person can
remove it again. That is the safe direction: nothing changes without being seen. Back and Home cannot hit the window (the
popup takes Back; Home pauses the frame clock and the removal runs on return, with its message); process death loses the
pick and the chip stays.

N1 (optional, engineer's call): `MigrelyMenus.kt` copies Material 1.4.0's standard springs. Switch to
`MaterialTheme.motionScheme` specs only if the motion stays the same; otherwise keep the constants and document why.

## Risks

| # | Risk | Mitigation |
|---|---|---|
| R1 | The position provider changes the menu's place in full windows | Scenario "A full window is unchanged"; QA's 20-placement sweep |
| R2 | TalkBack focus after removal lands on a neighbour and a quick double tap selects it (pre-existing) | Out of scope, #296 (1.5.0); stated as a known limit in D199 (5) and the spec; no focus code in this patch |
| R6 | The announcement fix changes when the message appears on the touch path | Touch rows of 5.3 (b) rerun; the message still shows once, Undo still works |
| R7 | A configuration change during the closing drops the pick (D8) | Accepted: the tag stays listed and the person sees it; walk row (i) |
| R3 | The German spoken name reads badly | Shipped-file check (task 5.5) |
| R4 | The 1 px floor case (showed 1 px) is untested | QA tries it; files a defect only if it fails |
| R5 | Undo after the list refilled | Restore use case checks the limit first (EC-ATK-26) |
