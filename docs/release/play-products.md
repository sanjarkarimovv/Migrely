# Play Console products

What to create in Play Console → Monetize with Play → Products for Migrely Premium (BRD §7.2, D107). The ids are in the
code (`SubscriptionProducts` in `:features:subscription:data`) and must match exactly; Play never lets an id be reused,
so a typo means a new product.

Play lets products be created only once a build that declares `com.android.vending.BILLING` is uploaded to a testing
track, which 13b `publish-to-play` does first.

## Subscription: `migrely_premium`

| Field | Value |
|---|---|
| Product ID | `migrely_premium` |
| Name | Migrely Premium |
| Benefits (up to 4, shown by Play) | A PDF report for your doctor · Triggers and patterns in your migraines · What helped, and when migraines come · The last 12 months at a glance |
| Icon | The 512 × 512 Play icon, also on `migrely_lifetime` |

### Base plans

| Base plan ID | Type | Billing period | Price (United States) | Other countries |
|---|---|---|---|---|
| `yearly` | Auto-renewing | 1 year | USD 29.99 | see Prices below |
| `monthly` | Auto-renewing | 1 month | USD 4.99 | see Prices below |

For both base plans:
- **Grace period:** on, Play's default length: 14 days for `yearly`, 7 days for `monthly`. Premium stays on and Play
  shows its own "fix your payment" message, which the app asks for at start (EC-BIL-03).
- **Account hold:** "Calculate account hold automatically", so grace and hold together make 60 days (46 days for
  `yearly`, 53 for `monthly`). Premium is off during the hold; Play shows its message (EC-BIL-04).
- **Customer base plan and offer changes:** Play's default, "Charge at next billing date".
- **Resubscribe:** allowed, so a cancelled subscription can be restored from Play's subscriptions page.
- **Prorations:** nothing to set. The app moves monthly to yearly with `WITH_TIME_PRORATION` (EC-BIL-10); there is
  no move from yearly to monthly in the app.

### Offer on `yearly`: `free-trial-7d`

| Field | Value |
|---|---|
| Offer ID | `free-trial-7d` |
| Base plan | `yearly` |
| Eligibility | New customer acquisition: **never had this subscription** |
| Phases | One phase: **free trial, 7 days**; then the base plan's price |
| Tags | none |

The app offers the trial only when Play returns this offer for the account, so a user who had the trial sees the
yearly price alone. `monthly` has no offer.

## One-time product: `migrely_lifetime`

| Field | Value |
|---|---|
| Product ID | `migrely_lifetime` |
| Name | Migrely Premium, lifetime |
| Description | Premium for as long as you use Migrely. One payment. |
| Type | One-time, non-consumable: the app acknowledges it and never consumes it |
| Purchase option | ID `buy`, type Buy, price USD 59.99, marked **Backwards compatible**: the app reads the product's single `oneTimePurchaseOfferDetails`, which is that option |
| Quantities | One per purchase (multi-quantity off) |
| Classification | Digital content |
| Age rating (US states) | All ages, as the app's content rating |

Created in Play Console and active on 25 October 2026: `migrely_premium` with `yearly` (`free-trial-7d`) and
`monthly` in 175 countries, and `migrely_lifetime` (`buy`) in 174.

## Prices

| Product | United States | Rule (BRD §7.2) |
|---|---|---|
| `yearly` | USD 29.99 | Play converts it for other countries |
| `monthly` | USD 4.99 | Play converts it for other countries |
| `migrely_lifetime` | USD 59.99 | Play converts it for other countries |

In lower-income markets, such as India, Brazil and Indonesia, set each price by hand to about 50% of the US price,
after Play's conversion. The app never writes a price: it shows the one Play gives for the user's country.

## Settings outside the products

- **Licence key:** Monetize with Play → Monetization setup → Licensing gives the app's base64 public key. Pass it to
  the build as the Gradle property `migrely.playLicenseKey` (for example in `~/.gradle/gradle.properties`); the app
  checks every purchase's signature with it, and `bundleRelease` fails without it (D107).
- **Family Library:** off for every product (EC-BIL-12).
- **Real-time developer notifications:** none; Migrely has no server.
- **License testers:** add the testers' Google accounts under Setup → License testing, so 13b can buy, cancel and
  renew on the internal testing track without being charged (D113).
