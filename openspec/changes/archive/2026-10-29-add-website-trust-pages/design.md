# Design

## Context

migrely.health (BRD D119) is built by `website/build.py` into `website/public/`. It is static, sends nothing anywhere
and runs under a CSP of `default-src 'none'` with `script-src 'none'`. JSON-LD data blocks are not scripts the browser
runs, so they are allowed. The launch plan positions Migrely as "private by proof". The proof is the app's missing
internet permission, which the build already enforces (`checkReleasePermissions`, issue #36). It writes the final list
to `app/build/reports/permissions/release.txt`.

The release build asks for five permissions:

- `POST_NOTIFICATIONS`;
- `RECEIVE_BOOT_COMPLETED` and `WAKE_LOCK`, from WorkManager;
- `com.android.vending.BILLING`;
- `com.migrely.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`, from AndroidX core.

The rules are BRD D133–D137 and EC-WEB-01…06.

## Goals and non-goals

**Goals:**
- Let a visitor check Migrely's privacy claim in about two minutes, without trusting us.
- Answer "who is behind this, and how do they make money?" with facts.
- Give search and answer engines clean signals: question headings, organization data and honest sitemap dates.
- See in Play Console how many installs come from the site, without tracking anyone.

**Non-goals:**
- analytics, cookies or any script on the site;
- marketing claims that cannot be checked;
- the content pages of later tasks.

## Decisions

### D1. Where each claim comes from

Every sentence on the two pages comes from a source in the repo or a public reference:

| Claim | Source |
|---|---|
| Publisher Sulton UzDev; support@ and privacy@ | footer, privacy policy, Play listing |
| No internet permission; what Android, Google Play and shared files still do | `privacy-policy.md`, manifest |
| Premium adds the doctor report and Insights; the trial and the plans | home page and support FAQ (BRD §7) |
| Logging, history, medications, reminders, export and safety warnings are free | home page; BRD §7: safety is never Premium |
| Medication limits: 15 days for simple painkillers, 10 for the rest, over 30 days | BRD MED-F5 and D45; ICHD-3 8.2 |
| Data safety: nothing collected, nothing shared | `docs/release/data-safety.md` |
| The permissions and why each is there | release permission report; the comments in the app manifest |

The pages say nothing about the people who make Migrely beyond that: no story, team size, location or credentials.
Under "What will Migrely never do?" the page lists the product rules that already bind the app:
- no internet access (D44 and the build check);
- no account;
- no ads or tracking code (§11.6, D101);
- no safety warning behind Premium (§7).

### D2. The privacy check

The page puts the check before the explanation:

1. **On the phone.** Settings → Apps → Migrely → Permissions → ⋮ → All permissions, verified on the API 36 emulator
   with the labels Android shows there. Phone makers rename things, and the page says so (EC-WEB-02).
2. **Airplane mode.** It shows that Migrely does not need the internet. Only the permission list shows that it cannot
   use it, and the page says this plainly.
3. **For developers.** `adb shell dumpsys package com.migrely.app`, with its "requested permissions" section.

Then each permission, in a list of cards rather than a table so that 320 px works. The emulator showed Android's
labels for the first three. It lists neither BILLING (Play Store, which defines it, is not on the emulator) nor the
AndroidX permission, so those two get descriptive titles instead:

| Shown as | Permission | Why |
|---|---|---|
| “show notifications” | `POST_NOTIFICATIONS` | reminders and "Did it help?"; Android asks first |
| “run at startup” | `RECEIVE_BOOT_COMPLETED` | WorkManager puts reminders back after a restart |
| “prevent phone from sleeping” | `WAKE_LOCK` | WorkManager keeps the phone awake while a reminder runs |
| Google Play billing | `com.android.vending.BILLING` | asks the Play Store app about Premium, on the phone |
| An internal AndroidX permission | `…DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` | only Migrely may message its own parts |

The labels in quotes are Android 16's own, written as the emulator shows them, in lower case. A networked app
(Chrome) shows “have full network access” in the same list. Then comes "What can still leave your phone": Android's backup,
Google Play and shared files, in the privacy policy's words, and a link to it.

**Drift guard (EC-WEB-01).** `PERMISSIONS` in `build.py` holds the five entries. `check_permissions()` reads the
release report. If the sets differ, it raises `SystemExit` with both lists. If the report is missing, it prints how to
create it (`./gradlew :app:checkReleasePermissions`) and goes on, so the site can still be built on a machine without
an app build. The release report is the right source because it is what Play ships. The debug report is the same today
but may differ later.

### D3. Structured data

| Page | JSON-LD |
|---|---|
| `/` | `@graph`: `Organization` (`@id` `/#organization`, name Migrely, url, logo `icon-512.png`, email, `ContactPoint` customer support), `WebSite` (`@id` `/#website`, publisher → organization), `MobileApplication` as before (publisher Sulton UzDev, as on Play) |
| `/about/` | `AboutPage` with url, name and `about`: the same `Organization` object |
| `/support/` | `FAQPage` as before |
| `/privacy-check/` | none. HowTo rich results were retired in 2023 |

The `Organization` is one constant, so both pages publish the same object. `sameAs` is added once the T05 profiles
exist (EC-WEB-06). Until then the key is absent, not empty.

### D4. `lastmod`

`website/lastmod.json` maps each sitemap URL to `{"sha256", "lastmod"}`. `main()` renders every page and then:

- hashes each page's HTML;
- keeps the stored date when the hash is unchanged, and otherwise sets today's date;
- writes the JSON back with sorted keys and a final newline, so an unchanged build leaves no diff;
- writes the sitemap from those dates.

The 404 page is not in the sitemap. A menu or footer change touches every page's HTML, so every date moves. That is
correct, because the pages did change.

### D5. Tagged Play links

`play_link(campaign)` returns `PLAY + '&referrer=' + quote('utm_source=migrely.health&utm_medium=website&utm_campaign='
+ campaign, safe='')`. This is the format of Google's Play URL builder: the tags live in `referrer`, which the Play
Store reads. `page()` derives the campaign from the path: `/` is `home`, and otherwise the path's name (`about`,
`privacy-check`, `support`, `delete-data`, `privacy`, `404`). It passes the campaign to the footer link, and each body
passes it to its own buttons. `href`s go through `esc()`, so `&` becomes `&amp;`. `downloadUrl` in the JSON-LD stays the
plain store URL, because it identifies the app rather than a visit.

