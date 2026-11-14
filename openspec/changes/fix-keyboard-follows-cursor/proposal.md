# Proposal

## Why

Three open issues share one cause: Migrely never tells Android how to treat the on-screen keyboard. All three predate 1.2.1.
They were confirmed by reading `develop` 046efe95, not by running it.

- **#141 (S3, accessibility).** On a migraine's details, a long note runs under the keyboard and the page does not follow the
  cursor. At 200% font it starts after about 5 lines. The scrolling column (`AttackDetailsScreen.kt:468-511`) takes no keyboard
  inset, only a `navigationBars` spacer, so the note field (`AttackImpactNoteSections.kt:112`) thinks the area under the keyboard
  is still visible. A person with large text types blind within a few lines.
- **#83 (S4).** With the keyboard open on the same screen, the whole window pans: Back's bounds are `[0,0][0,0]` and the trigger
  chips draw under the status bar's clock. The manifest has no `windowSoftInputMode`, and `MainActivity.kt:295` turns on
  edge-to-edge, so Android picks pan.
- **#145 (S3, investigated here).** On Android 8, after a copy from a text field's toolbar, every screen keeps the keyboard's
  space until a restart. Home and History pad their bottom with `safeDrawing`, which includes the keyboard
  (`TodayContents.kt:88`, `AttackModeContents.kt:105`, `HistoryScreen.kt:298`). A keyboard inset left behind on Android 8
  would explain it. That is a hypothesis until it is measured on API 26.

Turning pan off is not a one-screen fix. Edit medication and Log medication pad for the keyboard inside their scroll
(`EditMedicationScreen.kt:214-217`, `LogMedicationScreen.kt:329-332`), so today only the pan keeps their fields in sight.

The code reviewer of `fix-messages-everywhere` warned that the message's fit rule ignores the keyboard on purpose
(`MigrelySnackbar.kt:360`, `MessageFit.kt`, D178 (6)). Whatever this change does to the window, a message must still never
cover Back, and the fit rule must stay keyboard-independent.

The decision is BRD D180.

## What Changes

- **The window resizes instead of panning (D180 (1)).** `MainActivity` declares `adjustResize`. The top bar and Back stay in
  place with the keyboard open, and nothing draws under the status bar (#83).
- **The line being typed stays above the keyboard (D180 (2)).** Every screen with a text field shrinks its scrolling area's
  visible part to the keyboard's top, with the keyboard padding outside the scroll. The focused field and the cursor line stay
  in view while typing. Compose brings the cursor into view itself where it can; where it does not, the cursor's rectangle is
  brought into view. This covers Attack details' note (#141), Edit medication and Log medication. Delete all data already keeps
  its Delete row above the keyboard and only gets checked.
- **A screen with no text field ignores the keyboard (D180 (3)).** Home, History, Insights, Settings and its pages, the
  paywall, Doctor report, My medications, Your data and Reminders pad only for the system bars and the cutout. One shared
  inset in `designsystem` serves them and the message's fit rule.
- **Messages stay keyboard-independent (D180 (4)).** No keyboard padding goes on `MessageLayout`, the message host or any
  of their ancestors. A test opens the keyboard on Attack details and proves that the message does not move and does not cover
  Back. A mutation that pads the host for the keyboard makes that test fail.
- **#145 is measured first (D180 (6)).** `qa-engineer` logs the keyboard inset on API 26 before and after the copy. If a stale
  inset is the cause, D180 (3) fixes it and QA closes #145 after a retest. If not, #145 stays open.

## Out of scope

- The note's counter in UTF-16 units (#82), its limit and its words.
- Dialog windows: "Add your own" (custom tag) and the date pickers, whose input mode types a date. They have their own
  window and keep their padding; the walk only checks that they still work.
- New IME actions (Done, Next) on any field, and changing a field's size, lines or order.
- Pausing a message's countdown while typing, and anything else the message rules (D158, D165, D178) already decide.
- Floating keyboards, one-handed mode and a keyboard's own full-screen editor in landscape: Migrely follows the inset Android
  gives and adds nothing.
- The tab order with a hardware keyboard, which D178 (4) set.
- Any new string, permission, data, migration or Premium rule.

## Capabilities

### New Capabilities

None.

### Modified Capabilities

- `app-shell`: the window resizes for the keyboard, the top bar stays, fields stay above the keyboard, screens without a field
  ignore it (ADDED requirements).
- `attack-details`: the note keeps its cursor line above the keyboard (ADDED requirement).
- `in-app-messages`: a message ignores the keyboard (ADDED requirement).

## Impact

- **Code:** `app/src/main/AndroidManifest.xml` (one attribute), `designsystem` (a shared bars inset, maybe a cursor
  bring-into-view helper), `features/attack/ui` (`AttackDetailsScreen.kt`, maybe `AttackImpactNoteSections.kt`),
  `features/medication/ui` (`EditMedicationScreen.kt`, `LogMedicationScreen.kt`), and the bottom insets of the 10 screens
  without a field (11 files). `AttackDetailsScreen.kt` was also edited by `fix-messages-everywhere`, which is merged (D178), so there is no clash.
- **Tests:** Robolectric tests with a dispatched keyboard inset, the existing `MessageFitInsetsTest`, and the D167 card tests.
- **Manifest and permissions:** no new permission. `:app:checkReleasePermissions` must still match 1.2.1's list.
- **Data, backup, migration, Premium:** none.
- **BRD:** D180, EC-UX-39 to EC-UX-43, §13 row 48.
