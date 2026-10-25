# Proposal

## Why

On a real phone in Russian, Migrely's English screens showed Russian dates: home read "воскресенье, 25 октября" and
"октябрь. Opens History.", and the pickers and History mixed Russian month and weekday names into English text. The
app's words are English only (BRD EC-L10N-01), but its dates followed the phone's language (EC-L10N-03, EC-TIM-05).
D117 decides that while Migrely ships only English, every name a date or time produces is English, and only the
region's conventions come from the phone.

## What Changes

- One formatting locale for the whole app (D117): the phone's own when its language is English; otherwise English with
  the phone's country (ru-RU gives en-RU, uz-UZ gives en-UZ), or plain English with no country.
- The week still starts on the phone's first day, a first day chosen in Android's regional preferences included; the
  12- or 24-hour setting is still the system's.
- `:app` applies it in one place: the process default at start and on every configuration change, and the activity's
  configuration, so every screen, Material's date and time pickers, resources, the doctor report and notifications
  follow it.
- The CSV export keeps choosing its separator by the phone's own language (D72).

## Capabilities

### Modified Capabilities

- `month-sky`: weekday initials are English; weeks start on the phone's first day.

### Added Requirements

- `app-shell`: dates and times in English with the phone's region.

## Impact

- **`:app`:** `FormattingLocaleResolver` and `FormattingLocaleApplier` (`com.migrely.app.locale`), `MigrelyApplication`,
  `MainActivity`, and the DI modules whose formatters read the locale.
- **Backup:** the CSV repository is handed the phone's own locale.
- **Docs:** BRD D117, EC-L10N-03 and EC-TIM-05; ARCHITECTURE.

## Out of scope

- Translating the app's words, and any date order of its own: dates use English's order for the region.
- The CSV's and the PDF's own fixed English words and formats, which stay as they are (ARCHITECTURE section 13).
