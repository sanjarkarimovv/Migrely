# Design

## Context

The user's moment: a person whose headache feels wrong opens Settings › Help › Warning signs, often in a dark room and on
one hand. They need one answer fast: call now, get advice today, or neither. Today the page has one heading line and five
bullets (`WarningSignsContents` in `features/settings/ui/.../view/SettingsPages.kt`). The page scaffold (`SettingsPage`)
already gives the title as a heading, a scrolling column limited to `CONTENT_MAX_WIDTH`, 16 dp spacing and safe-drawing insets.

Source for every line: the NHS migraine page, "Immediate action required: Call 999" and "Urgent advice: Ask for an urgent
GP appointment or get help from NHS 111" (page last reviewed 10 March 2026, fetched 2026-11-10, saved as
`migrely-work/pm-194/nhs.txt` lines 56-78). NICE CG150 Recommendations (fetched 2026-11-10, `migrely-work/pm-194/nice.txt`
lines 36-63) was read only to settle the age-50 item.

## Goals and non-goals

Goals: no sign tells a person to wait; every NHS "Call 999" sign is on the page; urgent-only signs are not sent to the
emergency number; the page reads correctly with TalkBack, at 200% and in landscape.

Non-goals: dialling, country numbers, a symptom checker, any logic that reacts to the diary (for example, showing the
second part when an attack passes 72 hours). The app stays a diary (BRD §1, §11).

## Decisions

### D1. Two headed parts, both as bullet lists

The second part copies the NHS structure, "You have a migraine attack and:" followed by bullets. One sentence ending "or
you're pregnant" would read as "pregnant: get advice today", which the source does not say (EC-SAF-03). The same pattern
as the first part also means less to learn. Rejected: one combined list, because it would send the 72-hour attack to the
emergency number. Also rejected: a sentence-style second tier as drafted in #194, because of the pregnancy reading.

### D2. Neutral second-tier wording

"Get medical advice today" stands for the NHS "urgent GP appointment or NHS 111". The app is used worldwide and many users
have no GP and no 111. "Today" keeps the urgency. `medical-reviewer` confirmed it is faithful (BRD D171).

### D3. Order: NHS order, with weakness moved to second

The NHS order is kept except for one move: weakness on one side goes from 7th to 2nd. This was a `ux-designer` proposal.
It puts the weakness, speaking and vision signs together near the top, where a scanning eye lands first and where
they stay visible at 200%, and it keeps the long meningitis line low. Sudden, extremely bad pain stays first, as in the
NHS list and on the SAF-F2 card. All eight are in the same "call now" part, so the order makes no claim that one is
more urgent. `medical-reviewer` confirmed the order (BRD D171).

### D4. Removed items

"A first headache after the age of 50" is on neither NHS list, and NICE CG150 1.1.1 and 1.1.2 have no age item. "Numbness"
alone is an NHS aura symptom, and aura longer than 1 hour is in the second part. Both go (EC-SAF-05).

### D5. Layout and accessibility (`ux-designer` spec, `migrely-work/ux-194/design.md`)

- Top to bottom:
  - the app bar title (a heading, as today);
  - H1, the call line, then the 8 `Bullet`s;
  - a 24 dp gap, then H2, then the 3 `Bullet`s;
  - if it passed, a 24 dp gap and then the safety-net line as a plain `Paragraph` (not a heading).
- The 24 dp gap is the column's 16 dp `spacedBy` plus `padding(top = 8.dp)` on the heading, the same pattern as
  `LicenceGroup`.
- H1 and H2: `titleMedium`, `onSurface`, `Modifier.semantics { heading() }`. Today the call line has no heading, so
  it is added. Do not use `titleLarge`: it is the Fraunces serif, harder to read when vision is blurred, and too big
  at 200%.
- No colour coding, icon, divider, card, or sub-groups inside the 8 items. Red in a dark room adds alarm, not
  information, and red text loses contrast on dark.
- TalkBack swipe order:
  - back;
  - "Warning signs" (heading);
  - H1 (heading), items 1 to 8;
  - H2 (heading), items a to c;
  - the safety net, if present.

  That is 16 stops, or 15 without the safety net.
- Each `Bullet` is already one node: the glyph is cleared and the `Row` has no semantics. Do not add a merged
  `contentDescription`.
- No `collectionInfo`: "list, 8 items" only adds words. Nothing is focusable except back. No live regions.
- 200%, landscape and small phones: the existing `verticalScroll`, bottom inset padding and `CONTENT_MAX_WIDTH` are
  enough. No `maxLines`, overflow or fixed heights on this page. At 320 dp and 200%, H1 must fit whole on the first
  screen.
- RTL: the glyph mirrors to the start side.
- Previews: keep the dark one, and add a light one and a `fontScale = 2f`, 320 dp one.
- One static state. Free and Premium are identical. During an attack the page stays dark. Existing colour tokens only.
- No dial button (D95). `CALL_PHONE` is never added. A future no-number `ACTION_DIAL` needs its own decision.
- Strings for the engineer:
  - reuse `settings_warning_signs_call` and `settings_warning_signs_list` (8 items);
  - add `settings_warning_signs_today` and `settings_warning_signs_today_list` (3);
  - add `settings_warning_signs_safety_net` only if it passed.

### D6. About line

"If a headache comes with any of the warning signs, call your local emergency number." would now cover the second part
too, which contradicts it. It is the only string outside the page that changes for that reason. `long_check_body` and
`safety_body` do not contradict the new list and stay unchanged (lead scope rule 2026-11-10: change outside the page
only where today's text contradicts).

## Risks and trade-offs

- The page is longer: 8 + 3 signs instead of 5. At 200% it needs scrolling. The headings let TalkBack users jump to
  the second part, and sighted users see the first part first.
- No list is complete. A person with a sign that is not listed may read the page as "not urgent". See BRD D171 (3)
  for the decision on a safety-net line.
- Clinician sign-off (T31) is still open. The text follows the NHS lists more closely than the shipped one, which
  lowers but does not remove the risk.
- 1.1.1, now in review, keeps the old text until people update to 1.2.1.

## Migration

None. Strings and layout only, with no stored data. An update replaces the text.
