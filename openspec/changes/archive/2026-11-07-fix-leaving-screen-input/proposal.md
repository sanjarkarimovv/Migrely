# Proposal

## Why

A screen stays composed and clickable through its exit fade, and the library's default fade lasted 700 ms. Two touches
inside that time reached the wrong place:

- **#138 (S1):** the editor could show medication A while Save wrote to medication B, with no Undo. It could also remove
  an overuse warning.
- **#139 (S2):** two details screens stacked, so Delete landed on another migraine.
- **#140 (S3):** a second tap meant for the list could flip Kind to Preventive, taking the medication out of the overuse
  count.
- **#142 (S3):** a touch on a leaving screen still ran Remove, Delete migraine and Delete all data.
- **#148 (S3):** after Back, taps on the screen below did nothing for up to 700 ms. Google rejected 1.0.1 for unresponsive
  controls.

The code is in PR #143 and PR #147. This change records the rules, so they cannot be broken with no spec to say so. The
rules are BRD D154 to D157.

## What Changes

- **Only the current screen navigates.** A navigation event navigates only while its screen is the current back-stack
  entry, compared by entry id.
- **A touch on a screen that is not current does nothing.** The pointer input of this window on that entry is consumed,
  read at each event, so a resting finger cancels. Screen readers and switch access are deliberately not touched.
- **The medication editor belongs to one id.** A read of another medication is ignored, and a loaded form wins over
  NotFound.
- **The restart after Delete all data does not depend on any screen being current.**
- **Every navigation is a 200 ms linear fade,** set once.
- **On the medication form, Kind and Category ignore touches for 700 ms** after the form appears, on Add and Edit. Name,
  Dose, Save and Back are free.

## Out of scope

- Dialogs and popups of a leaving screen, which are separate windows (#149 stays open).
- A blank editor after a rotation during the first read (#150 stays open).
- Late async results sent by a leaving entry while another entry of the same destination is current. See design D1.
- Taps being refused during a pop for the 200 ms of the fade. They are shortened here, not removed.
- New strings, permissions, schema or layout.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-shell`: which screen may navigate, which screen takes touches, and the fade between screens.
- `medication-list`: the editor belongs to one medication, and Kind and Category while the form opens.
- `app-settings`: the restart after Delete all data.

## Impact

- `docs/BRD.md`: D154 to D157, EC-UX-21 to 23, EC-MED-14, EC-MED-15, EC-STO-05, §13 row 29.
- Code, already written, in `:architecture:ui`, `:app`, `:features:medication:ui`, `:features:settings:ui`. See
  `docs/ARCHITECTURE.md` section 8.
- This change adds no code. It stays unarchived until `qa-engineer` has run the R8 retest; `android-engineer` archives it.
- No health claim changes. The guard protects the overuse count, and no wording changes, so `medical-reviewer` is not
  needed.
