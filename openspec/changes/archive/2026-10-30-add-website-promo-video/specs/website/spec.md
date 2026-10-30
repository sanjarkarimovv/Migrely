## ADDED Requirements

### Requirement: Promo video on the home page

The home page SHALL show the promo video from the site itself, with a poster, playback controls and `preload="none"`. It SHALL NOT autoplay, loop, or load anything from another site. A text alternative SHALL say what each scene shows. The home page's JSON-LD SHALL describe it as a `VideoObject`. The CSP SHALL allow media only from the site (`media-src 'self'`).

#### Scenario: A visitor opens the home page

- **WHEN** the home page loads
- **THEN** the poster shows and the video file is not downloaded
- **AND** nothing moves until the visitor presses play

#### Scenario: A visitor presses play

- **WHEN** the visitor presses play
- **THEN** the video plays from `/assets/video/` with no Content Security Policy violation and no request to another
  site

#### Scenario: A screen reader user

- **WHEN** a screen reader reaches the video
- **THEN** the video is named by the section's heading and described by the text below it, which gives every scene,
  the end card included, with its on-screen words, and says it has music and no speech

#### Scenario: The browser can't play the file

- **WHEN** the browser can't decode the MP4
- **THEN** a visible link under the video downloads it

#### Scenario: The video is updated

- **WHEN** a new cut replaces the video
- **THEN** it gets a new file name, and `PROMO_VIDEO` is changed, so year-long caches never serve the old file
  (EC-WEB-20)

#### Scenario: A file is missing

- **WHEN** the video or its poster is not in `website/public/assets/video/`
- **THEN** the build stops and names the missing file

## MODIFIED Requirements

### Requirement: The trust pages keep the site's rules

The pages SHALL keep the site's rules (D119): no scripts, cookies or third-party requests, every asset served by the site, and a Content-Security-Policy that allows only the site itself; `media-src 'self'` was added for the promo video (D146). They SHALL read without horizontal scrolling at 320 px wide and at 200% text, in light and dark themes, with long permission names and commands wrapping.

#### Scenario: A small phone with large text

- **WHEN** `/privacy-check/` is shown 320 px wide with text at 200%
- **THEN** the page does not scroll sideways, and the long permission name and the `adb` command wrap inside their
  boxes

#### Scenario: Dark theme

- **WHEN** the visitor's system uses a dark theme
- **THEN** the new pages use the site's dark colours, and their text keeps AA contrast
