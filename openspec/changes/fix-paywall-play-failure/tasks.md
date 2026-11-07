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

## 3a. Messages (#152, design D7, BRD D158)

Do these in order. The baseline comes first, on the build before any of these changes.

- [ ] 3a.1 (partial: Attack details at 100% landscape not measured; Add past migraine and Doctor report cannot raise a message in normal use) Baseline on the current build, `emulator-5570`, debug. Measure #152's second table as "before": Home Today, History, My medications, Your data, Log medication, Attack details, Add past attack, Edit medication, Doctor report. At 100% and 200%, portrait and landscape, with a message showing, record the message's bounds, each control under it, and what a tap at that control's centre does. Verify: the "before" table is in the verification record. Restore every device setting.
- [x] 3a.2 `MigrelySnackbarHost`: one top offset parameter, default none, and one guard parameter, default 800 ms. Paywall: 48 dp and 400 ms. Every other screen with a top bar, and Home Today: the offset of design D7. Landscape attack mode, portrait attack mode and screens with no top bar: unchanged. Verify: `./gradlew :designsystem:assembleDebug :app:assembleDebug`, and `git grep -n "MigrelySnackbarHost(" -- 'features/*.kt'` reviewed call by call.
- [x] 3a.3 `MigrelySnackbar`: a tap on the body of a message with a timeout and no action calls `onDismiss`, by touch only with no new semantics. With an action, or with no timeout ("storage full"), a body tap does nothing. Each caller says so from `baseDurationMillis`, not from `timeLeft`, which is null for every message with no action. Verify: a `uiautomator dump` shows no new clickable node, and the Dismiss button is still there.
- [x] 3a.4 Tests. The guard parameter: 800 ms by default and 400 ms when passed, each with a tap just before and just after. The body tap: with a timeout and no action it dismisses; with an action it does nothing, and Undo itself still works; with no timeout and no action it does nothing, and Dismiss and a swipe still close it. A check that every "storage full" mapping reaches the snackbar as no timeout. Show a negative control for each. Verify: `:designsystem:connectedDebugAndroidTest` with `ANDROID_SERIAL=emulator-5570`, `MigrelySnackbarHostTest`, with counts recorded.
- [ ] 3a.5 (partial: re-tap at 200, 400 and 600 ms was run on the paywall and Your data only, not every screen; the rest is in the record) "After" measure, the same table as 3a.1 at 100% and 200%, portrait and landscape, plus:
  - Paywall: a first tap on Close closes it while the message shows.
  - Paywall: the buy button scrolled under the message: tap the message, then re-tap the button at 200, 400 and 600 ms, and record each.
  - Every screen in the table: tap a message with a timeout and no action, and re-tap the control under it at 200, 400 and 600 ms where a control lies under it.
  - Undo: a body tap does nothing, the message stays, and Undo still works.
  - "Storage full", if the walk can produce it, otherwise by the test in 3a.4: a body tap does nothing, and Dismiss closes it.
  - Reduce motion: the message is in its final place at once, and the guard and the body tap behave the same.
  - Home Today: Settings at 200% with a date that wraps, and after scrolling.
  - My medications: a tap on Remove of row 1 with Undo showing, at 100%.
  - Landscape Home Today, 100% and 200%: the bounds of the message against "Migraine started".
  - Portrait attack mode and landscape attack mode (D52): same as "before".
  - By eye, on the paywall: the two X icons at the right edge.
  Verify: the "after" table is beside "before" in the verification record. A control the message still covers is listed with what a tap did. If the message overlaps "Migraine started", send it to `team-lead` before the release is cut. Restore every device setting.

## 3b. Review of #154

