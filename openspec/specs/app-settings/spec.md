# app-settings Specification

## Purpose
Defines Settings: its sections, the theme, the warning signs, deleting all data, the privacy, about, licence and
version pages, and the Share, Rate and privacy policy links, which open other apps in their own task (BRD SET-F1, SET-F2,
SAF-F1, EC-STO-03, EC-STO-04, D93–D97, D185, EC-SHR-14).

## Requirements

### Requirement: The settings list
Settings SHALL list, in sections:
- "Your diary": Reminders, Track periods, Your data;
- "Look": Theme;
- "Help": Warning signs, About Migrely;
- "Privacy and about": Privacy, Open-source licences, Version;
- "Share and rate": Share Migrely, Rate Migrely on Google Play, only while no attack is in progress;
- then "Delete all data".

It SHALL offer no analytics switch, since Migrely has no internet permission and no analytics (BRD SET-F1, SET-F2, D93,
D181, D185).

#### Scenario: Opening Settings
- **WHEN** the user taps the gear on home and no attack is in progress
- **THEN** the sections show in that order, each item one labelled node

### Requirement: Theme
Theme SHALL offer "Dark", the default, "Light" and "Match the phone". The light theme SHALL use no pure white. Attack
mode and the dark room SHALL stay dark in every theme, and while an attack is in progress every screen SHALL be dark,
because light hurts during an attack. The choice SHALL apply at once and at every start, with no frame in the other
theme (BRD SET-F1, EC-UX-01, D94).

#### Scenario: Light
- **WHEN** the user chooses "Light"
- **THEN** Settings turns light at once, and home is light at the next start

#### Scenario: An attack in progress
- **WHEN** the theme is Light and an attack is in progress
- **THEN** every screen is dark, attack mode and the screens opened from it included
- **AND** once the attack ends, the screens are light again

### Requirement: Warning signs
Help SHALL show the warning signs in two headed parts. The first SHALL say to call the local emergency number if a headache comes
with any of nine NHS "Call 999" signs, with no condition to wait, and then not to drive to hospital. The rash sign SHALL say it may
be harder to see on brown or black skin. The page SHALL dial nothing, SHALL not list a first headache after 50 or numbness alone,
nor name "999", "GP" or "NHS 111" (BRD SAF-F1, D95, D171, D173, EC-SAF-01, 04, 05, 13, 14).

#### Scenario: Reading them
- **WHEN** the user opens Settings, then "Warning signs"
- **THEN** the first heading reads "Call your local emergency number if a headache comes with any of these:"
- **AND** under it, in this order: "Extremely bad pain that came on suddenly", "Weakness or loss of movement on one
  side of your body or face", "Problems speaking or remembering things", "Losing your vision, or blurred or double
  vision", "Feeling drowsy or confused", "A seizure or fit", "A very high temperature and signs of meningitis, such as
  a stiff neck", "A rash that doesn't fade when you press a glass against it (this may be harder to see on brown or
  black skin)", "A head injury in the last 3 months"
- **AND** after them, as plain text and not a sign: "Don't drive yourself to hospital. Call your local emergency
  number."

#### Scenario: A rash on brown or black skin
- **WHEN** a person with brown or black skin reads the rash sign
- **THEN** the same line says the rash may be harder to see on brown or black skin
- **AND** the sign stays one item, with no extra lines on where to look

#### Scenario: The rash sign with TalkBack and at 200% font
- **WHEN** TalkBack reads the page, or the font is 200% on a 320 dp screen or in landscape
- **THEN** the rash sign is one stop, read in full with its note, the page keeps its 18 stops, and the sign wraps with no line cut
  off

#### Scenario: Nothing tells the person to wait
- **WHEN** the user reads the emergency signs
- **THEN** no sign carries "doesn't pass", "recent" or any other condition to wait for
- **AND** "A first headache after the age of 50" is not on the page

#### Scenario: Numbness alone waits for the clinician
- **WHEN** the user reads either part of the page
- **THEN** no sign names numbness on its own, because the NHS migraine page counts it as an aura symptom
- **AND** this stays until a named clinician answers the question (T31) and a new BRD decision changes it

#### Scenario: Outside the UK
- **WHEN** the user reads the page in any country
- **THEN** it says "your local emergency number" and shows no "999", "GP", "NHS 111" and no dial button

### Requirement: Delete all data
"Delete all data" SHALL be confirmed by typing DELETE. It SHALL delete:
- every health record and the device state;
- the preferences;
- the automatic copies and the reports.

