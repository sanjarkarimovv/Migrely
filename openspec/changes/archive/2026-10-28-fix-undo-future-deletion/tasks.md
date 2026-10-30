# Tasks

## 1. Test first

- [x] 1.1 `DeletionOfferTest`:
  - a deletion just made, or 30 s old, is offered;
  - one more than 30 s old is not;
  - one up to 2 s ahead of now, as after an automatic correction, is;
  - one further ahead is not.
  The future cases failed with the old rule.
- [x] 1.2 `AttackDaoDetailsTest`:
  - a deletion stamped ahead does not hide a newer real one;
  - one stamped ahead is purged for good;
  - the window's edges: live attacks and deletions inside it stay, and only those outside go.
- [x] 1.3 `AttackDataSourceDetailsTest`:
  - the recent deletion is read up to now plus the 2 s tolerance;
  - the purge keeps the undo window on both sides of now, so a correction of a few seconds never breaks an Undo already offered (review).

## 2. Fix

- [x] 2.1 `AttackDao.recentlyDeleted(since, until)` and `purgeDeletedOutside(cutoff, ahead)`, used by `AttackDataSourceImpl`
- [x] 2.2 Shared `isFreshDeletion` in the design system; home and History use it
- [x] 2.3 BRD EC-ATK-21

## 3. Verify

- [x] 3.1 Gate: `assembleDebug test ktlintCheck detekt lint`
- [x] 3.2 `:datasource:implementation:connectedDebugAndroidTest` on emulator-5570
- [x] 3.3 `openspec validate fix-undo-future-deletion --strict`
- [x] 3.4 On emulator-5570:
  - with the clock 3 days ahead, delete a migraine;
  - set the clock back and cold start twice: no Undo on home, and none on History;
  - a new deletion then offers Undo, and Undo restores that migraine.

## 4. Review

- [x] 4.1 Independent review (`superpowers:code-reviewer`) of 3b442503. Its Important findings (History, the datasource) are fixed here.
- [x] 4.2 Review of the follow-up (e8b3226e). Its Important finding (the ahead purge) and its minors are fixed.

## Verification record

- **Gate:** `assembleDebug test ktlintCheck detekt lint` passes.
- **Instrumented:** `:datasource:implementation:connectedDebugAndroidTest` on emulator-5570 (API 36) is 112/112, including the new DAO tests.
- **Emulator walk:** emulator-5570, debug build of e8b3226e. The clock was moved with `cmd alarm set-time`, with `auto_time` off.
  - **Clock 3 days ahead:** a past migraine was added and deleted. The clock was then set back to real time.
    - Two cold starts: no Undo on home.
    - Two History visits: no offer.
    - The migraine is gone from home and History, and History counts only the migraines left.
  - **A real deletion after it:** home offered Undo, and Undo brought back that migraine, not the earlier one.
  - **Normal deletes:**
    - from History's day view: the offer shows and goes; there is none after a cold start more than 30 s later;
    - delete and Undo in History: restored.
  - **Control:** a cold start 12 s after a deletion still offers Undo, so the empty result above means something.
  - **Crashes:** 0.
  - **One ANR**, while a details screen opened in a fresh process. Logcat shows ART verifying Compose classes on the main thread at 45 bytecodes/s, a 17 s frame, CPU pressure 72% in the emulator, and a host load of 17–22. That points to the overloaded host and the debug build, not this change.
- **The review fix b0edcd7b came after the walk.** It keeps the undo window on both sides of now in the purge and lets the UI offer a deletion up to 2 s ahead. The walk's three-day case is purged and not offered under both rules. The window's edges are covered by the DAO test.
- **Found, not changed:** home's Undo bar still sits over the Settings gear for its few seconds (#88).
