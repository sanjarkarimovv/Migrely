# Design

## Context

The user's moment: a person whose headache feels wrong opens Settings › Help › Warning signs, often in a dark room and on
one hand. They need one answer fast: call now, get urgent advice, or neither. Today the page has one heading line and five
bullets (`WarningSignsContents` in `features/settings/ui/.../view/SettingsPages.kt`). The page scaffold (`SettingsPage`)
already gives the title as a heading, a scrolling column limited to `CONTENT_MAX_WIDTH`, 16 dp spacing and safe-drawing insets.

Sources, fetched 2026-11-10 by `medical-reviewer` (`migrely-work/med-194-2026-11-10/`): [M] NHS migraine, [H] NHS
headaches, [Men] NHS meningitis, NICE CG150, MedlinePlus. The main source for every line is the NHS migraine page, "Immediate action required: Call 999" and "Urgent advice: Ask for an urgent
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

The second part copies the NHS structure: "You have a migraine attack and:" followed by bullets. A single sentence ending
"or you're pregnant" would read as "pregnant: get urgent advice", which the source does not say (EC-SAF-03). Using the
same pattern as the first part also means less to learn.

Rejected:
- one combined list, because it would send the 72-hour attack to the emergency number;
- the sentence-style second tier drafted in #194, because of the pregnancy reading.

### D2. "Urgent", not "today", and no country services

The second heading is "Get urgent medical advice if you have a migraine attack and:".
- "Urgent" is the source's word: [M] "Urgent advice: Ask for an urgent GP appointment or get help from NHS 111".
- `medical-reviewer` could not find "today" in any source and would not sign off a time frame, so "today", which the
  lead leaned towards, is not used.
- No "GP" and no "NHS 111": the app is used worldwide, and many users have neither.
- The SAF-F3 card uses the same words, so the two cannot disagree on how urgent it is (D6).

### D3. Order, the split meningitis item, and the "Don't drive" line

- **Order.** The NHS order, except that weakness on one side moves from 7th to 2nd (a `ux-designer` proposal).
  Weakness, speaking and vision then sit together near the top, where they stay visible at 200%. All nine signs are
  in the same "call now" part, so the order makes no claim that one is more urgent. `medical-reviewer`: no objection.
- **Split meningitis item.**
  - "A very high temperature and signs of meningitis, such as a stiff neck" and "A rash that doesn't fade when you
    press a glass against it" are two items.
  - Drafted as one line, the rash counted only with a very high temperature. The NHS headaches and meningitis pages
    list it under 999 on its own, with the glass test.
  - The stiff neck stays tied to temperature on purpose: the NHS migraine page lists a stiff neck as an ordinary early
    migraine symptom.
- **Head injury.** "A head injury in the last 3 months", not "recent". "Recent" can be read as "today", which would
  under-alarm. Sources: [H] "within the last 3 months"; NICE CG150 1.1.1 "typically within the past 3 months".
- **"Don't drive" line.** "Don't drive yourself to hospital. Call your local emergency number." comes straight after
  the last sign, so the emergency part ends on the call. Source: [M] "Do not drive yourself to A&E". It is a plain
  `Paragraph`, not a bullet, because it is an instruction, not a sign.

### D4. Removed items and the last line

- **"A first headache after the age of 50"** is out.
  - It is on neither NHS list, and NICE CG150 1.1.1 and 1.1.2 have no age item.
  - Only MedlinePlus has it, under "seek medical help right away", not under its 911 list.
- **"Numbness" alone** is out.
  - The NHS migraine page calls it an aura symptom, and aura lasting over 1 hour is in the second part.
  - The NHS headaches page lists numbness under 999. The migraine page governs here, and this is flagged for the
    clinician (T31).
- **The last line** keeps the lists from reading as complete: "If you have any other symptoms you're worried about,
  get urgent medical advice." Source: [H], "any other symptoms you're worried about". No source supports "call the
  emergency number anyway", so the line does not say it.

### D5. Layout and accessibility (`ux-designer` spec, `migrely-work/ux-194/design.md`, and its update for the verdict)

