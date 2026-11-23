# Proposal

## Why

**#278 (S3, data integrity, pre-existing on `develop` 82df348f).** In T72's device walk on emulator-5570 (API 36), with the
phone's storage full, the user turned off "Track periods" or the doctor report's "Mention Migrely in the report". "Couldn't
save that. Try again." showed, which is right. But the switch stayed off while the file still said on, and after a restart
it was on again (credit switch 2 of 2, Track periods 1 of 1). A control that shows a value that was not stored breaks D178
and the doctor report's EC-REP-12, which is T72's (change 52).

**Cause.** `PreferencesDataSourceImpl` writes with `commit()`. Android's `SharedPreferencesImpl` puts a change into memory
before it writes the file, and keeps it in memory when the write fails. The view model then reads memory and shows the new
value. The next successful write of any other key (a paywall, review or safety mark, the theme) writes the whole map, so
the change the user was told was not saved gets saved later, without a word.

## What Changes

- The shared write helper keeps the values held in memory before each write. When `commit()` fails, every key that write
  changed goes back to its earlier value (or is removed again), and then `FileDataException` is thrown as today, so "Couldn't
  save that. Try again." stays (D197).
- This covers every setting and mark written through the helper, including the entitlement file. No string, permission,
  migration or UI code changes. The view models already read again after a failed save.

## Impact

- Code: `datasource/implementation` (`PreferencesDataSourceImpl`), with JVM tests on a fake that behaves as Android does,
  and one instrumented test on real preferences in a read-only folder.
- BRD: D197, EC-STO-13, §13 row 60. T72 (`add-word-of-mouth-loops`) picks this up by merging `develop`, and its EC-REP-12
  then holds on a device.
