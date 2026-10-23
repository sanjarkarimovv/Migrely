# Proposal

## Why

Logging an attack is the core of Migrely. It must work for someone who is in pain, light-sensitive and unable to read:
one tap to start, one tap to end, nothing lost if the app is killed. Today the home screen only shows the app name.
This change makes the home screen the place where attacks are logged, and turns the whole app into a "dark room" while
an attack is in progress (BRD section 18).

## What Changes

- **Start an attack** with one tap on the home screen or from the launcher shortcut. Undo is offered for 5 seconds.
  Only one attack can be in progress at a time.
- **Record pain intensity** during an attack with three large choices (Mild 3, Moderate 6, Severe 9). Each choice is
  stored as a timestamped intensity point, so pain that changes during the day is captured (BRD P2 persona).
- **Fix a late start**: during an attack, quick options move the start time earlier (15 min, 30 min, 1 h, 2 h).
- **End an attack** with one tap. Undo is offered for 5 seconds.
- **Check long attacks**: an attack still open after 72 hours prompts "Still going?" and shows the
  status-migrainosus safety note.
- **Dark room mode**: while an attack is in progress the home screen dims and shows one line of text, the elapsed time
  and large controls. No rewards, paywall, rating requests or progress messages appear.
- **Today's sky**: the home screen shows a sky card for today, drawn from data (calm so far, raining during an attack,
  hazy, cloudy or stormy after attacks).
- **Data foundation**: an on-device Room database for attacks and intensity points, a clock datasource, and a new
  `:designsystem` module for the theme and the sky drawing.
- **BREAKING (spec)**: the home screen no longer shows just the app name. The `app-shell` launch requirement is updated.

**Out of scope** (later changes, BRD section 13):
- **Attack details:** the detail screen, adding a past attack, location, symptoms, triggers, functional impact, notes,
  custom tags, overlap merging, deleting an attack, exact date/time pickers for any time, and the first-severe-pain
  card. All of these are in change 1b, `add-attack-details`.
- **History:** the month sky calendar and the attack list (change 2).
- **Medications:** the "Medication taken" button (change 3).
- **Pain-free day marking:** change 4. Until then, today is never shown as "clear".
- **Other features:** reminders, onboarding, subscription and analytics.

## Capabilities

### New Capabilities

- `attack-logging`: starting, ending and timing attacks; intensity points; one attack in progress at a time; undo of
  start and end; the 72-hour check; how times and days are stored and attributed; durability.
- `attack-mode`: how the app behaves while an attack is in progress: the dark room, minimal text, large controls,
  motion and haptics rules, and what must never be shown.
- `today-sky`: the home screen's sky card for today and the rules that map data to a sky, without guessing.

### Modified Capabilities

- `app-shell`: the "Launch opens the home screen" requirement now expects today's sky and the start control instead of
  only the app name.

## Impact

- **Code:**
  - `:features:home:*` gains its domain, data, presentation and UI.
  - New `:designsystem` module; the theme moves out of `:app`.
  - `:datasource:source` and `:datasource:implementation` get their first resources: `database` (Room) and `clock`.
    `:datasource:implementation` becomes an Android library.
  - `:app`: DI modules, the `Home(startAttack)` route and a static launcher shortcut.
- **Dependencies:** Room 2.8.5 (KSP). No network libraries.
- **Docs:** `docs/ARCHITECTURE.md` records the `:designsystem` module, the Room binding and the reduce-motion helper.
- **BRD references:** ATK-F1, ATK-F2, ATK-F4, ATK-F6, EC-ATK-01, EC-ATK-02, EC-ATK-06, EC-ATK-07, EC-ATK-08,
  EC-ATK-10, EC-TIM-01, EC-TIM-02, EC-TIM-03, SAF-F3, EC-UX-02, EC-UX-07, EC-UX-08, EC-UX-09, EC-UX-10, EC-UX-12,
  EC-UX-13, EC-UX-15, EC-STO-01, EC-STO-02, section 18 (D15, D16, G1–G6), 6.1 item 13 (launcher shortcut).
