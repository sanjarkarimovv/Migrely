# migrely.health

The website for Migrely (BRD D119). It is static, except one redirect Function that only answers 302 (BRD D196): no scripts, cookies or third-party
requests. Fonts and images are served from the site itself, and the privacy policy is built from `docs/release/privacy-policy.md`.

## Build

```
python3 website/build.py
```

This writes `website/public/`. It needs Pillow and `cwebp`. The images come from `docs/release/store/` and
`docs/release/icon/`.

- **The permission list on `/privacy-check/`** must match what the app ships (BRD D134, EC-WEB-01). The build compares
  it with `app/build/reports/permissions/release.txt` and stops when they differ, so run
  `./gradlew :app:checkReleasePermissions` first. Without the report it prints a note and skips the check.
- **`website/lastmod.json`** keeps each page's hash and the date its HTML last changed. The sitemap's `lastmod` comes
  from it (BRD D136). The build updates it, and it is committed with the site.
- **The headache diary template** (`/headache-diary/*.pdf`, BRD D139, D140) is made by
  `python3 website/make_diary_pdf.py`, which prints it with Google Chrome as A4 and US Letter (macOS path; set `CHROME`
  for another) and checks that each PDF has 6 pages. Run it when the template changes and commit the PDFs. `build.py`
  only checks that they exist. When the page's facts are checked again, update `DIARY_REVIEWED` in `build.py`, which
  dates both the page and the PDFs' footer, and remake the PDFs.
