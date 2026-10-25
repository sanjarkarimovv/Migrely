# Data safety answers

The answers for Play Console → App content → Data safety (BRD §11.3, D101). They must match the code. Re-check them
whenever a permission, a library or the billing changes.

## Why the answers are "none"

- **No transmission:** the app has no INTERNET permission, and the permission check fails the build if one appears
  (D44). Nothing the user enters can leave the phone through the app.
- **No SDKs that collect:** no analytics, advertising or crash reporting.
- **Android's own backup** is a system service run by Google; Play's guidance does not count it as the app collecting
  data.
- **Files the user exports** are shared by the user, through the system share sheet, to a destination they choose. Play's
  guidance treats that as user-initiated sharing, not collection.

## The form

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |
| Is all of the user data collected by your app encrypted in transit? | Not applicable: none is collected |
| Do you provide a way for users to request that their data is deleted? | Yes: Settings → Delete all data removes everything on the phone; nothing is held elsewhere |

**Data types collected:** none. **Data types shared:** none.

## Open points

- **TODO(change 9), purchases:** with the Play Billing Library used directly and no server of our own, Google Play
  processes purchases and the app keeps only the entitlement on the phone, so the answer stays "No". If a service such
  as RevenueCat were chosen, it would collect purchase history and device identifiers, and this form would change.
- **Data deletion URL:** Play may ask for a web page on deleting data. The privacy policy's "Deleting your information"
  section serves, at its hosted URL (TODO(user)).