It SHALL cancel planned reminders and remove notifications, and then start the app again at onboarding, whichever screen is
current. The subscription SHALL be untouched (BRD EC-STO-03, EC-STO-05, D96, D154).

#### Scenario: Not typed
- **WHEN** the user opens the confirmation and types "delete"
- **THEN** the button stays off and nothing is deleted

#### Scenario: Deleted
- **WHEN** the user types DELETE and confirms
- **THEN** the app starts again at onboarding with no records, no copies and no reminders

#### Scenario: The deletion ends while the screen fades out
- **WHEN** the deletion ends while the delete screen is leaving
- **THEN** the app starts again once

#### Scenario: The deletion ends after the screen is gone
- **WHEN** the deletion ends after a launcher shortcut or a fast Back has taken the delete screen away
- **THEN** the app starts again

#### Scenario: Recreated after the deletion ended
- **WHEN** the screens are composed again after the deletion has ended
- **THEN** the app starts again

#### Scenario: Back before deleting
- **WHEN** the user presses Back on the delete screen before confirming
- **THEN** the screen leaves and nothing is deleted

### Requirement: Privacy and about
Settings SHALL offer:
- **Privacy:** where entries can go, as "The Privacy page says where entries can go" sets out, ending with a link to
  https://migrely.health/privacy/, address written out, which opens in the phone's browser;
- **About Migrely:** the full disclaimer;
- **Open-source licences:** the libraries and their licences;
- **Version:** the app's version.

(BRD SET-F1, EC-STO-04, D97, D119, D181.)

#### Scenario: The version
- **WHEN** the user looks at Settings
- **THEN** the Version item shows the installed version

#### Scenario: The full privacy policy
- **WHEN** the user taps "Read the full privacy policy" on the Privacy page
- **THEN** the phone's browser opens https://migrely.health/privacy/

#### Scenario: No browser
- **WHEN** the user taps the link on a phone with no browser
- **THEN** nothing opens, the app keeps running, and the address stays on the page

### Requirement: Premium in Settings
Settings SHALL have a "Premium" section at the top. It SHALL show:
- the status: Free, Premium with its plan, Free trial, or Payment pending;
- the trial's end when known;
- "Restore purchases";
- for a subscription, "Manage subscription", which opens Google Play's subscriptions page.

A free user's status row SHALL open the paywall. Delete all data SHALL leave the purchase in Google Play, and Premium
SHALL be found again at the next check (BRD SUB-F2, D96, D112).

#### Scenario: A subscriber
- **WHEN** a yearly subscriber opens Settings
- **THEN** the section reads "Premium · yearly", with "Restore purchases" and "Manage subscription"

### Requirement: Urgent medical advice
Under a second heading, Warning signs SHALL say to get urgent medical advice if the person has a migraine attack and it
has lasted longer than 72 hours, aura symptoms last longer than 1 hour at a time, or they are pregnant or have just had
a baby, as on the NHS urgent list. These signs SHALL not be sent to the emergency number (BRD SAF-F1, D171, EC-SAF-02,
EC-SAF-03).

#### Scenario: Reading the second part
- **WHEN** the user scrolls past the emergency signs
- **THEN** the second heading reads "Get urgent medical advice if you have a migraine attack and:"
- **AND** under it, in this order: "It has lasted longer than 72 hours", "Aura symptoms last longer than 1 hour at a
  time", "You're pregnant or have just had a baby"

#### Scenario: Pregnancy without an attack
- **WHEN** the user reads the line about pregnancy
- **THEN** it sits under the heading that ends "if you have a migraine attack and:", never on its own

### Requirement: Other symptoms
Warning signs SHALL end with one plain line saying that any other worrying symptom needs urgent medical advice, so that
the lists never read as complete (BRD D171, EC-SAF-05).

#### Scenario: The last line
- **WHEN** the user scrolls to the end of the page
- **THEN** the last line reads "If you have any other symptoms you're worried about, get urgent medical advice."

### Requirement: The About page points to the emergency signs only
About Migrely's urgent line SHALL send to the local emergency number only the signs in the emergency part of Warning
signs, so that a sign from the second part never reads as a reason to call it (BRD D171, EC-SAF-02).

#### Scenario: Reading About
- **WHEN** the user opens Settings, then "About Migrely"
- **THEN** the last paragraph reads "If a headache comes with any of the emergency signs on the Warning signs page,
  call your local emergency number."

