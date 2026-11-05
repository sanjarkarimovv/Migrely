## MODIFIED Requirements

### Requirement: Organization structured data

The home page SHALL publish one JSON-LD `@graph` with an `Organization` (Migrely: url, logo, email, contactPoint), a `WebSite` published by it and the `MobileApplication`, whose publisher stays Sulton UzDev. `/about/` SHALL publish an `AboutPage` about the same `Organization`. `sameAs` SHALL list exactly the official profiles in `SOCIAL_PROFILES`, and every footer SHALL link them with `rel="me"`.

#### Scenario: Search engines read the home page

- **WHEN** a crawler parses the home page's JSON-LD
- **THEN** it finds an `Organization` with `@id` `https://migrely.health/#organization`, a `WebSite` whose publisher
  is that `@id`, and the `MobileApplication`

#### Scenario: Official profiles

- **WHEN** `SOCIAL_PROFILES` lists the YouTube channel
- **THEN** the `Organization`'s `sameAs` is `["https://www.youtube.com/@migrely"]`, every page's footer links it with
  `rel="me"`, and `/llms.txt` lists it

#### Scenario: No social profiles yet

- **WHEN** `SOCIAL_PROFILES` is empty
- **THEN** the `Organization` has no `sameAs` and no footer shows a profile link
