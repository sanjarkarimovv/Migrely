# Tasks

The code and tests are in PR #136 at 17b9275e. This change only records them.

- [x] 1.1 BRD: D152, EC-ATK-12, EC-ATK-13, EC-MED-13. Verify: `grep -c "^| D152 " docs/BRD.md` prints 1 and the live list on `origin/develop` has no D152.
- [x] 1.2 Deltas for `custom-tags`, `medication-list` and `attack-details`. Verify: `openspec validate fix-limited-text-fields --strict`.
- [x] 1.3 Check every scenario against a test that exists at 17b9275e (below).
- [x] 1.4 Archive. No new capability, so no Purpose needed writing. Verify: `openspec validate --specs --strict`.

## Scenario to test

Tests are read, not rerun, for this change. No product code changed after 17b9275e, and PR #136 holds the engineer's gate
result. PR #136 also says that the instrumented suites still owe a device run at that head, so the two
`...LimitTest` classes below are not yet proven there.

| Scenario | Test |
|---|---|
| `custom-tags` middle, emoji, paste, recreated | `CustomTagDialogLimitTest`: `givenFullNameWhenTypingInTheMiddleThenEndAndCursorStay`, `givenOneUnitLeftWhenTypingAnEmojiThenNoHalfIsKept`, `givenAPasteAcrossTheCapThenItIsCutAfterAWholeCharacter`, `whenTheScreenIsRecreatedThenTextAndCursorAreRestored` |
| `medication-list` middle of a name or dose | `MedicationFieldsLimitTest`: `editNameKeepsEndAndCursorWhenFull`, `editDoseKeepsEndAndCursorWhenFull`, `logNameKeepsEndAndCursorWhenFull` |
| `medication-list` emoji | `editNameNeverKeepsHalfAnEmoji`, `editDoseNeverKeepsHalfAnEmoji`, `logNameNeverKeepsHalfAnEmoji` |
| `medication-list` paste | `editNameCutsAPasteAfterAWholeCharacter`, `editDoseCutsAPasteAfterAWholeCharacter`, `logNameCutsAPasteAfterAWholeCharacter` |
| `medication-list` recreated | `recreatingTheEditFormRestoresTextAndCursorOfBothFields`, `recreatingTheLogFormRestoresTextAndCursor` |
| A long stored name | `EditMedicationFormTest`: `givenNoFormYetWhenTheFirstLoadArrivesThenStoredTextOpensWithTheCursorAtTheStart`, `givenANameWiderThanTheFieldWhenOpenedThenItsStartIsOnScreen` |
| Typing and tapping a kind in one frame | `givenOneFrameWhenTypingInTheNameAndTappingAKindThenShownNameIsSavedName`, the same for the dose |
| Deleting and retyping in one frame | `givenOneFrameWhenDeletingAndRetypingTheNameThenShownNameIsSavedName`, the same for the dose |
| A cursor move or a refused edit | `whenMovingTheCursorInTheNameThenTheFormIsLeftAlone`, the same for the dose, `givenAFullDoseWhenAnEditIsRefusedThenTheFormIsLeftAlone`, `movingTheCursorOrARefusedEditLeavesTheFormAlone`. These prove the form is left alone. That this keeps the problem on screen is read from `EditMedicationScreen.kt:106-108`, where only `onFormChange` clears it |
| `attack-details` opening, recreated | `NoteSectionTest`: `whenOpenedThenTheCursorIsAtTheEnd`, `whenRecreatedWithTheSameTextThenTheCursorIsRestored` |
| `attack-details` text changes from outside | `whenAMergeDropsTheDraftThenTheMergedNoteShowsWithTheCursorAtTheEnd`, `whenRecreatedWithChangedTextThenItShowsWithTheCursorAtTheEnd` |
