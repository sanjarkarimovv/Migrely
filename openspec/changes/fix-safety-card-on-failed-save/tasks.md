# Tasks

Base: `bugfix/155-safety-card-on-failed-save` from `origin/release/1.1.0` (94730827).

## 1. Plan

- [x] 1.1 BRD D163, EC-ATK-22, §13 row 32; proposal, design, delta. Verify: `openspec validate fix-safety-card-on-failed-save --strict`.

## 2. Fix

- [ ] 2.1 `AddIntensityPointUseCase` and `AddIntensityPointResultDomainModel`, design D1: a 10 with `maximumRecorded()` `NotRecorded` or `Failed` is a first maximum, worked out before every outcome but `NotFound`, and carried by each of them. Tests first in `AddIntensityPointUseCaseTest`: storage full, generic failure, attack lookup failed, outside the attack, in the future, unreadable history, a later 10 with one saved, a 9. Verify: `./gradlew :features:attack:domain:test` passes, and fails first on the old code.
- [ ] 2.2 `AttackDetailsViewModel.onAddPointAction`: notify `FirstMaximumRecorded` when the outcome carries it, then the outcome's own notification, in that order. Tests in `AttackDetailsViewModelTest` for each outcome, with the order asserted. Verify: `./gradlew :features:attack:presentation:test` passes.
- [ ] 2.3 Negative control, local only: make `StorageFull` carry false again. Verify: the new tests fail, named in the packet; `git diff --stat` empty afterwards.

## 3. Gate and device

- [ ] 3.1 Gate. Verify: `nice -n 19 ./gradlew --max-workers=2 assembleDebug test ktlintCheck detekt lint` exits 0.
- [ ] 3.2 On `emulator-5570`, debug, with TalkBack on: a first 10 that fails on storage full. Record what TalkBack speaks, in order, from its speech log. Verify: both the message and the card's text are spoken in full, the message first. If the card cuts off the message, make the card's live region polite in this failed case only (design D2), say so in the packet, and record again. Restore every setting.

## 4. QA

- [ ] 4.1 `qa-engineer`, a real full disk as in #155's measure (data partition filled to about 16 MB free), debug and R8: a first 10 shows the card and "Couldn't save"; after freeing space the same 10 saves and the card shows again; a later 10 shows none; rotation keeps both; TalkBack speaks both in full, the message first. Verify: a table in the report; restore the disk and every setting.
