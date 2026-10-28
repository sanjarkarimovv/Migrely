# Tasks

## 1. Test first

- [x] 1.1 `HomeViewModelTest`, with an executor that answers out of order:
  - when an older read answers last, the newer state stays;
  - an older read's failure says nothing.
  Both failed before the fix.

## 2. Fix

- [x] 2.1 `loadToday`: the newest read wins

## 3. Verify

- [x] 3.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [x] 3.2 `openspec validate fix-home-today-order --strict`
- [ ] 3.3 On emulator-5570, with home open: fire the "Start migraine" shortcut intent and expect attack mode within 5 s, then end the attack. Ten times, one of them after a clock jump.

## 4. Review

- [ ] 4.1 Independent review (`superpowers:code-reviewer`)
