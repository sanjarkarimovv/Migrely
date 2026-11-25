# app-language Specification

## Purpose
Defines which language the app speaks (English or German), how one rule picks it from the phone's languages and the
system's per-app setting, and how screens, notifications, TalkBack and the translation gate keep German complete, fitting
and as true as the English, while the PDF report and CSV stay English (BRD D195, EC-L10N-01, EC-L10N-04 to EC-L10N-30).

## Requirements

### Requirement: English and German
The app SHALL show every word of its own in German when its language is German, and in English otherwise, on every
screen, dialog, message, notification and content description, except the shared message's text and subject, which
SHALL stay English. The static launcher shortcut SHALL be German wherever Android picks German. Words the person typed
SHALL stay as typed. No other language and no right-to-left layout SHALL be added (BRD EC-L10N-01, EC-L10N-22,
EC-L10N-25, EC-SHR-07, D185, D195).

#### Scenario: A phone in German
- **WHEN** the phone's language is German (Germany) and Migrely is opened
- **THEN** home, attack mode, History, Insights, Settings, the paywall and every other screen show German words

#### Scenario: The launcher shortcut with German first
- **WHEN** the phone's list is German (Germany) and the app icon is long-pressed
- **THEN** the shortcut label is German

#### Scenario: The launcher shortcut after another language
- **WHEN** the phone's list is Turkish (Turkey) then German (Germany), or Russian (Russia) then German (Germany)
- **THEN** the app is German and the static shortcut label may be English, because Android picks it from every language
  its libraries ship; this is recorded as a known limit, not a pass (seen in 8.2 on 2026-11-23: on `[tr-TR, de-DE]` the
  app showed "Montag, 23. November" and the shortcut "Migraine started")

#### Scenario: Sharing Migrely from the German app
- **WHEN** the app is German and the person taps "Share Migrely"
- **THEN** the chooser title ("Migrely teilen"), the row and any message are German, while the shared text and subject
  stay English, one message in one language, because the link opens the English Play page until the de-DE listing (T79)
  is approved

#### Scenario: The person's own words
- **WHEN** the app is in German and a migraine has a note, a trigger and a medication typed in English
- **THEN** they show exactly as typed

#### Scenario: A phone in another language
- **WHEN** the phone's language is Russian, Uzbek, French or Hebrew with no German or English in its list
- **THEN** the app is English, dates follow D117 and the layout stays left to right, as before

### Requirement: One rule picks the app's language
The app's language SHALL be the first language in the phone's language list that the app supports, English or German.
When it is German, the formatting locale SHALL be that German locale; otherwise D117 SHALL apply unchanged. Screens,
notifications, channel names and the default locale SHALL follow this one rule (BRD EC-L10N-04, D117, D195).

#### Scenario: German first
- **WHEN** the phone's list is German (Germany), German (Austria) or German (Switzerland) alone
- **THEN** the app is German, with dates in that locale

#### Scenario: German after an unsupported language
- **WHEN** the phone's list is Russian (Russia), then German (Germany)
- **THEN** the app is German, with dates in German (Germany)

#### Scenario: English before German
- **WHEN** the phone's list is Russian (Russia), English (United States), then German (Germany)
- **THEN** the app is English with dates in English (Russia), as before, and its notifications are English too

#### Scenario: English with a German region
- **WHEN** the phone's language is English (Germany)
- **THEN** the app is English with dates in English (Germany), as before

### Requirement: The system's app language on Android 13 and newer
On Android 13 and newer the app SHALL offer English and German in the system's "App language" setting, and that choice
SHALL win over the phone's language. On Android 8 to 12 the app SHALL follow the phone's language and offer no choice
inside the app (BRD EC-L10N-05, D195).

#### Scenario: German for Migrely on an English phone
- **WHEN** on Android 13 or newer the phone is in English and Deutsch is chosen for Migrely in the system settings
- **THEN** Migrely is German and other apps stay English

