# migrely.health

The website for Migrely (BRD D119). It is static: no scripts, cookies, analytics or third-party requests. Fonts and
images are served from the site itself, and the privacy policy is built from `docs/release/privacy-policy.md`.

## Build

```
python3 website/build.py
```

This writes `website/public/`. It needs Pillow and `cwebp`. The images come from `docs/release/store/` and
`docs/release/icon/`.

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
   - `https://migrely.health`, `/privacy/`, `/support/` and `/delete-data/` answer 200, and a missing page answers 404;
   - `www` and `http` redirect with 301;
   - the response headers include the `Content-Security-Policy` from `_headers`;
   - the pages show `support@` and `privacy@` as plain addresses, with no `email-decode` script;
   - an email from another mailbox to each address arrives.

To update the site, run `python3 website/build.py`, zip `website/public` again with its files at the root, then
Deployments → **Create deployment** → upload.

## Google Play

- Privacy policy URL: `https://migrely.health/privacy/`
- Data deletion URL: `https://migrely.health/delete-data/`
- Website: `https://migrely.health`
- Contact email: `support@migrely.health`