### Requirement: Warning signs are readable for everyone
Both part headings SHALL be accessibility headings. Each sign and line SHALL be one node read in screen order, with no
bullet glyph spoken. At 200% font, in landscape and on small screens, the page SHALL scroll and show every line whole.
It SHALL work offline, in the dark theme during an attack, and the same for Free and Premium (BRD EC-SAF-06…09,
EC-UX-03).

#### Scenario: TalkBack
- **WHEN** TalkBack reads the page from the top
- **THEN** it stops 18 times, in this order: back, "Warning signs" (heading), the first heading, the nine signs, the
  "Don't drive" line, the second heading, the three signs of the second part, the last line
- **AND** each heading is announced as a heading, no bullet glyph is read, and moving by headings goes from the first
  heading to the second in one step

#### Scenario: 200% font and landscape
- **WHEN** the font scale is 200%, the screen is 320 dp wide, or the phone is in landscape
- **THEN** the first heading shows whole on the first screen in portrait, the app bar title is not cut, and the user
  can scroll to the last line with every line whole

#### Scenario: During an attack, offline, Free
- **WHEN** a Free user with no network opens the page while an attack is in progress
- **THEN** the page shows in the dark theme with every line and no paywall

#### Scenario: After the update
- **WHEN** a user of 1.1.1 updates to 1.2.1 and opens the page
- **THEN** only the new text shows, and the diary and settings are unchanged

### Requirement: The Privacy page says where entries can go
The Privacy page SHALL show 7 paragraphs in this order, each one TalkBack stop and none a heading: internet permission,
Google Play purchase, phone and backup, files, delete, Android backup, policy link. Its text SHALL be legal's final
wording (D181 (1)), the same for Free and Premium, every Android version and offline. No app string SHALL say "internet
access", "sends nothing", "never leaves", "nothing leaves" or "100% offline" (BRD EC-STO-11, EC-STO-12, D179 (2), D181).

#### Scenario: The first two paragraphs
- **WHEN** the user opens Settings, then "What Migrely keeps, and where"
- **THEN** the first paragraph reads "Migrely has no internet permission, so the app itself cannot send anything over
  the internet. There's no account to make and nothing to sign in to."
- **AND** the second reads "Premium is bought through Google Play, which goes online for the purchase."

#### Scenario: Phone, backup and transfer
- **WHEN** the user reads the third paragraph
- **THEN** it reads "Your entries stay on this phone. If Android's own backup is on, it may also copy them to your Google
  account. On Android 9 and later, it does that only when it can encrypt the copy end to end, which needs a screen lock.
  On Android 8, Migrely cannot require that, so the copy may not be end-to-end encrypted. Android can also move your
  entries to a new phone when you set it up."

#### Scenario: Files
- **WHEN** the user reads the fourth paragraph
- **THEN** it reads "Apart from Android's own backup and moving to a new phone, your entries leave the phone only in a
  file you export yourself: a backup, a spreadsheet or a report for your doctor. That file goes wherever you send it."

#### Scenario: An Android 8 phone, backup off, or no screen lock
- **WHEN** the page is opened on Android 8, on a phone with Android's backup turned off, or on Android 9 or later
  without a screen lock
- **THEN** the same text shows, because it names each case itself, and nothing else on the page changes

#### Scenario: A Free user, offline
- **WHEN** a Free user with no connection opens the page
- **THEN** the same 7 paragraphs show, including the Google Play sentence, and nothing asks for a connection

#### Scenario: After the update
- **WHEN** a user who had 1.2.1 or earlier updates and opens the page
- **THEN** the new text shows, with no data change, migration or permission prompt

#### Scenario: TalkBack order
- **WHEN** TalkBack is on and the user swipes forward from the page's Back through the page
- **THEN** TalkBack reads the 7 paragraphs in the order above, each once, the last as the policy link with its address

#### Scenario: Largest font size and landscape
- **WHEN** the font size is 200% on a 360 dp phone, in portrait or landscape, in the dark or the light theme
- **THEN** every paragraph wraps whole, the page scrolls, and the policy link and its address are reachable by
  scrolling, Tab and TalkBack

#### Scenario: No overclaim left
- **WHEN** the app's `res/values*` strings are searched for "internet access", "sends nothing", "never leaves", "nothing
  leaves" or "100% offline", ignoring case
- **THEN** there is no match

