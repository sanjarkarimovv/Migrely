# Release documents

Everything Google Play asks for that does not live in the code (BRD D104). `TODO(user)` marks the user's own details,
which 13b `publish-to-play` fills in; `TODO(change 9)` marks what the subscription settles.

| File | For |
|---|---|
| `listing.md` | the store listing: name, descriptions, category, contact |
| `privacy-policy.md` | the privacy policy; `make_policy_html.py` turns it into `privacy-policy.html` for hosting |
| `data-safety.md` | App content → Data safety |
| `content-rating.md` | App content → Content rating and target audience |
| `health-declaration.md` | App content → Health apps |
| `icon/` | the icon's SVG source and generator, and the 512×512 Play icon |
