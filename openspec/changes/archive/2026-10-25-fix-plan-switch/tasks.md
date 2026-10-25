# Tasks

## 1. Fix

- [x] 1.1 Switch plans with `WITHOUT_PRORATION`, kept as the named constant `PLAN_SWITCH_REPLACEMENT_MODE`
- [x] 1.2 A unit test that the mode is one Play allows within a subscription
- [x] 1.3 Comments, BRD EC-BIL-10 and D120, the products guide
- [x] 1.4 versionCode 3

## 2. Verify

- [x] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [x] 2.2 On the phone, from Google Play's internal track: a monthly subscriber switches to yearly, and Settings shows
  the yearly plan. Build 1.0.0 (2) failed with responseCode 5 every time. Build 1.0.0 (3) opened Play's sheet with the
  first yearly payment on the monthly plan's next billing date, Play accepted the switch, and Settings showed "Premium ·
  yearly".