- [x] 3b.1 Try again (I1, BRD D153): `onRetryAction` shows `PaywallViewState.Loading` at once, ignores a second tap while a reading is out, and delivers `PlayUnreachable` if the reading is still `Unreachable`. If the executor reports a failure the notice returns too; a throw inside `executeInBackground` does not reach that branch on a device, because `BackgroundExecutingUseCase` runs it in its own coroutine, so R5 proves the branch and not a device throw. Verify: `PaywallViewModelTest` with mutants R1 to R5, and a live measure.
- [x] 3b.2 A reading from an earlier paywall (M3): `onEnter` invalidates it, so it delivers no message and no state on the new paywall, and the new paywall's own reading still shows Premium. A rotation does not invalidate it. Verify: `PaywallViewModelTest` with mutants V1 to V3.
- [x] 3b.3 A buy tap clears an older message (M4): the screen's message and the view model's undelivered outcome. Verify: mutant B1 for the view model, and a live row for the screen, which has no test.
- [x] 3b.4 A migraine's message comes after its mode (M2), and so does an undo's (N4). On a failed reading the message is told and the error then replaces it, as before. Verify: `HomeViewModelTest` with mutants O1 to O6. These prove the order the screen hears, by unit test. The picture of the jump was not compared before and after, and the undo order was not walked.
- [x] 3b.5 A dismiss acts only on the message still shown, on the four hosts that did not check (M5), and the paywall's always-true check goes (M7). Verify: by reading only: nothing drives a body tap on a message fading out.
- [x] 3b.6 Each screen's message setup is pinned (I2). `MigrelySnackbar` takes `baseDurationMillis`, so no screen works it out. Verify: each mapper's storage full, and `MessageCallSitesTest` for each site, with a mutant for each.

## 4. Gate and walk

- [x] 4.1 Gate. Verify: `./gradlew assembleDebug test ktlintCheck detekt lint`, with exit code and test count recorded.
- [x] 4.2 No permission change. Verify: `./gradlew :app:checkReleasePermissions` passes, and `git diff origin/develop... -- '*AndroidManifest.xml' 'app/build.gradle.kts'` is empty.
- [ ] 4.3 (partial: Try again and Restore under the message on 360 x 640dp and buy at 200% landscape not run, TalkBack itself not run, most rows predate D7) Walk on `emulator-5570` with the simulated store, one row per scenario:
  - The line under the terms for Yearly, Monthly and Lifetime, and identical on each.
  - Cancel in the simulated sheet: no message, the button works again.
  - Store set to `unreachable` with the paywall open, then buy: "The purchase didn't go through. Try again." and then the unreachable notice with no line.
  - The reload fails after a sheet: in that same state the notice reads "Google Play can't be reached right now" and exactly "Check your connection, then try again. Your diary works offline.", with "Try again" and no line.
  - Store set to `unavailable`: the region notice only, no line, no second message.
  - Pending: both show. Monthly subscriber from Settings: the line is above "Manage subscription". Lifetime owner: no button, no line.
  - Restore with the store `unreachable`: the message on the paywall and the note in Settings.
  - 200% font in portrait and landscape: nothing cut, everything reachable. Rotation with the message showing. Dark and light. TalkBack order from `uiautomator dump`.
  - Controls under the paywall's message. Answered in #152, on d60dbb4c: at 100% a tap at Close lands on Dismiss, at 200% nothing happens, the buy button under Restore's message opens no sheet, and system Back closes. The fix is design D7 and task 3a, which measures the result. In this walk, confirm on the final build: Make a purchase fail (store `unreachable`, paywall open, buy) and a first tap on Close closes the paywall at 100% and 200%. "Try again" and Restore under the message at 200%: tap the message, then re-tap. A control that does not answer as 3a.5 recorded goes to `team-lead` before the release is cut.
  Verify: each row and its result are in the verification record below. Restore every device setting.

## 5. QA, then the real phone

- [ ] 5.1 `qa-engineer`: `.maestro/subscription/paywall-and-purchase.yaml` asserts the paywall from the top with no scroll, and its order changes here (design D5). Update it, add the line's assertion and the `Covers:` lines, and check `.maestro/device/` and `.maestro/a11y-tour/paywall.yaml`. Show that the new assertion fails on a build without the line. Verify: the flow passes on `emulator-5580`.
- [ ] 5.2 `code-reviewer` on `git diff origin/develop...bugfix/146-paywall-play-failure`, with fixes recorded here.
- [ ] 5.3 Real phone, before the 1.1.0 Go/No-Go, on the route used in `docs/qa/go-no-go-1.0.0.md` ("Real Google Play"): main session, owner present, license tester. Also look at "Try again" when Play answers fast: the page moves twice (design D7). The owner's order of no Play Console uploads while the app is in review applies, so `team-lead` decides when a build can reach the phone. With airplane mode on and the paywall showing prices, tap each of the three plans and record for each: whether Play's sheet appears, and what the paywall shows after it. No message means a cancel or no answer, "didn't go through" means `Failed`, "can't be reached" means `Unavailable`. So this also answers the open question in #146 of which code comes back, with no logging. Then tap Restore and record what it says. Restore airplane mode after. Verify: the results are in the QA report for the release.

