## ADDED Requirements

### Requirement: Shift+Tab from the day returns to the chosen day
On History, Shift+Tab from the first control of the chosen day's section SHALL land on the chosen day in the month, with
its two-tone ring visible and the whole cell on screen. That control is the first migraine row, else Period, else
Pain-free. Tab from the day SHALL return to it. The same order SHALL hold in landscape's two panes (BRD EC-UX-46,
D181).

#### Scenario: A day with migraines
- **WHEN** a day with migraines is chosen, focus is on its first migraine row, and the user presses Shift+Tab
- **THEN** focus is on the chosen day in the month, its ring is visible, and the cell lies inside the screen
- **AND** Tab from there returns to the first migraine row

#### Scenario: A day without migraines
- **WHEN** a day without migraines is chosen, Period shows, focus is on Period, and the user presses Shift+Tab
- **THEN** focus is on the chosen day with its ring visible, never on a stop that shows nothing

#### Scenario: Period hidden
- **WHEN** period tracking is off, a day without migraines is chosen, Pain-free is enabled and focused, and the user
  presses Shift+Tab
- **THEN** focus is on the chosen day with its ring visible

#### Scenario: Largest font size and landscape
- **WHEN** the font size is 200%, in portrait or landscape, in the dark or the light theme, and the user presses
  Shift+Tab from the first control of the day's section
- **THEN** the chosen day is scrolled fully into view and shows its ring
