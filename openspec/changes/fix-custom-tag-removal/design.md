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

Goals: the menu never sits on its chip; every removal is announced with a way back; #262's wording matches the code and
QA's threshold.

Non-goals: other menus (there are none), other Undo messages, tag editing, any keyboard-follow change.

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
| `snackbar_undo_tag_description` | Undo removing %1$s | Rückgängig: %1$s zurück in die Liste |

The visible button reuses `attack_snackbar_undo`. The German follows the spoken-name rule (starts with the visible word,
EC-UX-45); the existing German medication Undo name does not, so it is not a model. The German is a ux draft and gets the
T23-style check on the shipped `values-de/strings.xml`; if that check changes it, this design and the spec change first.
No health statement: no medical-reviewer.

### D4. Focus (D199 (5))

Keyboard: after removal request focus on the next chip in the section, or "+ Your own" if it was last; after Undo on the
restored chip; after closing the menu without removal on the chip. TalkBack: no forced focus (no public Compose API,
D194); the polite announcement carries the outcome. QA records where TalkBack focus lands and that the next double tap
toggles nothing.

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

## Risks

| # | Risk | Mitigation |
|---|---|---|
| R1 | The position provider changes the menu's place in full windows | Scenario "A full window is unchanged"; QA's 20-placement sweep |
| R2 | TalkBack focus after removal lands on a neighbour and the next double tap toggles it | QA row; a stop rule: report to PM before adding focus code |
| R3 | The German spoken name reads badly | Shipped-file check (task 5.5) |
| R4 | The 1 px floor case (showed 1 px) is untested | QA tries it; files a defect only if it fails |
| R5 | Undo after the list refilled | Restore use case checks the limit first (EC-ATK-26) |
