# Proposal

## Why

QA found two ways the attack note's 1,000-character limit (EC-ATK-13) loses or breaks text in 1.0.0. Both come from
the note field keeping `text.take(1000)` after every edit, which cuts from the end of the note:

- **#80 (S3):** in a full note, a letter typed in the middle is inserted, and the note's last character is deleted
  without a word.
- **#81 (S3):** when typing or pasting crosses the limit, an emoji is split in half. A lone surrogate is stored; the
  backup and CSV write it as "?", and accessibility tools fail to read the screen.

## What Changes

- **Design system:** a new `limitedEdit`, for text and for a field value with its cursor.
  - An edit that fits, or that shortens the text, is kept as typed.
  - Otherwise only the inserted text that fits is kept, cut after a whole character (`java.text.BreakIterator`, so
    emoji sequences stay whole).
  - The field version takes the edit to start no later than the old cursor and to end at the new cursor. That
    protects two cases: a paste starting like the text after the cursor is cut at its own end, and a keyboard
    rewriting a word before the cursor never costs the end of the note.
  - When nothing fits, the text and the cursor stay as they were.
- **Attack details:** the note field keeps a field value with its cursor and limits each edit with `limitedEdit`. A
  reload or a merge of the note still replaces the text, and puts the cursor at the end.
- **BRD** EC-ATK-13, and the attack-details "Note with a limit" requirement with three new scenarios.
- **Unit tests** for `limitedEdit`.

**Unchanged:**
- the limit, still 1,000 UTF-16 units, so the domain, datasource, merge (which already avoids splitting) and import
  checks are untouched;
- the counter;
- saving.

**Out of scope:**
- **#82**, a counter and limit in visible characters. It changes the limit across every layer and import, so it goes
  to develop.
- The same `take` pattern in tag names and in medication name and dose fields. These are short fields, and the
  pattern there goes to develop.
- Repairing lone surrogates already stored by 1.0.0.

## Capabilities

### Modified Capabilities

- `attack-details`: an edit that does not fit inserts only the whole characters that fit.

## Impact

- `:designsystem`: new `input/LimitedText.kt` and `LimitedTextTest`.
- `:features:attack:ui`: `AttackImpactNoteSections.kt` (`NoteSection`).
- `docs/BRD.md`: EC-ATK-13.
