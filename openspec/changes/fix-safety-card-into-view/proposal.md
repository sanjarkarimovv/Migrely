# Proposal

## Why

#171, an S1 in 1.1.0. If the person has scrolled down to Pain, a first 10's safety card appears above the view and stays there. QA's raw log
(`migrely-work/qa-t53/logs/t53.jsonl`), checked by `team-lead`:
- the card was not visible at 9 s in 16 of 16 valid runs: 8 saved and 8 failed saves, at 100% and 200%, with animations on and off;
- with no scroll, the card was fully visible in 6 of 6;
- with the card off-screen, TalkBack spoke the title 0 of 4 times. The control was 2 of 2.

`git diff v1.1.0 c2761cba` on product code is empty, so 1.1.0 has the bug. 1.0.x had it for saved 10s. The cause: the card is inserted at the top of a
`verticalScroll`, and nothing scrolls to it. Safety ranks first, so this joins hotfix 1.1.1 (BRD D167).

## What Changes

- When the card appears and is not fully in view, the content scrolls so the card's top is at the top of the content area. A taller card has its top
  aligned.
- The scroll eases over 300 ms, or jumps with "Remove animations". A touch does not stop it.
- Focus does not move, and a restored card does not scroll.
- TalkBack speaks the card's title and sentence once the card is in view.

This is design D2 of `fix-save-message` (P8), alone.

## Out of scope

The push under a message, #166 (a message over the title at 100% on a failed save) and #167. They stay on `bugfix/166-save-message` for 1.2, whose D165
(2) then refers to D167. Card text, "Got it" and when the card shows do not change, so `medical-reviewer` is not needed.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `attack-details`: the card comes into view when it appears, and TalkBack speaks it there.

## Impact

`features/attack/ui`: `AttackDetailsScreen.kt` (the column's scroll state, an effect keyed on the card event), and possibly `AttackPainSection.kt`
(`SafetyCard`'s live region, D2 below). BRD: D167, EC-ATK-23, §13 row 36. Issue #171 (`Refs #171`).
