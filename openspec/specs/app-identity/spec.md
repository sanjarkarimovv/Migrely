# app-identity Specification

## Purpose
Defines Migrely's launcher icon and its store icon (BRD D103).

## Requirements

### Requirement: The launcher icon
The launcher icon SHALL be a night sky with a crescent moon and two small stars, in the home screen's colours, with no
text and no picture of a head. It SHALL be an adaptive icon whose foreground stays within the 66 dp safe zone, with a
monochrome layer for themed icons, and a 512×512 PNG of the same art SHALL exist for the Play listing (BRD D103).

#### Scenario: Themed icons
- **WHEN** the phone uses themed icons
- **THEN** Migrely's icon shows the moon and stars in the theme's colour

#### Scenario: Any mask
- **WHEN** the launcher masks icons as circles or squircles
- **THEN** the moon and stars are never cut