## 6. Finish

- [ ] 6.1 Archive. Verify: `openspec archive fix-paywall-play-failure --yes`, then `openspec validate --specs --strict`.
- [ ] 6.2 PR into `develop` with `Refs #146`, never `Fixes`: the reporter retests and closes it.

## Verification record

Filled in by `android-engineer`, 2026-11-07. Evidence: `migrely-work/walks/bugfix-146/`.

### Gate

On the code of the delta review's commit. The instrumented row ran before that commit, and it changed nothing in `designsystem`.

| Check | Result |
|---|---|
| `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint`, `set -o pipefail` | Exit 0. 2437 test results, debug and release variants both counted, 0 failures |
| `:app:testDebugUnitTest --rerun`, Konsist | Exit 0. Architecture 15, 0 failures. `MessageCallSitesTest` 4, 0 failures |
| `:app:checkReleasePermissions` | Exit 0. Manifest and `app/build.gradle.kts` diff against `develop` empty |
| `ANDROID_SERIAL=emulator-5570 :designsystem:connectedDebugAndroidTest` | Exit 0. 52, 0 failures |

Module counts: `PaywallViewModelTest` 38, `HomeViewModelTest` 40, `MessagePlaceAndTapTest` 17, `PaywallContentsTest` 15, `MessageTopOffsetTest` 4.

### 3.1 and 3.3: the paywall's geometry

Emulator API 36, simulated store, Yearly, free user, doctor report's paywall. Base build c3d06f15 against the build with the line (d60dbb4c). "y" is the control's top in the scrolling content, in px, read from dumps with the scroll between them added up. Drag counts alone did not repeat between two runs of the same build, so y is the number compared. "Steps" are 200 px drags until the control is on screen.

| Display, font, orientation | Buy y, base | Buy y, after | Steps, base and after | Restore y, base | Restore y, after | Steps, base and after | Line y, height |
|---|---|---|---|---|---|---|---|
| Default, 100%, portrait | 1177 | 1177 | 0, 0 | 2092 | 1718 | 0, 0 | 1571, 105 |
| Default, 100%, landscape | 1112 | 1112 | 1, 1 | 1911 | 1595 | 6, 4 | 1448, 105 |
| Default, 200%, portrait | 1982 | 1982 | 0, 0 | 3957 | 2945 | 10, 4 | 2610, 293 |
| Default, 200%, landscape | 1566 | 1566 | 4, 4 | 3109 | 2425 | 12, 8 | 2090, 293 |
| 360 x 640dp, 100%, portrait | 1300 | 1300 | 0, 0 | 2347 | 1841 | 5, 2 | 1694, 105 |
| 360 x 640dp, 100%, landscape | 1112 | 1112 | 2, 2 | 1911 | 1595 | 6, 4 | 1448, 105 |
| 360 x 640dp, 200%, portrait | 2168 | 2168 | 4, 4 | 4575 | 3339 | 17, 10 | 2900, 397 |
| 360 x 640dp, 200%, landscape | 1566 | 1566 | 4, 4 | 3109 | 2425 | 13, 9 | 2090, 293 |

The button's y is unchanged in 8 of 8 rows, and Restore's y is smaller in 8 of 8.

At 200%, and in every row where the button needs a scroll, the line is not on screen when the button first comes into view, as the design accepted. At 100% portrait on the default display, line and button show together.

### Mutation

`PaywallViewModel`, `:features:subscription:presentation:test`, 46 tests: M1 the flag always false, 3 fail; M2 always true, 3 fail; M3 on `Cancelled`, 4 fail; M4 no check of the offers, 2 fail; M5 `else if` as `if`, 1 fails; M6 `notify` for `deliver`, 1 fails. Restored: 0 fail.

The line and the order, `:features:subscription:ui:testDebugUnitTest`, 34 tests: L1 line outside `choice != null`, 2 fail; L2 rule inverted, 8 fail; L3 above the terms, 3 fail; L4 under Manage, 1 fails; L5 Restore back under the benefits, 4 fail; L6 one word of the line, 6 fail; L7 notice text back, 1 fails. Restored: 0 fail.

