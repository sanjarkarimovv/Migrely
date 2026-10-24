# Proposal

## Why

A new user opens Migrely and lands on an empty home with no idea what it can do for them. The MVP promises a short,
skippable start (BRD §6.1 item 8, ONB-F1–F3) that:
- learns why they came and how often migraines hit;
- asks whether to track periods (DAY-F1);
- offers the daily check-in;
- shows the one-line medical disclaimer.

A user who lost their phone needs a way back to their diary from the very first screen (EC-BAK-05).

## What Changes

- **Onboarding** (D76, D77): four steps on first start, each with "Skip".
  - It is never shown to a phone that already holds health records, nor when the app opens to start an attack.
  - The splash screen holds while the app decides, so home never flashes.
- **Step 1:** the goal, the disclaimer, and "Restore from a backup file", which opens Your data (EC-BAK-05).
- **Step 2:** how often migraines come. **Step 3:** period tracking.
- **Step 4:** the daily reminder offer, from the reminders feature (D80).
- **Track periods** (D79):
  - "No" hides the ways to add a period mark; marks already saved stay;
  - Settings gets a "Track periods" switch;
  - it is on by default.
- **Storage** (D78): answers and the setting live in SharedPreferences. The database stays at v6.

**Out of scope:**
- the paywall after step 4 (change 9);
- funnel analytics (ADR-09), which needs network access, the user's decision.

## Capabilities

### New Capabilities

- `onboarding`: when it shows, its steps, skipping, and restoring from a backup.

### Modified Capabilities

- `day-markers`: period marks can be added only while "Track periods" is on, or to remove a mark.
- `reminders`: the daily reminder can also be turned on from onboarding.

## Impact

- **New** `:features:onboarding:{domain,data,presentation,ui}`.
- **New** `:features:settings:{domain,data}`, for the "Track periods" switch. This ends the ARCHITECTURE §13 row
  "settings has only presentation and UI".
- **Datasource:** `preferences` (`PreferencesDataSource`), backed by SharedPreferences, in `:datasource:implementation`.
- **Reminders:** a `DailyReminderOffer` slot, with its own small view model, reusing the permission flow.
- **Home and History:** they read "Track periods".
- **`:app`:**
  - `androidx.core:core-splashscreen`, and the start decision in `MainActivity`;
  - the route, the slot, and DI.
