# Proposal

## Why

A start, an end and three pain levels are enough to log an attack, but not enough to understand it.
- A doctor asks where it hurt, which symptoms came with it, what may have triggered it and how much it stopped the
  patient (BRD 5, P1–P3).
- The insights (change 11) and the doctor report (change 10) are built from these details.
- Today an attack cannot be opened again after it ends, its times cannot be corrected exactly, and it cannot be deleted.

## What Changes

- **Attack details screen** (new `attack` feature), opened from:
  - today's card on the home screen, which now lists today's attacks;
  - a dim "Details" link in attack mode, so a long attack (P2) can be described while it is still going (BRD D24).
- **Exact times (BRD EC-ATK-03, EC-ATK-04, EC-TIM-04, EC-TIM-05):**
  - The start and end can be set to any date and time with Material date and time pickers. An attack in progress
    keeps no end.
  - Times more than 5 minutes in the future are rejected, and so is an end before the start, each with an inline
    error.
  - A time that would overlap another attack is rejected and names that attack's times. Merging arrives in change 1c
    (BRD D22).
- **Pain over time (BRD ATK-F4, EC-ATK-09):**
  - The user can add a pain level from 1 to 10 at a chosen time inside the attack, and remove one with undo.
  - When new times leave pain entries outside the attack, those entries are removed and the change can be undone.
- **First 10 out of 10 (BRD SAF-F2):** the first time the user ever records a 10, a non-blocking card says to call
  emergency services if the pain was sudden and the worst of their life.
- **Details (BRD ATK-F5):**
  - where it hurts (several choices);
  - symptoms and triggers, from built-in lists plus the user's own;
  - functional impact (0–3);
  - a note of up to 1,000 characters with a counter (BRD EC-ATK-13).
- **Custom symptoms and triggers (BRD EC-ATK-11, EC-ATK-12):**
  - A name is 1–40 characters. A name that already exists is selected, not duplicated (case-insensitive).
  - The user can have up to 100 per kind.
  - Removing a custom tag from the list archives it: it stays on past attacks but is no longer offered.
- **Autosave (BRD ATK-F6, EC-ATK-10):** every change is saved at once, with no "Save" button. The note saves while
  typing and when the screen is left.
- **Delete (BRD ATK-F7, EC-ATK-14, D23):** "Delete migraine" deletes without a dialog and returns home, where
  "Migraine deleted" offers undo for the usual undo window. After that the attack is gone for good.
- **Storage:** the Room database moves to version 2 with a tested auto-migration. It adds:
  - impact, note and deleted-at columns on `attack`;
  - an `attack_tag` table for locations, symptoms and triggers;
  - a `custom_tag` table.
- **Design system:** the date-then-time picker moves to `:designsystem` and is shared by the home 72-hour card and the
  details screen.

**Out of scope:**
- **Change 1c:** adding a past attack, and merging overlapping attacks (BRD ATK-F3, EC-ATK-05).
- **Change 2:** opening attacks from earlier days, which needs history.
- **Change 3:** medications on an attack.
- **Later:** a built-in-tag editor, renaming custom tags, and showing archived tags somewhere other than on the attacks
  that use them.

## Capabilities

### New Capabilities

- `attack-details`: viewing and editing one attack. It covers:
  - times and their validation;
  - pain entries and the first-10 safety card;
  - location, symptoms, triggers, impact and note;
  - autosave and deletion with undo.
- `custom-tags`: the user's own symptoms and triggers. It covers adding, limits, duplicates and archiving.

### Modified Capabilities

- `attack-mode`: the dark room's control list adds a dim "Details" link.
- `today-sky`: today's card lists the attacks that touched today and opens their details.

## Impact

- **New modules:** `:features:attack:{domain,presentation,data,ui}`, generated with `cag` and completed by hand.
- **Changed:**
  - `:features:home` gains today's attack list, the "Details" link, navigation events, and the deleted message with
    undo.
  - `:datasource:*` gains the database v2 entities and DAO queries, soft delete with purging, and the tag datasource.
  - `:designsystem` gains the date-time picker.
  - `:app` gains DI, the `AttackDetails(attackId)` route and the navigation mappers.
- **Docs:** BRD 13 (1b and 1c), 17 (D22–D25). `docs/ARCHITECTURE.md` §3 and §6a cover the attack feature, tags, soft
  delete and auto-migrations.
- **BRD references:** ATK-F4, ATK-F5, ATK-F6, ATK-F7, EC-ATK-03, EC-ATK-04, EC-ATK-09, EC-ATK-10, EC-ATK-11,
  EC-ATK-12, EC-ATK-13, EC-ATK-14, EC-TIM-04, EC-TIM-05, EC-STO-01, EC-STO-02, EC-UX-03, EC-UX-04, EC-UX-13, EC-UX-16,
  SAF-F2, D22, D23, D24, D25.
