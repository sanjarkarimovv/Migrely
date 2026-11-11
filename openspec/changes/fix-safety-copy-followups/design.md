# Design

BRD D173, issue #200. Sources are the medical-reviewer's saved copies in `migrely-work/med-194-2026-11-10/` (private):

- [M] NHS migraine, https://www.nhs.uk/conditions/migraine/ (reviewed 10 March 2026, fetched 2026-11-10);
- [H] NHS headaches;
- [Men] NHS meningitis (reviewed 12 June 2026);
- [NICE] CG150 recommendations;
- [MP] MedlinePlus Headache.

## Context

The person who sees the SAF-F2 card has just logged the worst pain on the scale, often in the dark room, on one hand. They read
the first words, maybe the first line. The card must say what to do first and what it is about in the same words the Help page
uses, so that the two never look like two different signs.

The person on Help › Warning signs is usually calmer and reading to decide. The rash sign's only instruction is the glass test.
On brown or black skin that test is harder to read, and the NHS puts that warning inside its own emergency bullet.

## Goals / Non-Goals

**Goals:** one name for the "sudden, extremely bad pain" sign across the app; the NHS rash note on the page; an explicit,
recorded hold on numbness alone; no layout, timing or a11y regression on the card.

**Non-goals:** see the proposal's out of scope. No change to when the card shows, its button, colours or scroll.

## Decisions

### D1. The card: the page's own words, action first

| | Old | New | Source |
|---|---|---|---|
| `safety_title` | "Sudden, worst-ever pain?" | "Sudden, extremely bad pain?" | [M] "have a headache that came on suddenly and is extremely painful"; page item 1 "Extremely bad pain that came on suddenly" (D171, medical-reviewer PASS 2026-11-10) |
| `safety_body` | "If this pain came on suddenly and is the worst of your life, call emergency services now." | "Call your local emergency number now if this is extremely bad pain that came on suddenly." | Same [M] line; the page heading "Call your local emergency number if a headache comes with any of these:" (D171); [M] heads the list "Immediate action required: Call 999" |
| `safety_dismiss` | "Got it" | unchanged | |

Options considered:

- **A. Keep the card.** Rejected: two names for one sign; "worst of your life" comes only from [MP] ("Your headache is 'the worst
  ever'"), which sends it to "seek medical help right away", not 911; and it narrows the condition for people who have had worse.
- **B. [M] verbatim, "came on suddenly and is extremely painful".** Rejected: true to the source, but then the card and the page
  use two wordings again. The page's "extremely bad pain" already passed medical review as the same sign.
- **C. The page's item 1 inside a call-to-action sentence.** Chosen. The phrase "extremely bad pain that came on suddenly" is
  identical on both surfaces. "Your local emergency number" replaces "emergency services" (EC-SAF-04). The sentence starts with
  the action, like the page's first heading. The title keeps the question form, so the condition is read first and not every 10
  reads as an emergency. "Now" stays from the old card and matches [M] "Immediate action required".

Length: the title grows from 24 to 27 characters. The sentence stays at 89. The card has no `maxLines`. Nothing else in
`SafetyCard` changes, but its KDoc says the text is unchanged (D165 (3)), so the comment gets updated.

### D2. The rash note: the NHS bracket, nothing more

| | Old | New | Source |
|---|---|---|---|
| `settings_warning_signs_list` item 8 | "A rash that doesn't fade when you press a glass against it" | "A rash that doesn't fade when you press a glass against it (this may be harder to see on brown or black skin)" | [Men] emergency list: "spots or a rash that does not fade when pressed with a glass (this may be harder to see on brown or black skin)" |

Options considered:

- **No note.** Rejected. Then the item's only instruction fails more often for some readers, and the audience is worldwide.
  Safety ranks above the cost of a site update.
- **The bracket only.** Chosen. These are the NHS's own words in the NHS's own list item, so medical-reviewer can check it against
  one line.
- **Add where to look** ([Men]: "Check paler areas, such as the palms of the hands, soles of the feet, roof of the mouth, whites of
  the eyes or inside the eyelids"). Rejected for now. It would triple the item at 200% font, turn a list of signs into a how-to,
  and the NHS emergency bullet does not carry it. Logged for the clinician (T31).

The item stays one bullet, one TalkBack stop. The page keeps 18 stops (EC-SAF-07). At 200% on 320 dp it wraps to more lines and
the page already scrolls (EC-SAF-06).

### D3. Numbness alone: hold until the clinician answers

No string change. [M] lists "numbness or a tingling that feels like pins and needles" as aura. [NICE] 1.2.3 lists "negative (for
example, numbness)" sensory symptoms as typical aura. [H] lists "numbness or weakness in the body or face" under 999. Adding it
would send every ordinary aura to the emergency number. The dangerous cases stay covered: item 2 (weakness on one side), urgent
item b (aura over 1 hour) and the last line. The spec now carries a scenario so that a later edit cannot add it silently. A
clinician's answer (T31) leads to a new D-number, not to an edit of this one.

### D4. The pending `fix-safety-card-into-view` spec

That change (hotfix 1.1.1, task 3.2 still open) is not archived. Its delta adds "TalkBack speaks the card once it is in view", whose
scenario quoted the old title and sentence. Its "Card text" scenario said the text is "the same as in 1.1.0". If it were archived
after this change, the main spec would quote the old words again. Both scenarios now say "the card's title and sentence" without
quoting them. That is what they always meant: D167 (4) says the hotfix did not change the text. No requirement of that change moves.

### D5. A test that guards the match

The card's strings live in `features/attack/ui`, the page's in `features/settings/ui`, and neither module depends on the other.
`:app` sees both resource sets, and its `testDebug` already runs Robolectric with `isIncludeAndroidResources = true`. A test there
reads `safety_body` and `settings_warning_signs_list[0]` and checks:

- the sentence contains the page's item 1 in lower case;
- neither card string contains "worst" or "emergency services".

The exact-text tests stay in each module. The negative control: the same tests on `3d098049` fail.

### D6. The site

`website/build.py` `urgent_help()` and `website/make_diary_pdf.py` carry the page word for word (#199). `aso-seo-specialist` adds
the bracket to both. They rebuild the four generated pages and both PDFs, add [Men] to the sources of each page that carries the
section, and move each "Last reviewed" date as the website spec requires. medical-reviewer checks the site text. The owner deploys
(handbook section 5). The site may run ahead of the shipped app by this one note. The difference only adds a warning, so it is
accepted (EC-SAF-15).

## Layers

UI resources and tests only: `features/attack/ui`, `features/settings/ui`, and `:app` `testDebug`. No domain, presentation, data,
datasource, DI or navigation change.

## Risks / Trade-offs

- **Over-alarm from leading with the action.** Mitigation: the title is a question that carries the condition, and the sentence
  keeps the "if". medical-reviewer judges the order.
- **The title wraps to two lines at 200% on 320 dp.** It could already wrap. `ux-designer` checks the card whole below the message
  (EC-SAF-12).
- **People with a saved 10 never see the new card** (it shows only for the first 10). Accepted: the page carries the same words
  (EC-SAF-16).
- **Not a clinical sign-off.** T31 stays open.

## Open questions for medical-reviewer

1. Is "Call your local emergency number now if this is extremely bad pain that came on suddenly." a faithful, non-softened
   rendering of [M]'s first 999 item, with the action before the condition?
2. Is "Sudden, extremely bad pain?" acceptable as the title? The sentence is written to stand alone (condition and action), because
   a reader in pain may skip the title. Since D165 no message covers the title on `develop`.
3. Is the [Men] bracket correct, word for word, inside the migraine page's rash item?