The message, `MessagePlaceAndTapTest`, 17 tests: A1 host ignores `guardMillis`, 1 fails; A2 default 400, 1 fails; A3 host ignores `topOffset`, 3 fail; A4 no body tap, 1 fails; A5 body tap with an action, 1 fails; A6 body tap with no timeout, 1 fails; A7 body tap with semantics, 1 fails; A8 dismisses twice, 1 fails. Restored: 0 fail. These compare the two guards rather than exact times: under Robolectric each guard measured about twice its length (400 ms swallowed a tap at 576 and passed one at 1024; the default swallowed at 1520 and passed at 3024). The cause is not known. Exact times are in `MigrelySnackbarHostTest`, instrumented and not run yet.

The review's fixes, each mutant restored (`mutation-review.txt`), one run per mutant:

| Mutant | Tests failing |
|---|---|
| R1 no guard on a second Try again | 2 |
| R2 no state at once | 3 |
| R3 Try again says nothing | 4 |
| R4 retrying never cleared | 2 |
| R5 a failure the executor reports leaves it stuck | 1 |
| V1 enter does not invalidate | 4 |
| V2 reading not checked against the visit | 4 |
| V3 enter leaves retrying | 1 |
| B1 buy keeps an undelivered outcome | 1 |
| O1 start's message before the state | 1 |
| O2 end's message before the state | 1 |
| O3 a failed reading tells only the error | 1 |
| O4 a failure the executor reports loses the message | 1 |
| O5 an undo's message before the state (`mutation-delta.txt`) | 2 |
| O6 "not possible" before the state | 1 |
| H1 attack gets the offset; H2 short window gets it; H3 Today gets none; H4 the constant is 48 dp | 1 each |
| Each of 10 sites passes no duration: Home, Attack, History, Medication, Report, Your data, Reminders, Paywall, Settings, Onboarding | 1 each |
| P1 paywall offset 40 dp; P2 paywall guard 800; P3 guard not passed; P4 Home does not use its rule (redone after N1) | 1 each |
| Each of 7 mappers gives storage full a duration: Home, Attack, History, Medication, Report, Your data, Reminders | 1 each |

`MessageCallSitesTest` pins source text. It proves each screen passes the right value, not that the screen draws it. The chain to a body tap is: the mapper's test, this pin, then `MessagePlaceAndTapTest`. R5 and O4 prove the `onException` branches, not a throw on a device: `BackgroundExecutingUseCase` keeps that from reaching them.

### 4.3 walk

Merged build 3ddc80b9, except where a row says otherwise. These rows ran before D7's code. The rows D7 can change are confirmed in 3a below on the final code.

| Row | Result |
|---|---|
| Line on Yearly, Monthly, Lifetime | Same text, under each plan's terms (d60dbb4c) |
| Cancel in the sheet | No message, and the button opens the sheet again (d60dbb4c) |
| Store `unreachable`, buy | "The purchase didn't go through. Try again.", then the notice, no line (d60dbb4c) |
| Notice text | "Google Play can't be reached right now", exactly "Check your connection, then try again. Your diary works offline.", "Try again", no line (d60dbb4c) |
| Store `unavailable`, buy | Region notice only, no line, no message 3 s later |
| Pending | "Payment pending" and the line both show |
| Monthly subscriber from Settings | Line at y 1504, above Manage subscription at 1683 |
| Lifetime owner | Settings offers no way to the plans, and the success screen has no button or line. The plans with nothing to choose are covered by `PaywallContentsTest` only |
| Restore, store `unreachable` | Paywall: "Google Play can't be reached. Try again later." Settings: the same note |
| 200% portrait and landscape | Line whole at 293 px, every part reached by scrolling |
| Rotation with a message showing | **The message goes.** Gone 2.5 s after rotating; cards and line stay. On-screen messages are `remember`, so this reads as older than this change. Not run on the base build |
| Process death, `kill -9`, paywall open | Reopens on Home. The next paywall shows the line |
| Dark and light | Line reads as the terms in both; checked by eye on one screenshot each |
| TalkBack order from the dump | Close, headline, lead, three cards, button, terms, line, Restore, benefits. The line is stop 9, Restore stop 10. TalkBack itself not run |
| `Unavailable`, products offered (temporary one-line build, never committed) | Cards back, "Google Play can't be reached. Try again later.", gone by 9 s, and the button opens the sheet again |
| Rotation during the sheet, same temporary build | The message shows on the rotated screen, gone by 9 s |
| Close under the message, final code | Failed purchase: a first tap on Close closes the paywall at 100% and 200%, portrait and landscape |
| Try again and Restore, final code, 200% portrait, default display | At maximum scroll they stay at y 591 and 780, below the message (text ends at 496). Tap the message, then Try again: the cards come back |

