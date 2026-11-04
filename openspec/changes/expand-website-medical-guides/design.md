# Design

## Context

- **Where the pages come from:** `website/build.py` writes each guide from one function.
- **Shared parts:** `urgent_help()`, `reviewed_block()` and `medical_ld()`.
- **Fact packs:** the research files list every drafted sentence with its source, an exact quote and the section
  number. They also list what was rejected and why.

## Decisions

### D1. Length follows the sources, not a target

The research agents drafted about 1,690, 1,665 and 2,190 words. Cuts were made in their own order of weakest fit:

| Page | Cut | Why |
|---|---|---|
| `/headache-diary/` | "How do I summarise my diary?" folded into "What does a doctor look for?" | The two overlap. The summary is now one bullet and points to page 6 of the template |
| `/medication-overuse-headache/` | "Almost anyone may be at risk", "the occasional month is safe", the ICHD-3 leaflet line, Frontiers 2023 lines | Vague, awkward on a "not safe limits" page, or one lower-tier source |
| `/menstrual-migraine/` | Pregnancy section; the 2004 and 2006 study sentences; repeated diary and medicine-day advice; the closing AMF list | Off-topic, or already carried by NHS, Migraine Trust and the Lancet review |

### D2. Direct answers, checked by the build

- The first `<p>` after each `<h2>` on the three guides must be 40–60 words.
- `check_direct_answers()` stops the build otherwise. It lists the page and the heading.
- **Exempt** (in `ANSWER_EXEMPT`):
  - the app sections ("Would you rather use your phone?", "How can I keep count?");
  - the privacy section ("Is my period data private in Migrely?");
  - "Sources".

### D3. Attribution in the text

| Kind of claim | How it reads |
|---|---|
| Guidance (NICE, NHS, ICHD-3) | As now: "NICE suggests…", "ICHD-3 counts…" |
| A charity or foundation (Migraine Trust, AMF) | Named in the sentence |
| One study or review | Named by year and kind: "a 2017 review", "in a 2013 lab study" (EC-WEB-24) |
| Sources disagree | The more cautious wording, attributed. How to stop a medicine is left to the doctor (EC-WEB-23) |

### D4. The full urgent-help list

- `urgent_help()` covers every item in the NHS migraine page's "Call 999" box:
  - sudden, extremely painful headache;
  - after a head injury;
  - weakness on one side;
  - speaking or remembering;
  - loss of vision, or blurred or double vision;
  - drowsiness or confusion;
  - a seizure;
  - a very high temperature with signs of meningitis.
- It is one 57-word sentence, followed by the link to the NHS page. The same list is already on page 1 of the
  printable template.

## Risks

| Risk | Mitigation |
|---|---|
| A source changes after today | Each page keeps "Last reviewed" and the source list; README says to re-read the fact pack's sources before moving the date |
| Readers take a study figure as fact | Single studies are attributed and their limits stated (specialist centres, abstract only) |
| Readers read diagnostic counts as safe limits | The NHS "2 days a week" advice stays next to every count, and "not safe limits" stays |
| Pages get long | Each section answers first, and the first H2 comes straight after the lead |
