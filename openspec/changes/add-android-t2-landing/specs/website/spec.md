## ADDED Requirements

### Requirement: A short ad landing page for test 2

The site SHALL serve `/android-t2/`, a `noindex` page outside the sitemap, `llms.txt` and every navigation, with no script and one lazy image, the QR code. Every text segment in its `<main>` SHALL already appear on `/android/` or `/instagram/`, and at most 20 words SHALL come before its first Play button, which SHALL follow the line "Not available for iPhone."

#### Scenario: The page is built

- **WHEN** `python3 website/build.py` runs
- **THEN** `/android-t2/` exists, is `noindex`, has one `h1`, and the build stops if a text segment is not on `/android/` or `/instagram/`
- **AND** the build stops if more than 20 words come before the first button or the Android-only line comes after it

#### Scenario: No other page leads to it

- **WHEN** any other page links to `/android-t2/` or `/go/`
- **THEN** the build stops

### Requirement: The Play button can count taps without storing anything

With `T2_COUNT` on, both Play buttons on `/android-t2/` SHALL link to `/go/android-t2`, served by the project's only Pages Function, which answers 302 to the exact test-2 Play link with `utm_*` and `referrer` unchanged and reads, stores and calls nothing. With it off, both buttons SHALL carry the Play link and `website/functions/` SHALL be empty.

#### Scenario: A visitor taps the button

- **WHEN** `GET /go/android-t2` is requested, with or without a cookie, a `Referer` or a query string
- **THEN** the response is `302` and `Location` equals the test-2 Play link byte for byte
- **AND** it sets no cookie and its headers include `Cache-Control: no-store` and `Referrer-Policy: no-referrer`

#### Scenario: The Function drifts

- **WHEN** the Function's link differs from the page's, answers 301, reads the request's headers or IP, logs, or a second Function file exists
- **THEN** the build stops

#### Scenario: The counter is not used

- **WHEN** `T2_COUNT` is false
- **THEN** both buttons carry the test-2 Play link directly, `play.google.com` appears twice in the page, and the build stops if any file remains under `website/functions/`

#### Scenario: A deploy leaves the Function out

- **WHEN** the site is deployed without the `functions/` folder (for example a dashboard zip upload)
- **THEN** `/go/android-t2` does not answer 302, and the deploy check `curl -I https://migrely.health/go/android-t2` fails, so the deploy is not accepted

### Requirement: Crawlers are told to stay out of the counter

`robots.txt` SHALL contain `Disallow: /go/` while the counter is on, and the build SHALL stop if it does not.

#### Scenario: robots.txt loses the rule

- **WHEN** the generated `robots.txt` has no `Disallow: /go/` and `T2_COUNT` is true
- **THEN** the build stops

### Requirement: The test-2 desktop QR code matches the Play link

The QR code on `/android-t2/` SHALL encode the test-2 Play link directly, and `docs/release/play-links.md` SHALL list that link for `ig-ads-test2`.

#### Scenario: The QR code is scanned

- **WHEN** `assets/img/qr-ig-ads-test2-v1.svg` is rendered and decoded
- **THEN** the decoded text equals the test-2 Play link byte for byte
- **AND** the `ig-ads-test1` QR code does not decode to it
