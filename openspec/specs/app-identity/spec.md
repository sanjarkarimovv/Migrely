# app-identity Specification

## Purpose
Defines Migrely's launcher icon and its store icon (BRD D103).

## Requirements

### Requirement: The launcher icon
The launcher icon SHALL be a white calendar page with a navy band and a navy crescent moon on flat Migrely violet, with
no text and no picture of a head. It SHALL be an adaptive icon whose foreground stays within the 66 dp safe zone, with a
monochrome layer for themed icons. The launch screen SHALL show the same glyph in a violet circle, and a 512×512 PNG of
the same art SHALL exist for the Play listing (BRD D116).

#### Scenario: Themed icons
- **WHEN** the phone uses themed icons
- **THEN** Migrely's icon shows the calendar with the moon cut out of it, in the theme's colour

#### Scenario: Any mask
- **WHEN** the launcher masks icons as circles, squircles, rounded squares or teardrops
- **THEN** the calendar and its rings are never cut

#### Scenario: Launch screen
- **WHEN** Migrely opens on Android 12 or later
- **THEN** the dark launch screen shows the white calendar in a violet circle
