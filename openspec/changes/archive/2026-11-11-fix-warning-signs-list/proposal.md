# Proposal

## Why

#194, found by `medical-reviewer` on 2026-11-10 (private verdict `migrely-work/ig-skill/medical-verdict-skill-2026-11-10.md`, section C).
Help › Warning signs (BRD SAF-F1, D95) ships in 1.1.1 (in review), 1.2.0 (never uploaded) and `develop`. It says "Call your local emergency
number if any of these happen." and lists five signs. Compared with the "Call 999" list on the NHS migraine page (last reviewed
10 March 2026, re-fetched 2026-11-10):

- **Under-alarm.** "Weakness, numbness or trouble speaking that doesn't pass" tells people to wait. The NHS lists weakness on one side
  and problems speaking with no such condition.
- **Missing signs, on a list that reads as complete:** losing your vision, or blurred or double vision; feeling drowsy or confused; a
  seizure or fit.
- **Over-alarm.** "A first headache after the age of 50" is on neither NHS list, and NICE CG150 1.1.1 and 1.1.2 (re-read 2026-11-10)
  have no age item. "Numbness" alone is an NHS aura symptom.
- **No urgent tier.** The NHS asks for an urgent GP appointment or NHS 111 when an attack has lasted longer than 72 hours, aura lasts
  longer than 1 hour, or the person is pregnant or has just had a baby.

Safety comes first in the team's risk order. The lead put the fix in release 1.2.1, before the tag (BRD D171).

## What Changes

- The Warning signs page gets two headed parts:
  - **First part:** call the local emergency number if a headache comes with any of nine signs from the NHS "Call 999"
    lists. No sign has a waiting condition. The part ends with "Don't drive yourself to hospital. Call your local
    emergency number."
  - **Second part:** get urgent medical advice if a migraine attack has lasted longer than 72 hours, aura symptoms last
    longer than 1 hour at a time, or the person is pregnant or has just had a baby.
  - **Last line:** any other worrying symptom needs urgent medical advice.

  The exact strings are in the spec and in BRD D171, as passed by `medical-reviewer`.
- Two strings outside the page change, because today they contradict it (BRD D171 (4)):
  - `settings_about_urgent` sends "any of the warning signs" to the emergency number, which would now include the
    second part;
  - `long_check_body` (SAF-F3) reads as a routine doctor visit, while the page puts more than 72 hours under urgent
    advice.
- Nothing else changes: no dial button (D95 stays), no new screen, no data, no permission.

## Out of scope

- The SAF-F2 card (`safety_title`, `safety_body`). It does not contradict the new list. Matching its "worst of your
  life" to "extremely bad pain that came on suddenly" is a follow-up after 1.2.1.
- Country-specific numbers, "999", "GP" or "NHS 111" in the app, and dialling or linking any number.
- Numbness on its own as an emergency sign. The NHS migraine page treats it as aura; the NHS headaches page lists it
  under 999. This is a question for the clinician (T31).
- The website and the printable diary. They already carry the NHS lists (D149). Matching their head-injury and rash
  wording is a separate follow-up for `aso-seo-specialist`.
- `docs/release/whats-new.md` for 1.2.1 (495 of 500 characters): no change.
- Clinician sign-off (T31): an open residual, not a blocker (D171 (7)).
- `.maestro/` and `qa/` flows that assert the old text: `qa-engineer` updates them.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-settings`: the Warning signs requirement (new list, the "Don't drive" line, headings). Added requirements for the
  urgent part, the last line, About's urgent line, and readability.
- `attack-logging`: the 72-hour check uses the urgent-advice wording.

## Impact

- `features/settings/ui`:
  - `strings.xml`: the call line, the list, the "Don't drive" line, the urgent heading and list, the last line, and
    `settings_about_urgent`;
  - `SettingsPages.kt` `WarningSignsContents`: the second heading and list, heading semantics, two paragraphs.
- `features/home/ui`: `strings.xml` `long_check_body` only.
- No domain, data, database, backup or manifest change. Free and Premium alike. Works offline.
- Releases: 1.1.1, now in review, keeps the old text. 1.2.1 replaces it.
- BRD: SAF-F1, SAF-F3, D95 (pointer), D171, EC-SAF-01…10, §13 row 40.
