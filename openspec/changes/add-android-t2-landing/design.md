# Design

## Context

The site is static, on Cloudflare Pages (project `migrely`, deployed with the wrangler CLI). The Pages Free plan counts
no path: zone analytics show Requests, Bandwidth, Unique Visitors and Requests by country, and the Path and Query filters
are Pro and above (Cloudflare, "Zone analytics", read 2026-11-23). `/assets/*` is immutable for a year; a changed
asset takes a new name.

## Decisions

| # | Decision | Why |
|---|---|---|
| 1 | Count with a Pages Function and read the count in Pages "Functions metrics" | The metrics "aggregate request data for an individual Pages project" and keep "up to three months" (Cloudflare, "Functions metrics", 2026-11-23). It needs four lines of code and stores nothing of ours |
| 2 | Not a static `_redirects` rule | It redirects, but Free plan analytics cannot count one path |
| 3 | Not a Worker with KV, D1 or Analytics Engine | More code, and a store of ours that holds a number the dashboard already holds |
| 4 | The Function must be the project's only one | The metric is per project, so a second Function would be counted with the first. The build stops on any other file under `functions/` |
| 5 | The Function reads nothing from the request | No method, IP, user agent, `Referer`, cookie, URL or body; no `console`, `fetch`, storage, import or `waitUntil`. An allowlist: the build compares every non-comment line with the expected code, so anything extra stops it |
| 6 | Response headers `Cache-Control: no-store`, `Referrer-Policy: no-referrer`, `X-Robots-Tag: noindex` | `_headers` does not apply to a Function's response; `no-store` keeps an edge copy from answering without the Function (and so from being counted) |
| 7 | Every request gets the same 302, whatever its method | "Unconditional" is then literally true (no branch at all, which also answers Meta's cloaking rules). The count therefore includes HEAD, POST and prefetch (a limit, below) |
| 8 | Wrangler's generated routes include only `/go/android-t2` (`wrangler pages functions build --output-routes-path`, wrangler 4.158.0) | Static requests never invoke a Function, so the quota and the count stay clean |
| 9 | No `Disallow: /go/`; no page but `/android-t2/` links to `/go/` | legal (2026-11-23): a Disallow is the one line Meta's cloaking rules could be read against. Crawlers rarely find the path; if one does, the count is an upper bound anyway |
| 10 | QR code: the direct Play link | A desktop visitor who scans it is a different step from a tap on the button; the QR is not counted |
| 11 | `T2_COUNT` flag | If the device test or `legal-finance-analyst` says no, one value returns the page to the direct link |
| 13 | The Function never branches on user agent, IP or country, and stores nothing per visit, ever | legal condition (Meta cloaking; a stored IP or Referer would be a per-person record of health interest) |
| 14 | With the counter on, the build stops unless `privacy-policy.md` describes it and no longer says the site runs no "analytics" | the policy goes live no later than `/go/` |
| 12 | Page words: a subset of cleared text, enforced | `text_segments()` of `/android-t2/`'s `<main>` must all appear on `/android/` or `/instagram/` |

## What Cloudflare processes anyway

Cloudflare sees the IP and the path of every request to any page of the site, and publishes no fixed retention for edge
logs (its privacy policy: kept "as long as consistent with its business purposes"). The privacy policy's "This website"
section already says so. New here: a tap is one more request to our own domain before Google Play.

## Risks

- **Does Instagram's in-app browser open the Play app after a 302?** Unknown until the device test (2 phones, one not the
  owner's). A direct link opened Play on the owner's Redmi; a redirect is a different path through the WebView.
- **The count is requests, not people.** HEAD, link prefetch, a reload and a double tap are counted; it is an upper bound
  on taps. The metrics also lag a few minutes.
- **A later deploy without `functions/` silently kills the button** (404). Mitigation: the deploy check
  `curl -I /go/android-t2`, written in the README and in the owner's steps. A dashboard zip upload cannot deploy it.
- **The Free quota** (100,000 Functions and Workers requests a day, shared) is far above the test's traffic.
- The page being "better" than `/android/` is not shown; test 2 measures it.

## Deploy rule (lead, 2026-11-23)

The branch is **not** merged into `develop` before deploy day. On deploy day the main session sets the policy's effective date in one commit, merges (settling any `feature/policy-word-of-mouth` conflict in number order) and the owner deploys from that merge, the same day. Otherwise a routine deploy from `develop` would carry a policy dated before the counter sentence, or a button with no Function. `SITE_DEPLOY=1` makes the build refuse a date before 28 November 2026 (the day after the live policy's last date) or after today.
