## ADDED Requirements

### Requirement: The Warning signs row names both tiers
The summary under "Warning signs" in Settings SHALL name both actions of the Warning signs page in the page's words:
English "When to call your local emergency number and when to get urgent medical advice", German "Wann den Notruf
wählen, wann dringend ärztlichen Rat holen". It SHALL not say "care", join the two tiers with "or" or "oder", judge the
person's headache or add a claim (BRD SAF-F1, D171, EC-SAF-18).

#### Scenario: Reading the row in English
- **WHEN** the app is in English and the user opens Settings
- **THEN** the summary under "Warning signs" reads "When to call your local emergency number and when to get urgent
  medical advice"
- **AND** it does not contain "care" or " or "

#### Scenario: Reading the row in German
- **WHEN** the app is in German and the user opens Settings
- **THEN** the summary under "Warnzeichen" reads "Wann den Notruf wählen, wann dringend ärztlichen Rat holen"
- **AND** it does not contain "oder" or "Hilfe", and names no phone number

#### Scenario: The old wording fails the check (negative control)
- **WHEN** the summary test runs against `develop`'s strings ("When a headache needs urgent care", "Wann Kopfschmerzen
  dringend ärztliche Hilfe brauchen")
- **THEN** it fails for both languages, which shows the check can fail

#### Scenario: The page itself is unchanged
- **WHEN** the user taps the row
- **THEN** the Warning signs page shows the same two headings, signs and last line as before, and only the summary key
  differs from `develop` in `values/strings.xml` and `values-de/strings.xml`

#### Scenario: 200% font on a narrow phone and in landscape
- **WHEN** the font is 200% on a 360 dp wide screen, in portrait and in landscape, in English and in German
- **THEN** the summary wraps onto as many lines as it needs, with no "…" and no line cut off, and the row grows

#### Scenario: A shorter English line only if the walk demands it
- **WHEN** the 200% walk shows the English line does not fit
- **THEN** the only allowed replacement is "When to call your emergency number and when to get urgent medical advice",
  with this spec and EC-SAF-18 updated first

#### Scenario: TalkBack reads the row once
- **WHEN** TalkBack reaches the row
- **THEN** it is one node read as the title, then the summary, then the button role, in English and in German

#### Scenario: Free, Premium, offline and during an attack
- **WHEN** the user is Free or Premium, offline, or Settings shows while an attack is in progress
- **THEN** the row and its summary are the same, free, and in the dark theme during an attack (EC-SAF-08)

#### Scenario: Upgrade from a shipped version
- **WHEN** the user upgrades from 1.4.0 or any earlier version
- **THEN** the new summary shows at once; no data, migration or backup changes, since only a resource changes

### Requirement: Outside apps run in their own task
"Rate Migrely on Google Play" SHALL start both its intents with `FLAG_ACTIVITY_NEW_TASK`. "Share Migrely" SHALL set
`FLAG_ACTIVITY_NEW_TASK` on the `ACTION_SEND` intent inside the chooser and not on the chooser, so the chooser stays over
Settings and the chosen app runs in its own task. A crash or forced close of the other app SHALL leave Migrely's task
and Settings as they were. The order of tries, the texts and the messages SHALL not change (BRD SET-F2, D185, EC-SHR-14).

#### Scenario: The intents carry the flag
- **WHEN** the launcher builds the Google Play intent, the https intent and the share chooser
- **THEN** the Google Play and https intents have `FLAG_ACTIVITY_NEW_TASK`, the `ACTION_SEND` intent inside the chooser
  has it, and the chooser itself does not
- **AND** neither intent has a URI grant flag, and every extra stays as before (medical-reviewer condition W3)

#### Scenario: Without the flag (negative control)
- **WHEN** the launcher test runs against `develop`'s `ExternalAppLauncher`
- **THEN** the flag assertions fail, which shows the check can fail

#### Scenario: The browser crashes at launch
- **WHEN** a phone has no Google Play app and its only browser crashes at launch, and the user taps "Rate Migrely on
  Google Play" (API 26, as in #280)
- **THEN** Migrely is not closed: its task stays, and when the person returns to it Settings shows with no message
  (EC-SHR-14)

#### Scenario: A share target crashes
- **WHEN** the user picks an app in the share sheet and that app crashes or is force-stopped
- **THEN** Migrely's task stays, and Settings shows with no message when the person returns (EC-SHR-14)

#### Scenario: Its own task on a device
- **WHEN** the user opens Google Play, the browser or a share target from Settings, on API 26 and on API 36
- **THEN** `dumpsys activity activities` lists the other app's activity in a task other than Migrely's, and Recents
  shows it as its own card; the share chooser itself is in Migrely's task

#### Scenario: Back returns to Settings
- **WHEN** the other app was not open before and the user presses Back on its first page
- **THEN** Settings shows at the same scroll position, with no message, and nothing was written or counted (EC-SHR-06)

#### Scenario: The other app was already open
- **WHEN** Google Play or the browser was already open on another page and the user taps the row
- **THEN** Back may first show that app's earlier page; Migrely stays unchanged in Recents and shows Settings when
  reopened (EC-SHR-15)

#### Scenario: Split screen
- **WHEN** Migrely is in split screen and the user taps the row
- **THEN** the other app opens without closing Migrely, and Back or Recents returns Settings; which half it opens in is not
  promised (EC-SHR-15)

#### Scenario: TalkBack focus on return
- **WHEN** TalkBack is on and the user returns from the other app with Back
- **THEN** TalkBack focus is on the activated row or at the top of Settings, never on "Delete all data" (EC-SHR-16)

#### Scenario: Nothing can open it
- **WHEN** both Google Play intents, or the share chooser, fail with `ActivityNotFoundException` or `SecurityException`
- **THEN** the same messages as before show, "This phone has no app that can open Google Play." or "This phone has no
  app to share the link with." (EC-SHR-01, EC-SHR-02)

#### Scenario: One opening per tap still holds
- **WHEN** the user double taps "Rate Migrely on Google Play" or "Share Migrely"
- **THEN** one Google Play page or one share sheet opens, as before (EC-SHR-05)

#### Scenario: Rotation or process death while away
- **WHEN** the phone is rotated in the other app, or Android kills Migrely's process meanwhile, and the user returns
- **THEN** Settings shows with no message, as before (EC-SHR-06)

#### Scenario: No permission or manifest change
- **WHEN** the release build's merged manifest is checked
- **THEN** `:app:checkReleasePermissions` passes with the same permission list as `develop`, and no `<queries>` entry is
  added
