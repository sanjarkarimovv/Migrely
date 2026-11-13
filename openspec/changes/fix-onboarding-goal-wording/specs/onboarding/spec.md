## ADDED Requirements

### Requirement: The goal choices name a record, not an outcome
Step 1 SHALL offer "Keep a record for my doctor", "Find what triggers my migraines" and "Keep track of my medication days".
No goal SHALL suggest taking less medicine or promise a result. The medicine goal SHALL keep its stored value
`fewer_painkillers`, so an upgrade, a backup and a restore change nothing (BRD ONB-F1, D77, D177).

#### Scenario: The label
- **WHEN** step 1 shows
- **THEN** its third choice reads "Keep track of my medication days"
- **AND** no text on step 1 says "fewer painkillers", in any letter case

#### Scenario: Choosing it
- **WHEN** a new user taps "Keep track of my medication days"
- **THEN** the goal is saved as `fewer_painkillers` and step 2 shows (BRD EC-ONB-04)

#### Scenario: An upgrade with the old goal stored
- **WHEN** a user who chose "Take fewer painkillers" in an earlier version updates to this one
- **THEN** the stored `fewer_painkillers` still reads as the medicine goal, with no migration and no error
- **AND** onboarding does not show again (BRD EC-ONB-01, D76)

#### Scenario: Large font, narrow screen, landscape
- **WHEN** step 1 shows at 200% font on a 360 dp wide screen, in portrait or in landscape
- **THEN** "Keep track of my medication days" wraps onto more lines without being clipped, cut with "…" or broken inside a word
- **AND** each choice keeps a touch target of at least 48 dp, and the disclaimer, "Restore from a backup file" and "Skip" are
  reachable by scrolling (BRD EC-ONB-02)

#### Scenario: TalkBack and keyboard
- **WHEN** a TalkBack, keyboard or Switch Access user moves through step 1
- **THEN** the third choice is announced as "Keep track of my medication days" and is actionable
- **AND** the order stays: title, the three choices, disclaimer, "Restore from a backup file", "Skip" (BRD EC-ONB-03)

#### Scenario: Skip and Back on step 1
- **WHEN** the user taps "Skip" on step 1
- **THEN** step 2 shows and no goal is saved
- **AND** the system Back on step 1 still leaves the app, and onboarding shows again next time (BRD EC-ONB-04, D77)

#### Scenario: Backup and restore
- **WHEN** the user exports a JSON backup, or imports one with Add or Replace
- **THEN** the file holds no goal, exactly as before this change, and it stays readable by earlier versions
- **AND** an Auto Backup restore of `fewer_painkillers` on a new phone reads as the medicine goal (BRD EC-ONB-05, D78)

#### Scenario: Offline, Free or Premium, dark theme
- **WHEN** step 1 shows with no network, for a free or a Premium user, in the dark theme
- **THEN** the same three choices show, with the same colours and contrast as before (BRD EC-ONB-06)