- **Top to bottom:**
  - the app bar title (a heading, as today);
  - H1, the call line;
  - the 9 `Bullet`s;
  - the "Don't drive" line as a `Paragraph`, with the normal 16 dp spacing so it reads as part of the emergency part;
  - a 24 dp gap, then H2 and its 3 `Bullet`s;
  - a 24 dp gap, then the last line as a plain `Paragraph` (not a heading).
- **The 24 dp gap** is the column's 16 dp `spacedBy` plus `padding(top = 8.dp)`, the same pattern as `LicenceGroup`.
- **H1 and H2:** `titleMedium`, `onSurface`, `Modifier.semantics { heading() }`.
  - Today the call line has no heading, so one is added.
  - Not `titleLarge`: that is the Fraunces serif, harder to read when vision is blurred, and too big at 200%.
  - The two headings start with different verbs ("Call", "Get"), which keeps the parts distinct.
- **Nothing else:** no colour coding, icon, divider, card, or sub-groups inside the list. Red in a dark room adds alarm,
  not information, and red text loses contrast on dark.
- **TalkBack:** 17 stops, in this order:
  - back;
  - "Warning signs" (heading);
  - H1 (heading) and the 9 signs;
  - the "Don't drive" line;
  - H2 (heading) and the 3 signs;
  - the last line.
- **Nodes:** each `Bullet` is already one node, with the glyph cleared. Do not add a merged `contentDescription`.
  No `collectionInfo`. Nothing is focusable except back. No live regions.
- **200%, landscape, small phones:**
  - The existing `verticalScroll`, bottom inset padding and `CONTENT_MAX_WIDTH` are enough.
  - No `maxLines`, overflow or fixed heights on this page.
  - At 320 dp and 200%, H1 fits whole on the first screen.
  - In landscape at 200%, the app bar title is not cut.
- **RTL:** the glyph mirrors to the start side.
- **Previews:** keep the dark one, and add a light one and a `fontScale = 2f` 320 dp one.
- **States:** one static state. Free and Premium are identical. During an attack the page stays dark. Existing colour
  tokens only.
- **No dial button** (D95). `CALL_PHONE` is never added.
- **Strings for the engineer:**
  - reuse `settings_warning_signs_call` and `settings_warning_signs_list` (now 9 items);
  - add `settings_warning_signs_no_drive`, `settings_warning_signs_urgent`, `settings_warning_signs_urgent_list` (3)
    and `settings_warning_signs_other`.

### D6. Strings outside the page

Lead's rule: outside the page, a string changes only where today's text contradicts the new page.
- `settings_about_urgent` changes to "If a headache comes with any of the emergency signs on the Warning signs page,
  call your local emergency number.". Today's "any of the warning signs" would send the second part to the emergency
  number.
- `long_check_body` (SAF-F3, `features/home/ui`) changes to "If it's still going after 72 hours, get urgent medical
  advice.". `medical-reviewer` judged today's "Attacks longer than 72 hours should be checked by a doctor." an
  under-alarm: it reads as a routine visit, while the page puts more than 72 hours in the urgent part. The two would
  disagree on urgency.
- `safety_body` (SAF-F2) stays unchanged. It says to call the emergency number, which does not contradict item 1.
  "Worst of your life" is narrower than "extremely painful": aligning it is a follow-up, not part of 1.2.1.

## Risks and trade-offs

- The page is longer: 9 + 3 signs and two lines, instead of 5 signs. At 200% it scrolls. The headings let TalkBack
  users jump to the second part, and sighted users see the first part first.
- No list is complete. The last line covers other worrying symptoms, but a person can still read a missing sign as
  "not urgent".
- The SAF-F3 card text changes, so the 1.2.1 QA scope includes the 72-hour card on Home.
- Clinician sign-off (T31) is still open. The text follows the NHS lists more closely than the shipped one, which
  lowers but does not remove the risk.
- 1.1.1, now in review, keeps the old text until people update to 1.2.1.

## Migration

None. Strings and layout only, with no stored data. An update replaces the text.
