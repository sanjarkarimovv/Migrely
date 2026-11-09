# Tasks

Base: `hotfix/1.1.1`, alongside `fix-offline-play-sheet`. `Refs #171`.

- [x] 1.1 BRD D167, EC-ATK-23, §13 row 36. Verify: `grep -c "^| D167 " docs/BRD.md` prints 1.
- [x] 2.1 Compose tests first (Robolectric): the card out of view after the event scrolls so its top is at the viewport top; in view, no scroll; a restore,
  no scroll; reduced motion, a jump; a drag during the scroll does not stop it. Verify: `./gradlew :features:attack:ui:testDebugUnitTest --tests
  '*SafetyCard*'` fails first, then passes. Done: bae78209, in the gate.
- [x] 2.2 The scroll (design D1) and the live region in view (design D2 (a)). Verify: 2.1 passes, and a semantics test shows that the live region is set
  only once the card is in view. Done: bae78209.
- [x] 2.3 A walk on `emulator-5570` with TalkBack: S1 and S2 at 100% and 200%. Restore font scale and TalkBack afterwards. Verify: the speech lines are in
  the packet. If the title is not spoken, use design D2 (b) and walk again. Done at eeb69f8a (#174 fix), `migrely-work/walks/hotfix-111/shots/debug-eeb69f8a-*` and its logcats: failed and saved, 100% and 200%, PASS.
  TalkBack: saved at 200% speaks the title and the sentence; failed at 100% speaks the sentence and not the title, as #166 expects. Negative control
  without P8: `nc3-no-p8-S4-*` (card above the view, title not spoken).
- [x] 3.1 The gate, with the offline change (`fix-offline-play-sheet` 6.1). Verify: exit 0. Done: `gate.exit` 0 at a3f25cdb, `gate2.exit` 0 at 4233719a.
- [ ] 3.2 `qa-engineer` runs S1 to S7, S4 on 1.1.0 or a local build without the change. Verify: they are in the 1.1.1 Go/No-Go.