#### Scenario: English for Migrely on a German phone
- **WHEN** on Android 13 or newer the phone is in German and English is chosen for Migrely
- **THEN** Migrely is English, with dates in that English locale

#### Scenario: The list offered
- **WHEN** the system's "App language" page for Migrely is opened
- **THEN** it lists English and Deutsch and no other language

#### Scenario: Android 8 to 12
- **WHEN** the phone runs Android 8 to 12 in German
- **THEN** Migrely is German, and switching the phone to English switches Migrely to English

### Requirement: A language change keeps what is on screen
When the app's language changes while Migrely is open or in the background, the screen SHALL come back in the new
language and nothing SHALL be lost: a migraine in progress, unsaved typed text with its cursor, the open screen and data
(BRD EC-L10N-06, D195).

#### Scenario: During a migraine
- **WHEN** attack mode is open and the language changes from English to German
- **THEN** attack mode shows again in German with the same start time and the timer still running

#### Scenario: Unsaved typed text
- **WHEN** a note on Attack details has unsaved text and the language changes
- **THEN** the text and its cursor are still there in the new language

#### Scenario: After process death
- **WHEN** the language changes while Migrely is in the background and Android has ended its process
- **THEN** Migrely opens on the same screen in the new language with nothing lost

### Requirement: A per-app language is read fresh and applied on return
On Android 13 and newer the app SHALL read its per-app language from the system, not from a configuration kept across a
process death. A per-app language the platform did not deliver SHALL be applied when Migrely next comes to the front, by
recreating the screen once, with nothing lost and words and dates in one language. Notifications and channel names SHALL
resolve the language fresh each time (BRD EC-L10N-06, D195 (19)).

#### Scenario: A change while the process is dead
- **WHEN** Migrely is German, Android ends its process in the background, and English is chosen for Migrely in the
  system settings before it is opened again
- **THEN** the restored screen shows English words and English dates

#### Scenario: A change back that the platform does not deliver
- **WHEN** after such a restore German is chosen for Migrely again while it is in the background, and no configuration
  change reaches the app
- **THEN** when Migrely next comes to the front it recreates the screen once and shows German words and German dates, on
  the same screen, with typed text and a migraine in progress kept

#### Scenario: Words and dates agree
- **WHEN** a restored screen is rebuilt for a language the platform did not deliver
- **THEN** no screen shows a title in one language and a date in the other

#### Scenario: One recreate per change
- **WHEN** Migrely comes to the front after an undelivered change, or is finishing, or is rotating
- **THEN** the screen is recreated at most once for that language, never while finishing or changing configuration, and
  again after a later process death and restore if the language is stale once more

#### Scenario: A notification after the change
- **WHEN** the per-app language changed while Migrely was not opened and a reminder fires
- **THEN** the reminder and the channel names are in the new language

#### Scenario: A change while Migrely stays in front
- **WHEN** the per-app language changes while Migrely's screen stays started, as side by side in multi-window or by
  `adb` with Migrely in front, and the platform delivers no configuration change
- **THEN** the change applies the next time Migrely starts; this is a recorded limit, not a pass

#### Scenario: Android 8 to 12
- **WHEN** the phone runs Android 8 to 12
- **THEN** the language is read from the configuration, as before, because there is no per-app language

### Requirement: Notifications speak the app's language
Each notification, its dates included, SHALL be in the app's language when shown, and its full text SHALL be readable
when expanded. Android's notification settings SHALL show channel names and descriptions in the app's language after
each app start and language change, updating only channels that already exist (BRD EC-L10N-07, EC-L10N-19, EC-L10N-26,
D195).

#### Scenario: A daily reminder in German
- **WHEN** the app's language is German and the daily reminder fires
- **THEN** its title and text are German

#### Scenario: A notification already shown
- **WHEN** a reminder is in the shade and the language then changes
- **THEN** it stays in the old language until it is replaced or dismissed, and the next one is in the new language