Not checked: a real Play sheet (task 5.3), buy under the message at 200% landscape, Try again and Restore under the message on 360 x 640dp (the driver could not open the paywall at that size).

### 3a.1 and 3a.5: messages, before and after

Emulator API 36, 1080x2400, debug. Before is 3ddc80b9; after is the code of f6f9ce9e. Each cell restores a seed, raises a real message, proves it by a dump, and taps the control's centre. "Covered" means the control has no node in the dump while the message shows. "Tap" names the screen afterwards, so "home" after Back on a screen reached from Home means Back answered. "Message after" was read 3 to 5 s later, because a dump takes up to 3 s, so "None" after a tap that did nothing can be the timeout.

| Screen | Font | Orientation | Control | Before | After |
|---|---|---|---|---|---|
| details | 100% | land | Back | control not on screen | control not on screen |
| details | 100% | port | Back | covered; text y 194-241; tap: attack details; message after: None | not covered; text y 341-388; tap: home; message after: None |
| details | 200% | land | Back | not covered; text y 116-201; tap: home; message after: None | not covered; text y 263-348; tap: home; message after: None |
| details | 200% | port | Back | covered; text y 181-266; tap: attack details; message after: None | not covered; text y 328-413; tap: home; message after: None |
| edit | 100% | land | Back | not covered; text y 116-221; tap: my medications; message after: None | not covered; text y 263-368; tap: my medications; message after: None |
| edit | 100% | port | Back | covered; text y 181-286; tap: edit medication; message after: You can keep up to 50 | not covered; text y 328-433; tap: my medications; message after: None |
| edit | 200% | land | Back | not covered; text y 116-305; tap: my medications; message after: None | not covered; text y 263-452; tap: my medications; message after: None |
| edit | 200% | port | Back | covered; text y 181-474; tap: edit medication; message after: You can keep up to 50 | not covered; text y 328-621; tap: my medications; message after: None |
| history | 100% | land | Back | not covered; text y 129-176; tap: home; message after: Migraine deleted | not covered; text y 276-323; tap: home; message after: Migraine deleted |
| history | 100% | land | Doctor report | not covered; text y 129-176; tap: doctor report; message after: None | not covered; text y 276-323; tap: doctor report; message after: None |
| history | 100% | port | Back | covered; text y 194-241; tap: history; message after: None | not covered; text y 341-388; tap: home; message after: Migraine deleted |
| history | 100% | port | Doctor report | covered; text y 194-241; tap: history; message after: Undone | not covered; text y 341-388; tap: doctor report; message after: None |
| history | 200% | land | Back | not covered; text y 116-201; tap: home; message after: Migraine deleted | not covered; text y 263-348; tap: home; message after: Migraine deleted |
| history | 200% | land | Doctor report | not covered; text y 116-201; tap: ?Back,Doctor report; message after: None | not covered; text y 263-348; tap: ?Back,Doctor report; message after: None |
| history | 200% | port | Back | covered; text y 181-266; tap: history; message after: None | not covered; text y 328-413; tap: home; message after: Migraine deleted |
| history | 200% | port | Doctor report | covered; text y 181-266; tap: ?Sunday, November 22,Monday, November 23; message after: Undone | not covered; text y 328-413; tap: doctor report; message after: None |
| home | 100% | land | Settings | covered; text y 129-176; tap: home; message after: None | covered; text y 129-176; tap: home; message after: None |
| home | 100% | port | Settings | covered; text y 194-241; tap: attack mode; message after: Undone | not covered; text y 341-388; tap: settings; message after: None |
| home | 200% | land | Settings | covered; text y 116-201; tap: home; message after: None | covered; text y 116-201; tap: ?Back,Settings; message after: None |
| home | 200% | port | Settings | covered; text y 181-266; tap: attack mode; message after: Undone | not covered; text y 328-413; tap: settings; message after: None |
| log | 100% | land | Back | not covered; text y 129-176; tap: home; message after: None | not covered; text y 276-323; tap: home; message after: None |
| log | 100% | port | Back | covered; text y 194-241; tap: log medication; message after: Pick a | not covered; text y 341-388; tap: home; message after: None |
| log | 200% | land | Back | not covered; text y 116-201; tap: home; message after: None | not covered; text y 263-348; tap: home; message after: None |
| log | 200% | port | Back | covered; text y 181-266; tap: log medication; message after: Pick a | not covered; text y 328-413; tap: home; message after: None |
| medications | 100% | land | Back | not covered; text y 129-176; tap: log medication; message after: None | not covered; text y 276-323; tap: log medication; message after: None |
| medications | 100% | land | Remove | not covered; text y 129-176; tap: my medications; message after: Medication removed | not covered; text y 276-323; tap: my medications; message after: Undone |
| medications | 100% | port | Back | covered; text y 194-241; tap: my medications; message after: None | not covered; text y 341-388; tap: log medication; message after: None |
| medications | 100% | port | Remove | not covered; text y 194-241; tap: my medications; message after: Medication removed | not covered; text y 341-388; tap: my medications; message after: Undone |
| medications | 200% | land | Back | not covered; text y 116-201; tap: log medication; message after: None | not covered; text y 263-348; tap: log medication; message after: None |
| medications | 200% | land | Remove | not covered; text y 116-201; tap: my medications; message after: Medication removed | not covered; text y 263-348; tap: my medications; message after: Undone |
| medications | 200% | port | Back | covered; text y 181-370; tap: my medications; message after: None | not covered; text y 328-517; tap: log medication; message after: None |
| medications | 200% | port | Remove | not covered; text y 181-370; tap: my medications; message after: Medication removed | not covered; text y 328-517; tap: my medications; message after: Undone |
| yourdata | 100% | land | Back | not covered; text y 129-176; tap: settings; message after: None | not covered; text y 276-323; tap: settings; message after: None |
| yourdata | 100% | port | Back | covered; text y 181-286; tap: your data; message after: This phone has no | not covered; text y 328-433; tap: settings; message after: None |
| yourdata | 200% | land | Back | not covered; text y 116-305; tap: ?Back,Settings; message after: None | not covered; text y 263-452; tap: ?Back,Settings; message after: None |
| yourdata | 200% | port | Back | covered; text y 181-370; tap: your data; message after: This phone has no | not covered; text y 328-517; tap: settings; message after: None |

