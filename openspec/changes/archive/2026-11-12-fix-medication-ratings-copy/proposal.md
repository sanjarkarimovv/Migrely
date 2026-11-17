# Proposal

## Why

#205 (S3, pre-existing). Insights' medication section is headed "What helped". The section only counts the person's own answers
to "Did it help?", but as a heading "What helped" reads as the app saying which medicine works: a relief claim. The
medical-reviewer rejected the same words for marketing on 2026-11-06 for that reason (verdict B4.3: "'what helped' implies
relief"; the site now says "how you rated your medicines").

The paywall repeats the claim ("…and what helped") and adds a second one: "Premium shows your triggers" implies that the app
knows what causes the person's migraines, which INS-F6 forbids. The BRD (INS-F4, §7.1) and the `medication-effect` spec's Purpose
frame the feature as medicine effectiveness in the same way.

A diary shows what the person logged. It never says which medicine works (BRD §1, INS-F6).

## What Changes

- **Insights heading:** `insights_medications_title` "What helped" → "How you rated your medications".
- **Insights paywall lead:** `subscription_lead_insights` → "Premium shows triggers often logged with your migraines, when they
  come, and how you rated your medications." It matches the approved benefit line "Triggers often logged with your migraines"
  and the site.
- **Paywall benefit row:** `subscription_benefit_timing` → "When migraines come, and how you rated your medications". If it does
  not fit the row at 200% font, the fallback is "When migraines come, and how you rated medications" (EC-INS-04).
- **Comments, KDoc and a test name** that say "what helped" or "how often it helped" now say what is counted: the answers.
- **Specs:** `trigger-insights` › "What helped" is RENAMED to "How you rated your medications" and its text counts answers
  instead of saying "how many helped". `subscription` gets a requirement that the paywall names Insights without a cause or a
  relief claim.
- **`medication-effect` Purpose** ("…so the doctor report and insights can tell which medications work") → "…can show how the
  person rated each medication". A Purpose is not a requirement, so a delta cannot carry it: this change edits
  `openspec/specs/medication-effect/spec.md` directly.
- **BRD:** INS-F4 rewritten, §7.1 table and REM-F2 reworded, D174, EC-INS-04…06, §13 row 43.

Copy verdicts (ux-designer, then medical-reviewer, 2026-11-12) are final and recorded in BRD D174.

## Out of scope

- The "Did it help?" question and its four answers (D39), the counts, the 3-answer threshold (D92), "Helped a lot or completely:
  N of M answers (P%)" and "Needs 3 answers (has N)". They are the person's own answers, and the word "answers" says so.
- The answer labels in attack details ("Helped a lot", "Did not help"…), "Ask if a medication helped" (reminders, settings),
  and the onboarding goal "Find what triggers my migraines" (the person's own words about their goal, not an app claim).
- The doctor report's layout and wording.
- Follow-ups, each through medical-reviewer, not in this PR:
  - `aso-seo-specialist`: Play listing and screenshots (worst: screenshot 05 "Learn what really helps"),
    `docs/release/listing.md:46` and `docs/release/play-products.md:16` (Premium benefit shown by Play). Entering them in Play
    Console is the owner's step (handbook section 5; no uploads while the app is in review).
  - `content-creator`, then `aso-seo-specialist`: the promo video and its transcript on the site.
  - `content-creator`: social post 05.
- Other languages: the app is English only (D148).
- Clinician sign-off (T31). Nothing may say "medically reviewed".

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `trigger-insights`: "What helped" renamed to "How you rated your medications" and rewritten to count answers, with scenarios
  for the heading, fewer answers, no medication, 200% font and landscape, TalkBack, and an upgrade.
- `subscription`: an added requirement, "The paywall names Insights without a claim", with the lead line, the benefit row and its
  200% fallback.
- `medication-effect`: Purpose only, edited in place (see What Changes).

## Impact

- `features/insights/ui`: `strings.xml` (`insights_medications_title` and the comment above it), the KDoc of `MedicationsSection`
  in `InsightsSections.kt`.
- `features/insights/domain`: the test name in `InsightsCalculatorTest.kt` (behaviour unchanged).
- `features/subscription/ui`: `strings.xml` (`subscription_lead_insights`, `subscription_benefit_timing`).
- QA: `.maestro/subscription/paywall-and-purchase.yaml:62` asserts the old lead; `qa/device_scenarios.sh:2013,2019` and
  `qa/seed_backup.py:724` name the old section.
- No domain logic, data, database, backup, manifest, permission or Gradle change. Free and Premium alike. Works offline.
- BRD: INS-F4, §7.1, REM-F2, D174, EC-INS-04…06, §13 row 43.
