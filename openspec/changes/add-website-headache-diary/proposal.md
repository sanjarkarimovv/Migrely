# Proposal

## Why

People search for "headache diary template" and "migraine diary for doctor" when they are told to keep a diary or
are preparing for an appointment. That is the moment Migrely is for. migrely.health has no page for it. A free
printable template is also the kind of resource clinics, charities and bloggers link to, which the site needs: it has no
backlinks yet. Task T13 of the launch plan. The rules are BRD D138–D140 and EC-WEB-07…10.

## What Changes

- **`/headache-diary/`** (D138). Its headings are questions, and every fact cites its source:
  - why keep a diary (NICE CG150 1.1.3, 1.3.1);
  - what to write down (NICE 1.1.4);
  - for how long: 8 weeks, or 2 cycles if headaches seem linked to periods (NICE 1.2.6);
  - what counts as a headache day (ICHD-3);
  - why medicine days are counted (ICHD-3 8.2 and 8.2.6, with the NHS's 2-days-a-week advice, so the thresholds are
    not read as safe limits);
  - what is in the template.

  Migrely is mentioned once, at the end. A "Last reviewed" date and a Sources section close the page, which says
  plainly that no clinician reviewed it. JSON-LD `MedicalWebPage` with `lastReviewed`.
- **A printable 8-week template** (D139): A4 and US Letter PDFs, landscape, 6 pages:
  - page 1: how to use it, the key, and the warning signs;
  - pages 2–5: four two-week logs, one row a day;
  - page 6: a summary for the doctor.

  The PDFs print well in black and white (EC-WEB-07).
- **How the PDFs are made** (D140). `website/make_diary_pdf.py` prints the template with headless Chrome into
  `website/public/headache-diary/`. It runs only when the template changes, and the PDFs are committed, so
  `build.py` needs nothing new.
- **Links:** the footer gets "Free headache diary", the page joins the sitemap, and its Play button carries
  `utm_campaign=headache-diary` (D137).

## Out of scope

- `/medication-overuse-headache/` and `/menstrual-migraine/` (tasks T14 and T15). This page links to them once they
  exist.
- A fillable PDF form, and translations.
- Any app change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `website`: adds the headache diary page, the printable template and the footer link.

## Impact

- `website/build.py`, the new `website/make_diary_pdf.py`, `website/public/` (the new page, its two PDFs, all pages
  for the footer link), `website/public/assets/site.css`, `website/lastmod.json` and `website/README.md`.
- `docs/BRD.md`: D138–D140, EC-WEB-07…10, §13 row 17, §14.4.
