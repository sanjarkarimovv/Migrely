# Design

## Context

Change 17 added `/headache-diary/` with a "Why count medicine days?" section. Its review fixed the ICHD-3 wording:
- headache on 15 or more days a month and more than 3 months of overuse are part of the definition;
- a mix of kinds counts toward 10 days (8.2.6);
- simple painkillers alone, one or several, stay at 15 (8.2.3).

This page is that section at full length. The facts were read on 2026-10-29:
- **ICHD-3 8.2:** the description, the criteria, "usually, but not invariably, resolves after the overuse is
  stopped", and "rebound headache" among the previously used terms.
- **NICE CG150:** 1.2.7, 1.3.6 and 1.3.42–1.3.48. The last are the management recommendations: withdraw for at least
  a month, abruptly, with close follow-up; consider prophylaxis; review after 4–8 weeks; specialist help for strong
  opioids.
- **NHS migraine page:** no more than 2 days a week.

The app's own rule is BRD D45: the last 30 days, per kind, a soft note 2 days before the count and a doctor note at
it, free.

## Decisions

### D1. Page structure

The H1 is "Medication-overuse headache: the 10- and 15-day rules". It is followed by:

1. **The short answer** (lead).
2. **What is medication-overuse headache?** The definition, with "once called rebound headache" and its link to
   chronic migraine (both diagnoses can apply).
3. **How many days count as overuse?** A two-column table:

   | Medicine | Days a month, for more than 3 months |
   |---|---|
   | Triptans | 10 or more |
   | Ergots (ergotamine) | 10 or more |
   | Opioids, such as codeine | 10 or more |
   | Combination painkillers: two or more drugs in one, or with caffeine | 10 or more |
   | A mix of the kinds above, or of these and simple painkillers | 10 or more in total |
   | Simple painkillers alone (paracetamol/acetaminophen, aspirin, ibuprofen and other NSAIDs), one or several | 15 or more |

4. **Does it count doses or days?** Days, with the same rule as the template's page 6.
5. **Are these safe limits?** No: the NHS's 2 days a week, and NICE 1.3.6 (doctors should explain the risk).
6. **How is it treated?** General information attributed to NICE, then "plan it with your doctor; opioids need
   medical guidance; don't stop or change medicines on your own", and ICHD-3's "usually, but not always, gets better".
7. **How can I tell if I'm getting close?** Count days with the free template, or with Migrely (D45). The note is a
   heads-up, not a diagnosis, because the app looks at 30 days and ICHD-3 at more than 3 months (EC-WEB-12).
8. **When should I get help straight away?** The shared paragraph.
9. **Sources** and the review block.

### D2. Shared medical-page helpers

`build.py` gains three helpers, and `/headache-diary/` moves onto them, so the two pages cannot drift:
- `urgent_help()` returns the "When should I get help straight away?" section;
- `reviewed_block(sources, reviewed)` returns the Sources list and the "Last reviewed … not reviewed by a clinician"
  line;
- `medical_ld(path, name, reviewed, about)` returns the `MedicalWebPage` JSON-LD.

`MOH_REVIEWED` is separate from `DIARY_REVIEWED`, because each page is re-checked on its own.

### D3. Table style

`.prose table` gets full width, collapsed borders in `--line`, cell padding, and `th` in the body font at weight 600.
Long words already wrap (`overflow-wrap: anywhere` on `body`). With two columns no horizontal scroll is needed.

### D4. Links

- `/headache-diary/`: "Why count medicine days?" ends with "More on medication-overuse headache".
- `/about/`: the ICHD-3 sentence links "medication-overuse headache" to the new page instead of to ICHD-3. ICHD-3
  stays linked from the new page's Sources.

## Risks

- **Treatment wording read as advice.** It is attributed to NICE and followed by "plan it with your doctor". The
  review checks it against CAP 12.2.
- **Brand names.** None are used; only drug classes and generic examples.
