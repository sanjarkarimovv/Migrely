# Tasks

## 1. Test first

- [x] 1.1 `PremiumSectionShownResolverTest`:
  - a note-less read of the same status keeps the note;
  - a new status drops it;
  - a new restore shows its own;
  - with nothing shown, the answer shows.
  It does not compile before the resolver exists.

## 2. Fix

- [x] 2.1 `PremiumSectionShownResolver`, provided through `PremiumSectionDependencies`
- [x] 2.2 `PremiumSection` resolves each answer against what it shows

## 3. Verify

- [x] 3.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [x] 3.2 `openspec validate fix-restore-note-rotation --strict`
- [ ] 3.3 On emulator-5570:
  - Settings › Restore purchases, then turn the phone: the line stays, with and without Premium;
  - a new restore still shows its own line.

## 4. Review

- [ ] 4.1 Independent review (`superpowers:code-reviewer`)
