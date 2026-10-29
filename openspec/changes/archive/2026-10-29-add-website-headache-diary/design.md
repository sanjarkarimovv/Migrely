# Design

## Context

`website/build.py` builds migrely.health from Python strings with no template engine. It keeps the site static and
script-free (D119), and checks the privacy page's permission list (D134). Page dates come from `lastmod.json` (D136), and
Play links are tagged per page (D137). The medical wording reuses the facts behind the launch's social posts, which an
independent review checked against ICHD-3, NICE CG150, the NHS and the Migraine Trust. The two corrections that review
made apply here as well:
- ICHD-3 8.2.6 counts a mix of kinds (for example a triptan and a simple painkiller) toward 10 days, while simple
  painkillers alone, one or several, stay at 15 (8.2.3 counts them as one class). MOH also needs headache on 15 or
  more days a month and more than 3 months of overuse;
- the thresholds are diagnostic, not safe limits.

The rules are BRD D138–D140 and EC-WEB-07…10.

## Goals and non-goals

**Goals:**
- A page that answers the searcher's questions accurately and quotes its sources.
- A template a clinic would hand out: plain, black-and-white friendly, and 8 weeks long.
- No new runtime or build dependency for the site.

**Non-goals:**
- a fillable PDF form;
- the MOH and menstrual migraine pages (T14, T15);
- any claim that Migrely or the template is recommended by anyone.

## Decisions

### D1. The page

The page is `headache_diary()` in `build.py`, in `.prose` like `/about/`.

1. **H1:** "Headache diary: what to write down, with a free printable template". The lead is followed by the two
   download links.
2. **Questions:**
   - Why keep a headache diary?
   - What should I write down?
   - How long should I keep it?
   - What counts as a headache day?
   - Why count medicine days?
   - What's in the template?
   - Would you rather use your phone?
3. **Sources**, with links, then "Last reviewed: 29 October 2026. Written by Migrely from the sources above; not
   reviewed by a clinician. Educational only, not medical advice."

The reviewed date is a constant, `DIARY_REVIEWED`, used in both the text and the JSON-LD. The JSON-LD is a
`MedicalWebPage` with `url`, `name`, `inLanguage`, `lastReviewed`, `about` (`MedicalCondition` "Headache") and
`publisher` (the D135 organization). The Play button uses `play_button(path)`, so its campaign is `headache-diary`.

### D2. The template

The template is a landscape page with 10 mm margins, set in the site's own fonts (Figtree and Fraunces). Lines are
grey `#888` at 0.75 pt and text is black, with no colour that carries meaning.

| Page | Content |
|---|---|
| 1 | Title, name and start date lines, how to use it (4 steps, including pain-free rows, attacks past midnight and painkillers for other pain), the key (pain 0–10; symptoms N L S A, with aura described; medicine kind T C P O with examples), why medicine days count, the warning-signs box (the NHS migraine page's emergency and same-day signs) |
| 2–5 | "Weeks 1–2" … "Weeks 7–8". Fourteen rows. Columns: date, headache Y/N, started, ended, worst pain 0–10, symptoms, medicine (name, dose, time), kind, helped Y/S/N, possible triggers, period, notes |
| 6 | Summary for your doctor: the counting rule (days, not doses), then weeks 1–4 and 5–8 × headache days, days with N, L, S or A, days with P, T, C and O each, days with any medicine for the attack, worst pain. Then "Medicines I take to prevent headaches" and "Questions for my doctor", with ruled lines |

Every page footer reads "Free from migrely.health · A diary, not a medical device · Reviewed <date>", and "Page n of
6". The date is `DIARY_REVIEWED`, imported from `build.py`, so printed copies show how current they are.

The kinds follow ICHD-3's classes, simplified for patients:
- **T:** triptan;
- **C:** combination painkiller, such as one with caffeine or codeine;
- **P:** simple painkiller (paracetamol/acetaminophen, aspirin, ibuprofen or another NSAID);
- **O:** other medicine for the attack (opioid, ergot or another, with its name).

The summary page counts each kind, then any medicine for the attack, so it maps onto both 8.2 rules.

### D3. Making the PDFs

`website/make_diary_pdf.py` builds the six pages as one HTML document, with `@page { size: A4 landscape }` or
`letter landscape`. For each size it runs Chrome with `--headless=new --print-to-pdf=<target> --no-pdf-header-footer`
and a throwaway profile. The output goes to `website/public/headache-diary/migrely-headache-diary-a4.pdf` and
`…-letter.pdf`.

The script checks that each PDF has exactly 6 pages, because an overflowing page would print as an extra one. It
shows Chrome's error output if printing fails. `CHROME` overrides the macOS path.

`build.py` does not call Chrome. It only checks that both PDFs exist, and stops with a pointer to the script if they
don't, so a deploy never has a broken download link.

The PDFs are not in `/assets/`, because `_headers` caches that folder as immutable for a year and these files keep
their names when updated. They are not in the sitemap: the page is.

`_headers` detaches the site's CSP (`default-src 'none'`) from the two PDFs. Browsers open PDFs in their own viewer,
which such a policy can block, and the PDFs run nothing. Their being served and opening is checked after deploy.

The review noticed that `site.css` sits under that same immutable rule with a fixed name, so returning visitors would
keep an old stylesheet. `build.py` now links `site.css?v=<first 10 hex of its SHA-256>`.

### D4. Styles

The page reuses `.prose`, `.cta` and `.play`. The download links get a quiet second style (`.play.alt`, an outline
on `--violet`), so the page has one main button. A "Last reviewed" line uses `.meta` (muted, smaller).

## Risks

- **Chrome's PDF output changes between versions.** The PDFs are made once and committed, and `pdfinfo`/`pdftotext`
  in the verification catch a broken page count or clipped text.
- **Medical wording drifts from the social posts.** Both come from the same checked facts, and the review of this
  change compares them.
- **No clinician review.** The page says so. A clinician review is a later trust step, not a blocker for an
  educational page that cites primary sources.
