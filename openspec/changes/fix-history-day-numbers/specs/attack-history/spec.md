## ADDED Requirements

### Requirement: Two panes only when each pane is wide enough
In a window under 480 dp tall, History SHALL show the calendar and the list side by side only when each pane would be at
least 200 dp wide. Otherwise it SHALL use the single-column layout it uses in portrait, so a day number never needs less
than its 100% size (BRD D198 (4), EC-HIS-06).

#### Scenario: A phone in landscape
- **WHEN** History opens in a 640 × 360 dp or 800 × 360 dp window
- **THEN** the calendar and the list sit side by side, as before

#### Scenario: Split screen in portrait
- **WHEN** History opens in a 360 dp wide window under 480 dp tall, as in split screen in portrait, at 100% font
- **THEN** History shows one column, and every day number lays out on 1 line

#### Scenario: The check can fail
- **WHEN** the split-screen test runs against `develop`'s History
- **THEN** it shows two panes of about 152 dp with wrapped day numbers, and the test fails

#### Scenario: A pop-up window or a larger display size
- **WHEN** History runs in a small pop-up window, or with a larger display size that narrows the window
- **THEN** the same 200 dp rule decides the layout

#### Scenario: Switching layout keeps the place
- **WHEN** the window changes so History switches between one column and two panes
- **THEN** the chosen day and the shown month stay, as they do on rotation today, and nothing is written

### Requirement: A list row puts the peak under the date when the date would break
In History's month list and the chosen day's list, the peak SHALL sit beside the date range only while the room left
beside it holds the date range's widest unbreakable piece; else the date takes the full width, wrapping only between
words, and the peak goes on its own line under it, 4 dp lower, same style. Padding, 56 dp minimum height, one tap target
and the spoken label SHALL not change (BRD D181 (5), EC-HIS-07).

#### Scenario: Landscape at 200%
- **WHEN** a row with "Not recorded" or "7/10" shows at 200% font in a 640 × 360 dp landscape window, in English and German
- **THEN** every line break of the date falls at a space, "12:27 PM", "Mon," and "Nov" stay whole, and the peak's top is
  below the date's bottom

#### Scenario: The longest peak
- **WHEN** the app is German and the peak reads "Nicht erfasst"
- **THEN** the time is never cut to "12:2", and the peak sits under the date

#### Scenario: Room enough (negative control)
- **WHEN** the row shows at 100% at 360 dp, or at 200% in portrait at 360 dp or 411 dp
- **THEN** the peak stays beside the date, its top above the date's bottom, as before

#### Scenario: The check can fail
- **WHEN** the row test runs against `develop`'s row
- **THEN** English breaks inside "Mon," and German cuts the time, and the test fails

#### Scenario: An attack in progress
- **WHEN** the row's date range ends with "In progress"
- **THEN** the same rule applies, and nothing is cut

#### Scenario: TalkBack and keyboard
- **WHEN** TalkBack or a keyboard reaches a stacked row
- **THEN** it is one stop with the same label as before, such as "Monday, November 23, 12:27 PM to 12:27 PM, peak not
  recorded", role Button
