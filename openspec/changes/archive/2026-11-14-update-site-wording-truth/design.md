# Design

## D1. One truth rule, two ad levels

The truth rule (no outcome promise, hedges kept, privacy worded as a permission) applies to every channel at the same
level. Meta's personal-attributes rule (no second-person health) is a placement rule, not a truth rule, so it applies
only to paid ads, boosts and `/android/`, where `BANNED_AD` already stops the build. Organic pages and store art may say
"your migraines" when the hedge stays, because the reader chose a migraine app and the app itself says "Often logged
with your migraines".

## D2. Why "internet permission" and not "internet access"

The app holds `ACCESS_NETWORK_STATE` (D166), so "no internet access" is ambiguous, and a user-started export or
Android's backup does leave the phone. "No internet permission, so the app itself cannot send entries over the
internet" is literally true and checkable on `/privacy-check/`.

## D3. Image names

`/assets/` is `immutable` for a year (EC-WEB-20). A changed image under an old name would show the old caption to
anyone with a cache. So each re-render gets a new `-vN` name, and `images()` finds the store source with
`re.sub(r'-v\d+$', '', name)`. Old files are not deleted: a cached page or a shared preview that names them still loads.

## D4. The promo transcript

The text alternative must describe the video that plays. Rewriting it before v3.4 exists would make it false for
screen-reader users, so it waits. v3.3 makes no unsafe or false medical claim (`medical-reviewer`), plays only on
request, and never appears on `/android/` or in a paid placement.
