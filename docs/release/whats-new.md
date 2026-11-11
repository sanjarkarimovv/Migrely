# What's new (Play release notes)

Each release's text for Play Console › Create new release › Release notes. Play allows at most 500 characters per
language. en-US is the default, and other languages fall back to it.

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
