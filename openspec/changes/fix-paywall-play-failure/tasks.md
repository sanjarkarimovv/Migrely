# Tasks

Layers: no domain, data or datasource change. `sheetResultOf` and `BuyPremiumUseCase` stay as they are. Before coding, take
the baseline in 3.1 from the current develop build.

## 1. Plan

- [x] 1.1 BRD: D153, EC-BIL-14…18, §7.3, §13 row 28. Verify: `grep -c "^| D153 " docs/BRD.md` prints 1, and `git show origin/develop:docs/BRD.md | grep -c "^| D153 "` prints 0 right before the merge. If another branch took D153 first, the second to merge renumbers.
- [x] 1.2 Proposal, design, spec delta, tasks. Verify: `openspec validate fix-paywall-play-failure --strict`.
- [x] 1.3 `ux-designer` reads the copy and placement in design D1, D2, D5. Any change comes back to `product-manager`, who updates the BRD and spec first.

## 2. Presentation

- [x] 2.1 `PaywallViewModel`, design D4: after a sheet answered `Unavailable`, once the reload has `PaywallOffersDomainModel.Loaded` and no `Unlocked` is being delivered, deliver `PlayUnreachable`. Correct the comment in `outcomeOf`. Verify: `./gradlew :features:subscription:presentation:test`.
- [x] 2.2 `PaywallViewModelTest`: `Unavailable` with products delivers `PlayUnreachable`; `Unavailable` with `PaywallOffersDomainModel.Unavailable` delivers nothing; with `Unreachable` delivers nothing; `Cancelled` delivers nothing; `Failed` still delivers `SomethingWentWrong`; an `Unavailable` outcome the screen missed is delivered once on `onResume`. Show a negative control: the first test fails with 2.1 reverted. Verify: same command, and record the failing run.

## 3. UI

- [x] 3.1 Baseline before changing the UI: on `emulator-5570`, debug build of the base commit, from `uiautomator dump`, record the top `y` of the buy button and of "Restore purchases" for: 100% and 200% font; portrait and landscape; the default 411 x 914dp display and an `adb shell wm size` override near 360 x 640dp. A control off the first screen has no bounds in the dump, so record it as "below the fold" with the number of swipes to reach it. Restore every setting after. Verify: the numbers are in the verification record.
- [x] 3.2 `strings.xml`: add `subscription_purchase_needs_play` and change `subscription_unreachable_text`, both with the exact text in design D1. Verify: `grep -c "Purchases need Google Play and an internet connection. Your diary works offline." features/subscription/ui/src/main/res/values/strings.xml` prints 1, and `grep -c "Check your connection, then try again. Your diary works offline." features/subscription/ui/src/main/res/values/strings.xml` prints 1.
- [x] 3.3 `PaywallScreen.kt`, design D2: in `Plans`, inside `if (choice != null)`, a `Text` after the terms, `bodyMedium`, `onSurfaceVariant`, with no live region, heading or `maxLines`. Design D5: in `PaywallOffer`, swap `Benefits()` and the Restore `TextButton`. Verify: `./gradlew :features:subscription:ui:assembleDebug`, then repeat 3.1: the buy button's position equals the baseline in every row, and Restore is at or above its baseline (a higher `y` position, or the same or fewer swipes). A row that fails stops the task.

## 4. Gate and walk

- [ ] 4.1 Gate. Verify: `./gradlew assembleDebug test ktlintCheck detekt lint`, with exit code and test count recorded.
- [ ] 4.2 No permission change. Verify: `./gradlew :app:checkReleasePermissions` passes, and `git diff origin/develop... -- '*AndroidManifest.xml' 'app/build.gradle.kts'` is empty.
- [ ] 4.3 Walk on `emulator-5570` with the simulated store, one row per scenario:
  - The line under the terms for Yearly, Monthly and Lifetime, and identical on each.
  - Cancel in the simulated sheet: no message, the button works again.
  - Store set to `unreachable` with the paywall open, then buy: "The purchase didn't go through. Try again." and then the unreachable notice with no line.
  - The reload fails after a sheet: in that same state the notice reads "Google Play can't be reached right now" and exactly "Check your connection, then try again. Your diary works offline.", with "Try again" and no line.
  - Store set to `unavailable`: the region notice only, no line, no second message.
  - Pending: both show. Monthly subscriber from Settings: the line is above "Manage subscription". Lifetime owner: no button, no line.
  - Restore with the store `unreachable`: the message on the paywall and the note in Settings.
  - 200% font in portrait and landscape: nothing cut, everything reachable. Rotation with the message showing. Dark and light. TalkBack order from `uiautomator dump`.
  - Controls under the paywall's message (lead's decision: the category Google rejected on). Default accessibility timeout, so the message lasts 8 seconds. Make a purchase fail: store `unreachable`, paywall open, buy. While the message shows, tap Close, and record whether Close answers or what the tap did instead. At 200%, do the same for "Try again" and Restore if the message lies over them. For the buy button: a failed purchase on the simulated store leaves no buy button, because its reload fails too. So use the same message host with the cards on screen: Restore with nothing bought ("No purchases were found for this Google account."), at 200%, with the button scrolled to the top, and if the message lies over the button, tap it within the 8 seconds. Record whether each control answers. This may not be left as a later note: if a control does not answer, send it to `team-lead` with the evidence before the release is cut. Do not fix it in this change without that decision.
  Verify: each row and its result are in the verification record below. Restore every device setting.

## 5. QA, then the real phone

- [ ] 5.1 `qa-engineer`: `.maestro/subscription/paywall-and-purchase.yaml` asserts the paywall from the top with no scroll, and its order changes here (design D5). Update it, add the line's assertion and the `Covers:` lines, and check `.maestro/device/` and `.maestro/a11y-tour/paywall.yaml`. Show that the new assertion fails on a build without the line. Verify: the flow passes on `emulator-5580`.
- [ ] 5.2 `code-reviewer` on `git diff origin/develop...bugfix/146-paywall-play-failure`, with fixes recorded here.
- [ ] 5.3 Real phone, before the 1.1.0 Go/No-Go, on the route used in `docs/qa/go-no-go-1.0.0.md` ("Real Google Play"): main session, owner present, license tester. The owner's order of no Play Console uploads while the app is in review applies, so `team-lead` decides when a build can reach the phone. With airplane mode on and the paywall showing prices, tap each of the three plans and record for each: whether Play's sheet appears, and what the paywall shows after it. No message means a cancel or no answer, "didn't go through" means `Failed`, "can't be reached" means `Unavailable`. So this also answers the open question in #146 of which code comes back, with no logging. Then tap Restore and record what it says. Restore airplane mode after. Verify: the results are in the QA report for the release.

## 6. Finish

- [ ] 6.1 Archive. Verify: `openspec archive fix-paywall-play-failure --yes`, then `openspec validate --specs --strict`.
- [ ] 6.2 PR into `develop` with `Refs #146`, never `Fixes`: the reporter retests and closes it.

## Verification record

To be filled in by `android-engineer`: the baseline and after tables from 3.1 and 3.3, the walk rows from 4.3, and what was not checked and why.

Known limits, already accepted in the design:

- Emulators cannot run Play's sheet, so everything before 5.3 uses the simulated store.
- `Unavailable` with products still offered is proven only by the view model test, because the simulated store fails both together.
- The buy button under the message is tested with Restore's message, not a failed purchase's, for the same reason. It is the same host and the same 8 seconds.
