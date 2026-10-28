# Tasks

## 1. Test first

- [x] 1.1 `PremiumSectionShownResolverTest`:
  - a note-less read of the same status keeps the note;
  - a new status drops it;
  - a new restore drops it while checking and then shows its own;
  - a trial that ended drops it;
  - loading keeps what is shown.
  It does not compile before the resolver exists.

## 2. Fix

- [x] 2.1 `PremiumSectionShownResolver`, provided through `PremiumSectionDependencies`
- [x] 2.2 `PremiumSection` resolves each view state against what it shows, and drops the note when Settings pauses
  for anything but a configuration change (review)

## 3. Verify

- [x] 3.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [x] 3.2 `openspec validate fix-restore-note-rotation --strict`
- [ ] 3.3 On emulator-5570:
  - Settings › Restore purchases, then turn the phone: the line stays, with and without Premium;
  - a new restore still shows its own line;
  - leaving Settings (a sub-page, the paywall, Home) and coming back: the line is gone, as before.

## 4. Review

- [ ] 4.1 Independent review (`superpowers:code-reviewer`)
