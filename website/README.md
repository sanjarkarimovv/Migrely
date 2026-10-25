# migrely.health

The website for Migrely (BRD D119). It is static: no scripts, cookies, analytics or third-party requests. Fonts and
images are served from the site itself, and the privacy policy is built from `docs/release/privacy-policy.md`.

## Build

```
python3 website/build.py
```

This writes `website/public/`. It needs Pillow and `cwebp`. The images come from `docs/release/store/` and
`docs/release/icon/`.

## Deploy to Cloudflare Pages (first time, about 10 minutes)

1. **Domain:** buy `migrely.health` in Cloudflare (Domain Registration → Register Domains). If it was bought elsewhere,
   add the site to Cloudflare and point the registrar's nameservers to the two Cloudflare gives.
2. **Create the project:** Workers & Pages → Create → Pages → **Upload assets**. Name it `migrely`, then drag the
   `website/public` folder in and deploy.
3. **Connect the domain:** in the project, Custom domains → **Set up a custom domain** → `migrely.health`. Add
   `www.migrely.health` too, and redirect it to the bare domain (Rules → Redirect Rules, or a second custom domain on
   the same project).
4. **Set up email:** in the domain, Email → Email Routing → enable it. Add routes `support@migrely.health` and
   `privacy@migrely.health`, each to the owner's Gmail, and confirm the Gmail address.
5. **Check:**
   - `https://migrely.health`, `/privacy/`, `/support/`, `/delete-data/` all open;
   - an email to support@ arrives;
   - the response headers include the `Content-Security-Policy` from `_headers`.

To update the site, rebuild, then Deployments → Create deployment, and upload `website/public` again.

## Google Play

- Privacy policy URL: `https://migrely.health/privacy/`
- Data deletion URL: `https://migrely.health/delete-data/`
- Website: `https://migrely.health`
- Contact email: `support@migrely.health`
