# Tasks

## 1. Import

- [x] 1.1 `BackupDao.add` removes a migraine being deleted that the backup holds, with what hangs on it, and an intake
  hidden with one that matches the backup's, then adds the backup's copies by the usual rules.
- [x] 1.2 Instrumented tests: the backup's copy takes the hidden migraine's place; an intake hidden with another
  migraine; "Replace" leaves no hidden row; export, delete without undo, "Add", and the migraine is back.
- [x] 1.3 Repository and use case tests for "Add" bringing a deleted migraine back.

## 2. Purge at start

- [x] 2.1 `AttackDataSource.purgeExpiredDeletions`, with unit tests.
- [x] 2.2 `PurgeExpiredDeletionsUseCase` and `PurgeExpiredDeletionsRepository` in the attack feature; a failure is quiet.
- [x] 2.3 `:app`: DI, `StartEntryPoint`, and `MigrelyApplication` runs it with the other start-up clean-ups.

## 3. Docs

- [x] 3.1 BRD D118, and `docs/ARCHITECTURE.md` section 6a.

## 4. Verify

- [x] 4.1 Gate (`./gradlew assembleDebug test ktlintCheck detekt`), the instrumented sources compile, and
  `openspec validate fix-import-deleted --strict`.
- [x] 4.2 Instrumented tests on emulator-5570: datasource 100, designsystem 37, app 1, none failing.
- [x] 4.3 On emulator-5570 (`migrely-work/walks/qa5658/`):
  - #58: deleted then "Add" brings the migraine back ("Added 1 migraine. 15 were already here."), both after leaving
    for Home and after a force-stop inside the undo window; a deletion older than its undo time is purged at start;
  - #56: "Nothing new" stays 7.8 s by default and 20 s with both accessibility timeouts at 20 s; Report's message
    follows the same rule;
  - D110: with the simulated store unavailable, no paywall shows itself.
