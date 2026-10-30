## ADDED Requirements

### Requirement: Files through the system picker are checked by their bytes
Files the app writes or reads through the system file picker SHALL be checked on the QA emulator without test code in
the app. Exported files SHALL be pulled from the device and checked byte by byte against the format the specs
describe, and files to import SHALL be prepared by the QA tools and pushed to the device's Download folder, including
broken files and files from a newer version (BRD D200).

#### Scenario: An exported CSV
- **WHEN** a CSV export is saved to Download and pulled from the device
- **THEN** the check confirms the byte order mark, CRLF line ends, the header, the separator for the app's language, and
  a `'` before a note that starts with `=`, `+`, `-` or `@`

#### Scenario: A prepared broken file
- **WHEN** QA pushes a file that is not a Migrely backup and imports it
- **THEN** the check confirms "This file isn't a Migrely backup. Nothing was changed." and that every entry is still
  there
