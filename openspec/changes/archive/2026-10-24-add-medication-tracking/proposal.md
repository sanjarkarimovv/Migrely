# Proposal

## Why

Medication is half of the migraine story. Doctors ask what was taken and how often, and medication overuse is the
main avoidable reason migraine becomes chronic (BRD MED-F1 to MED-F5, persona P2). Migrely can log attacks but not a
single pill. The effect question and the overuse warning (change 3a) need logged intakes first.

## What Changes

- **My medications (BRD MED-F1, MED-F2, D35, EC-MED-01, EC-MED-07):**
  - add, edit and archive medications;
  - each has a name (1–60 characters), a kind (acute or preventive) and, for acute ones, a category: simple painkiller,
    triptan, combination or other, or "Not sure". It can also have an optional default dose;
  - names are unique, ignoring case. Typing an archived name brings that medication back;
  - archived medications stay in the history and disappear from the lists.
- **Log a medication taken (BRD MED-F3, D34, EC-MED-08, EC-MED-09):** one "Medication taken" screen for every entry
  point.
  - **Attack mode:** "+ Medication taken" opens it with large buttons for the dark room (BRD 18.3 B). The attack-mode
    requirement is MODIFIED.
  - **Details screen:** a new "Medications" section lists the attack's intakes, oldest first, removes one with Undo, and
    opens the screen for this attack.
  - **Home, normal mode:** "Log medication" opens it on its own. An intake whose time falls inside an attack is linked
    to that attack.
  - **Logging:** tapping a medication logs it at the shown time with its default dose, then confirms with Undo and
    "Done". The time can be changed but never set in the future. With no medication yet, the screen offers to add one
    first.
- **Attacks and intakes (EC-MED-10):** deleting an attack hides its intakes too, and Undo brings them back. Merging moves
  them to the remaining attack (D27).
- **Storage:** database version 3 adds the `medication` and `medication_intake` tables, through an additive
  `AutoMigration` with a migration test.

**Out of scope:**
- **Change 3a:** "Did it help?" and the overuse warning (MED-F4, MED-F5).
- **Change 6:** reminders and notifications.
- **Later:** a medication database or autocomplete (BRD 6.3), and schedules for preventive medication (6.2).
- **Change 5:** stats per medication.

## Capabilities

### New Capabilities

- `medication-list`: the user's medications and their rules.
- `medication-intakes`: logging a medication taken, from attack mode, the details screen and home.

### Modified Capabilities

- `attack-mode`: the dark room adds "+ Medication taken".
- `attack-details`: a "Medications" section.
- `today-sky`: "Log medication" on home.

## Impact

- **New modules:** `:features:medication:{domain,presentation,data,ui}`, with three screens: the medication list, the
  medication editor, and "Medication taken".
- **Changed modules:**
  - `:datasource:*`: a `medication` package with a datasource, DAO and entities; database v3; attack merge and purge
    handle intakes;
  - `:features:attack:*`: the intakes on the details screen;
  - `:features:home:*`: the attack-mode button and the home link;
  - `:app`: routes, DI and navigation.
- **Docs:**
  - BRD 9 (EC-MED-07 to 10), 13 (rows 3 and 3a) and 17 (D33–D35);
  - `docs/ARCHITECTURE.md` §3 and §6a.
- **BRD references:** MED-F1, MED-F2, MED-F3, ATK-F5, EC-MED-01, EC-MED-07, EC-MED-08, EC-MED-09, EC-MED-10, EC-ATK-03,
  EC-UX-02, EC-UX-03, EC-UX-04, EC-UX-16, D27, D33, D34, D35, 18.3 B.
