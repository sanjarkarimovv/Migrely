# Tasks

## 1. Fix

- [x] 1.1 BRD EC-ATK-19
- [x] 1.2 Tests first (instrumented `AttackDaoTest`):
  - an attack ending after the new start ends at it, and its later pain entry moves to it. This test failed before
    the fix;
  - attacks ending before or at the start are unchanged (a regression guard);
  - an attack in progress blocks the start, and nothing moves (a regression guard);
  - from the review: a deleted attack in its undo window, an attack wholly after the start, and one ending past the
    tolerance keep their ends and points.
- [x] 1.3 `AttackDao`: in the start transaction, move the pain entries and then the end of every ended attack that runs
  past the start by at most the clock tolerance

## 2. Verify

- [x] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [x] 2.2 `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest`
- [x] 2.3 `openspec validate fix-start-inside-ended-attack --strict`
- [x] 2.4 On emulator-5570, QA #78's steps: home lists two migraines that touch and do not overlap

## 3. Review

- [x] 3.1 Independent review (`superpowers:code-reviewer`) of efd78be

## Verification record

- **Tests:**
  - The main new test failed before the fix ("expected:<3000> but was:<5000>").
  - After the fix and the review's additions, `:datasource:implementation:connectedDebugAndroidTest` on emulator-5570
    passes 104/104.
- **Gate:** `assembleDebug test ktlintCheck detekt` passes. The first run failed only on one query line over 100
  characters, which was wrapped.
- **Emulator (emulator-5570, API 36, debug build, empty diary), QA #78's steps:**
  - Past migraine 1:00 PM to 3:05 PM, added at 15:00:58. "Migraine started" at 15:01:22, "Migraine ended" at
    15:02:07.
  - Home: "Migraine from 1:00 PM to 3:01 PM", "Migraine from 3:01 PM to 3:02 PM", "2 migraines today". The past
    migraine's details show "Ended … 3:01 PM", "Duration 2 h 1 min".
  - A past migraine 11:00 AM to 12:30 PM, then a start and an end: no earlier end changed. 0 FATAL EXCEPTION.
- **Review findings:**
  - **Important: an ended attack wholly after the start can still overlap.** The details screen and a merge accept a
    start up to the end, which may lie up to 5 minutes after now. The spec now promises only what the code does: a
    start never falls inside an ended attack. Capping those starts at now goes to develop, as listed in the proposal.
  - **The trim had no bound.** It now applies only within the 5-minute clock tolerance, so a phone clock moved back
    never cuts a real attack silently. BRD, spec and code agree.
  - **Test gaps:** a deleted attack in its undo window, an attack wholly after the start, and one past the tolerance
    now keep their ends and points.
  - **Not changed:** points moved to the same instant sort by id in different places. That shows only on an attack
    with two or more future-dated points, and there is no write impact.
  - **Named for develop:** Undo of an end reopens the attack without checking ended attacks added since. This existed
    before 1.0.0.
- **Not checked on the emulator after the bound:** the bound does not change QA #78's case (4 minutes ≤ 5 minutes).
  The instrumented test covers the clock-moved-back case.
