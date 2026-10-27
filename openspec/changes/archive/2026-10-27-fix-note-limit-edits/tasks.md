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
  - the cursor kept on refusal and placed after a shortened paste;
  - from the walk and the review: a paste starting like the text after the cursor, and a keyboard rewriting a word
    before the cursor.
- [x] 1.3 Design system: `limitedEdit` for text and for `TextFieldValue`
- [x] 1.4 Attack details: the note field limits each edit with it and keeps its cursor

## 2. Verify

- [x] 2.1 Gate: `assembleDebug test ktlintCheck detekt`
- [x] 2.2 `openspec validate fix-note-limit-edits --strict`
- [x] 2.3 On emulator-5570: QA #80's and #81's steps. The note is unchanged by a letter typed in the middle of a full
  note and by an emoji at 999. A paste across the limit inserts only whole characters. The saved note has no lone
  surrogate (`uiautomator dump` succeeds).

## 3. Review

- [x] 3.1 Independent review (`superpowers:code-reviewer`) of 6c4f6ba, b67b5c3 and 47f4725

## Verification record

- **Tests:**
  - `LimitedTextTest` was written first and failed to compile ("Unresolved reference 'limitedEdit'"). It now passes
    12/12.
  - The emoji-sequence test needs `java.text.BreakIterator` with grapheme clusters (JDK 20 or later) and is skipped
    on an older JDK. Android's ICU keeps sequences whole.
- **Gate:** `assembleDebug test ktlintCheck detekt` passes.
- **First walk (emulator-5570, debug):**
  - #80: in a full note, "Q" typed in the middle was refused, the note still ends in "WXYZ", and the cursor stayed.
  - #81: a typed 😀 at 999 was refused, and the dump succeeds.
  - It also found the bug fixed in b67b5c3: a paste "abc😀😀" into a run of 'a's at 994 dropped an 'a' and put the
    cursor between the emoji's halves, so one DEL stored U+DE00 and `uiautomator dump` failed.
- **Second walk, after b67b5c3:** the same paste at 994 gives 999/1000. All 994 'a's stay, with "abc" plus one whole
  😀. One DEL removes the whole emoji (997). The dump succeeds, and there are 0 FATAL EXCEPTION.
- **Review findings:**
  - **The paste scenario's arithmetic:** it now starts at 996 and gives 999.
  - **A keyboard rewriting a word before the cursor in a full note cost its last letter:** the kept end is now
    anchored at the new cursor, with a test.
  - **The emoji-sequence test depended on the JDK:** it now assumes JDK 20 or later.
- **Not checked:**
  - a keyboard proofread on a device, which is covered by the unit test;
  - the split of very new ZWJ sequences on API 26–28 by older ICU. That leaves no lone surrogate, only a sequence
    shown as its parts.
