# Tasks

Base: `feature/add-doctor-visit-mode`. This change comes first (T21 task 0.3). BRD D169, `Refs #178` in every commit and
the PR. Never `Fixes #178`: the lead filed it and closes it after the retest. Ownership:
- `ux-designer` and `medical-reviewer`: 0.1;
- `android-engineer`: 1 to 3;
- `qa-engineer`: 4.

## 0. Copy

- [ ] 0.1 `ux-designer` confirms, and `medical-reviewer` passes, the one new line "Not marked. Today already has an entry." (the other strings already passed on 2026-11-09). Verify: their written OK, or a replacement line that `product-manager` puts into `specs/reminders/spec.md`.

## 1. Strings

- [ ] 1.1 `notification_effect_title` becomes "Did it help?" with no argument. `notification_attack_logged` becomes the line from 0.1. Add `notification_public_title` = "Reminder". Verify: `grep -n 'notification_effect_title\|notification_attack_logged\|notification_public_title' app/src/main/res/values/strings.xml` shows the three lines, and `./gradlew :app:lintDebug` reports no unused or format-argument warning for them.

## 2. Notifications

- [ ] 2.1 In `NotificationDataSourceImpl`, every builder sets `VISIBILITY_PRIVATE` and a public version titled `notification_public_title`, with no text and no actions, from one place, so the T21 `visit` channel gets it too. The effect notification no longer formats the medication name. Verify: a Robolectric or unit test on the built `Notification` objects asserts `visibility == VISIBILITY_PRIVATE`, a public version with that title, and no medication name in the extras. Then `./gradlew :app:testDebugUnitTest`.
- [ ] 2.2 On `emulator-5570`, post each notification. Verify: `adb shell dumpsys notification --noredact | grep -E 'vis=|android.title'` shows `vis=PRIVATE` and the new titles.

## 3. Gate

- [ ] 3.1 Verify: `set -o pipefail; ./gradlew assembleDebug test ktlintCheck detekt lint` exits 0, and `openspec validate fix-notification-privacy --strict` exits 0.

## 4. QA (`qa-engineer`)

- [ ] 4.1 Update `qa/device_scenarios.sh`. Its asserts on "A migraine is logged today." and on `^Did Ibuprofen help\?` move to the new texts, and a negative assert checks that the medication name is absent. Verify: the `reminder_effect` and daily-answer scenarios pass on `Migrely_QA_API_36`.
- [ ] 4.2 Lock-screen screenshots of all three notifications, under "Show all content" and under "Hide sensitive content", on a secure lock. Check that no medication, "migraine" or channel name shows. Negative control: the 1.1.1 build under "Show all" shows the medication name. Verify: the screenshots and the result are in the change's Go/No-Go section in `docs/qa/`.
