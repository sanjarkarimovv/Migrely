# migrely.health

The website for Migrely (BRD D119). It is static: no scripts, cookies, analytics or third-party requests. Fonts and
images are served from the site itself, and the privacy policy is built from `docs/release/privacy-policy.md`.

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
- **VA page** (`/va-migraine-log/`) uses `urgent_help()` with the Veterans Crisis Line, `reviewed_block()` with
  `VA_DISCLAIMER`, and a `WebPage` JSON-LD. It quotes 38 CFR 4.124a (DC 8100), VA's Headaches DBQ and VA's
  representative FAQ word for word. Before updating `VA_REVIEWED`, re-read all three and check the Federal Register
  for a final rule under RIN 2900-AQ73.
- **Comparison** (`/compare/`, BRD D144) is data in `COMPARE_APPS`: only facts read from each app's own listing,
  permission list, Data safety, website or policy. A fact that can't be verified gets no row. Before updating
  `COMPARE_REVIEWED`, re-read every source on the cards; prices and Data safety change often.
- **CSP and PDFs:** `_headers` detaches the CSP from the two PDFs so browsers' PDF viewers can open them. After a
  deploy, open both on a phone and in Chrome, Safari and Firefox.
- **Stylesheet:** pages link `site.css?v=<hash>`, because `/assets/` is cached for a year. The hash changes with the
  file.
- **Google Play links** carry `utm_source=migrely.health`, `utm_medium=website` and `utm_campaign=<page>` inside
  `referrer` (BRD D137). Play Console shows them under the store listing's acquisition report, as UTM source and
  campaign.

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
   - the response headers include the `Content-Security-Policy` from `_headers`;
   - the pages show `support@` and `privacy@` as plain addresses, with no `email-decode` script;
   - an email from another mailbox to each address arrives.

To update the site:

1. Run `./gradlew :app:checkReleasePermissions`, then `python3 website/build.py`.
2. Commit `website/public` and `website/lastmod.json`.
3. Zip `website/public` again with its files at the root.
4. Deployments → **Create deployment** → upload.

## Google Play

- Privacy policy URL: `https://migrely.health/privacy/`
- Data deletion URL: `https://migrely.health/delete-data/`
- Website: `https://migrely.health`
- Contact email: `support@migrely.health`
