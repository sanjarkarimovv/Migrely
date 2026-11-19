# What's new (Play release notes)

Each release's text for Play Console › Create new release › Release notes. Play allows at most 500 characters per
language. en-US is the default, and other languages fall back to it.

## 1.3.2 (versionCode 14)

The text covers `develop` f70b6450 since `v1.3.1`: D190 (#237), D191 (#239) and D192 (#245). `aso-seo-specialist` (aso-132)
drafted it. `medical-reviewer` (med-132) checked it on 2026-11-19 against f70b6450 and narrowed it to what the code does:
"Medication taken" is the screen's title (the Home link is "Log medication"); the first bullet names the "Couldn't save"
message; the second says Name is brought into view; and the top bar steps aside only "if needed", because the trigger is the
measured room, not a font size. It does not say "medically reviewed" or "approved" (the clinician sign-off, T31, is open). The
text is 497 of 500 characters.

```
<en-US>
Fixes on medication screens and with large text:
• "Medication taken": a successful log now clears an earlier "Couldn't save" message, so it no longer covers what you logged.
• Edit medication: Save with an empty name now brings Name into view, and TalkBack says "Enter a name."
• Very large text in landscape: the line you are typing stays above the keyboard in a migraine's details, when logging or editing a medication, and in Delete all data. If needed, the top bar steps aside while you type.
</en-US>
```

## 1.3.1 (versionCode 13)

The text covers `develop` 9075aff8 since `v1.3.0`: D182 (T64) and D189 (#238, T74). `medical-reviewer` (med-131)
checked the text on 2026-11-18 against 9075aff8 and narrowed it to what the code does: the keyboard bullet names the
four screens, the message bullet does not claim every message, and the larger buttons are only on Home during a
migraine and on "Medication taken" opened from it. The Undo limit (D182 (3)) is not announced. It does not say
"medically reviewed" or "approved" (the clinician sign-off, T31, is open). The text is 494 of 500 characters.

```
<en-US>
Easier with TalkBack and "Time to take action":
• Closing the keyboard in a migraine's details, when logging or editing a medication, or in Delete all data no longer moves the page or pulls TalkBack off the field you were typing in.
• Messages such as "Saved." now follow Android's "Time to take action" setting and pause while TalkBack or keyboard focus is on them.
• Between "Migraine started" and "Migraine ended", message buttons on Home and on "Medication taken" opened from it are larger.
</en-US>
```

## 1.3.0 (versionCode 12)

The text covers `develop` e46f2b22 since `v1.2.1`: D177, D178, D180, D181 and D183. T64 (D182) is not in 1.3.0.
`medical-reviewer` checked the text on 2026-11-16, word for word against e46f2b22; bullets 1 and 4 changed on its
verdict. `legal-finance-analyst` passed the privacy bullet. It does not say "medically reviewed" or "approved" (the
clinician sign-off, T31, is open). Fixes that need no action from users (D172 to D176) are not announced. The text is
494 of 500 characters.

```
<en-US>
New:
• The daily reminder, if held up more than an hour past its time, now arrives quietly.
• While you type a note or a medication name, the line you are typing stays above the keyboard and the top bar with Back stays in place.
• Clearer privacy wording: no internet permission, how Premium is bought, what Android's own backup may copy.
Fixes:
• A goal on the first setup screen now reads "Keep track of my medication days".
• After a failed save, the next save that works replaces the error.
</en-US>
```

## 1.2.1 (versionCode 11)

Replaces 1.2.0, which was tagged but never uploaded (D170). Users never saw 1.2.0. 1.2.1 also rewrote the in-app
Warning signs page to follow the NHS emergency and urgent lists (D171, #194), so the notes now say so. They were
written for 1.2.0 and changed after the `v1.2.1` tag: the copy of this file in the tag is the 1.2.0 text, and the owner
pastes the notes from `develop` at upload. `medical-reviewer` checked the text on 2026-11-11, word for word against the v1.2.1
code and BRD D168, D169 and D171. It does not say "medically reviewed" or "approved" (the clinician sign-off, T31, is
open). 1.2.1 also keeps the diary and reminders
across an update after "Clear storage" on Android 8.0 (#193, #189). That fix is not announced: it needs no action from
users. The text is 496 of 500 characters.

```
<en-US>
New:
• Doctor visit: set your next visit's date in Reminders. Two days before, Migrely reminds you to make a report for your doctor (Premium).
• Lock screen reminders no longer name a medication or say that you logged a migraine.
• Settings, Warning signs, now follows NHS lists: when to call your local emergency number and when to get urgent medical advice.
Fixes:
• A quick second tap on Remove in a tag's menu no longer selects what was under it.
• A message no longer covers the safety card.
</en-US>
```

## 1.2.0 (versionCode 10), not published

Superseded by 1.2.1 (D170): tagged `v1.2.0`, never uploaded. Kept for the record.

The doctor visit reminder (D168), private lock screen notifications (D169) and the fixes for #161 (D164) and
#166, #167 (D165).

```
<en-US>
New:
• Doctor visit: set your next visit's date in Reminders. Two days before, Migrely reminds you to make a report for your doctor. The Doctor report can cover the time since your last visit.
• Lock screen reminders no longer name a medication or say that you logged a migraine.
Fixes:
• A quick second tap on Remove in a custom tag's menu no longer selects what was under it.
• On the Migraine screen, a message no longer covers the safety card, and "Couldn't save" clears once a save works.
</en-US>
```

## 1.1.1 (versionCode 9)

The hotfix for Google's rejection of 1.0.1 (D166) and the safety card (D167).

```
<en-US>
Fixes:
• With no internet connection, Premium's buttons now tell you you're offline instead of opening Google Play.
• Restore purchases no longer says no purchases were found when you're offline.
• The safety card after a first pain of 10 now scrolls into view.
Migrely now checks whether the phone is online ("view network connections") before opening Google Play. It still has no internet permission.
</en-US>
```

## 1.1.0 (versionCode 8)

1.0.2 was tagged but never shipped, so 1.1.0's notes cover everything since 1.0.1.

```
<en-US>
Fixes and improvements:
• A quick double tap is far less likely to press a second button by mistake, on a new screen, in a dialog or as one closes.
• Screens change faster.
• The Premium page says that purchases need Google Play and an internet connection. Your diary works offline.
• Clearer wording in History's medication summary and the Doctor report.
• Fixes for the date picker, Insights, the app shortcut and Undo.
</en-US>
```

## 1.0.2 (versionCode 7)

```
<en-US>
Fixes and accessibility improvements:
• The date picker fits in landscape, and the Doctor report's dates can be picked at large text sizes.
• A purchase finished while the app was closed shows at once.
• Insights always shows the period you chose.
• Starting a migraine from the app shortcut always shows the migraine screen.
• Undo is no longer offered for a migraine deleted before the phone's clock was set back.
</en-US>
```

## 1.0.1 (versionCode 6)

The first Production release. Its notes listed the 1.0.1 hotfixes. Because this was the first release, they read
oddly and were left as they were, so that the review would not restart (D126).