### Requirement: A setting that is not saved stays as stored
When a setting or mark cannot be written to the phone, Migrely SHALL show "Couldn't save that. Try again." where the
screen shows its messages, SHALL show the value that is stored, and SHALL never save the failed change later as part of
another write (BRD D178, D197, EC-STO-13).

#### Scenario: Storage full when turning off Track periods
- **WHEN** the phone's storage is full and the user turns off "Track periods"
- **THEN** "Couldn't save that. Try again." shows and the switch shows on, as stored, also after a restart

#### Scenario: A later write succeeds
- **WHEN** a switch change was not saved, space is freed, and another setting is then saved
- **THEN** the file holds the other setting's new value and the earlier stored value of the switch, not the change that
  failed

### Requirement: Rate Migrely on Google Play
"Rate Migrely on Google Play", summary "Opens Migrely's store page, outside the app", SHALL open Migrely's page with
`ACTION_VIEW`: `market://details?id=com.migrely.app` in the Google Play app, else
`https://play.google.com/store/apps/details?id=com.migrely.app` in a browser. It SHALL never start the in-app review
flow, show a pop-up, reward anything or record anything (BRD SET-F2, D185).

#### Scenario: Opening Google Play
- **WHEN** the user taps "Rate Migrely on Google Play" on a phone with the Google Play app
- **THEN** Migrely's page opens in Google Play, and the in-app review flow is not started

#### Scenario: No Google Play app
- **WHEN** the Google Play app is missing or disabled and a browser is installed
- **THEN** `https://play.google.com/store/apps/details?id=com.migrely.app` opens in the browser (EC-SHR-01)

#### Scenario: Nothing can open it
- **WHEN** both `market://` in Google Play and the https page in a browser fail with `ActivityNotFoundException` or a
  `SecurityException`, as on a work profile or managed phone that blocks them
- **THEN** Settings shows "This phone has no app that can open Google Play." in its message host, the app keeps running,
  and the tap is never silent (EC-SHR-01, EC-SHR-02)

#### Scenario: Offline
- **WHEN** the phone has no connection and the user taps the row
- **THEN** Google Play opens and shows its own offline state; Migrely adds nothing (EC-SHR-04)

### Requirement: Share Migrely
"Share Migrely", summary "Send someone the link to Migrely. Your entries aren't included.", SHALL open the system share
sheet (`ACTION_SEND`, `text/plain`) with "Migrely is a private migraine diary for Android. No internet permission, no
account, no ads.", a newline and the "In-app Settings, Share Migrely" link of `docs/release/play-links.md`. No file,
stream or `ClipData` URI. A share SHALL never be rewarded, unlock anything or be counted (BRD SET-F2, D184, D185).

#### Scenario: Sharing
- **WHEN** the user taps "Share Migrely" and picks an app
- **THEN** `EXTRA_TEXT` equals the sentence, a newline and the link, the link equal byte for byte to
  `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=app&utm_medium=share&utm_campaign=settings_share&referrer=utm_source%3Dapp%26utm_medium%3Dshare%26utm_campaign%3Dsettings_share`
- **AND** `EXTRA_SUBJECT` and `EXTRA_TITLE` are "Migrely, a migraine diary for Android", and the chooser title is "Share
  Migrely"

#### Scenario: Text only
- **WHEN** the share intent is built
- **THEN** it has no `EXTRA_STREAM`, no `ClipData` with a URI and no grant flag, so "Your entries aren't included" stays
  true; a test asserts this (medical-reviewer condition W3)

#### Scenario: A debug build
- **WHEN** a debug build, or any build with another package name, shares
- **THEN** the link still names `com.migrely.app`, never the running package (EC-SHR-09)

#### Scenario: No outcome promise and no reward
- **WHEN** the share text is read, and after any number of shares
- **THEN** it promises no health outcome and says nothing about what stays on or leaves the phone beyond "No internet
  permission"; nothing is rewarded, unlocked or counted, so a share stays the person's own choice (EC-SHR-08)

#### Scenario: No app can take it
- **WHEN** the share sheet cannot be started
- **THEN** Settings shows "This phone has no app to share the link with.", and the app keeps running (EC-SHR-02)

#### Scenario: A messenger drops the referrer
- **WHEN** the receiving app or its in-app browser drops the `referrer` or UTM parameters
- **THEN** the person still lands on Migrely's Play page, and Migrely neither checks nor records it (EC-SHR-10)

