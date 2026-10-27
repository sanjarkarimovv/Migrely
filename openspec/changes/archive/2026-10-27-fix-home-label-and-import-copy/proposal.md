# Proposal

## Why

QA's build 4 sweeps filed two small wording defects (S4, launch blocker: no):

- **#84:** the screen-reader label of home's "Logged today" row reads "Migraine from 1:54 PM to 1:55 PM, peak Not
  recorded. Opens its details." The capital N in mid-sentence comes from reusing the visible "Not recorded" text.
- **#87:** adding a backup that holds no migraine at all says "Added the other entries. Every migraine in it was
  already here.", which speaks of migraines the file does not have.

## What Changes

- **Home:** the spoken peak uses its own lowercase "not recorded". The visible "Not recorded" stays.
- **Your data:** an Add that brings only other entries says "Added the new entries." when the file had no migraine.
  The old sentence stays for a file whose migraines were all already here.
- **Spec:** data-backup "Replace or add" gains the scenario "A file with no migraines".

**Unchanged:** every other text, and the data the import writes.

## Capabilities

### Modified Capabilities

- `data-backup`: the result of an Add of a file without migraines.

## Impact

- `:features:home:ui`: `strings.xml`, `TodayContents.kt`.
- `:features:backup:ui`: `strings.xml`, `YourDataScreen.kt`.
