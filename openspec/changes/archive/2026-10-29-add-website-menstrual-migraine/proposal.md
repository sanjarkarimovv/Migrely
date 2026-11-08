# Proposal

## Why

Migraine around periods is common and searched for as "menstrual migraine" and "migraine before period". Period data
is also among the most sensitive data a phone holds, which is exactly where Migrely's "no internet permission" proof
matters. The site has no page on it. Task T15 of the launch plan. The rules are BRD D142 and EC-WEB-13…14.

## What Changes

- **`/menstrual-migraine/`** (D142):
  - **A short answer first:** the day −2 to +3 window, in 2 of 3 cycles; pure vs menstrually-related; a doctor
    confirms it.
  - **A small table of the five days.**
  - **Questions:**
    - what it is (ICHD-3 appendix criteria; pill and HRT withdrawal bleeds count; Migraine Trust figures);
    - how to count the days;
    - how long to track it (NICE 2 cycles, Migraine Trust 3 months; link to the diary template);
    - contraception and aura (NICE 1.3.30, as general information);
    - treatment (talk to a doctor; NICE describes options, with no drugs or doses named);
    - whether period data is private in Migrely, and what the app shows (period marks free; Insights, Premium, counts
      attacks in the window without drawing conclusions);
    - when to get help straight away.
  - Sources, "Last reviewed", no clinician review, and `MedicalWebPage` JSON-LD.
- **Links:** `/headache-diary/`'s "How long should I keep it?" links here. The page joins the sitemap, and its Play
  button carries `utm_campaign=menstrual-migraine`.

## Out of scope

- Menstrual migraine with aura subtypes (A1.2.0.x), which are rare research categories.
- Any app change.

## Capabilities

### Modified Capabilities

- `website`: adds the menstrual migraine page and its link.

## Impact

- `website/build.py` and `website/public/` (the new page, the diary page's link, the sitemap), `website/lastmod.json`
  and `website/README.md`.
- `docs/BRD.md`: D142, EC-WEB-13…14, §13 row 19, §14.4.
