# Design

## Context

See proposal.md (Why).
- **Build under test:** build 4, `release/1.0.0` @ 29aadb3, with the same product code as develop 438be90. Its debug
  and R8 APKs are in `../migraine-app-qa-rc` and are named with `QA_APK` (D201).
- **The audit:** `qa/a11y_audit.sh` drives every flow in `.maestro/a11y-tour/` with Maestro. It captures each state 3
  times (100% portrait, 200% portrait, 100% landscape) and checks the captures with `qa/a11y_audit.py`.
  - The rules are contrast, 48 dp targets, labels and keyboard focus.
  - Today's 10 states give 30 captures.
- **Browsers on the QA emulator:** only Chrome answers the privacy link. `cmd package query-activities` for VIEW +
  BROWSABLE on https://migrely.health/privacy/ lists `com.android.chrome` alone.
- **The report's notes cap (#67, D84):** with "Include notes" on, the notes follow the table. Only the latest N
  migraines' notes are kept, under "Notes from the latest N migraines.".
  - The `report-heavy` seed numbers its notes from HEAVY-0001, the newest.
  - With that seed, N is 308.

## Goals / Non-Goals

**Goals:**
- every release-fix scenario named in the proposal has a check, or a D61 reason;
- every screen the app shows is captured by the audit;
- exploratory findings on build 4 are filed before users meet them.

**Non-Goals:**
- the old audit states change only if a new rule needs it;
- no new audit rules.

## Decisions

### D1. Tour states for the screens never audited (D203)

Each tour flow starts with `subflows/launch-clean.yaml` and ends on its screen, scrolled to its top, as today's states
do. Landscape and 200% font use the same flow, so each scroll waits for its element instead of assuming a position.

| State | How it is reached |
|---|---|
| `onboarding-first` | a clean install, without the Skip of `launch-clean` |
| `settings-main` | Settings |
| `settings-warning-signs` | Settings › Warning signs |
| `settings-privacy` | Settings › What Migrely keeps, and where, scrolled to "Read the full privacy policy" |
| `settings-theme` | Settings › Theme |
| `medications-list` | My medications, with one medication added |
| `reminders` | Settings › Reminders |
| `your-data` | Settings › Your data |
| `paywall` | History › Doctor report, as a free user. The yearly plan is chosen first |
| `report-period` | Premium bought (`subflows/buy.yaml`), then History › Doctor report |
| `insights` | Premium bought and 3 migraines logged, then History › Insights ("N more migraines") |

- **Premium in a tour flow:** the purchase goes through the simulated sheet, so these states need the debug APK. The
  audit already runs on it. On the R8 APK the premium states are not reachable, and the audit says so.
- **Rejected: one state per page of a long screen.** The audit's "not judged" rule already re-checks nodes cut off at an
  edge on a capture where they are fully in view. A long page gets a second state only when a part never comes into
  view.

### D2. The privacy link and "No browser" (device scenario `privacy_link`)

- **Link:**
  1. `logcat -c`.
  2. The device step `privacy-policy-link.yaml` taps the link.
  3. The check reads `ActivityTaskManager`'s START line: a VIEW intent to `migrely.health` that resolves to Chrome.
     Android shortens the logged URI, so the full address is read from the page's own text, which shows it (D119).
- **No browser:**
  1. `pm disable-user --user 0 com.android.chrome`, then the intent query must list no activity.
  2. The same tap.
  3. The checks:
     - Migrely is still the resumed activity;
     - logcat has no `FATAL EXCEPTION` for `com.migrely.app`;
     - the page still shows the address.
  4. `restore()` runs `pm enable com.android.chrome`, also when the scenario fails.
- **Rejected: uninstalling Chrome's updates or using a browserless AVD.** The first changes the QA image for every
  later run. The second costs a third emulator.

### D3. Track periods off (flow `day-markers/track-periods-off-marks.yaml`)

One flow, in this order. Marks are checked by the cell's spoken label (`<day>: nothing logged. Period.`), as
`mark-and-remove.yaml` does.
1. With tracking on, mark today from home and mark one empty past day in History. Keep its date in `output.marked`.
2. Turn "Track periods" off in Settings, and go back.
3. Home still shows "Period today", since today has a mark. Tap it:
   - the mark is gone;
   - "Period today" is still there.
   Then go to History and back, and the toggle is gone ("Removing today's mark with tracking off").
4. Restart the app. Home shows no "Period today" ("Period today from home › Period tracking off").
5. In History, choose `output.marked`. "Period" shows, and a tap removes the mark ("Removing a mark with tracking
   off").
6. Choose an empty past day. Below its heading, only "Pain-free day" shows ("Mark a chosen day › Period tracking off").
- **Limit:** like `mark-and-remove`, the flow needs a past day of the current month, so it cannot run on the 1st.

### D4. No note among the migraines shown (seed `report-heavy-old-notes`)

- **The seed:** like `report-heavy`, 900 migraines in 12 months, but only the oldest 100 have a note (HEAVY-0801 to
  HEAVY-0900).
- **The check:** `report_heavy` imports it with Replace, after its own two reports, and makes a 12-month report with
  notes on. The text must have:
  - "Showing the latest N of 900 migraines.";
  - "Notes from the latest N migraines.", with the same N;
  - no "HEAVY-" note at all.
- **Rejected: a separate scenario.** It would buy Premium and import 900 migraines again for one check.

### D5. Google Play offers no products (D110)

When D122 lands, the dev session names the command. The scenario `premium_store_states` then sets it, and checks:
- no paywall shows itself after onboarding, nor after the 5th migraine;
- a tap on Doctor report opens the paywall with "Premium isn't available";
- with the clock 30 minutes on, then 61 minutes on, the app asks again at most once an hour, if the command or the
  store's log makes the asks visible.

If D122 is not on develop when the rest is verified, the scenario stays a D61 gap with that follow-up.

### D6. Exploratory charters on build 4

Each charter is 30 to 45 minutes on the debug APK (the R8 APK for the launch path). A defect is filed with its steps
and a screenshot.
1. **Rotation and process death** on the paywall, the purchase sheet, the report while it is being made, and Insights.
   The questions: are the chosen plan, the chosen period and any message kept? Does the state come back after
   `am kill`?
2. **200% font and dark theme** on the report and Insights flows, end to end, beyond the audit's single captures.
3. **Restarts around a purchase:** the app is force-stopped while the sheet is open and while a payment is pending. Is
   Premium right on the next start?
4. **First launch of the R8 APK as a new user:** onboarding, the first migraine, the Settings pages, then the privacy
   link. This is the path every new user takes on day one.

## Risks / Trade-offs

- **Chrome left disabled:** a failed scenario that skips `restore()` would leave the QA emulator without a browser. So
  `restore()` re-enables Chrome, and `qa/reset_device.sh` enables it too.
- **The audit's run time grows** from 30 captures to about 63, about 40 minutes more. It runs at the Go/No-Go and after
  UI changes, not on every flow change.
- **New states may fail on old screens.** They are expected to find real issues. Those are filed, not worked around
  (D203).