### Requirement: One opening per tap
After a tap on "Share Migrely" or "Rate Migrely on Google Play", both rows SHALL ignore activation until Settings is
resumed after leaving or, if Settings never pauses, until Android's double-tap timeout has passed. This SHALL hold for
touch, TalkBack, Switch Access and key repeat. A failure message SHALL be stamped, so a later deliberate tap shows and
speaks it again (BRD D185, D178, EC-SHR-05).

#### Scenario: A quick double tap
- **WHEN** the user taps "Share Migrely" twice within the double-tap timeout
- **THEN** one share sheet opens (EC-SHR-05)

#### Scenario: Share then Rate in a burst
- **WHEN** the user taps "Share Migrely" and then "Rate Migrely on Google Play" within the double-tap timeout
- **THEN** only the share sheet opens

#### Scenario: Held Enter key
- **WHEN** a keyboard user holds Enter on "Rate Migrely on Google Play" so the key repeats
- **THEN** one Google Play page opens

#### Scenario: A second deliberate tap after a failure
- **WHEN** nothing can open Google Play and the user taps the row again after the timeout
- **THEN** the message is shown and spoken again as a new message

#### Scenario: Without the guard (negative control)
- **WHEN** QA runs the double-tap check on a build with the guard removed
- **THEN** two share sheets or two Play intents start, which shows the check can fail

### Requirement: Share and rate stay out of the way
The "Share and rate" section SHALL be hidden while an attack is in progress or the attack state is unknown, and SHALL
never appear by itself: no pop-up, banner, badge, notification or reminder. Both rows SHALL look and behave the same for
Free, subscription, lifetime and promo-code Premium, store nothing, and add no permission, dependency or manifest entry
(BRD SET-F2, D185).

#### Scenario: During an attack
- **WHEN** an attack is in progress while Settings is shown or loads again (the UI opens Settings only from home's
  today mode, never from attack mode or the dark room)
- **THEN** the "Share and rate" header and both rows are absent, and they return once the attack has ended (EC-SHR-03)

#### Scenario: Loading
- **WHEN** Settings is still loading and the attack state is not yet known
- **THEN** the section is absent, and it appears with the rest of the content once no attack is known (EC-SHR-12)

#### Scenario: An attack starts while Settings is open
- **WHEN** an attack starts while Settings is open and TalkBack or keyboard focus is on the section's header or a row
- **THEN** the section fades and collapses in 200 ms (at once with "Remove animations"), focus moves to "Open-source
  licences" (or, if TalkBack focus cannot be moved there, to the "Settings" title), never to "Delete all data", and
  nothing is announced (EC-SHR-13; unit and Compose tests only, since no UI path changes the attack state while
  Settings stays on screen)

#### Scenario: The attack ends while Settings is open
- **WHEN** the attack ends while Settings is open
- **THEN** the section returns, focus stays where it was, and nothing is announced (EC-SHR-13; unit and Compose tests
  only)

#### Scenario: Back from Google Play or the share sheet
- **WHEN** the user returns from Google Play or the share sheet, after a rotation there, or after Android killed Migrely's
  process meanwhile
- **THEN** Settings shows with no message, and nothing was written or counted; the scroll position after process death
  is not promised, as for all of Settings before this change (EC-SHR-06)

#### Scenario: TalkBack, 200% font, landscape and keyboard
- **WHEN** the rows are reached with TalkBack, at 200% font, in landscape or with a keyboard
- **THEN** each row is one node whose name starts with its visible title, with its summary, the button role and the
  action "choose an app to share with" or "open Google Play"; the text wraps without "…"; the rows have no chevron; Tab
  reaches them in visual order and Enter or Space opens them (EC-SHR-07)

#### Scenario: Free or Premium
- **WHEN** the user is Free, or has Premium by subscription, lifetime or promo code
- **THEN** the same rows and text show, and nothing is rewarded, unlocked or counted (EC-SHR-08)

#### Scenario: Warning signs stay first in Help
- **WHEN** the new section is present
- **THEN** "Warning signs" is still the first row of "Help", free, and in the same place as before

#### Scenario: Delete all data, backup and upgrade
- **WHEN** the user deletes all data, restores a backup, or upgrades from any shipped version
- **THEN** the rows show as on a new install, with no "new" badge, since they keep nothing (EC-SHR-11)

