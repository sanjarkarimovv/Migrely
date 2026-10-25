# Data safety answers

The answers for Play Console → App content → Data safety (BRD §11.3, D101). They must match the code. Re-check them
whenever a permission, a library or the billing changes.

## Why the answers are "none"

- **No transmission:** the app has no INTERNET permission, and the permission check fails the build, the bundle too, if
  one appears (D44). Nothing the user enters can leave the phone through the app, and nothing reaches the developer.
- **No SDKs that collect:** no analytics, advertising or crash reporting.
- **Purchases (Migrely Premium):** the Play Billing Library talks only to the Google Play app on the phone, and Google
  Play processes every purchase under its own policy, so purchase data is Google's, not the app's (D105). The app keeps
  on the phone only whether Premium is on: the kind of purchase, its plan when bought there, and when it was last
  checked. The library bundles Google's own usage logging, which needs the INTERNET permission; the app removes that
  permission, so nothing is sent.
- **Android's own backup** is an operating-system service that the user controls, not the app. From Android 9 the app
  allows only end-to-end encrypted cloud backup, which Play's guidance exempts from "collection" when no one but the
  user can read it. On Android 8 the system, not the app, sends the backup.
- **Files the user exports:** the user creates them and chooses where they go. JSON and CSV through the system file
  picker, the PDF through the share sheet. Data the user moves on their own initiative is not collection by the app.

## The form

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |

Once the first answer is "No", Play skips the questions on encryption in transit and on deletion requests. The app still
offers deletion: Settings → Delete all data removes everything on the phone, and nothing is held elsewhere.

**Data types collected:** none. **Data types shared:** none.

## Open points

- **Purchases:** settled by change 9 (`add-subscription`): with the Play Billing Library used directly and no server
  of our own, Google Play processes purchases and the app keeps only the entitlement on the phone, so the answer stays
  "No". A service such as RevenueCat would collect purchase history and device identifiers, and this form would change.
- **Data deletion URL:** Play asks for one only from apps that let users create an account; Migrely has none. If that
  changes, the privacy policy's "Deleting your information" section serves, at its hosted URL (TODO(user)).
