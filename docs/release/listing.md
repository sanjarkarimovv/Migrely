# Play store listing

The listing text for Google Play (BRD §14.2, D104, D121). The keyword plan and the rules it follows are under
[Keywords](#keywords) and [Rules](#rules).

## App details

| Field | Value |
|---|---|
| App name (≤ 30) | Migrely: Migraine Tracker |
| Short description (≤ 80) | Private migraine diary & headache log. Track triggers and painkiller days. |
| Category | Health & Fitness. Migrely is a diary, not a medical device (D104). |
| Tags | Health & fitness, Medical |
| Contact email | support@migrely.health |
| Website | https://migrely.health |
| Privacy policy URL | https://migrely.health/privacy/ |
| Ads | No |
| In-app purchases | Yes: Migrely Premium, a yearly or monthly subscription or a one-time lifetime purchase (`play-products.md`) |
| Target audience | 18 and over (BRD §11.5) |

## Full description (≤ 4000)

```text
Migrely is a private migraine tracker and headache diary. Log an attack in three taps, even in a dark room, see each month drawn as a night sky, and take a clear report to your doctor.

Log a Migraine in Seconds
• "Migraine started" in one tap, in the app or by long-pressing its icon
• Pain level, where it hurts, symptoms such as aura, nausea or sensitivity to light, possible triggers and notes, all optional
• Attack mode stays dark and simple, and keeps the time until the end is logged
• Add a past migraine, edit any entry, and undo a deletion

See Your Months
• A calendar where each day is a small sky, drawn only from what you logged
• Migraine days, attacks, medication days and pain-free days for every month
• Mark period days and pain-free days, so your headache log shows the whole picture

Keep an Eye on Medication
• Log what you took, and later say whether it helped
• A gentle note when painkillers or triptans reach the number of days a month linked to medication-overuse headache, so you can talk to your doctor about it
• Optional reminders: one to log your day, and one to say whether a medication helped

For Your Doctor (Premium)
• A PDF migraine diary report: a summary, a calendar and every migraine and medication, ready to share with your doctor or neurologist

Understand Your Patterns (Premium)
• Triggers often logged with your migraines, the days and hours they come, the days around your period for menstrual migraine, how you rated your medications, and the last 12 months

Migrely Premium
• Premium adds the doctor report and Insights, all through Google Play: yearly (a 7-day free trial for new subscribers, then paid), monthly, or a single lifetime payment. Subscriptions renew automatically and can be cancelled in Google Play at any time
• Logging, history, medications, reminders, backup and spreadsheet export, and every safety warning are free. Premium adds only the doctor report and Insights

Private by Design
• Migrely has no internet permission. On Android 13 and later you can check it yourself: in the app's settings under All permissions, "have full network access" is not listed. Premium is bought through Google Play, which goes online for the purchase. There is no account and no ads, and Migrely has no analytics or tracking code of its own
• Your entries stay on your phone. If Android's own backup is on, it may also copy them to your Google account
• Export a backup or a spreadsheet, or delete everything, whenever you like. A file you export goes where you send it

In English and German
• Migrely is available in English and German. It follows your phone's language or, on Android 13 and later, the app language you pick for it
• The PDF report and the spreadsheet export stay in English

Gentle on Your Eyes
• Dark by default, with a light theme, and always dark during a migraine
• Supports large text, TalkBack and keyboards

Safety First
• The warning signs, with when to call your local emergency number and when to get urgent medical advice, are always at hand in Settings

Migrely is a diary. It is not a medical device and does not diagnose, treat, cure or prevent any medical condition. Talk to a doctor or other healthcare professional about your headaches, and before you start, stop or change a medication. If a headache comes with any of the emergency signs on the Warning signs page, call your local emergency number.
```

The text is 3,392 characters (counted 2026-11-27 with the 1.4.0 additions below, the medical and legal changes and the permission wording). Paste it as it is: each paragraph is one line, because Play keeps line breaks.

## Changes for 1.4.0 (aso-140, 2026-11-27, proposed: nothing here is applied in Play Console)

| Change | Reason and evidence |
|---|---|
| New section "In English and German" (2 bullets) | The app is in German from 1.4.0 (D195). The bullets also say the PDF and the spreadsheet stay English (D195 (4), (5)), the same as the release notes. Medical text is not touched. |
| "Large text, TalkBack and keyboards work throughout" becomes "Supports large text, TalkBack and keyboards" | The 1.4.0 notes themselves say TalkBack moves in order "on most scrolling pages", so "throughout" says more than we can show. |
| Title, short description and first sentence stay | D184's short description has been live since 2026-11-24. One snapshot three days later (positions.log, 2026-11-27) shows "migraine tracker" #15, "headache tracker" #26 and "migraine app", "migraine diary", "headache diary" outside the top 29, so there is no sign yet that "migraine diary" helped, and one snapshot cannot say it hurt. One variable at a time: repeat on 2026-12-01 and 2026-12-08 before changing the name. |
| Disclaimer line and Premium bullet (verdicts 2026-11-27) | The last line now names the "Warning signs" page like the app (`settings_about_urgent`). The Premium bullet says the trial turns into a paid plan and that subscriptions renew and can be cancelled in Google Play. |
| Permission self-check (QA, `migrely-work/qa-140/aso-perm-check.md`) | "you can check that in Android's settings" held on Android 16 only. On Android 8 the Permissions row is greyed out ("No permissions requested"), probably up to Android 12. The line now limits the check to Android 13 and later and names the absent line. Migrely's list does show "view network connections" (reading whether a connection exists), which is not internet access. |
| Left unchanged on purpose | The share and rate rows and the doctor report's credit line are not in the listing: they sell nothing the listing needs, and "rate" must never sound like a reward. |

German listing (de-DE, DE, AT and CH): [`listing-de.md`](listing-de.md).

## Graphics

| Asset | File |
|---|---|
| Icon 512×512 | `icon/migrely-play-icon-512.png` |
| Feature graphic 1024×500 | `store/feature-graphic-1024x500.png` |
| Phone screenshots 1080×1920 | `store/screenshots/01…08`, uploaded in file-name order |
| Tablet screenshots (7" and 10") | the same eight phone screenshots |

Video, Chromebook and XR assets are left empty. Play's question about AI-generated assets is answered "Don't label
assets".

The first three screenshots show in search results, so they carry the three main reasons to install: logging in
seconds, the month as a sky, and the doctor report (D121). Insights, "Did it help?", privacy, history and the themes
follow. The composer is outside the repo (`migrely-work/store-art/compose.py`); a new caption means a new render.

## Keywords

Ranked from Play's own search suggestions on 2026-10-26. Play publishes no search volumes, so the order is an estimate.

| Rank | Phrase | Where it is |
|---|---|---|
| 1 | migraine tracker | name, first sentence of the description |
| 2 | headache tracker | "headache" in the short description, "tracker" in the name |
| 3 | migraine app | name and description words |
| 4 | migraine diary | short description (D184), "PDF migraine diary report" |
| 5 | headache diary | first sentence of the description |
| 6 | migraine log, headache log | short description ("headache log"), "your headache log" bullet |
| 7 | aura, triggers, menstrual migraine, medication-overuse headache | description only (no suggestions, so low volume) |

Position check (logged-out web search, 2026-11-16, `migrely-work/aso/positions.py`, one snapshot, 30 results): "migraine
tracker" #14, "migraine app" #14, "migraine diary" #26, "migraine diary app" not in top 30, "headache diary" #15,
"headache tracker" #19. That is the baseline before the D184 text, which went live on 2026-11-24. After it (2026-11-27):
"migraine tracker" #15, "headache tracker" #26, and "migraine app", "migraine diary", "migraine diary app" and "headache
diary" outside the top 29 or 30. Repeat on 2026-12-01 and 2026-12-08, and watch "migraine diary", the one phrase D184 adds.

Each phrase appears once or twice. Play treats repetition as keyword stuffing.

Left out: "migraine relief" (Migrely promises no relief), "weather" and "barometric pressure" (Migrely has no internet permission),
and competitors' names (§11.8).

## Outside links

Every Play link we use outside the listing, with its UTM tags: [`play-links.md`](play-links.md).

## Rules

From Play's metadata policy, preview-asset guidelines and health-app policy, checked 2026-10-26:

- **Name, icon and developer name:**
  - no emoji;
  - no ALL CAPS beyond the brand;
  - no "free", "best", "#1", "top", "new", "sale" or ranking claims.
- **Screenshots and feature graphic:**
  - no "free", "best", "#1", "new", "sale" or "download now";
  - the caption stays under 20% of the image;
  - at least four 1080px screenshots, which is what makes the app eligible for Play's recommendations.
- **Health:**
  - the description carries the "not a medical device ... does not diagnose, treat, cure or prevent" disclaimer;
  - it tells users to see a healthcare professional;
  - the medication-overuse note is information, not advice.
- **Description:**
  - no testimonials, no comparisons with other apps, no keyword lists;
  - it does not repeat the short description.

## Notes

- No competitor names and no superlatives (BRD §11.8).
- **Name check (TODO(user)):** published apps with similar names: "Migrelle - Migraine Tracker" (Google Play and App
  Store, 2026), "Migrella: Migraine Tracker" (App Store) and "Migru: Migraine Tracker" (Google Play). Search the USPTO
  and EUIPO databases for "Migrely" before the production launch.
- Premium features are the doctor report and Insights, marked "(PREMIUM)" above (BRD D108). Safety features are never
  premium (§7.1). The products and prices are in `play-products.md`.
- English (en-US) is the default listing. German (de-DE) follows with 1.4.0, which ships the German app: see
  `listing-de.md`. en-GB, en-AU, en-CA and en-IN listings may follow once the en-US listing has data (Store listing
  experiments). Other languages wait until the app itself is translated.
