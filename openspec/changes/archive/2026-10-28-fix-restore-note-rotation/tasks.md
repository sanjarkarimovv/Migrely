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
- [x] 3.3 On emulator-5570:
  - Settings › Restore purchases, then turn the phone: the line stays, with and without Premium;
  - a new restore still shows its own line;
  - leaving Settings (a sub-page, the paywall, Home) and coming back: the line is gone, as before.

## 4. Review

- [x] 4.1 Independent review (`superpowers:code-reviewer`)

## Verification record

- **Gate:** `assembleDebug test ktlintCheck detekt lint` passes.
- **Unit test:** `PremiumSectionShownResolverTest` is 6/6.
- **Emulator walks (emulator-5570, API 36, debug build, simulated store):**
  - **Free:** "No purchases were found for this Google account." stays through landscape and back.
  - **Premium:** "Premium was found and is on." stays through a rotation.
  - **Unreachable:** "Google Play can't be reached. Try again later." stays through a rotation. On a new restore the old line goes while it checks, and the new one shows.
  - **Leaving Settings removes the line on the first return:** a sub-page and Back; the paywall and Back or Close; HOME and return through the launcher or Recents; and after a rotation, a sub-page.
  - **Staying on Settings:** the line stays for more than 20 s.
  - **Crashes:** 0.
- **Review findings fixed:**
  - the note was kept whenever the status was the same, so "can't be reached" could outlast an answer from Play. Now it stays across a rotation only;
  - a new restore drops the last note while it checks;
  - the resolver takes the whole view state.
- **Walk finding fixed:** the first walk saw the line come back after the first exit. The cause was the composable resolving the last view state again at every composition, which brought back the note it had just dropped. Each view state is now resolved once, as it comes, in a `LaunchedEffect` collector, the pattern Insights uses.
