# brand-typography Specification

## Purpose
TBD - created by archiving change refine-messages-and-type. Update Purpose after archive.

## Requirements

### Requirement: Bundled brand typefaces
The app SHALL ship its typefaces inside the APK and SHALL NOT download fonts. Titles, the day's sky title and the
calming line in attack mode SHALL use Fraunces with soft serifs (Soft 100, Wonky 0). All other text, buttons, labels
and numbers SHALL use Figtree (BRD D20, 18.5, NFR offline).

#### Scenario: First launch offline
- **WHEN** the app is opened for the first time with no network connection
- **THEN** the sky card title is drawn in Fraunces and the "Migraine started" button in Figtree

#### Scenario: A script the fonts do not cover
- **WHEN** a text contains characters outside the fonts' Latin and Latin Extended coverage
- **THEN** those characters are drawn with the system font and nothing is shown as an empty box

### Requirement: Comfortable reading on a dark screen
Body text SHALL use a line height of at least 1.4 times its font size, and no text style SHALL use a weight below 400,
to reduce pattern glare and halation on the dark theme (BRD EC-UX-17, EC-UX-01).

#### Scenario: Type scale check
- **WHEN** the theme's typography is inspected
- **THEN** every body style's line height is at least 1.4 times its size and every style's weight is 400 or more

### Requirement: Tabular figures for times that tick
Times and durations that update while visible SHALL use tabular figures, so the text does not shift sideways as digits
change (BRD EC-UX-17).

#### Scenario: Elapsed time ticks
- **WHEN** the elapsed time in attack mode changes from "11 min" to "12 min"
- **THEN** the text that follows the number does not move sideways

### Requirement: Font licences ship with the app
The SIL Open Font License text of every bundled typeface SHALL be included in the app's assets (BRD D20).

#### Scenario: Licence files present
- **WHEN** the release APK is inspected
- **THEN** it contains the OFL texts for Fraunces and Figtree under `assets/licenses/fonts/`
