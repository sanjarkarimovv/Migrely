# Play links outside the listing

One table of every Google Play link we put outside the store listing, with its UTM tags (BRD D184; D137 for the site).
Checked 2026-11-16 against `website/public` at `origin/develop` 50c784e5.

## The scheme

`utm_source`, `utm_medium` and `utm_campaign` appear twice in each link: as top-level parameters (what Play Console
Help describes for deep links to a store listing) and again inside `referrer=` (Play's install referrer). It is not
confirmed that Console reads values inside `referrer=`, so every link carries both. Template, with `<s>`, `<m>`, `<c>`:

`https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=<s>&utm_medium=<m>&utm_campaign=<c>&referrer=utm_source%3D<s>%26utm_medium%3D<m>%26utm_campaign%3D<c>`

Keep the campaign list short: Play folds small values into "Other", and with the install band we have now most will
fold anyway. Detail (which story, which post) goes in our own log by date, not in the URL.

**What Play Console reports (Play Console Help "View acquisition reports", retrieved 2026-11-16):** only `utm_source` and
`utm_campaign` are dimensions, and the source only when the traffic filter is ads and referrals. `utm_medium`, `utm_term` and
`utm_content` are not reported. So `utm_medium` is for our own hygiene only, and anything we want to tell apart in the
Console must be in `utm_campaign`. Directories are the one place with a per-target value (`dir_<target>`, four values). The app and
the site collect nothing: attribution is only what Play Console shows. Structured data `downloadUrl` stays bare (D137).

## Table

Each URL was built by script and checked: top-level tags equal the decoded `referrer` tags, and the two live strings
(`/android/`, `/instagram/`) are reproduced byte for byte.

| Placement | utm_source | utm_medium | utm_campaign | State today | URL |
|---|---|---|---|---|---|
| Site: home `/` and `/llms.txt` | `migrely.health` | `website` | `home` | referrer-only: `home`, `llms` | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=migrely.health&utm_medium=website&utm_campaign=home&referrer=utm_source%3Dmigrely.health%26utm_medium%3Dwebsite%26utm_campaign%3Dhome` |
| Site: guide pages `/headache-diary/`, `/menstrual-migraine/`, `/medication-overuse-headache/`, `/va-migraine-log/`, `/compare/` | `migrely.health` | `website` | `guide` | referrer-only, one campaign per page today | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=migrely.health&utm_medium=website&utm_campaign=guide&referrer=utm_source%3Dmigrely.health%26utm_medium%3Dwebsite%26utm_campaign%3Dguide` |
| Site: `/privacy-check/` | `migrely.health` | `website` | `privacy-check` | referrer-only: `privacy-check` (kept as its own row: the proof page) | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=migrely.health&utm_medium=website&utm_campaign=privacy-check&referrer=utm_source%3Dmigrely.health%26utm_medium%3Dwebsite%26utm_campaign%3Dprivacy-check` |
| Site: `/about/`, `/privacy/`, `/support/`, `/delete-data/`, `/404.html` | `migrely.health` | `website` | `trust` | referrer-only, one campaign per page today | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=migrely.health&utm_medium=website&utm_campaign=trust&referrer=utm_source%3Dmigrely.health%26utm_medium%3Dwebsite%26utm_campaign%3Dtrust` |
| `/android/` (paid ad landing, live) | `instagram` | `paid_social` | `ig-ads-test1` | LIVE, both forms. Keep exactly as is | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=instagram&utm_medium=paid_social&utm_campaign=ig-ads-test1&referrer=utm_source%3Dinstagram%26utm_medium%3Dpaid_social%26utm_campaign%3Dig-ads-test1` |
| `/instagram/` (bio link page, live) | `instagram` | `social` | `bio` | LIVE, both forms, since 2026-11-16 | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=instagram&utm_medium=social&utm_campaign=bio&referrer=utm_source%3Dinstagram%26utm_medium%3Dsocial%26utm_campaign%3Dbio` |
| YouTube video description | `youtube` | `social` | `video_desc` | planned | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=youtube&utm_medium=social&utm_campaign=video_desc&referrer=utm_source%3Dyoutube%26utm_medium%3Dsocial%26utm_campaign%3Dvideo_desc` |
| Instagram story link sticker | `instagram` | `social` | `story` | planned (scheme in `account.md`) | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=instagram&utm_medium=social&utm_campaign=story&referrer=utm_source%3Dinstagram%26utm_medium%3Dsocial%26utm_campaign%3Dstory` |
| Instagram highlight with a link sticker | `instagram` | `social` | `highlight` | planned | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=instagram&utm_medium=social&utm_campaign=highlight&referrer=utm_source%3Dinstagram%26utm_medium%3Dsocial%26utm_campaign%3Dhighlight` |
| Instagram keyword DM reply | `instagram` | `social` | `dm` | planned | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=instagram&utm_medium=social&utm_campaign=dm&referrer=utm_source%3Dinstagram%26utm_medium%3Dsocial%26utm_campaign%3Ddm` |
| Collab partner's story or bio | `instagram` | `social` | `collab` | planned | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=instagram&utm_medium=social&utm_campaign=collab&referrer=utm_source%3Dinstagram%26utm_medium%3Dsocial%26utm_campaign%3Dcollab` |
| In-app Settings, Share Migrely | `app` | `share` | `settings_share` | pm-loops, not built | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=app&utm_medium=share&utm_campaign=settings_share&referrer=utm_source%3Dapp%26utm_medium%3Dshare%26utm_campaign%3Dsettings_share` |
| Doctor PDF footer (clickable link only; the visible text is plain `migrely.health`) | `app` | `pdf` | `doctor_pdf` | pm-loops, optional | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=app&utm_medium=pdf&utm_campaign=doctor_pdf&referrer=utm_source%3Dapp%26utm_medium%3Dpdf%26utm_campaign%3Ddoctor_pdf` |
| Outreach email to a directory or organisation, Healthline | `directory` | `email` | `dir_healthline` | planned, Part B | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=directory&utm_medium=email&utm_campaign=dir_healthline&referrer=utm_source%3Ddirectory%26utm_medium%3Demail%26utm_campaign%3Ddir_healthline` |
| Outreach email, Migraine & Headache Australia | `directory` | `email` | `dir_mha` | planned, Part B | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=directory&utm_medium=email&utm_campaign=dir_mha&referrer=utm_source%3Ddirectory%26utm_medium%3Demail%26utm_campaign%3Ddir_mha` |
| AlternativeTo page (link in the description or website field, if it accepts a Play link) | `directory` | `listing` | `dir_alternativeto` | planned, Part B | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=directory&utm_medium=listing&utm_campaign=dir_alternativeto&referrer=utm_source%3Ddirectory%26utm_medium%3Dlisting%26utm_campaign%3Ddir_alternativeto` |
| Healthify NZ screening form (the Play link we are asked for) | `directory` | `listing` | `dir_healthify` | planned, Part B | `https://play.google.com/store/apps/details?id=com.migrely.app&utm_source=directory&utm_medium=listing&utm_campaign=dir_healthify&referrer=utm_source%3Ddirectory%26utm_medium%3Dlisting%26utm_campaign%3Ddir_healthify` |

## What needs a site PR and an owner deploy

- **Site rows (the first four).** Today every site Play link is `referrer=` only, and each page has its own campaign
  (13 values). The change: add the top-level `utm_*` parameters to `play_url()` in `website/build.py`, and fold the
  page campaigns into `home`, `guide`, `privacy-check` and `trust` (the table). The price of folding: Console can no
  longer say which guide page converted. At our traffic every page campaign would be "Other" anyway. It amends D137, so
  it is its own OpenSpec change, and it goes live only on the owner's deploy. Nothing is deployed by this PR.
- **No site change:** `/android/` (a live ad uses it, do not touch), `/instagram/` (live), YouTube, stories, DMs,
  collabs, directories. These need only the owner to paste the link.
- **App change (`pm-loops`):** the share link and the PDF footer. The footer's visible text is plain `migrely.health`; a
  clickable link is optional. No `&listing=` in either (below).

## Play Console items for later (owner steps, nothing is done)

Custom store listings. Source: Play Console Help "Custom store listings", retrieved 2026-11-16 (the page has no date).

- Up to 50 per app. Each may change name, icon, descriptions and graphics; contact details, privacy policy and
  category stay shared. Targets: country, ads traffic, **search keywords** ("users who find the app through selected
  Play Search terms"), or a unique URL `...details?id=com.migrely.app&listing=<parameter>` (lowercase letters, digits
  and `.-_~`). The default listing must be published first (it is, since 2026-11-13).
- The help text excludes only churned and lapsed segments for new apps, so keyword and URL targets look available for
  a new app.
- **Unverified:** that the Console offers them to this account today; any minimum traffic for keyword targeting; when
  Google starts showing a keyword listing; any measured effect on rank or conversion (no published evidence). A listing
  edit goes through review like any other.
- Use if wanted: a listing for the search keyword "migraine diary" that leads with the diary and the doctor report,
  and `&listing=` URLs per channel (site, bio, PDF) for per-channel conversion data. Not planned now: with 1 install
  band there is no traffic to compare, and the default listing is still being fixed (D184). Revisit after 4 weeks of
  data. The listing text stays under the same Metadata and health rules, disclaimer included.
- Store listing experiments need traffic we do not have; expect "More data needed".
