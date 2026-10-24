# Proposal

## Why

The overuse warning on home (change 3a) shows at most once per 30 days and category, and "Got it" quiets it. EC-MED-06
asks for a standing indicator on the statistics screen, so a user can always see how close they are to medication
overuse, the most common cause of migraine getting worse that a patient can act on.

## What Changes

- **"Last 30 days" in History's summary** (D59), for the current month only, after "Days with attack medication":
  - one row per category used in the last 30 days, with its limit, for example "Simple painkillers: 6 of 15 days";
  - with two or more categories, "All attack medication: 12 of 10 days";
  - rows at or over their limit are amber, with one calm line suggesting a talk with a doctor;
  - nothing shows without an attack-medication intake in 30 days.
- **One counting rule:** the day counting of change 3a moves into one class, used by the home warning and by the
  indicator (D45).
- **Ownership:** the medication feature owns the indicator, and `:app` places it in a slot of History, as with home's
  medication card (D46).

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `medication-overuse`: adds the standing indicator.

## Impact

- **`:features:medication`:** domain `OveruseDayCounter` and `GetOveruseStatusUseCase`; presentation
  `OveruseIndicatorViewModel`; UI `OveruseIndicator` with its `Dependencies`.
- **`:features:history:ui`:** a slot in the month summary for the current month.
- **`:app`:** DI, and the slot filled on History's route.
- **No database change.**
