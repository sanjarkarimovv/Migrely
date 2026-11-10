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

- The Warning signs page gets two headed parts. The first keeps the emergency-number advice, now with the NHS "Call 999" signs and
  no waiting condition. The second says to get medical advice today and lists the NHS urgent signs, which apply during a migraine
  attack. The exact strings are in the spec and in BRD D171, as passed by `medical-reviewer`.
- `settings_about_urgent` on About Migrely changes, because today it sends anyone with "any of the warning signs" to the emergency
  number. With a second tier that would include the urgent-only signs (D171 (4)).
- Nothing else: no dial button (D95 stays), no new screen, no data, no permission.

## Out of scope

- The SAF-F2 card (`safety_title`, `safety_body`): it does not contradict the new list, so it stays unchanged.
- The SAF-F3 card text (`long_check_body`): it changes only if `medical-reviewer` judges it a contradiction (D171 (4)). Otherwise it is a
  logged follow-up, not part of 1.2.1, because each extra string widens the 1.2.1 QA scope.
- Country-specific numbers, "999", "GP" or "NHS 111" in the app, and dialling or linking any number.
- The website and the printable diary: they already carry the full NHS lists (D149).
- `docs/release/whats-new.md` for 1.2.1 (495 of 500 characters): no change.
- Clinician sign-off (T31): open residual, not a blocker (D171 (5)).
- `.maestro/` and `qa/` flows that assert the old text: `qa-engineer` updates them.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-settings`: the Warning signs requirement (new list, second tier, headings) and the About requirement's urgent line.

## Impact

- `features/settings/ui`: `strings.xml` (the call line, the list, the new heading and list for the second tier, `settings_about_urgent`)
  and `SettingsPages.kt` `WarningSignsContents` (a second heading and list, heading semantics).
- No domain, data, database, backup or manifest change. Free and Premium alike. Works offline.
- Releases: 1.1.1, now in review, keeps the old text. 1.2.1 replaces it.
- BRD: SAF-F1, D95 (pointer), D171, EC-SAF-01…10, §13 row 40.
