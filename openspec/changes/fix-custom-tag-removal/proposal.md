# Proposal

## Why

**#179 (S3, pre-existing).** In a short window at 200% font, a long press on a custom tag opens its menu ("Remove from my
list") on top of the tag's own chip: Material's `DropdownMenu` falls back to overlapping its anchor when it fits neither
below nor above with 48 dp margins. The next tap on the chip lands on Remove, and the tag leaves the list with no message
(QA `n1rep`, 2 of 2 at 1080 × 900 px, 200%).

Removal is silent on every path, not only this one: the menu item, the long-press menu opened by TalkBack, and the
TalkBack custom action, which removes at once and says nothing. The tag is archived (it stays on attacks that use it, and
re-adding the name brings it back), but the person cannot know that. Every other remove in the app (medication, pain
level, attack) answers with Undo. A silent change to what the person entered ranks above accessibility in our risk order.

**#262 (S4).** On the TalkBack reopen path, a one-step keyboard keeps 1 px less of the note than showed before. D194 H2 and
the app-shell reopen scenario allow no rounding, while the same requirement's text and another scenario already allow
1 px. ux-designer ruled: 1 px of rounding is acceptable, with a floor of 1 px (below that, the field leaves the
accessibility tree, the #259 S1 failure). Wording only; no code.

## What Changes

- The popup menu never covers any part of its anchor: below if it fits with 8 dp, else above, else the larger side with
  scrolling content; a tap outside, on the chip too, only closes it (EC-UX-91; a fix, no new decision).
- Every removal path shows "Removed from your list" with Undo; Undo un-archives the tag and restores the chip, unless the
  list is full (D199, new).
- The message is spoken once on every removal path, TalkBack's menu path included (the device walk found it silent there,
  0 of 6: the closing popup dropped the live-region event).
- A tag that is archived but still shown on an attack offers no remove (no menu, no long-press label, no TalkBack action)
  until Undo (EC-ATK-28; it offered one on develop and on the first build).
- New strings in `features/attack/ui`: EN "Removed from your list", spoken Undo name "Undo removing %1$s"; DE "Aus deiner
  Liste entfernt", "Rückgängig: %1$s zurück in deine Liste" (ux shipped-file check, 2026-11-27).
- #262: D194 H2 and the app-shell requirement "The keyboard never hides a focused field that shows" allow 1 px of rounding,
  never below 1 px; the QA threshold becomes `kept_px ≥ max(1, min(shown_px, line_with_clearance_px) − 1)`.

## Out of scope

- Other Undo messages, editing tag names, a list of archived tags.
- Focus after a removal or an Undo, keyboard or TalkBack: pre-existing (develop behaves the same), needs focus code in the
  chip group; moved to #296 for 1.5.0 (team-lead scope decision 2026-11-27). D199 (5) states the known limit.
- ux rule N1 (hide the keyboard before the menu opens): dropped, no code. On a device the menu takes window focus and the
  keyboard already closes by itself.
- The message's position: it follows the screen's existing message placement; any overlap with the top bar at 200% is the
  message host's, checked against develop by QA.
- #283, #279, #273, #291, #296.
- Any keyboard-follow code for #262 (wording only). The 1 px floor case is untested; QA tries it before anyone files it.

## Capabilities

### Modified capabilities

- `app-shell`: ADDED "A popup menu never covers its anchor"; MODIFIED "The keyboard never hides a focused field that
  shows" (#262 wording).
- `custom-tags`: ADDED "Removing a tag says so and can be undone".

## Impact

- Code: `designsystem` (`MigrelyMenus.kt`, the only caller is `ChoiceChip`), `features/attack/domain` (a restore use case
  that checks the 100 limit), `features/attack/presentation` (`AttackDetailsViewModel`, a message model with Undo),
  `features/attack/ui` (`AttackChoiceSections.kt`, `AttackMessages.kt`, strings EN and DE), `app` DI; tests per layer.
- No permission, manifest, schema, migration or backup change: the archive flag exists and is backed up today.
- BRD: D199 (new), EC-UX-91, EC-ATK-25, EC-ATK-26, EC-ATK-27, EC-ATK-28, §13 row 64, D194 H2 reworded in place.
- If the patch must shrink: the placement fix and a message without Undo ship first, Undo after (D199 (6)); the PM then
  moves the Undo scenarios to a follow-up change before this one is archived.
- 1.5.0 (T78, T70): no shared file.
