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
| Benefits (up to 4, shown by Play) | A PDF report for your doctor · Triggers and patterns in your migraines · Your medication ratings, migraine timing · The last 12 months at a glance |
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
- **Prorations:** nothing to set. The app moves monthly to yearly with `WITHOUT_PRORATION`, the only kind of switch
  besides charging the full price that Play allows between base plans of one subscription (EC-BIL-10, D120); there is
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
| Purchase option | ID `buy`, type Buy, price USD 59.99. It is the product's only purchase option, so the app's `oneTimePurchaseOfferDetails` reads it; Play marks it **Backwards compatible** (for the old `querySkuDetailsAsync`), which changes nothing for Migrely. Keep it the only option |
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


## German names (de-DE, DRAFT, T79, 2026-11-27)

For the de-DE listing (`listing-de.md`). The words come from the app's German strings: "Einmalkauf" is `subscription_plan_lifetime`, and
"Bleibt dir erhalten" and "Eine Zahlung. Nichts verlängert sich." are `subscription_status_lifetime_summary`, `subscription_success_lifetime_text` and
`subscription_terms_once` (T23 fix batch 1, §4: legal chose "Einmalkauf" over "lebenslang" and "Gehört dauerhaft dir"). Play's own
limits (Play Console Help, read 2026-11-27): a subscription name up to 55 characters and up to four benefits of up to 40 characters.
No limit was found for a one-time product's text. Benefits 1 and 3 follow the verdicts of 2026-11-27 (the PDF is English and the benefit stands alone in Play's purchase sheet; "deine Medikamentenbewertung" says who rates).
The benefits' English versions are in the table at the top. Both products' IDs, base plans and prices do not change.

| Field | German |
|---|---|
| Subscription `migrely_premium`, name (≤ 55) | Migrely Premium (15) |
| Benefit 1 (≤ 40) | PDF-Bericht für die Praxis (Englisch) (37) |
| Benefit 2 (≤ 40) | Oft gemeinsam notierte Auslöser (31) |
| Benefit 3 (≤ 40) | Zeitmuster, deine Medikamentenbewertung (39) |
| Benefit 4 (≤ 40) | Die letzten 12 Monate auf einen Blick (37) |
| One-time `migrely_lifetime`, name | Migrely Premium, Einmalkauf (27) |
| One-time `migrely_lifetime`, description | Premium bleibt dir erhalten. Eine Zahlung. Nichts verlängert sich. (66) |