How to read it:

- **Home Today, window 480 dp or taller:** before, a tap at Settings hit Undo and reopened the migraine at 100% and 200%. After, it opens Settings. True to #152.
- **History:** before, Doctor report hit Undo, and Back did nothing. After, both answer. True to #152. Insights, measured at 100% portrait before D7, also hit Undo.
- **Your data, Log medication, Attack details, Edit medication, My medications:** before, Back did nothing in portrait. After, it answers. In landscape it already answered: the bar's corners lie outside a message 560 dp wide. So #152's "up to 8 s" holds in portrait only.
- **My medications, Remove on row 1:** #152's "200%" row was false before D7: the Remove's centre lay below the message and removed the row. After D7 Undo lies over it, and a tap at its centre hits Undo at 100% and 200%, portrait and landscape (message after: Undone). Design D7 accepted this for 100%. The "not covered" in those four rows is the driver matching another row's Remove; `remove` measured row 1 alone: no node in the dump, and the tap undid.
- **Doctor report and Add past migraine:** no message can be raised in normal use. Create report with nothing logged shows inline text, and the pickers refuse a wrong time. With the clock moved back, a future time shows an inline field error. Only storage full and generic failures reach these hosts, so their offset is read from the code and not measured. Report's after cells were not run.
- **Attack details, 100% landscape:** not measured on either build, because the driver found no Back there. 200% landscape is in the table.

Paywall, a first tap on Close with a failed purchase's message showing:

| Font, orientation | Before | After |
|---|---|---|
| 100% portrait | Close covered; stays on the paywall | Close in the dump, message text at y 307-412; closes |
| 100% landscape | Close in the dump; closes | Same; closes |
| 200% portrait | Close covered; stays on the paywall | Close in the dump, message text at y 307-496; closes |
| 200% landscape | Close in the dump; closes | Same; closes |

