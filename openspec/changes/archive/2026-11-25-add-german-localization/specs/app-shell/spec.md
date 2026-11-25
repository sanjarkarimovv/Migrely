## RENAMED Requirements

- FROM: `### Requirement: Dates and times in English`
- TO: `### Requirement: Dates and times in the app's language`

## MODIFIED Requirements

### Requirement: Dates and times in the app's language
Every name a date or time shows, months, weekdays and AM or PM, SHALL be in the app's language. In German, dates SHALL be
formatted in the chosen German locale; in English, in the phone's locale when its language is English, otherwise in
English with the phone's country, or plain English with none. The first day of the week and the 12- or 24-hour setting
SHALL come from the phone (BRD EC-L10N-03, EC-L10N-08, EC-TIM-05, D117, D195).

#### Scenario: A phone in Russian
- **WHEN** the phone's locale is Russian (Russia) with the 24-hour setting
- **THEN** home's date names its weekday and month in English, such as "Sunday" and "October", History's weeks start on
  Monday, and times read like "14:30"

#### Scenario: A phone in English (United States)
- **WHEN** the phone's locale is English (United States) with the 12-hour setting
- **THEN** the weeks start on Sunday and times read like "2:30 PM"

#### Scenario: A phone in German (Germany)
- **WHEN** the phone's locale is German (Germany) with the 24-hour setting
- **THEN** home's date reads like "Sonntag, 22. November", History's weeks start on Monday, times read like "14:30" and a
  decimal reads like "2,5"

#### Scenario: German with the 12-hour setting
- **WHEN** the phone's locale is German (Germany) and the system is set to the 12-hour clock
- **THEN** times follow the 12-hour setting, and the month and weekday names stay German

#### Scenario: A phone in German (Austria)
- **WHEN** the phone's locale is German (Austria)
- **THEN** month names are the system's own Austrian ones, such as "Jänner", and the app does not replace them

#### Scenario: The language changes while the app runs
- **WHEN** the phone's language is changed from English to Russian while Migrely is open
- **THEN** on returning to Migrely its dates are still English, and its weeks start on Monday

#### Scenario: Changing to German while the app runs
- **WHEN** the phone's language is changed from English to German while Migrely is open
- **THEN** on returning to Migrely its words and dates are German

#### Scenario: A first day chosen in the phone's settings
- **WHEN** a phone in Russian or German has Sunday chosen as the first day of the week in its regional preferences
- **THEN** Migrely's weeks start on Sunday

#### Scenario: The CSV on a phone in Russian
- **WHEN** a phone in Russian exports the CSV
- **THEN** its columns are still separated by semicolons, by the phone's own language (D72)

## ADDED Requirements

### Requirement: A full-screen picker's title moves under its buttons when a word would not fit
A full-screen date or date-range picker SHALL show Cancel, its title and Done in one row while the title's longest word
fits between the buttons. Otherwise the title SHALL take its own line under them, whole and a heading, and TalkBack
SHALL go Cancel, Done, then the title. Type SHALL NOT shrink (BRD EC-L10N-28, D127, D181, D195 (18)).

#### Scenario: A German title at the largest font
- **WHEN** the app is German at 200% font on a 360 dp phone and the "Nächster Termin" or "Zeitraum wählen" picker opens
- **THEN** the title shows on its own line under "Abbrechen" and "Fertig", and every line of it ends at a space

#### Scenario: A title that fits
- **WHEN** the title's longest word fits between Cancel and Done, as in English at 100% font
- **THEN** Cancel, the title and Done share one row, as before

#### Scenario: TalkBack on a stacked title
- **WHEN** TalkBack is on and the title sits under the buttons
- **THEN** TalkBack goes Cancel, Done, then the title, which it names as a heading