### D6. Styles

`site.css` gains only what the pages need, using the existing colour tokens, which pass AA in both themes:

- `code`, in the system monospace font on `--violet-soft`;
- `pre`, which wraps (`white-space: pre-wrap`);
- `.perms`, a grid of cards like `.features`, with `minmax(min(240px, 100%), 1fr)` so it is one column on a small
  phone;
- `overflow-wrap: anywhere` on `body`.

The walk at 320 px with every font size doubled showed that long words pushed every page sideways, the old ones too:
`support@migrely.health`, and headings at their 200% minimum size. Grid items cannot shrink below their longest word.
`overflow-wrap: anywhere` lets such a word wrap, and only when it does not fit, so normal sizes look the same.
Permission names get `<wbr>` after each dot and underscore (`breakable()`), so they wrap between their parts and not
inside a word.

## Risks

- **Play Console's UTM handling.** Google's help page names the dimensions but does not show the link format. The
  format here is the URL builder's, and the most common. If Play Console shows nothing or "Other" after real traffic,
  the cost is a missing number, not broken links. Check it at the day-30 review (T22).
- **Settings paths differ between phone makers.** The page says so and offers `adb` as the universal check. The Play
  Store's own permission list is not offered, because it could not be checked here: the emulator has no Play Store.
- **A stale permission report.** If the app adds a permission and the site is built without a new app build, the old
  report passes. The README tells whoever builds the site to run `:app:checkReleasePermissions` first. The release
  pipeline runs it anyway.
- **`lastmod.json` not committed.** Then the next build dates pages from the old hashes. The README and the PR review
  catch it, and the effect is a wrong date, not a broken site.
