# Tasks

## 1. Content

- [ ] 1.1 `/headache-diary/`: the new sections, direct answers, the "at least 3 months" fix, `DIARY_SOURCES`, `DIARY_REVIEWED` (D1, D3). Verify: every sentence maps to `marketing/research/2026-11-04-headache-diary-facts.md`.
- [ ] 1.2 `/medication-overuse-headache/`: the new sections, direct answers, `MOH_SOURCES`, `MOH_REVIEWED` (D1, D3). Verify: against `2026-11-04-moh-facts.md`; never "abruptly", no doses.
- [ ] 1.3 `/menstrual-migraine/`: the new sections with the HRT H3, direct answers, `MENSTRUAL_SOURCES`, `MENSTRUAL_REVIEWED` (D1, D3). Verify: against `2026-11-04-menstrual-facts.md`; the contraception scenario words stay.
- [ ] 1.4 `urgent_help()` with the whole NHS 999 list (D4). Verify: the same text on the three guides and the VA page.

## 2. Checks and docs

- [ ] 2.1 `check_direct_answers()` and `ANSWER_EXEMPT` (D2). Verify: a 64-word answer stops the build and names it.
- [ ] 2.2 Remake the diary PDFs (`make_diary_pdf.py`, 6 pages each, new footer date). README.

## 3. Verify

- [ ] 3.1 Build twice (stable), static checks, word counts per page.
- [ ] 3.2 Headless Chrome: CSP 0, 320 px with no overflow; look at one new section per page.
- [ ] 3.3 `openspec validate expand-website-medical-guides --strict` and the verification record.

## 4. Review and archive

- [ ] 4.1 Independent medical-accuracy review against the fact packs. Fix the findings and record them.
- [ ] 4.2 Archive, `openspec validate --specs --strict`, PR into `develop` and merge, and a new deploy zip.
