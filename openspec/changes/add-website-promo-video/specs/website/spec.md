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
- **THEN** the video is described by the text below it, which names each scene and says it has music and no speech

#### Scenario: The video is updated

- **WHEN** a new cut replaces the video
- **THEN** it gets a new file name, and `PROMO_VIDEO` is changed, so year-long caches never serve the old file
  (EC-WEB-20)

#### Scenario: A file is missing

- **WHEN** the video or its poster is not in `website/public/assets/video/`
- **THEN** the build stops and names the missing file
