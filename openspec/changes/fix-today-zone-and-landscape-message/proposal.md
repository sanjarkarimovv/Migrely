# Proposal

## Why

QA found two defects on the home screen (GitHub issues #19 and #20). Both are S3.

- **#19, today's card after travel:** an attack logged at 23:15 Berlin time on 24 October belongs to 24 October
  (EC-TIM-02). The month calendar on home shows it there. After a flight to Tashkent it is 02:15 on 25 October, and
  today's card still says "1 migraine today" and lists the attack under "Logged today". The card picks the attacks for
  today's bounds in the device's current zone, while the calendar and History use the attack's own zone. So the home
  screen gives two answers for the same attack, and stats (change 5) and the doctor report (change 10) must not copy
  the card's rule. The same row also repeats the city: "11:15 PM (Berlin) – 11:15 PM (Berlin)".
- **#20, landscape attack mode:** in landscape the attack layout puts the guidance on the left and the controls on the
  right. Messages are centred at the top, up to 560 dp wide, so "Migraine started" with Undo covers "Mild" and
  "Moderate" for the whole undo window. That is 5 s by default, and up to 2 minutes with Android's "Time to take
  action". Screen readers and Maestro cannot find the two chips while it shows.

## What Changes

- **Today by the attack's own days (#19, EC-TIM-02, D36):** today's card and the "Logged today" list take the ended
  attacks whose days, worked out in the zone where each one started, include the device's current date. This is the
  rule the calendar and History already use. Home reads a margin of two days on each side and filters in the domain,
  as History does.
- **The city once in a time range (#19 side note, D36):** a row that shows a start and an end in the same zone names
  the city once, after the end: "11:15 PM – 11:45 PM (Berlin)". This applies to home's "Logged today" rows and to
  History's rows. A time shown on its own keeps its city.
- **Messages beside the controls in landscape attack mode (#20, EC-UX-20, D37):** when attack mode is side by side,
  messages appear over the left (guidance) column only. The intensity choices, "+ Medication taken" and "Migraine
  ended" stay visible and usable for as long as the message shows. Undo stays above the tap guard. Portrait and normal
  mode are unchanged.
- **Keyboard order decision (#22, D38):** recorded only. The month grid becomes one Tab stop with arrow keys in change
  4, which reworks the calendar days.

**Out of scope:**
- the keyboard grid itself (#22), which comes in change 4;
- messages on other screens in landscape, where no controls sit at the top right;
- a reload when the time zone changes while home is open. Home already reloads on every resume and every date change.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `today-sky`: today's card and list use the attack's own days; a listed time range names the city once.
- `attack-history`: a listed time range names the city once.
- `in-app-messages`: in landscape attack mode, messages stay over the guidance column.

## Impact

- **Changed modules:**
  - `:features:home:domain`: `GetTodayUseCase` decides today's attacks; `GetTodayRepository.today(from, to)` reads a
    window; the snapshot no longer carries the clock;
  - `:features:home:data`: `HomeRepository.today(from, to)` only reads;
  - `:features:home:ui`: the message area in `HomeContents`, and `TimeUiFormatter` for the city;
  - `:features:history:ui`: `HistoryTimeUiFormatter` and the History row.
- **Unchanged:** presentation, datasource and `:app` code, apart from the use case's new constructor parameter in
  `HomeModule`.
- **Docs:** BRD 9.9 (EC-UX-20), 13 (row 2c) and 17 (D36–D38).
- **QA retest** on emulator-5580:
  - `qa/device_scenarios.sh travel` (#19);
  - `.maestro/in-app-messages/landscape-message-clear.yaml`, tagged known-bug until this merges (#20);
  - the landscape audit states `attack-mode-message`, `details-bottom` and `home-with-attacks`.
- **BRD references:** EC-TIM-02, EC-UX-16, EC-UX-19, EC-UX-20, D36–D38.
