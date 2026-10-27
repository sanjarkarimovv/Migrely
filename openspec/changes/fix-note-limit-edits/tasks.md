# Tasks

## 1. Fix

- [x] 1.1 BRD EC-ATK-13
- [x] 1.2 Tests first: `LimitedTextTest` covers the following. It failed to compile before the change.
  - an edit that fits;
  - a refused letter in a full text;
  - a refused emoji;
  - a shortened paste;
  - a whole ZWJ sequence or none of it;
  - a replaced selection;
  - a deletion over the limit;
  - the cursor kept on refusal and placed after a shortened paste.
- [x] 1.3 Design system: `limitedEdit` for text and for `TextFieldValue`
- [x] 1.4 Attack details: the note field limits each edit with it and keeps its cursor

## 2. Verify

- [ ] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [ ] 2.2 `openspec validate fix-note-limit-edits --strict`
- [ ] 2.3 On emulator-5570: QA #80's and #81's steps. The note is unchanged by a letter typed in the middle of a full
  note and by an emoji at 999. A paste across the limit inserts only whole characters. The saved note has no lone
  surrogate (`uiautomator dump` succeeds).
