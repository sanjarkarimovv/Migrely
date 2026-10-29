# Design

## Context

These sources were read on 2026-10-29:
- **38 CFR 4.124a DC 8100,** through the eCFR API, current text. Its four levels:
  - 50%: "With very frequent completely prostrating and prolonged attacks productive of severe economic
    inadaptability";
  - 30%: "With characteristic prostrating attacks occurring on an average once a month over last several months";
  - 10%: "With characteristic prostrating attacks averaging one in 2 months over last several months";
  - 0%: "With less frequent attacks".

  No proposed or final 2026 change was found.
- **VA's Headaches DBQ** (updated 2024-07-09, Section IV): "For VA purposes, the term prostrating means 'causing
  extreme exhaustion, powerlessness, debilitation or incapacitation with substantial inability to engage in ordinary
  activities.'"
- **VA's accredited-representative FAQ:** "The services that an accredited VSO representative provides on your VA
  benefit claim are always free", and the "Find a VA-accredited representative or VSO" tool.

The app records each attack's start and end, peak pain, "Impact on your day" (No impact, Less active, Had to stop,
Bed rest), medicines and a note. The CSV (`AttacksCsvMapper`) has one row per attack with the columns Start, End,
Duration (minutes), Time zone, Peak pain, Pain level, Where it hurts, Symptoms, Triggers, Medications, Impact and
Note. It is free. The PDF report is Premium (D108).

## Decisions

### D1. Page structure

The H1 is "Keeping a migraine log for a VA claim". The page then runs:

1. **The notice** (lead), as the spec requires.
2. **How does VA rate migraine?** One sentence, then a two-column table (Rating | VA's words) with caption "38 CFR
   4.124a, diagnostic code 8100, read on 29 October 2026", then "VA can change its rating schedule; the current text
   is on eCFR" (link).
3. **What does "prostrating" mean?** The DBQ quote, and that the examiner's form asks how often such attacks happened,
   on average, over the last several months.
4. **What should a migraine log record?** For each attack: the date; when it started and ended; how bad it got; what
   it stopped you doing, such as lying down or leaving work; the medicines and whether they helped; any missed work.
   Over months, every attack, the mild ones too, so the record is complete and honest.
5. **How can Migrely help?** What it records, the free CSV with its columns, the Premium PDF, and that "Bed rest" is
   the user's label while VA decides what is prostrating. The Play button.
6. **Who can help with a claim?** VA-accredited representatives: VSO representatives (always free), attorneys and
   claims agents. The tool link.
7. **Urgent help,** Sources and the review block with the VA disclaimer.

### D2. `reviewed_block(sources, reviewed, disclaimer=MEDICAL_DISCLAIMER)`

`MEDICAL_DISCLAIMER = 'not reviewed by a clinician. Educational only, not medical advice.'` keeps the medical pages'
line byte-identical. `VA_DISCLAIMER = 'not reviewed by a lawyer or by VA. General information, not legal or medical
advice.'`

### D3. JSON-LD

A `WebPage` with `url`, `name`, `inLanguage`, `lastReviewed` and `publisher`. It is not a `MedicalWebPage`, because
the page is about benefits records, not a condition.

## Risks

- **The regulation changes.** The date is on the page, and `VA_REVIEWED` reminds whoever re-checks it.
- **The page is read as legal advice.** The notice comes first, the wording is factual, and the page sends readers to
  free accredited help.
- **"Prostrating" equated with "Bed rest".** This is said explicitly (EC-WEB-16).