Home Today in a window under 480 dp, against "Migraine started", "Migraine ended" with Undo showing:

| Build, window, font | Undo | Button | Live tap | Result |
|---|---|---|---|---|
| D7 with the offset, 411 dp high, 200% | y 242-368 | y 314-503 | Centre, y 408 | Starts a migraine |
| Same | Same | Same | Top edge under Undo | Undo fires |
| Same | Same | Same | Top edge under the body | Nothing |
| Final, 411 dp high, 100% | y 89-215 | y 372-561 | Top edge | Starts a migraine |
| Final, 411 dp high, 130% | y 89-215 | y 342-531 | Top edge | Starts a migraine |
| Final, 411 dp high, 200% | y 95-221 | y 314-503 | Top edge | Starts a migraine |
| Final, 274 dp high, 100% | y 89-215 | y 221-363 | Not tapped | 6 px gap |
| Final, 274 dp high, 200% (#153) | y 95-221 | y 63-305 | Under Undo; under the body; below the message | Undo fires; nothing; starts a migraine |

A tap on Settings while that message shows, in those windows: 411 dp at 100%, 130%, 200% and 274 dp at 100%, 200%. Settings does not open and Undo does not fire: Undo starts at x 1317 or more, and Settings ends at x 1233 or less. One 411 dp, 200% read did open Settings, 5.3 s after the message was seen, which is past its 5 s. #153, filed by `team-lead`, holds the 274 dp case; it predates D7, since the offset there is 0.

Guard timing, device-measured gap between the two taps, on `-no-window -gpu swiftshader_indirect`:

| Guard | Control | Gap | Result | p90 frame time |
|---|---|---|---|---|
| Paywall, 400 ms | Buy, 200% portrait, under Restore's message | 228 ms | Swallowed | 25 ms |
| | | 415 ms | Swallowed | 29 ms |
| | | 514 ms | Sheet opens | 36 ms |
| | | 619 ms | Sheet opens | 32 ms |
| Default, 800 ms | Your data's first row, 100% portrait | 213 ms | Swallowed | 24 ms |
| | | 412 ms | Swallowed | 28 ms |
| | | 615 ms | Swallowed | 25 ms |
| | | 715 ms | Swallowed | 23 ms |
| | | 920 ms | Reaches the row | 26 ms |

The gap runs from the end of the first tap to the end of the second, on the device. A tap at 415 ms still being swallowed is about 15 ms past the guard's nominal end, within a frame or two. Robolectric's "about double" is not on the device.

Instrumented: `:designsystem:connectedDebugAndroidTest`, 52 tests, 0 failures, including `MigrelySnackbarHostTest`'s four guard tests. Controls on the device: host ignores `guardMillis`, 1 of 7 fails (`aGuardOf400MsLetsATapThroughJustAfterItEnds`); default 400, 1 of 7 fails (`theDefaultGuardSwallowsATapJustBefore800Ms`). `MessageTopOffsetTest`: see the review's mutants.

Other rows, on the final code:

| Row | Result |
|---|---|
| Undo: a body tap, then Undo, with no dump between | The point comes back and "Undone" shows, the same as with no body tap |
| No new clickable node | `The body tap adds nothing a screen reader can act on` (Robolectric), with a mutant; a live dump keeps Dismiss |
| Storage full, a real full disk, 100% and 200% | A body tap leaves it up, and Dismiss closes it. A swipe by test only |
| Reduce motion, animator scale 0 | Attack details: message at y 341-388, its final place; a first tap on Back answers; a body tap on a message with no action dismisses it. Guard timing in its own table |
| Home Today, 200%, date wraps ("Wednesday, September 30", y 170-359) | Settings y 203-329, its centre above the message's top edge at about y 297, so a tap at its centre opens Settings. Its bottom 32 px lie under the message |
| Home Today, after scrolling, 100% and 200% | Settings scrolled off. The message stays at its place and lies over whatever is there; in the 100% picture, the "Period" chip and the top of "Logged today". `shots/scrolled-1.0.png`, `scrolled-2.0.png` |
| Portrait and landscape attack mode (D52) | Same bounds before and after at 100% and 200% |
| Two X icons on the paywall, final code, 100% | Close y 140-266, Dismiss y 297-423, both at the right edge. Picture for `team-lead`: `migrely-work/walks/bugfix-146/shots/two-x-icons-100.png` |


Known limits, already accepted in the design:

- Emulators cannot run Play's sheet, so everything before 5.3 uses the simulated store.
- `Unavailable` with products still offered is proven by the view model test, because the simulated store fails both together. A temporary local build also showed it on a device.
- The buy button under the message is tested with Restore's message, not a failed purchase's, for the same reason. It is the same host and the same 8 seconds.
- A real phone and real people: the message's limits in design D7 are not measured on people.

### The review's measures, final code

Headless, `-no-window -gpu swiftshader_indirect`, 1080x2400, portrait, a real Undo message unless a row says otherwise. No dump stands between a message and its tap, and a screencap proves the message at the moment of the tap. Those APKs were built before a formatter pass and a comment merge, so with the same logic.

| Row | Font | Control | Undo | Live tap | Result |
|---|---|---|---|---|---|
| History, Previous month | 100%, 200% | y 297-423, x 43-169 | | Centre | Message stays, no "Undone" |
| History, Next month | 100%, 200% | y 297-423, x 913-1039 | | Centre | **Undo fires** |
| Trial banner's close | 100% | x 913-1039, y 313-439 | x 875-1027, y 301-427 | (976,376) | **Undo fires**, migraine reopens. Accepted |
| Trial banner's close | 200% | x 913-1039, y 351-477 | x 797-1027, y 307-433 | (976,414) | **Undo fires**, migraine reopens. Accepted |

The month chevrons' title is covered too, so "Previous" did nothing is read from the message staying and no "Undone", not from the title. Whether the banner's close answers once the message and guard have gone is not measured. Pictures: `shots/chevron-*.png`, `shots/banner-*.png`.

Safety card on Attack details, top of the scroll, which is its natural place because it is the screen's first block:

| Message | Font | Message's text | Title | Body | "Got it" |
|---|---|---|---|---|---|
| "Pain level removed", Undo y 301-427 | 100% | y 341-388 | y 370-427, covered | y 448-633, clear | y 633-759, clear |
| "Pain level removed", Undo y 307-433 | 200% | y 328-413 | y 396-601, visible from 445 | y 622-1049, clear | y 1049-1196, clear |
| "Couldn't save", full disk, Dismiss y 318-444 | 100% | y 328-433 | Covered | y 448-633, visible from 465: **about 17 px covered**. Accepted | y 633-759, clear |
| "Couldn't save", full disk, Dismiss y 360-486 | 200% | y 328-517 | y 396-601, visible from 549 | y 622-1049, clear | y 1049-1196, clear |

Read from code and not measured: its live region, and the card staying through a scroll. A first 10 that fails to save, on a full disk at 100%: the message shows and no card appears, which is older behaviour, filed separately by `team-lead`. Pictures: `shots/safety-*.png`, `shots/fullcard-*.png`; the two full-disk pictures are partly white, because the screencap had no room, so their bounds come from dumps.

Guard timing under Reduce motion, animator scale 0, device-measured gap:

| Guard | Control | Gap | Result | p90 frame time |
|---|---|---|---|---|
| Paywall, 400 ms | Buy, 200% portrait | 246 ms | Swallowed | 34 ms |
| | | 427 ms | Swallowed | 32 ms |
| | | 527 ms | Sheet opens | 46 ms |
| Default, 800 ms | Your data's first row, 100% portrait | 632 ms | Swallowed | 32 ms |
| | | 737 ms | Swallowed | 32 ms |
| | | 944 ms | Reaches the row | 32 ms |

Try again with Play unreachable, on a temporary build whose simulated store takes 5 s to fail, never committed:

| Measure | Result |
|---|---|
| Tap to first visible change | 0.13 s, by comparing screencaps |
| Node tree while reading | "Getting prices from Google Play" present, "Try again" absent |
| Notice, Try again and the message back | Read at 6.2 s after the tap |
| TalkBack | **Did not run.** The text's polite live region is read from code. Where focus goes when the button leaves the tree is not known |

A buy tap with Restore's message showing: the sheet opens with the message gone, and after Cancel no message shows. A double tap on Try again, a rotation during the reading, and a body tap on a fading message are not run on the device; the first two have view model tests.

Not checked on the final code: rerunning the before and after table, which ran on the code of f6f9ce9e. The fixes since do not change a message's place, but this was not re-measured.