### Requirement: A dialog in a short window keeps its title and answers in reach
In a window under 480 dp tall, the pain, effect and custom tag dialogs SHALL leave the platform's narrow width, up to
560 dp with 24 dp margins. Each SHALL hold its title and content in one scrolling area, the title a heading that names
the pane, so every answer and the time row can be reached at 200% font, in English and German. Taller windows SHALL keep
the platform width (BRD EC-L10N-29, D181, D193, D195 (18), #276).

#### Scenario: The pain dialog in landscape
- **WHEN** the app is English or German at 200% font in a 640 x 360 dp landscape window and the pain dialog opens
- **THEN** the dialog is wide, and the score line ("5/10 · Mäßig" in German), the time row and every answer can be
  scrolled into view and reached

#### Scenario: The effect dialog with a long medication name
- **WHEN** at 200% font in a 640 x 360 dp landscape window the effect question names a long typed medication
- **THEN** the title scrolls away with the content, and every answer can be scrolled into view and chosen, in English
  and German

#### Scenario: The custom tag dialog at rest in landscape
- **WHEN** the German app at 200% font in a 640 x 360 dp landscape window opens the custom tag dialog and no keyboard is
  up
- **THEN** the dialog is wide and "Abbrechen" and "Hinzufügen" can share one row

#### Scenario: A phone in portrait
- **WHEN** the window is 480 dp tall or taller
- **THEN** the three dialogs keep the platform width, as before

#### Scenario: TalkBack opens a dialog
- **WHEN** TalkBack is on and the pain, effect or custom tag dialog opens, in English or German
- **THEN** the dialog is announced once, by its title, and the title is a heading

### Requirement: A scrolling dialog shows that more lies beyond
While a dialog's scrolling area has more below, its bottom edge SHALL show a 24 dp fade in the dialog's colour and a 1 dp
divider; while it has more above, its top edge SHALL show both. They SHALL ease over 150 ms, appear at once with reduced
motion, draw over the content only and add nothing for TalkBack. Content that fits SHALL show neither (BRD EC-L10N-29,
D195 (18)).

#### Scenario: At the start of a long dialog
- **WHEN** a dialog's area opens scrolled to the top and more content lies below
- **THEN** the bottom edge shows the fade and the divider, and the top edge shows neither

#### Scenario: At the end
- **WHEN** the person scrolls the area to its end
- **THEN** the top edge shows the fade and the divider, and the bottom edge shows neither

#### Scenario: Content that fits
- **WHEN** the whole dialog fits, as at 100% font in portrait
- **THEN** no fade and no divider show, as before

#### Scenario: Reduced motion
- **WHEN** Remove animations is on and the area starts or stops being able to scroll one way
- **THEN** that edge's fade and divider appear or go at once, with no easing

#### Scenario: TalkBack over the cues
- **WHEN** TalkBack moves through a dialog whose edges show the cues
- **THEN** it makes no stop and says nothing for them

### Requirement: A scrolling dialog is announced once, by its title
A dialog whose title scrolls with its content SHALL name its one pane by that title, in place of Material's "Dialog",
and show the title as a heading, set from one title, so TalkBack announces it once, by its title. Production code SHALL
build such a dialog only through the design system's scrolling alert dialog, and a check SHALL fail otherwise (BRD
EC-L10N-29, D181, D195 (19)).

#### Scenario: The pain dialog opens with TalkBack
- **WHEN** TalkBack is on and the pain dialog opens, at any font size and window
- **THEN** TalkBack announces "How strong was it?" in English or "Wie stark war es?" in German, once, and never
  "Dialog" or "Dialogfeld"

#### Scenario: The effect and custom tag dialogs
- **WHEN** TalkBack is on and the effect dialog or the custom tag dialog opens, in English or German
- **THEN** each is announced once, by its own title, such as "Your own symptom" or "Eigenes Symptom"

#### Scenario: A new dialog built around the scrolling area
- **WHEN** production code outside the design system's dialog folder uses the scrolling dialog area directly
- **THEN** the gate's architecture check fails, so the new dialog uses the scrolling alert dialog and cannot be
  announced as "Dialog"
