# Tasks

## 1. Fix

- [ ] 1.1 BRD EC-ATK-19
- [ ] 1.2 Tests first (instrumented `AttackDaoTest`):
  - an attack ending after the new start ends at it, and its later pain entry moves to it;
  - attacks ending before or at the start are unchanged;
  - an attack in progress blocks the start, and nothing moves.

  They fail before the fix.
- [ ] 1.3 `AttackDao`: in the start transaction, move pain entries and then the end of every ended attack that runs
  past the start

## 2. Verify

- [ ] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [ ] 2.2 `ANDROID_SERIAL=emulator-5570 ./gradlew :datasource:implementation:connectedDebugAndroidTest`
- [ ] 2.3 `openspec validate fix-start-inside-ended-attack --strict`
- [ ] 2.4 On emulator-5570, QA #78's steps: home lists two migraines that touch and do not overlap