#### Scenario: Channel names
- **WHEN** the language changes to German and Migrely is opened once
- **THEN** Android's notification settings list Migrely's existing channels with German names, and no new channel appears

#### Scenario: Long German text in the shade
- **WHEN** the daily reminder, "Did it help?" or the visit reminder shows in German and is expanded
- **THEN** its whole text is readable, and the collapsed line still carries the meaning

#### Scenario: The visit date on a phone in another language
- **WHEN** the app is English on a phone in Russian (Russia) and the doctor-visit reminder fires
- **THEN** its date is English with the Russian region, like the app's screens, and no longer Russian

#### Scenario: After the update to this version
- **WHEN** a German phone updates from 1.3.x with a reminder planned
- **THEN** the first opening is German, entries and settings are unchanged, and the planned reminder fires in German

### Requirement: No untranslated text passes the gate
Every string, plural and array in every module's default resources SHALL have a German value, or be marked
`translatable="false"`. Placeholders and array lengths SHALL match English. A copy of English SHALL come only from a
reviewed list. No value, read as compiled with escapes decoded, SHALL hold a soft hyphen, a zero-width character or a
plain space between a number and its unit. The gate SHALL fail otherwise (BRD EC-L10N-12, EC-L10N-13, D195).

#### Scenario: A new English-only string
- **WHEN** a change adds a string to any module's `values/strings.xml` without a German value
- **THEN** `./gradlew assembleDebug test ktlintCheck detekt lint` fails and names the string

#### Scenario: A module with no German file
- **WHEN** a module has `values/strings.xml` and no `values-de/strings.xml`
- **THEN** the gate fails, although Android Lint alone would not see it

#### Scenario: A broken placeholder
- **WHEN** a German value drops `%1$d` or turns it into `%1$s`, or a German array has another number of items
- **THEN** the gate fails

#### Scenario: A copied English value
- **WHEN** a German value is identical to its English one and is not in the reviewed list, which holds words such as
  "Migrely", "Premium", "PDF", "CSV" and "Stress"
- **THEN** the gate fails

#### Scenario: An invisible break or a split unit
- **WHEN** a German value holds a soft hyphen or a zero-width space, or "15 Min." with a plain space
- **THEN** the gate fails, and passes with a no-break space between the number and its unit

#### Scenario: An escaped invisible break
- **WHEN** a German value writes a soft hyphen as the escape `\u00AD` inside a 17-letter word
- **THEN** the gate decodes the escape and fails, as it does for the character itself

#### Scenario: A long word without a walk date
- **WHEN** after the 8.1 walk a line of the long-word list has no `seen YYYY-MM-DD` date
- **THEN** the gate fails

#### Scenario: Not to translate
- **WHEN** a string is the app's name, a licence or library name, or lives only in the debug build
- **THEN** it is marked `translatable="false"` and the gate passes without a German value

### Requirement: German fits at the largest font
German text SHALL keep the no-break rule (D181) at 200% font on a 360 dp phone, portrait and landscape: no word broken,
cut or ellipsized, no smaller type. A German word over 16 characters, counted whole across the joins Android never
breaks at, SHALL be on a reviewed list with the date it was seen in place. A German-only failure SHALL block the merge.
Umlauts, ß and „ “ SHALL use the brand fonts (BRD EC-L10N-02, EC-L10N-14, D181, D195).

#### Scenario: Buttons and chips at 200%
- **WHEN** the app is German at 200% font on a 360 dp phone, in portrait and landscape
- **THEN** every button, chip, tab and attack-mode control shows whole words, and none is cut or ellipsized

#### Scenario: Layouts that switch at a font size
- **WHEN** the app is German at 130% font on a 360 dp phone in portrait, on the "Did it help?" card, Insights bars, the
  doctor visit section and the onboarding header
- **THEN** every word is whole in the layout that size chooses

