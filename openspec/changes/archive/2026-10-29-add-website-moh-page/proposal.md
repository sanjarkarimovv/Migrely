# Proposal

## Why

"Medication overuse headache" and "rebound headache" are what people search for when painkillers seem to stop
working. They are also the search terms closest to Migrely's free medicine-day note. migrely.health mentions the rule
on three pages but has no page that explains it with the full ICHD-3 conditions. Task T14 of the launch plan. The
rules are BRD D141 and EC-WEB-11…12.

## What Changes

- **`/medication-overuse-headache/`** (D141):
  - **A short answer first,** for answer engines: headache on 15 or more days a month after more than 3 months of
    taking:
    - triptans, ergots, opioids, combination painkillers or a mix of kinds on 10 or more days a month; or
    - simple painkillers alone, one or several, on 15 or more days a month.
  - **A two-column table:** medicine kind → days a month.
  - **Questions:**
    - what it is (once called rebound headache);
    - how many days count;
    - days or doses;
    - whether these are safe limits (no; the NHS says 2 days a week);
    - how it is treated, as general information from NICE CG150 1.3.42–1.3.48, with "plan it with your doctor";
    - how to see whether you are close: the template, and Migrely's free note, described as a heads-up, not a
      diagnosis;
    - when to get help straight away.
  - Sources, "Last reviewed", no clinician review, and a `MedicalWebPage` JSON-LD.
- **Links:** `/headache-diary/`'s "Why count medicine days?" and `/about/`'s medication-limits sentence link to the new
  page. It joins the sitemap, and its Play button carries `utm_campaign=medication-overuse-headache`.
- **Shared helpers for medical pages:** the sources and review block, the urgent-help paragraph and the JSON-LD, so
  `/headache-diary/` and the new page say these things the same way.

## Out of scope

- `/menstrual-migraine/` (T15), and translations.
- Any app change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `website`: adds the medication-overuse headache page and its links.

## Impact

- `website/build.py`, `website/public/` (the new page, links on `/headache-diary/` and `/about/`, the sitemap),
  `website/public/assets/site.css` (a `.prose table` style), `website/lastmod.json` and `website/README.md`.
- `docs/BRD.md`: D141, EC-WEB-11…12, §13 row 18, §14.4.
