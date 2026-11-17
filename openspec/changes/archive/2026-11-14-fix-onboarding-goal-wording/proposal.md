# Proposal

## Why

#192 (S4, pre-existing, found in the T44 content review). Onboarding step 1, "What brings you to Migrely?", offers the goal
"Take fewer painkillers" (`onboarding_goal_painkillers`). `medical-reviewer` (2026-11-13, med-192) gave it a FAIL. It reads as
the app backing a cut in medicine without a doctor's plan, and as an outcome the app promises. Sources: Migraine Trust ("discuss a
plan with your doctor before the withdrawal of painkillers") and NICE CG150 1.3.42–1.3.48 (withdrawal is advised and followed up by a clinician).

The 2026-11-06 decision to keep the string (`archive/2026-11-06-fix-overuse-wording`) was a wording review, not a medical one.
BRD D177 replaces it.

A diary records what the person did. The goal should name a record, not a reduction.

## What Changes

- **One string:** `onboarding_goal_painkillers` "Take fewer painkillers" → "Keep track of my medication days". This is
  `medical-reviewer`'s exact replacement. It matches the store listing's "medication days". The word "painkiller" is avoided
  because, in the app's own categories, it excludes triptans.
- **Spec:** `onboarding` gains the requirement "The goal choices name a record, not an outcome", with a scenario for each of
  EC-ONB-01…06.
- **BRD:** ONB-F1, D77, D177, §9.15 EC-ONB-01…06, and §13 row 45 (already in this change's first commit).

## Out of scope

- **The storage key `fewer_painkillers` and the code names** (`FewerPainkillers` in the data, domain and presentation
  models, and the resource id `onboarding_goal_painkillers`) stay as they are. The goal is written to preferences but never
  read (`PreferencesDataSource.goal()` has no caller), and it is not in the JSON backup (D78), so renaming would only add a
  migration risk. A later rename of the code identifiers alone (not the stored value) is allowed but not part of this change.
- **The other step 1 text:** the title, "Keep a record for my doctor", "Find what triggers my migraines" and the disclaimer. All
  got a medical PASS. Adding "might" to the triggers goal was judged optional and is not done.
- **A caveat line** under the goals. None is added: the new goal suggests no action.
- **Anything that uses the goal.** Nothing reads it today (D79), and none is added.
- **Store and site:** no store screenshot (`docs/release/store/screenshots` 01–08 and their `compose.py` raw captures), no site
  image (`website/public/assets/img`) and nothing in `Migrely-Play-Assets` shows onboarding. There is nothing for
  `aso-seo-specialist` to replace. Video, Reels and Highlights were not checked: a follow-up for `content-creator`.
- **The Instagram lint** (`ig_lint.py`) keeps "Take fewer painkillers" as a banned med-advice phrase. That is right, and it
  stays.
- **Other languages.** The app is English only (D148); German comes in 1.4.0.
- **Clinician sign-off (T31).** Nothing may say "medically reviewed".

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `onboarding`: an added requirement, "The goal choices name a record, not an outcome", with scenarios for:
  - the label;
  - choosing it;
  - an upgrade with the old key;
  - 200% font;
  - TalkBack and keyboard;
  - Skip and Back;
  - backup and restore;
  - offline, Free and Premium.

## Impact

- `features/onboarding/ui/src/main/res/values/strings.xml`: one string.
- No change to domain, presentation, data, database, preferences format, backup, manifest, permission or Gradle. Free and
  Premium alike. Works offline.
- QA: `.maestro/onboarding/skip-restore-disclaimer.yaml` asserts step 1's title and disclaimer and taps "Restore from a backup
  file". No flow or script names the old or the new goal label (`git grep` on 5bbf8f94).
- BRD: ONB-F1, D77, D177, EC-ONB-01…06, §13 row 45.