#### Scenario: A failure English shares
- **WHEN** a layout fails in German and also fails in English at the same settings
- **THEN** it is filed as an issue and does not block this change, unless it is on a safety path, where it blocks

#### Scenario: A failure only German has
- **WHEN** a layout fails in German and passes in English at the same settings
- **THEN** the change does not merge until it is fixed, by shorter copy first and never by smaller type

#### Scenario: A long compound word
- **WHEN** a German value contains a word over 16 characters, such as "Lichtempfindlichkeit", that is not on the list
- **THEN** the gate fails, and the word joins the list only after it was seen whole in its place at 200% on 360 dp

#### Scenario: A hyphenated compound
- **WHEN** a German value holds "Open-Source-Bibliotheken", 24 characters with its hyphens and no part over 16, and the
  word is not on the list
- **THEN** the gate counts it as one word and fails, because Android does not break a line at a written hyphen

#### Scenario: Umlauts written decomposed
- **WHEN** a German value writes "Migränetagebücher" with each umlaut as a base letter and a combining diaeresis
- **THEN** the gate composes the value first (NFC), counts one 17-character word and fails

#### Scenario: A non-breaking hyphen or a no-break space
- **WHEN** a German value joins letters with U+2011, as "Ende‑zu‑Ende‑Schutz", or joins "Kopfschmerz" and "Tagebuch"
  with a no-break space (U+00A0)
- **THEN** the gate counts the joined letters as one word, over 16, and fails

#### Scenario: A placeholder joined to a word
- **WHEN** a German value holds "%1$s-Sicherungsdatei"
- **THEN** the placeholder counts as letters of its own length, the gate sees one 20-character word and fails unless that
  word is on the list as written

#### Scenario: At the limit
- **WHEN** a German word has exactly 16 characters, such as "Kopfschmerztagen", or a number stands before its unit
  with a no-break space, as "12 Min."
- **THEN** the gate passes it without a list entry

#### Scenario: A number and its unit
- **WHEN** a duration, percent, pain score or price wraps at 200% in German
- **THEN** the number and its unit stay on one line

#### Scenario: German letters
- **WHEN** a title in Fraunces or a label in Figtree contains ä, ö, ü, ß or „ “
- **THEN** they are drawn in that font, with no fallback glyph and no empty box

### Requirement: German health and safety words say what the English says
Every German health or safety sentence SHALL carry the meaning of its English source and add no claim, time frame or
number. The emergency line SHALL name the emergency services without a number. Insights SHALL describe what was logged,
never a cause. `medical-reviewer` SHALL pass every such key before release (BRD EC-L10N-15, D95, D171, D174, D195).

#### Scenario: Warning signs in German
- **WHEN** the app is German and the Warning signs page is opened
- **THEN** it lists the same emergency and urgent signs as English, in the same order, and tells the person to call the
  emergency services, with no phone number

#### Scenario: The overuse warning in German
- **WHEN** the medication overuse card shows in German
- **THEN** it says what the English card says about days with attack medication, using the glossary term, and adds no
  claim

#### Scenario: Overuse wording is not a limit
- **WHEN** any German overuse or indicator string is read
- **THEN** it holds none of Grenze, Grenzwert, Limit, erlaubt, sicher, Höchstmenge, zu viel, Übergebrauch, absetzen,
  reduzieren or Pause, and says "kann" where English says "can"

#### Scenario: The report is for a doctor
- **WHEN** a German string names the doctor report
- **THEN** it says the report is for a doctor's practice, never "Arztbericht", and the lock-screen visit reminder never
  says "Arzt"

#### Scenario: A key without a pass
- **WHEN** any German health or safety key has no `medical-reviewer` PASS
- **THEN** the release is No-Go until it is rewritten and passes

#### Scenario: Clinician review still open
- **WHEN** the German text ships without a German-speaking clinician's review (T31)
- **THEN** the BRD records it as a limit, and no screen, listing or page calls the text medically reviewed

