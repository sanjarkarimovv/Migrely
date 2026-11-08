# Tasks

Base: `bugfix/155-safety-card-on-failed-save` from `origin/release/1.1.0` (94730827).

## 1. Plan

- [x] 1.1 BRD D163, EC-ATK-22, §13 row 32; proposal, design, delta. Verify: `openspec validate fix-safety-card-on-failed-save --strict`.

## 2. Fix

- [x] 2.1 `AddIntensityPointUseCase` and `AddIntensityPointResultDomainModel`, design D1: a 10 with `maximumRecorded()` `NotRecorded` or `Failed` is a first maximum, worked out before every outcome but `NotFound`, and carried by each of them. Tests first in `AddIntensityPointUseCaseTest`: storage full, generic failure, attack lookup failed, outside the attack, in the future, unreadable history, a later 10 with one saved, a 9. Verify: `./gradlew :features:attack:domain:test` passes, and fails first on the old code.
- [x] 2.2 `AttackDetailsViewModel.onAddPointAction`: notify `FirstMaximumRecorded` when the outcome carries it, then the outcome's own notification, in that order. Tests in `AttackDetailsViewModelTest` for each outcome, with the order asserted. Verify: `./gradlew :features:attack:presentation:test` passes.
- [x] 2.3 Negative control, local only: make `StorageFull` carry false again. Verify: the new tests fail, named in the packet; `git diff --stat` empty afterwards.

## 3. Gate and device

- [x] 3.1 Gate. Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0.
- [x] 3.2 On `emulator-5570`, debug, with TalkBack on: a first 10 that fails on storage full. Record what TalkBack speaks, in order, from its speech log. Verify: both the message and the card's text are spoken in full. If the card cuts off the message, make the card's live region polite in this failed case only (design D2), say so in the packet, and record again. Restore every setting. Result: the message and the card's body sentence were spoken in full, the message first, which is observed and not set by the code (design D2). At 100% the message covers the card's title, which was neither in the tree nor spoken: accepted for 1.1.0 by `team-lead`, and QA files the follow-up issue after measuring. No fallback was needed.

## 4. QA

The change is archived with 4.1 open: QA runs it on the release candidate.

- [ ] 4.1 `qa-engineer`, a real full disk as in #155's measure (data partition filled to about 16 MB free), debug and R8: a first 10 shows the card and "Couldn't save"; after freeing space the same 10 saves and the card shows again; a later 10 shows none; rotation keeps both; TalkBack speaks the message and the card's body sentence in full, recording the order; at 100% and 200%, whether the message covers the card's title, so QA can file the follow-up issue that moves the message off the card. Verify: a table in the report; restore the disk and every setting.

## Verification record

Filled in by `android-engineer`, 2026-11-08. Evidence: `migrely-work/walks/bugfix-155/`.

### Tests, first red

The model's new shape went in with every new flag false, the old behaviour, before the fix:

| Module | Tests | Failing on the old behaviour |
|---|---|---|
| `:features:attack:domain:test` | 113 | 6: storage full, write failure, attack lookup failure, outside the attack, in the future, unreadable history |
| `:features:attack:presentation:test` | 64 | 4: card then storage full, card then failure, card then "outside", card then "in the future" |

After the fix: `AddIntensityPointUseCaseTest` 17 and `AttackDetailsViewModelTest` 37, 0 failures. The old test "Given the check fails when adding 10 then the card is not shown" is replaced, because the spec now says the opposite: an unreadable history shows the card.

### Negative controls (task 2.3), each restored (`mutation.txt`)

| Mutant | Tests failing |
|---|---|
| N1 storage full carries false (task 2.3's own) | 1: `Given storage is full when adding a first 10 then the outcome carries the card` |
| N2 a failed write carries false | 1 |
| N3 a failed attack lookup carries false | 1 |
| N4 a time before the start carries false | 1, after adding that test; it survived at first |
| N5 a time after the end carries false | 1 |
| N6 a time in the future carries false | 1 |
| N7 an unreadable history counts as a saved 10 | 1 |
| N8 any value counts as a 10 | 1 |
| V1 the message before the card | 4 |
| V2 the card only when saved | 4 |

### Gate (task 3.1)

| Check | Result |
|---|---|
| `nice -n 19 ./gradlew --continue --max-workers=2 assembleDebug test ktlintCheck detekt lint` | Exit 0. 2719 test results, debug and release variants both counted, 0 failures |
| `:app:testDebugUnitTest --rerun` | Exit 0. Konsist 16, 226 in that task, 0 failures |
| `:app:checkReleasePermissions` | Exit 0. Manifest and `app/build.gradle.kts` unchanged from 94730827 |
| D159 to D161 tests, unchanged | `DoubleTap*` 37, `SecondTapRule*` 23, `DialogLift*` 14 in `:app`, `Dialog*` 127 in `designsystem`: 0 failures |

### Device (task 3.2)

`emulator-5570`, `-no-window -gpu swiftshader_indirect`, debug build of e3637f0e, 100% portrait. A migraine was started for today and its details opened. The data partition was filled to about 16 MB free, then a 10 added.

| Row | Result |
|---|---|
| Card on a failed first 10 | Shown with "Couldn't save. Your phone's storage is full.": message text y 328-433, Dismiss y 318-444; card body y 465-633 visible, "Got it" y 633-759. The card's title "Sudden, worst-ever pain?" is absent from the tree (`walk.tsv`: `title absent`): the message covers it at 100% |
| TalkBack, read from its own verbose log (`talkback-speech.log`) | 16.038 s: "Couldn't save. Your phone's storage is full." starts, then "Dismiss", "Button" to 19.7 s. The card's speech is queued "waiting for talkback_3", not interrupting it. 20.352 s: "If this pain came on suddenly and is the worst of your life, call emergency services now." starts, then "Got it", "Button" to 26.5 s. The title is never spoken: 0 lines with "Sudden". The message's event came 36 ms before the card's (16.036 s and 16.072 s) |

So TalkBack spoke the message first and the card's body sentence after it, each in full, on this one run; the order is observed, not set by the code, which notifies the card first. The card's title was not spoken, because the message covers it at 100% (accepted for 1.1.0, design D2). Both stay assertive, and the card's live region is unchanged.

Restored: the filler files removed (free space back to 8.39 GB), TalkBack off and its data cleared, because this was its first run on this AVD. Also app data (database and every preference file equal the snapshot by md5), font 1.0, rotation 0, auto-rotate 1, `wm size` 1080x2400, and the clock in step with the host. 5570 is off.

Not checked:
- A real phone and a person.
- Rotation with both showing.
- The same 10 saved after freeing space.
- A later 10 on the device, and 200%.
- A generic failure or a refused time on the device; these are covered by tests only.
- TTS on this headless emulator ran with no audio output, so the times are TalkBack's, not heard.
- A body tap on the card's message failed to send while the disk was full; that input is not part of this change.
