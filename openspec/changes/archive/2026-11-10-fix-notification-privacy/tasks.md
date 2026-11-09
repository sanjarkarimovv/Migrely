# Tasks

Base: `feature/add-doctor-visit-mode`. This change comes first (T21 task 0.3). BRD D169, `Refs #178` in every commit and
the PR. Never `Fixes #178`: the lead filed it and closes it after the retest. Ownership:
- `ux-designer` and `medical-reviewer`: 0.1;
- `android-engineer`: 1 to 3;
- `qa-engineer`: 4.

## 0. Copy

- [x] 0.1 `ux-designer` confirms, and `medical-reviewer` passes, the one new line. Verify: their written OK, or a replacement line that `product-manager` puts into `specs/reminders/spec.md`. Done: ux chose "Not marked. Open Migrely to see today.", which replaces "Not marked. Today already has an entry." because that one hinted at an attack. The lead agreed and medical-reviewer passed it by the same criteria. It shipped in 0596755c (`app/src/main/res/values/strings.xml:12`), and the spec is updated.

## 1. Strings

- [x] 1.1 `notification_effect_title` becomes "Did it help?" with no argument. `notification_attack_logged` becomes the line from 0.1. Add `notification_public_title` = "Reminder". Verify: `grep -n 'notification_effect_title\|notification_attack_logged\|notification_public_title' app/src/main/res/values/strings.xml` shows the three lines, and `./gradlew :app:lintDebug` reports no unused or format-argument warning for them.

## 2. Notifications

- [x] 2.1 In `NotificationDataSourceImpl`, every builder sets `VISIBILITY_PRIVATE` and a public version titled `notification_public_title`, with no text and no actions, from one place, so the T21 `visit` channel gets it too. The effect notification no longer formats the medication name. Verify: a Robolectric or unit test on the built `Notification` objects asserts `visibility == VISIBILITY_PRIVATE`, a public version with that title, and no medication name in the extras. Then `./gradlew :app:testDebugUnitTest`. Done: `NotificationLockScreenTest` (Robolectric, SDK 35), 3 tests, 0 failures. Negative control on the old code: 3 of 3 fail with "no public version". The visibility assertion alone cannot fail (`VISIBILITY_PRIVATE` is 0, the default); the public version and title assertions carry the check. The medication name no longer reaches the datasource: `showEffectQuestion(intakeId)` from the use case down.
- [x] 2.2 On `emulator-5570`, post each notification. Verify: `adb shell dumpsys notification --noredact | grep -E 'vis=|android.title'` shows `vis=PRIVATE` and the new titles. Done for the visit notification on 5570 (`vis=PRIVATE`, public "Reminder"); "Did it help?" and the daily update are covered by `NotificationLockScreenTest`, not posted on the device. QA 4.2 shoots all three.

## 3. Gate

- [x] 3.1 Verify: `set -o pipefail; ./gradlew assembleDebug test ktlintCheck detekt lint` exits 0, and `openspec validate fix-notification-privacy --strict` exits 0. Done: gate exit 0 (`migrely-work/walks/t21/gate.exit`), 1186 JVM tests, 0 failures; both `openspec validate --strict` exit 0.

## 4. QA (`qa-engineer`)

- [x] 4.1 Update `qa/device_scenarios.sh`. Its asserts on "A migraine is logged today." and on `^Did Ibuprofen help\?` move to the new texts, and a negative assert checks that the medication name is absent. Verify: the `reminder_effect` and daily-answer scenarios pass on `Migrely_QA_API_36`. Done: `reminder_daily` 1/1 ("Not marked. Open Migrely to see today.") and `reminder_effect` 1/1 ("Did it help?", no "Ibuprofen") at d29ffb82, every notification private with a public version; control on the 1.1.1 tree: 2/2 fail as required (`docs/qa/go-no-go-doctor-visit.md`, "Notification privacy").
- [x] 4.2 Lock-screen screenshots of all three notifications, under "Show all content" and under "Hide sensitive content", on a secure lock. Check that no medication, "migraine" or channel name shows. Negative control: the 1.1.1 build under "Show all" shows the medication name. Verify: the screenshots and the result are in the change's Go/No-Go section in `docs/qa/`. Done: A25 on d29ffb82, API 36 and API 26: the visit, "Did it help?" and the daily update show no medication, "migraine" or channel name, and "Reminder" under Hide sensitive content. The bundled "Did it help?" pair shows only "Migrely" (no leak; void as a check of that line). Control: the 1.1.1 build shows "Did Ibuprofen help?" under Show all (`docs/qa/go-no-go-doctor-visit.md`). #178 stays open for the lead to close.