#### Scenario: No new permission or manifest entry
- **WHEN** the release build is made
- **THEN** `:app:checkReleasePermissions` passes and the release merged manifest has no new permission, `<queries>`
  element or component compared with `develop`

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
Every outside app Settings starts SHALL get `FLAG_ACTIVITY_NEW_TASK`: Rate's Google Play and https intents, the privacy
policy's https page, and the `ACTION_SEND` inside the Share chooser, never the chooser. On API 28 and later, a crash at
launch of that app SHALL leave Migrely's task and Settings as they were; on API 26 and 27 this is a recorded platform
limit. Tries, texts and messages SHALL not change (BRD SET-F2, D185, EC-SHR-14, EC-SHR-17).

#### Scenario: The intents carry the flag
- **WHEN** the launcher builds the Google Play intent, the https intent, the privacy policy intent and the share chooser
- **THEN** the Google Play, https and privacy policy intents have `FLAG_ACTIVITY_NEW_TASK`, the `ACTION_SEND` intent inside
  the chooser has it, and the chooser itself does not
- **AND** no intent has a URI grant flag, and every extra stays as before (medical-reviewer condition W3)

#### Scenario: Without the flag (negative control)
- **WHEN** the launcher tests and the privacy link test run against `develop`'s code
- **THEN** the flag assertions fail, which shows the checks can fail

#### Scenario: The browser crashes at launch, API 28 and later
- **WHEN** on API 28 or later (API 36 in QA) Google Play is missing or disabled, the only app for https pages crashes in
  its `onCreate`, and the user taps "Rate Migrely on Google Play"
- **THEN** Migrely is not closed: its task stays, and Settings shows with no message when the person returns
- **AND** `develop`'s build on the same phone closes Migrely's screen, which shows the check can fail (EC-SHR-14)

#### Scenario: The privacy policy page crashes at launch, API 28 and later
- **WHEN** on API 28 or later the only app for https pages crashes in its `onCreate`, and the user opens the privacy
  policy link on the Privacy page
- **THEN** Migrely is not closed, and the Privacy page shows when the person returns; `develop`'s build closes it
  (EC-SHR-14)

#### Scenario: API 26 and 27, a recorded limit
- **WHEN** the same crash at launch happens on API 26 or 27
- **THEN** Migrely's screen may still close, as on `develop`, because those versions keep all full-screen tasks in one
  stack and the crash handler finishes the paused activity below; nothing is lost, since Settings holds no unsaved entry,
  and opening Migrely again starts at Home
- **AND** QA records what the device shows; if Migrely stays open there, QA reports it and this scenario is reworded
  (EC-SHR-17)

#### Scenario: Share on the system chooser
- **WHEN** the user picks an app in the AOSP share sheet, on `develop` or on this branch
- **THEN** the chosen app runs in a task of its own on both, because the chooser itself adds
  `FLAG_ACTIVITY_NEW_DOCUMENT | FLAG_ACTIVITY_MULTIPLE_TASK` to an `ACTION_SEND` target; the inner flag only guards
  choosers that do not, which is unproven (EC-SHR-14)

#### Scenario: Its own task on a device
- **WHEN** the user opens Google Play, the browser, the privacy policy page or a share target from Settings, on API 36
- **THEN** `dumpsys activity activities` lists the other app's activity in a task other than Migrely's, where `develop`
  listed Google Play, the browser and the privacy page in Migrely's task; the share chooser itself stays in Migrely's
  task

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

#### Scenario: No browser for the privacy policy
- **WHEN** no app opens web pages, or a work profile forbids it, and the user opens the privacy policy link
- **THEN** nothing opens and nothing is shown, the address under the link can still be typed elsewhere, and the app
  keeps running; with no browser this is as before, while with a work profile's `SecurityException` it is new, since
  `develop` let that exception escape the click and crash

#### Scenario: One opening per tap still holds
- **WHEN** the user double taps "Rate Migrely on Google Play" or "Share Migrely"
- **THEN** one Google Play page or one share sheet opens, as before (EC-SHR-05)

#### Scenario: Rotation or process death while away
- **WHEN** the phone is rotated in the other app, or Android kills Migrely's process meanwhile, and the user returns
- **THEN** Settings shows with no message, as before (EC-SHR-06)

#### Scenario: No permission or manifest change
- **WHEN** the release build's merged manifest is checked
- **THEN** `:app:checkReleasePermissions` passes with the same permission list as `develop`, and the merged release
  manifest is identical to `develop`'s, so no `<queries>` entry is added