- **Medical pages** (`/headache-diary/`, `/medication-overuse-headache/`, `/menstrual-migraine/`) share
  `urgent_help()`, `reviewed_block()` and `medical_ld()`. Each has its own review date (`DIARY_REVIEWED`,
  `MOH_REVIEWED`, `MENSTRUAL_REVIEWED`): update it when the page's facts are checked against its sources again.
  Their facts come from fact packs in `marketing/research/2026-11-04-*-facts.md` (outside the repo). Each claim there
  has its source and an exact quote, so re-read those sources before moving a date (BRD D149).
  - Every question heading (H2 or H3) on these pages opens with a 40- to 60-word direct answer;
    `check_direct_answers()` stops the build otherwise. The app and privacy sections, "Sources" and the urgent-help
    section's three headings (the app's Warning signs wording) are exempt (`ANSWER_EXEMPT`).
  - A figure from one study or review names it in the sentence (EC-WEB-24). Where sources disagree on how to stop a
    medicine, the page leaves it to the doctor (EC-WEB-23).
  - `urgent_help()` and the diary PDF's box carry the app's Warning signs page word for word (BRD D171, #199); change
    them only when `settings_warning_signs_*` in the app changes, and send every changed word to `medical-reviewer`.
- **VA page** (`/va-migraine-log/`) uses `urgent_help()` with the Veterans Crisis Line, `reviewed_block()` with
  `VA_DISCLAIMER`, and a `WebPage` JSON-LD. It quotes 38 CFR 4.124a (DC 8100), VA's Headaches DBQ and VA's
  representative FAQ word for word. Before updating `VA_REVIEWED`, re-read all three and check the Federal Register
  for a final rule under RIN 2900-AQ73.
- **Comparison** (`/compare/`, BRD D144) is data in `COMPARE_APPS`: only facts read from each app's own listing,
  permission list, Data safety, website or policy. A fact that can't be verified gets no row. Before updating
  `COMPARE_REVIEWED`, re-read every source on the cards; prices and Data safety change often.
- **Guides** (BRD D145) are listed once in `GUIDES`: the home page's "Free guides" cards and every footer's "Guides"
  navigation come from it. Add a new guide there, with a summary that claims nothing the page doesn't. The build stops
  when a sitemap page can't be reached by following links from the home page (EC-WEB-19).
- **Promo video** (BRD D146): `/assets/video/` holds the home page's 30-second cut and its poster, named in
  `PROMO_VIDEO`. It plays from the site itself: never embed YouTube, which would load Google's scripts and cookies. The
  player has no autoplay and `preload="none"`, and `PROMO_SCENES` is its text alternative. The video is made in
  `marketing/video/promo-v3` (outside the repo) from real app screens. `/assets/` is cached for a year, so a new cut
  gets a new file name (EC-WEB-20). Update `PROMO_VIDEO`, `PROMO_UPLOADED` and `PROMO_SCENES` with it. The build stops
  when either file is missing. `_headers` allows `media-src 'self'` for it.
- **CSP and PDFs:** `_headers` detaches the CSP from the two PDFs so browsers' PDF viewers can open them. After a
  deploy, open both on a phone and in Chrome, Safari and Firefox.
- **Stylesheet:** pages link `site.css?v=<hash>`, because `/assets/` is cached for a year. The hash changes with the
  file.
- **Google Play links** carry `utm_source=migrely.health`, `utm_medium=website` and `utm_campaign=<page>` inside
  `referrer` (BRD D137). Play Console shows them under the store listing's acquisition report, as UTM source and
  campaign. The link in `/llms.txt` uses the campaign `llms`.
- **Answers** (BRD D147): `WHAT_IS` is the home page's 40–60-word definition and `/llms.txt`'s summary. `HOME_FAQ`
  holds the questions asked before installing (home page) and `FAQ` those asked after (`/support/`). Each answer
  starts with the direct answer, and both lists are published as `FAQPage`. A question may sit in only one list, or the
  build stops (EC-WEB-21). `PREMIUM_PRICES` feeds `/compare/`, `/support/` and `/llms.txt`.
- **Structured data and breadcrumbs** (BRD D147):
  - every inner page gets its JSON-LD from `page_ld()`: the `Organization`, the page node and its `BreadcrumbList`;
  - the visible "Home › page" trail and the `BreadcrumbList` both take the page's name from `GUIDES` or `TRUST_PAGES`;
  - `CHECK_STEPS` and `DELETE_STEPS` are both the visible steps and the `HowTo`.
  - Add `reviewedBy`, a named `author` or `aggregateRating` only when the page shows the same reviewer, person or real
    Play rating.
- **Official profiles** (BRD D150): `SOCIAL_PROFILES` (name, URL) feeds the `Organization`'s `sameAs`, a plain footer
  link with `rel="me"` and `/llms.txt`. Add a profile only when it is Migrely's own, public and presented as Migrely.
  An empty list removes all three.
- **`/llms.txt`** (llmstxt.org) is built from `WHAT_IS`, the app's facts, `GUIDES` and `TRUST_PAGES`. The build stops
  when a sitemap page is missing from it, so a new trust page goes into `TRUST_PAGES`.
- **Android version:** the site says "Android 8 and later" (`MIN_SDK`, `MIN_ANDROID`). The build stops when
  `minSdk` in `gradle/libs.versions.toml` differs (EC-WEB-22).
- **Languages** (BRD D148): the site is in English, like the app. A language gets its own pages, with `hreflang`, only
  once the app ships in that language and a native-speaking clinician has checked the medical text.

## How it is deployed

Live since 26 October 2026:

- **Domain:** `migrely.health`, registered at Porkbun with auto-renew, the registrar lock and WHOIS privacy on. It
  expires on 26 October 2027 and renews at about $62 a year.
- **DNS:** Cloudflare, Free plan; Porkbun's nameservers are replaced by Cloudflare's two.
- **Site:** Cloudflare Pages project `migrely`, direct upload, with `migrely.health` and `www.migrely.health` as custom
  domains.
- **Email:** Cloudflare Email Routing; `support@` and `privacy@migrely.health` go to the owner's Gmail.

## Deploy from scratch (about 20 minutes)

1. **Domain:** buy `migrely.health` at a registrar. Turn on auto-renew, and confirm ICANN's contact email within 15
   days, or the domain is suspended.
2. **Cloudflare:** add the domain to a Cloudflare account on the Free plan.
   - The scan imports the registrar's parking records (A records to its parking IPs, and `*` and `www` CNAMEs). Delete
     them all.
   - At the registrar, replace its nameservers with the two Cloudflare gives. DNSSEC must be off there.
3. **The project:** Workers & Pages → Create, then **"Continue to Pages"** (the Pages workflow, not "Upload your static
   files", which makes a Worker).
   - Choose **Upload assets** and name it `migrely`.
   - Upload `website/public` as a zip, with its files at the zip's root (`index.html`, `_headers`, …), and deploy.
   - Then deploy once with the update recipe below (wrangler, with `functions/`); `/go/android-t2` must answer 302.
   - Pages applies `_headers` and serves `404.html` for missing pages.
4. **Turn off Email Address Obfuscation** (search for it in the dashboard; under Security → Settings, or Scrape
   Shield). If it is on, Cloudflare rewrites every address on the site into "[email protected]" and adds a script that
   the site's CSP blocks, so visitors and Google Play's reviewers see no contact address.
5. **Domains:** in the project, Custom domains → **Set up a custom domain**.
   - Add `migrely.health`, then `www.migrely.health`.
   - Then Rules → Redirect Rules → the template **"Redirect from WWW to root"** (301, "Preserve query string" on). If
     Cloudflare warns that `www` may not be proxied, deploy anyway: the Pages custom domain proxies it.
6. **Email:** Email Service → Email Routing → **Onboard Domain** → `migrely.health`, and let it add its MX and SPF
   records.
   - Add the owner's Gmail as a destination and verify it.
   - Create the routes `support@` and `privacy@migrely.health` to it. Leave catch-all off.
7. **DMARC:** add a TXT record `_dmarc` with `v=DMARC1; p=reject; sp=reject; adkim=s; aspf=s;`. The domain receives mail
   only, so nobody may send as it.
8. **Check:**
   - `https://migrely.health`, `/about/`, `/privacy-check/`, `/headache-diary/` (and its two PDFs),
     `/medication-overuse-headache/`, `/menstrual-migraine/`, `/va-migraine-log/`, `/compare/`, `/privacy/`,
     `/support/` and `/delete-data/` answer 200,
     and a missing page answers 404;
   - `www` and `http` redirect with 301;
   - `https://migrely.health/go/android-t2` answers 302 with the Play link;
   - the response headers include the `Content-Security-Policy` from `_headers`;
   - the pages show `support@` and `privacy@` as plain addresses, with no `email-decode` script;
   - an email from another mailbox to each address arrives.

**The tap counter (BRD D196).** `website/functions/go/android-t2.js` is the project's only Pages Function. It answers
`302` to the test-2 Play link and reads, stores and logs nothing; the count is the project's "Functions metrics" in the
Cloudflare dashboard (Requests, up to three months). A dashboard zip upload does **not** compile `functions/`
(Cloudflare, "Direct Upload", read 2026-11-23), so the recipe below uses wrangler only.
**Deploy-day steps for the counter (BRD D196).** The branch is not merged into `develop` before deploy day: on that day the main session sets the date, merges and the owner deploys from that merge. (1) before anything, screenshot Cloudflare → Workers & Pages → migrely → Settings, showing Functions / Logs and any Logpush job; (2) the main session sets the policy's `Effective date:` in `docs/release/privacy-policy.md` to the deploy day in one commit, then regenerates `privacy-policy.html`; (3) build with `SITE_DEPLOY=1 python3 website/build.py`, which stops on an older date; (4) deploy with wrangler from the folder that holds `functions/`; (5) `curl -I /go/android-t2` must be 302. The date check runs only under `SITE_DEPLOY`: after the date commit, builds with and without it pass on that day; a `SITE_DEPLOY` build dated before 28 November 2026 (`T2_POLICY_FROM`, the day after the last live date) or after today stops. The committed date, 27 November 2026, is the live policy's date without the counter paragraph, so it now FAILS under `SITE_DEPLOY=1`: the deploy-day date commit is enforced, and the earliest deploy day is 28 November 2026. On a later day an old date from 28 November on still passes.
To go back to a direct Play link, set `T2_COUNT = False` in `build.py` and delete `website/functions/`.

To update the site (one recipe; a dashboard zip upload is not used, it cannot compile `functions/`):

1. Run `./gradlew :app:checkReleasePermissions`, then `python3 website/build.py` (`SITE_DEPLOY=1 python3 website/build.py`
   for a deploy build: with the counter on it refuses a policy date before 28 November 2026 or after today).
2. Commit `website/public` and `website/lastmod.json`.
3. In a clean folder put `site/` (a copy of `website/public`, committed bytes) and `functions/` (a copy of
   `website/functions`). From that folder run
   `CLOUDFLARE_ACCOUNT_ID=<account> npx wrangler@4.158.0 pages deploy site --project-name migrely --branch main --commit-hash <sha>`
   (the login is the owner's click). The Pages project's other deploy steps are in the owner's launch notes.
4. After every deploy run `curl -I https://migrely.health/go/android-t2`: it must answer `302` with the Play link.
   A deploy without `functions/` answers 404 and leaves `/android-t2/` with dead buttons.

## Google Play

- Privacy policy URL: `https://migrely.health/privacy/`
- Data deletion URL: `https://migrely.health/delete-data/`
- Website: `https://migrely.health`
- Contact email: `support@migrely.health`
