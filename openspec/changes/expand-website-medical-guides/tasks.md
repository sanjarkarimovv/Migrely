# Tasks

## 1. Content

- [x] 1.1 `/headache-diary/`: the new sections, direct answers, the "at least 3 months" fix, `DIARY_SOURCES`, `DIARY_REVIEWED` (D1, D3). Verify: every sentence maps to `marketing/research/2026-11-04-headache-diary-facts.md`.
- [x] 1.2 `/medication-overuse-headache/`: the new sections, direct answers, `MOH_SOURCES`, `MOH_REVIEWED` (D1, D3). Verify: against `2026-11-04-moh-facts.md`; never "abruptly", no doses.
- [x] 1.3 `/menstrual-migraine/`: the new sections with the HRT H3, direct answers, `MENSTRUAL_SOURCES`, `MENSTRUAL_REVIEWED` (D1, D3). Verify: against `2026-11-04-menstrual-facts.md`; the contraception scenario words stay.
- [x] 1.4 `urgent_help()` with the whole NHS 999 list (D4). Verify: the same text on the three guides and the VA page.

## 2. Checks and docs

- [x] 2.1 `check_direct_answers()` and `ANSWER_EXEMPT` (D2). Verify: a 64-word answer stops the build and names it.
- [x] 2.2 Remake the diary PDFs (`make_diary_pdf.py`, 6 pages each, new footer date). README.

## 3. Verify

- [x] 3.1 Build twice (stable), static checks, word counts per page.
- [x] 3.2 Headless Chrome: CSP 0, 320 px with no overflow; look at one new section per page.
- [x] 3.3 `openspec validate expand-website-medical-guides --strict` and the verification record.

## 4. Review and archive

- [x] 4.1 Independent medical-accuracy review against the fact packs. Fix the findings and record them.
- [ ] 4.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge, and a new deploy zip.

## Verification record

Run on 2026-11-05 at `feature/expand-website-medical-guides`, built from `develop` 56f254e7.

- **Words** in `<main>` (before: total and without sources; after the review fixes: total):

  | Page | Before | After |
  |---|---|---|
  | `/headache-diary/` | 606 (543) | 1,617 |
  | `/medication-overuse-headache/` | 648 (601) | 1,618 |
  | `/menstrual-migraine/` | 752 (698) | 1,893 |

- **Direct answers:** after the review fixes, every checked H2 and the H3 open with 40 to 60 words. The first real build stopped on "What counts as
  a headache day?" (65 words, then 39 after a split). The check named the page and the heading both times, which also
  covers the spec's "answer grows too long" scenario.
- **`urgent_help()`:** 60 words, the same on the three guides and the VA page. Every item is in the NHS migraine page's
  "Call 999" list, which was read on 2026-11-05 ("Page last reviewed: 10 March 2026").
- **PDFs:** both remade. Each has 6 pages, and the footer reads "4 November 2026" on every page.
- **Build:** static checks find nothing, and a second build changes nothing. `lastmod` moves to 2026-11-05 for the
  four changed pages; "Last reviewed" is 4 November, the day the sources were read.
- **Headless Chrome:** 0 CSP messages on 12 pages; the control page shows 2. At 320 px, all 12 pages fit.

## Review record

A `superpowers:code-reviewer` subagent checked every new sentence against the three fact packs. It also spot-checked
the live sources: NHS, NICE CG150, ichd-3.org, six PubMed abstracts, PMC5741367, the American Migraine Foundation, and
the Migraine Trust on web.archive.org. Every number matched its source. Nothing was Critical. Each finding was fixed:

| # | Severity | Finding | Fix |
|---|---|---|---|
| 1 | Important | Contraception: "brings a small but significant rise in stroke risk" dropped the source's hedge ("studies have suggested") and its reassurance ("still a very small risk"). The "stopped method" sentence was two paragraphs away | "Studies suggest…: still a very small risk, but an unnecessary one to take". The "stopped method" sentence now ends that paragraph, word for word |
| 2 | Important | MOH treatment credited "on a plan agreed with your doctor" to NICE. NICE 1.3.43 says to stop abruptly; the plan is the Migraine Trust's | "NICE says treatment means stopping… for at least a month; the Migraine Trust says to agree a plan with your doctor first" |
| 3 | Important | The relapse range and the specialist-centre caveat followed an ICHD-3 sentence, so they read as ICHD-3's. "Seem to" was lost (EC-WEB-24) | ICHD-3 sentence first, then "The same 2017 review says around 50 to 70% seem to respond well…, that relapse figures vary… and that most studies…" |
| 4 | Minor | Perimenopause: Migraine Trust clauses were credited to the NHS | Split by source |
| 5 | Minor | "The most widely accepted explanation" and "the evidence is still limited" leaned on an unnamed review | "A widely accepted explanation"; "a 2023 review says" |
| 6 | Minor | "Most people who get it already have migraine…" (ICHD-3) read as the Lancet review's | ", ICHD-3 says" |
| 7 | Minor | "at least 8 of those days" could read as 8 in total; "newer preventive treatments" also covered older ones | "at least 8 days a month"; "some preventive treatments" |
| 8 | Minor | `urgent_help()` dropped "cannot move", and "has the full list" implied a partial list | "weakness or loss of movement"; "also lists when to get an urgent GP appointment" |
| 10 | Minor | `check_direct_answers()` skipped H2s with attributes and every H3 | Matches H2 and H3, with or without attributes; the HRT H3 has 52 words |
| 11 | Important (process) | The README note was not committed | Committed, naming H2/H3 and "Sources" |
| 12 | Minor | New lines over 120 columns | Reflowed. The remaining long lines predate this change |
| 13 | Minor | "these medicines" had no referent; a "Pain-free days count too" bullet said the opposite; "secondary headache" was jargon | "took medicine for headache"; "Daily is the default"; "meaning one with another cause" |

Source lists: no finding. Each list covers what its text uses and nothing more, and every URL outside the Migraine
Trust returns 200.

After the fixes, the checks were run again:
- the static checks find nothing;
- a second build changes nothing;
- the old wording is gone;
- CSP: 0 on 12 pages, and the control page shows 2;
- 320 px: all 12 pages fit.