### Requirement: German subscription words match English
The German paywall, trial, renewal, cancellation and privacy words SHALL carry the meaning of the English ones. Prices and
periods SHALL come from Google Play, and Google's own sheets SHALL show in Google's language (BRD EC-L10N-16, EC-L10N-23,
D195).

#### Scenario: The paywall in German
- **WHEN** the app is German and the paywall opens with products loaded
- **THEN** its words are German, prices and periods are the ones Google returns, and `legal-finance-analyst` has checked
  the renewal and cancellation words against English

#### Scenario: Google's purchase sheet
- **WHEN** buy is tapped in the German app
- **THEN** Google Play's sheet shows in Google's own language and the app adds no text to it

### Requirement: TalkBack in German
Every content description, state and action label SHALL be German when the app is German. Each click label SHALL have
a key of its own, never the visible text reused, and its German SHALL be a noun form that fits TalkBack's "Zum %s
doppeltippen". On a phone whose language is German, TalkBack SHALL read Migrely in German (BRD EC-L10N-17, D195).

#### Scenario: A German phone with TalkBack
- **WHEN** the phone is in German and TalkBack moves through home, attack mode and Log medication
- **THEN** every control's name, state and hint is read in German, and none is English

#### Scenario: German for Migrely on an English phone
- **WHEN** on Android 13 or newer the phone is English, Migrely is German and TalkBack is on
- **THEN** QA records which voice reads Migrely's text, and an English voice is a known limit, not a pass

#### Scenario: A click label in German
- **WHEN** the phone is in German and TalkBack focuses the privacy policy link in Settings
- **THEN** it says "Zum Lesen der ganzen Datenschutzerklärung doppeltippen", from the label's own key, not the link's
  visible words

#### Scenario: English frame around a German label
- **WHEN** on Android 13 or newer the phone is English, Migrely is German and TalkBack focuses a control with a click
  label
- **THEN** TalkBack's English frame wraps the German label ("Double-tap to …"), and QA records it as a known limit that
  no label can fix, not a pass

### Requirement: German letters in typed names
Triggers, medications and notes typed with umlauts or ß SHALL be saved and shown unchanged within the existing length
limits. The duplicate check and the "DELETE" keyword SHALL stay as they are (BRD EC-L10N-18, EC-L10N-27, D195).

#### Scenario: A trigger with umlauts
- **WHEN** the person adds the trigger "Rotwein & Käse" and a medication "Ibuprofen 400"
- **THEN** both are saved and shown exactly as typed

#### Scenario: The delete keyword in German
- **WHEN** the app is German and the person opens Delete all data
- **THEN** the keyword to type is still "DELETE", the German label says so, and typing it works on a German keyboard

#### Scenario: ß and SS
- **WHEN** the person has a trigger "Straße" and adds "STRASSE"
- **THEN** they are two names, as the duplicate check ignores case only with `Locale.ROOT`

### Requirement: German outside the app follows the app
The German store listing SHALL be a de-DE translation of the main listing and SHALL promise only what the German app
does. The site's German page SHALL open only after the German app is live (D148). Swiss German SHALL use the German text.
No permission SHALL be added (BRD EC-L10N-20, EC-L10N-21, EC-L10N-24, D148, D195).

#### Scenario: The doctor report in the listing
- **WHEN** the German listing mentions the doctor report
- **THEN** it does not say the PDF is in German

#### Scenario: The site before the German app is live
- **WHEN** the German build is not yet live on Google Play
- **THEN** migrely.health has no German page

#### Scenario: A phone in Swiss German
- **WHEN** the phone's language is German (Switzerland)
- **THEN** the app shows the German text, with ß, and dates in the Swiss locale

#### Scenario: Permissions of the German build
- **WHEN** `./gradlew :app:checkReleasePermissions` runs on this change
- **THEN** it passes with the same permissions as 1.3.3, and the merged manifest has no INTERNET
